package org.AutomateFeatureSwitchCleanups.ExtractionService;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractFeatureSwitch {

    public ArrayList<String> extractKeywordsWithXWMLWM(String responseFromBitbucket) {
        JsonObject responseFromBitbucketjson = null;

        ArrayList<String> keywordsList = new ArrayList<>();
        responseFromBitbucketjson = JsonParser.parseString(responseFromBitbucket).getAsJsonObject();
        try {
            // Recursive function to handle nested JSON objects and arrays
            if (responseFromBitbucketjson != null) {
                extractKeywords(responseFromBitbucketjson, keywordsList);
            } else {
                System.err.println("The json Object is null");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return keywordsList;
    }

    private void addMatchingSubstrings(String strValue, ArrayList<String> keywordsList) {
        Pattern pattern = Pattern.compile("\\b(XWM|LWM)-\\d+(?!\\.)\\b", Pattern.CASE_INSENSITIVE); //regular expression pattern to find substrings that match the format XWM-<number> or LWM-<number>
        /*
        * \\b: This is a word boundary anchor. It ensures that the pattern matches only if it is at the beginning or end of a word.
          (XWM|LWM): This is a group that matches either “XWM” or “LWM”. The pipe | acts as an OR operator.
          -: This matches a literal hyphen.
           \\d+: This matches one or more digits. The \\d represents any digit, and the + means one or more of the preceding element.
            (?!\\.): This is a negative lookahead assertion. It ensures that what follows the digits is not a period (.). The ?! syntax is used for negative lookahead.
            \\b: Another word boundary anchor to ensure the pattern matches only if it is at the end of a word.*/
        Matcher matcher = pattern.matcher(strValue);
        while (matcher.find()) {
            keywordsList.add(matcher.group());
        }
    }

    private void extractKeywords(JsonObject jsonObject, ArrayList<String> keywordsList) throws Exception {
        for (String key : jsonObject.keySet()) { //iterating through the keys of the JSON object
            Object keyValue = jsonObject.get(key);
            if (keyValue instanceof JsonObject) { //If the value associated with a key is another JSON object, it recursively calls extractKeywords on that object.
                extractKeywords((JsonObject) keyValue, keywordsList);
            } else if (keyValue instanceof JsonArray) { //If the value is a JSON array, it calls extractKeywordsFromArray
                extractKeywordsFromArray((JsonArray) keyValue, keywordsList);
            } else if (((JsonElement) keyValue).isJsonPrimitive() && ((JsonElement) keyValue).getAsJsonPrimitive().isString()) { //If the value is a string, it calls addMatchingSubstrings to find and add matching substrings (based on a regular expression) to the keywordsList
                String strValue = ((JsonElement) keyValue).getAsString();
                addMatchingSubstrings(strValue, keywordsList);
            }
        }
    }

    private void extractKeywordsFromArray(JsonArray jsonArray, ArrayList<String> keywordsList) throws Exception {
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement element = jsonArray.get(i);
            if (element instanceof JsonObject) {
                extractKeywords((JsonObject) element, keywordsList);
            } else if (element instanceof JsonArray) {
                extractKeywordsFromArray((JsonArray) element, keywordsList);
            } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                String strValue = element.getAsString();
                addMatchingSubstrings(strValue, keywordsList);
            }
        }
    }

}
