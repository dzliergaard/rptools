package com.rptools.table;

import com.google.common.collect.Lists;
import com.rptools.io.TableFileParser;
import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@CommonsLog
public class TableReader {

  private final TableFileParser fileParser;
  @Getter
  private final RPTable tables;

  @Autowired
  public TableReader(TableFileParser fileParser) {
    this.fileParser = fileParser;
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    tables = Optional.ofNullable(classLoader.getResource("/data/tables/"))
                     .map(FileUtils::toFile)
                     .map(this::readTables)
                     .map(RPTable.Builder::build)
                     .orElse(null);
  }

  private RPTable.Builder readTables(File file) {
    log.fatal("Reading table file " + file.getName());
    if (file.isDirectory()) {
      Collection<File> files = FileUtils
          .listFiles(file, new String[]{"json", "txt"}, false);
      RPTable.Builder tables = RPTable.newBuilder().setName(processName(file));
      files.stream()
           .sorted(Comparator.comparing(File::getName))
           .map(File::toPath)
           .map(fileParser::parseFile)
           .filter(Objects::nonNull)
           .forEach(table -> tables.putTables(table.getName(), table.build()));
      List<File> subdirs = Lists.newArrayList(
          file.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY));
      subdirs.stream()
             .sorted(Comparator.comparing(File::getName))
             .filter(dir -> !dir.equals(file))
             .map(this::readTables)
             .forEach(table -> {
               if (tables.getTablesMap().containsKey(table.getName())) {
                 table.mergeFrom(tables.getTablesMap().get(table.getName()));
               }
               tables.putTables(table.getName(), table.build());
             });
      return tables;
    } else {
      return Optional.of(file.toPath()).map(fileParser::parseFile).orElse(null);
    }
  }

  private String processName(File file) {
    return file.getName().replace(".txt", "").replace(".json", "")
               .replaceAll("^[0-9]*", "");
  }
}
