package com.rptools.city;

import com.dzlier.weight.WeightedList;
import com.google.common.collect.Lists;
import com.rptools.city.City.Species;
import com.rptools.io.CityFileParser;
import com.rptools.name.NameGen;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@CommonsLog
public class CityGen {

  private static final Random rand = new Random();

  // search mods are static numbers based on number
  private static NavigableMap<Integer, Integer> searchMods = new TreeMap<Integer, Integer>() {
    {
      put(0, -6);
      put(500, -4);
      put(1500, -2);
      put(4500, 0);
      put(6750, 1);
      put(26500, 3);
      put(Integer.MAX_VALUE, 5);
    }
  };

  private final Cities cityData;
  private final NameGen nameGen;

  @Autowired
  public CityGen(CityFileParser cityFileParser, NameGen nameGen) {
    this.nameGen = nameGen;
    cityData = cityFileParser.parseFile("cityData.json");
  }

  public City generateCity(Double size, Double diversity, Species species) {
    City.Builder city = City.newBuilder();
    // generate name for city and ruler in same call
    List<String> names = nameGen.generateNames(2);
    if (rand.nextDouble() < .2) {
      city.setName(names.get(0));
    } else {
      city.setName(names.get(0).split(" ")[0]);
    }

    City.Population.Builder popBuilder = city.getPopulationBuilder();
    addPopulation(popBuilder, size, diversity, Optional.ofNullable(species).orElse(randSpecies()));

    // add 3 races, or sometimes 2 for low-diversity places
    int people = popBuilder.getPeopleMap().size();
    while (people < 3 || (people < 2 && rand.nextDouble() * .2 < diversity)) {
      if(addPopulation(popBuilder, size, diversity, randSpecies())) {
        people++;
      }
    }

    city.getRulerBuilder().setName(names.get(1)).setSpecies(getWeightedRace(popBuilder));
    city.addAllInns(generateInns(city.getPopulation().getTotal()));
    city.addAllGuilds(generateGuilds(city.getPopulation().getTotal()));

    return city.build();
  }

  private Species randSpecies() {
    int ind = rand.nextInt(Species.values().length);
    return Species.values()[ind];
  }

  private static boolean addPopulation(City.Population.Builder population, Double size, Double diversity, Species species) {
    Map<String, Integer> people = population.getPeopleMap();
    if (people.containsKey(species.name())) {
      return false;
    }

    int pop = new Double(size * (rand.nextDouble() + .5)).intValue();
    pop *= Math.pow(diversity, people.size());
    population.putPeople(species.name(), pop);
    population.setTotal(population.getTotal() + pop);
    population.setSearchMod(searchMods.ceilingEntry(population.getTotal()).getValue());
    return true;
  }

  private Species getWeightedRace(City.Population.Builder population) {
    WeightedList<String> races = new WeightedList<>();
    for (Entry<String, Integer> entry : population.getPeopleMap().entrySet()) {
      races.add(entry.getValue() * 3.0, entry.getKey());
    }
    return Species.valueOf(races.random());
  }

  private List<String> generateInns(int population) {
    double size = Math.sqrt(Math.sqrt(population));
    List<String> inns = Lists.newArrayList();
    for (int i = 0; i < size; i++) {
      inns.add(generateInn());
    }
    return inns;
  }

  private String generateInn() {
    String inn = getFrom(cityData.getInns().getBegPatList()) + " " + getFrom(cityData.getInns().getEndPatList());
    while (inn.matches(".*\\{[^-]\\}.*")) {
      inn = inn.replaceFirst("\\{a\\}", getFrom(cityData.getInns().getBegList()));
      inn = inn.replaceFirst("\\{n\\}", getFrom(cityData.getInns().getEndList()));
      inn = inn.replaceFirst("\\{p\\}", getName());
    }
    return inn;
  }

  private List<String> generateGuilds(int population) {
    List<String> guilds = Lists.newArrayList();
    population -= rand.nextInt(500) + 2000;
    while (population > 0) {
      population -= rand.nextInt(500) + 2000;
      guilds.add(generateGuild());
    }
    return guilds;
  }

  private String generateGuild() {
    String guild = getFrom(cityData.getGuilds().getPatList());
    while (guild.matches(".*\\{[^-]\\}.*")) {
      guild = guild.replaceFirst("\\{g\\}", getFrom(cityData.getGuilds().getGroupList()));
      guild = guild.replaceFirst("\\{n\\}", getFrom(cityData.getGuilds().getNounList()));
    }
    return guild;
  }

  private String getName() {
    String name = nameGen.generateNames(1).get(0);
    if (rand.nextDouble() > .2) {
      return name.split(" ")[0];
    }
    return name;
  }

  private String getFrom(List<String> list) {
    return list.get(rand.nextInt(list.size()));
  }
}
