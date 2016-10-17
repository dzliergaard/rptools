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

import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parses a file into a tangible/useful object.
 *
 * @param <T> The type of data the text file is parsed into.
 */
@CommonsLog
abstract class FileParser<T> {
  private static final String PARSE_ERROR = "Error parsing local file %s.";

  /**
   * Parses /data/[fileName] into an object T.
   * @param fileName The file to parse.
   * @return T Parsed contents of file.
   */
  public T parseFile(String fileName) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream input = classLoader.getResourceAsStream("/data/" + fileName);
    try {
      String data = IOUtils.toString(input, "UTF-8");
      return parseFileData(data);
    } catch (IOException e) {
      log.error(String.format(PARSE_ERROR, "/data/" + fileName), e);
      return null;
    }
  }

  /**
   * Given the string contents of a file, parse it into a useful object of type T.
   * @param data String contents of file.
   * @return T Parsed contents of string.
   */
  protected abstract T parseFileData(String data);
}
