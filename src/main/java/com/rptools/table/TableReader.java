package com.rptools.table;

import com.rptools.io.TableFileParser;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.Optional;

@Component
public class TableReader {
  private final TableFileParser fileParser;
  @Getter private final RPTable tables;

  @Autowired
  public TableReader(TableFileParser fileParser) {
    this.fileParser = fileParser;
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    tables = Optional.ofNullable(classLoader.getResource("/data/tables/"))
                     .map(FileUtils::toFile)
                     .map(this::readTables)
                     .orElse(null);
  }

  private RPTable readTables(File file) {
    if (file.isDirectory()) {
      Collection<File> files = FileUtils.listFiles(file, new String[]{"json", "txt"}, false);
      RPTable.Builder builder = RPTable.newBuilder().setName(processName(file)).setIsParent(true);
      files.forEach(found -> builder.addTables(fileParser.parseFile(found.toPath())));
      for (File dir : file.listFiles((FileFilter)DirectoryFileFilter.DIRECTORY)) {
        if (dir != file) {
          builder.addTables(readTables(dir));
        }
      }
      return builder.build();
    } else {
      return fileParser.parseFile(file.toPath());
    }
  }

  private String processName(File file) {
    return file.getName().replace(".txt", "").replace(".json", "").replaceAll("^[0-9]*", "");
  }
}
