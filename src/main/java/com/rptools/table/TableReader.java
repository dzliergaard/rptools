package com.rptools.table;

import com.google.common.collect.Lists;
import com.rptools.io.TableFileParser;
import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TableReader {

  private final TableFileParser fileParser;
  @Getter
  private final RPTable tables;

  @Autowired
  public TableReader(TableFileParser fileParser) {
    this.fileParser = fileParser;
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    RPTable.Builder builder = RPTable.newBuilder().setIsParent(true);
    Optional.ofNullable(classLoader.getResource("/data/tables/"))
        .map(FileUtils::toFile)
        .ifPresent(file -> readTables(file, builder));
    tables = builder.build().getTables(0);
  }

  private void readTables(File file, RPTable.Builder parent) {
    if (file.isDirectory()) {
      Collection<File> files = FileUtils.listFiles(file, new String[]{"json", "txt"}, false);
      RPTable.Builder builder = parent.addTablesBuilder()
          .setName(processName(file))
          .setIsParent(true);
      files.stream()
          .sorted((f1, f2) -> f1.getName().compareTo(f2.getName()))
          .forEach(found -> builder.addTables(fileParser.parseFile(found.toPath())));
      List<File> subdirs = Lists.newArrayList(
          file.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY));
      subdirs.stream()
          .sorted((d1, d2) -> d1.getName().compareTo(d2.getName()))
          .filter(dir -> !dir.equals(file))
          .forEach(dir -> readTables(dir, builder));
    } else {
      parent.addTables(fileParser.parseFile(file.toPath()));
    }
  }

  private String processName(File file) {
    return file.getName().replace(".txt", "").replace(".json", "").replaceAll("^[0-9]*", "");
  }
}
