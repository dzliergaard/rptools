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

package com.rptools.name;

import com.dzlier.markov.MarkovChain;
import java.util.Random;

/**
 * This class is generated when the application starts and needs to read/parse/interpret data from
 * the name files stored in S3. It contains maps of "group" substrings to the frequency in which
 * they appear at the beginning, middle, and end of the parsed names. One of these objects is
 * created for first names, one for last
 */
public class Names {

  private final MarkovChain<String, String> markovChain;
  private final Random random = new Random();

  public Names(MarkovChain<String, String> markovChain) {
    this.markovChain = markovChain;
  }

  String makeName(int depth) {
    return markovChain.generate(depth);
  }

  String makeName() {
    return makeName(random.nextInt(2) + 2);
  }
}
