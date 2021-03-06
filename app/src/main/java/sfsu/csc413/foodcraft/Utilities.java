package sfsu.csc413.foodcraft;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @file:Utilities.java
 * @author: Brook Thomas
 * @version: 1.0
 */
public class Utilities {

    /**
     * Private constructor - class exists solely for static methods
     */
    private Utilities() {
    }

    /**
     * Strips non-ASCII characters from a string.
     *
     * @param ingredient An ingredient.
     * @return An ingredient stripped of non-ASCII characters.
     */
    private static String stripUnicode(String ingredient) {

        StringBuffer buildString = new StringBuffer();

        char[] asArray = ingredient.toCharArray();

        for (char x : asArray) {
            if ((int) x <= 127) {
                buildString.append(x);
            }
        }

        return buildString.toString();

    }

    /**
     * Given a string, converts most common cases of plural to singular.
     * If the string was already singular it will be returned unmodified.
     *
     * @param ingredient An ingredient.
     * @return An ingredient reduced from plural to singular.
     */
    private static String removePlurals(String ingredient) {

        // berries -> berry
        if (ingredient.length() >= 9 && ingredient.substring(ingredient.length() - 7).equals("berries")) {
            return ingredient.substring(0, ingredient.length() - 3) + "y";
        }

        // es -> x
        if (ingredient.substring(ingredient.length() - 2).equals("es")) {
            return ingredient.substring(0, ingredient.length() - 2);
        }

        // s -> x
        if (ingredient.substring(ingredient.length() - 1).equals("s")) {
            return ingredient.substring(0, ingredient.length() - 1);
        }

        // base case
        return ingredient;
    }


    /**
     * Certain keywords can be safely removed from an ingredient while still preserving the context
     * necessary for the application. The word 'cold' can be removed from 'Cold Water' for example.
     * Given an ingredient, this method will strip those keywords determined unnecessary and return
     * the result.
     *
     * @param ingredient An ingredient.
     * @return An ingredient stripped of unnecessary keywords.
     */
    private static String scrubKeywords(String ingredient) {

        String[] keywords = {"granulated", "boiling", "softened", "diced", "sliced", "slices", "slice",
                "ground", "shredded", "grated", "minced", "dried", "finely chopped", "chopped",
                "mashed", "crushed", "boiled", "baked", "cold", "warm", "hot", "warmed", "fresh", "organic",
                "cooked", "large", "medium", "small", "plain", "distilled", "frozen", "pitted", "peeled",
                "cup", "cups", "tsp", "tbsp", "teaspoon", "tablespoon", "oz", "and", "/", ",", "(", ")",
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};


        // Replace keywords with nothing
        for (String k : keywords) {
            ingredient = ingredient.replace(k, "");
        }

        if (ingredient.substring(0, 1).equals(" ")) {
            ingredient = ingredient.substring(1);
        }

        if (ingredient.substring(ingredient.length() - 1, ingredient.length()).equals(" ")) {
            ingredient = ingredient.substring(0, ingredient.length() - 1);
        }

        ingredient.replaceAll("  ", " ");

        return ingredient;

    }

    /**
     * Main method for accessing Utilities' suite of cleaners and parsers. Given an ingredient, will
     * perform several rounds of cleaning and parsing to better match the app's ingredient formatting
     * standards.
     *
     * @param ingredient An ingredient.
     * @return An ingredient cleaned to match the app's ingredient format.
     */
    public static String cleanString(String ingredient) {

        ingredient = stripUnicode(ingredient);

        ingredient = ingredient.toLowerCase();

        ingredient = scrubKeywords(ingredient);

        ingredient = removePlurals(ingredient);

        return ingredient;

    }

}