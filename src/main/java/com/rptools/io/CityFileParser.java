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

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.rptools.city.Cities;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Component;

/**
 * Parses the city file from S3, which contains information about how to generate names of inns and shoppes.
 */
@Component
@CommonsLog
public class CityFileParser extends FileParser<Cities> {
    @Override
    protected Cities parseFileData(String data) {
        Cities cities = null;
        try {
            Cities.Builder builder = Cities.newBuilder();
            JsonFormat.parser().merge(data, builder);
            cities = builder.build();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return cities;
    }
}
