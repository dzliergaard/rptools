/*
 * RPToolkit - Tools to assist Role-Playing Game masters and players
 * Copyright (C) 2016 Dane Zeke Liergaard
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.rptools.io;

import com.google.gson.Gson;
import com.rptools.table.RPTable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Parses text files representing random tables from the DMG into {@code RPTable} objects. See
 * {@code RPTable} for expected table file format.
 */
@Component
@CommonsLog
public class TableFileParser {

  private static final String PARSE_ERROR = "Error parsing local file %s.";

  private Gson gson;
  private boolean columnsSkipped;

  @Autowired
  public TableFileParser(Gson gson) {
    this.gson = gson;
  }

  /**
   * Parse file found at path {@param file} into an RPTable object.
   * See {@link RPTable} for expected table file format.
   *
   * @param file Input table text file.
   * @return {@link RPTable} created from contents of input file.
   */
  public RPTable parseFile(Path file) {
    columnsSkipped = false;
    try (Stream<String> stream = Files.lines(file)) {
      RPTable table = new RPTable(gson, stream.findFirst().orElse(null));
      stream.forEach(line -> parseLine(table, line));
      return table;
    } catch (IOException e) {
      log.error(String.format(PARSE_ERROR, file.toString()), e);
      return null;
    }
  }

  private void parseLine(RPTable table, String line) {
    if (!columnsSkipped) {
      columnsSkipped = true;
      return;
    }
    table.parseEntry(line);
  }
}
