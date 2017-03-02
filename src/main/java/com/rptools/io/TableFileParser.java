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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.protobuf.util.JsonFormat;
import com.rptools.table.RPTable;
import com.rptools.table.RPTable.Entry;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Component;

/**
 * Parses text files representing random tables from the DMG into {@link
 * RPTable} objects. See {@link RPTable} for expected table file format.
 */
@Component
@CommonsLog
public class TableFileParser {

  private static final Joiner JOINER = Joiner.on("");
  private static final Pattern ROLL_PATTERN = Pattern
      .compile("(\\d+)(?:-(\\d+))?");
  private static final Pattern WORD_BREAK_PATTERN = Pattern
      .compile("([a-z])([A-Z])");
  private static final Splitter SPLITTER = Splitter.on('\t').omitEmptyStrings();
  private static final Charset UTF_8 = Charset.forName("UTF-8");
  private static final String PARSE_ERROR = "Error parsing local file %s: %s";
  private static final String EXT_TXT = ".txt";
  private static final String EXT_JSON = ".json";

  private int roll = 1;

  /**
   * Parse file found at path {@param file} into an RPTable object. See {@link
   * RPTable} for expected table file format.
   *
   * Json files are read directly as a proto3 RPTable message. Text files must
   * be parsed, have their json equivalent written, then deleted. This is to
   * ease adding future tables by pasting their text content instead of trying
   * to convert them to json by hand.
   *
   * @param file Input table text/json file.
   * @return {@link RPTable.Builder} created from contents of input file.
   */
  public RPTable.Builder parseFile(Path file) {
    roll = 1;
    try {
      List<String> lines = Files.readAllLines(file);
      RPTable.Builder builder = RPTable.newBuilder();
      // We want to keep json files around, and convert text files to json, then delete them.
      if (file.getFileName().toString().endsWith(EXT_JSON)) {
        JsonFormat.parser().merge(JOINER.join(lines), builder);
        return builder;
      }
      setTableName(file, builder);

      String headerRow = lines.remove(0);
      List<String> headers = SPLITTER.splitToList(headerRow);
      builder.addAllColumns(headers);

      lines.forEach(line -> parseLine(builder, headers, line));
      // only return a table for .txt files if the json file did not also
      // already exist to be read separately
      if (updateResourceFiles(file, builder)) {
        return builder;
      }
      log.debug("Not renewing json/text file");
      return null;
    } catch (IOException e) {
      log.error(String.format(PARSE_ERROR, file.toString(), e.toString()), e);
      return null;
    }
  }

  /**
   * Save new json file if it doesn't exist. Delete the text file.
   *
   * @param file Path to file
   * @param builder RPTable builder to print to JSON file
   * @return Boolean: If the .json file already existed and will be parsed separately
   */
  private boolean updateResourceFiles(Path file, RPTable.Builder builder)
      throws IOException {
    File json = new File(file.toString().replace(EXT_TXT, EXT_JSON));
    Files.delete(file);
    boolean isNew = json.createNewFile();
    Files.write(json.toPath(),
                Lists.newArrayList(JsonFormat.printer().print(builder)), UTF_8);
    return isNew;
  }

  private void setTableName(Path file, RPTable.Builder builder) {
    String filename = file.getFileName().toString().replace(EXT_TXT, "")
                          .replaceAll("^[0-9]*", "");
    Matcher wordBreak = WORD_BREAK_PATTERN.matcher(filename);
    while (wordBreak.find()) {
      filename = filename.replace(wordBreak.group(0),
                                  wordBreak.group(1) + " " + wordBreak.group(2));
    }
    builder.setName(filename);
  }

  private void parseLine(RPTable.Builder builder, List<String> columns,
                         String line) {
    if (line.isEmpty()) {
      return;
    }
    List<String> values = Lists.newArrayList(SPLITTER.splitToList(line));
    Matcher matcher = ROLL_PATTERN.matcher(values.get(0));
    Entry.Builder entryBuilder = Entry.newBuilder();
    int weight = 1;
    if (matcher.matches()) {
      weight = getEntryWeight(matcher);
      entryBuilder.setRoll(values.remove(0));
    } else {
      entryBuilder.setRoll("" + roll);
    }
    entryBuilder.setWeight(weight);
    roll += weight;
    builder.setMaxRoll(roll);
    entryBuilder.addAllValues(values);
  }

  private int getEntryWeight(Matcher matcher) {
    if (matcher.groupCount() < 2 || matcher.group(2) == null) {
      return 1;
    }
    return parseInt(matcher, 2) - parseInt(matcher, 1) + 1;
  }

  private int parseInt(Matcher matcher, int group) {
    return Integer.parseInt(matcher.group(group));
  }
}
