package com.rptools.table;

import com.dzlier.weight.WeightedList;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a table from the Dungeon Master's Guide, with a set of random possibilities which may
 * or may not all be equally likely.
 *
 * The top line of the table .txt file should contain column names, delineated by 1+ tab characters.
 * Each line should contain one entry of the table, with column values delineated by 1+ tabs.
 * The first column of the line may optionally be a number (representing what the die roll is) or a
 * range (representing a range of rolls, i.e. 5-8 on a d10). If the first column is not a number/
 * range, then each row has an equal chance of 1/(# of entries).
 *
 * Table .txt file example 1:
 * Deity			Attributes						Alignment	Domains		Symbol
 * Asmodeus		tyranny								LE				Trickery	Three triangles in tight formation
 * Avandra		change and luck				CG				Trickery	Three stacked wavy lines
 * Bahamut		justice and nobility	LG				Life, War	Dragon's head, in profile, facing left
 *
 * Table .txt file example 2:
 * Government
 * 01-08	Autocracy
 * 09-13	Bureaucracy
 * 14-19	Confederacy
 * 20-22	Democracy
 * 23-27	Dictatorship
 * 28-42	Feudalism
 * 43-44	Gerontocracy
 * 45-53	Hierarchy
 * 54-56	Magocracy
 * 57-58	Matriarchy
 * 59-64	Militocracy
 * 65-74	Monarchy
 * 75-78	Oligarchy
 * 79-80	Patriarchy
 * 81-83	Meritocracy
 * 84-85	Plutocracy
 * 86-92	Republic
 * 93-94	Satrapy
 * 95			Kleptocracy
 * 96-00	Theocracy
 */
public class RPTable extends WeightedList<HashMap<String, String>> {

  private static Pattern ENTRY_PATTERN = Pattern.compile("(\\d+)?(?:-(\\d+))\\t*?(.*)");
  private static Splitter SPLITTER = Splitter.on('\t').omitEmptyStrings();

  private Gson gson;
  private List<String> columns;

  /**
   * Create table with columns delineated by tab characters in input {@link String}.
   *
   * @param columns Input columns delineated by tab characters.
   */
  public RPTable(Gson gson, String columns) {
    super();
    this.gson = gson;
    this.columns = SPLITTER.splitToList(columns);
  }

  /**
   * Add entry with value for each column, returning the weight of new entry.
   *
   * @param line Tab-delineated values for each column.
   * @return Weight of new entry.
   */
  public int parseEntry(String line) {
    Matcher matcher = ENTRY_PATTERN.matcher(line);

    int weight = getEntryWeight(matcher);
    List<String> values = getEntryValues(line, matcher);
    HashMap<String, String> entry = createEntry(values);

    add(weight, entry);

    return weight;
  }

  private HashMap<String, String> createEntry(List<String> values) {
    HashMap<String, String> entry = Maps.newHashMap();
    for (int i = 0; i < columns.size(); i++) {
      entry.put(columns.get(i), values.get(i));
    }
    return entry;
  }

  private List<String> getEntryValues(String line, Matcher matcher) {
    List<String> values = SPLITTER.splitToList(matcher.group(2));
    assert columns.size() == values.size() : MessageFormat
        .format("Entry \"{0}\" should have {1} values, instead found {2}.", line, columns.size(),
            values.size());
    return values;
  }

  private int getEntryWeight(Matcher matcher) {
    int weight = 1;  // If value is not a range, weight is 1.
    String bottomStr = matcher.group(0);
    String topStr = matcher.group(1);
    if (!bottomStr.isEmpty() && !topStr
        .isEmpty()) {  // If value IS range (i.e. 1-8), weight = [top] - [bottom]
      weight = Integer.parseInt(topStr) - Integer.parseInt(matcher.group(0));
    }
    return weight;
  }

  @Override
  public String toString() {
    return gson.toJson(this);
  }
}
