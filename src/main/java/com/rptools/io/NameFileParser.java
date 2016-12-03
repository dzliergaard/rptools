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

import com.dzlier.markov.MarkovChain;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.rptools.name.Names;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Component;

/**
 * Parses first and last name data files. Creates {@link Names} object with a {@link MarkovChain}
 * that breaks {@link String} names down into their component parts. A component part is a series of
 * consonants or vowels.
 */
@Component
@CommonsLog
public class NameFileParser extends FileParser<Names> {
    private static final Joiner JOINER = Joiner.on("");
    private static final Pattern groupPat = Pattern.compile("[^QAEIOUY]+|Q?[AEIOUY]+");
    private static final Pattern namePat = Pattern.compile("\\w+:\\d+");

    @Override
    protected Names parseFileData(String data) {
        MarkovChain<String, String> markovChain = new MarkovChain<>(this::separateName, JOINER::join);

        Matcher nameMatcher = namePat.matcher(data);

        while (nameMatcher.find()) {
            String[] nameFreq = nameMatcher.group().split(":");
            String name = nameFreq[0];
            Double weight = Double.valueOf(nameFreq[1]);
            markovChain.process(name, weight);
        }

        return new Names(markovChain);
    }

    private List<String> separateName(String name) {
        Matcher segmentMatcher = groupPat.matcher(name);
        List<String> segments = Lists.newArrayList();
        while(segmentMatcher.find()) {
            segments.add(segmentMatcher.group());
        }
        return segments;
    }
}
