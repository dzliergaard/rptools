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
import com.google.gson.JsonObject;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.TextFormat;
import com.rptools.city.Cities;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//import org.springframework.integration.file.FileWritingMessageHandler;

/**
 * Parses the city file from S3, which contains information about how to generate names of inns and shoppes
 */
@Component
@CommonsLog
public class CityFileParser extends FileParser<Cities> {
    private final Gson gson;

    @Autowired
    public CityFileParser(Gson gson) {
        this.gson = gson;
    }

    @Override
    protected Cities parseFileData(String data) {
        JsonObject cities = gson.fromJson(data, JsonObject.class);
        JsonFormat
        log.error("Cities: " + cities.toString());
        Cities.Builder builder = Cities.newBuilder();
        Cities.Inns.Builder innsBuilder = builder.getInnsBuilder();
        Cities.Guilds.Builder guildsBuilder = builder.getGuildsBuilder();
        cities.getAsJsonObject("inns").getAsJsonArray("beg").iterator().forEachRemaining(el -> innsBuilder.addBeg(el.getAsString()));
        cities.getAsJsonObject("inns").getAsJsonArray("begPat").iterator().forEachRemaining(el -> innsBuilder.addBegPat(el.getAsString()));
        cities.getAsJsonObject("inns").getAsJsonArray("end").iterator().forEachRemaining(el -> innsBuilder.addEnd(el.getAsString()));
        cities.getAsJsonObject("inns").getAsJsonArray("endPat").iterator().forEachRemaining(el -> innsBuilder.addEndPat(el.getAsString()));
        cities.getAsJsonObject("guilds").getAsJsonArray("pat").iterator().forEachRemaining(el -> guildsBuilder.addPat(el.getAsString()));
        cities.getAsJsonObject("guilds").getAsJsonArray("group").iterator().forEachRemaining(el -> guildsBuilder.addGroup(el.getAsString()));
        cities.getAsJsonObject("guilds").getAsJsonArray("noun").iterator().forEachRemaining(el -> guildsBuilder.addNoun(el.getAsString()));
        Cities city = builder.build();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            File file = new File(classLoader.getResource("/data").getFile() + "/cityData.message");
            if (file.createNewFile()) {
                System.out.println("File is created!");
            } else {
                System.out.println("File already exists.");
            }
//            FileWritingMessageHandler handler = new FileWritingMessageHandler();
//            InputStream input = classLoader.getResourceAsStream("/data/cityData.message");
            FileOutputStream stream = new FileOutputStream(file);
            city.writeTo(CodedOutputStream.newInstance(stream));
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return city;
    }
}
