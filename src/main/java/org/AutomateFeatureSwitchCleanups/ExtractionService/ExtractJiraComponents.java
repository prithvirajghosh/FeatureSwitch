package org.AutomateFeatureSwitchCleanups.ExtractionService;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class ExtractJiraComponents {
public List<String> extractComponentNames(String responseFromJIRA) {
    List<String> names = new ArrayList<>();
    // Parse the input JSON string
    JsonObject jsonObject = JsonParser.parseString(responseFromJIRA).getAsJsonObject();
    // Navigate to the 'components' array inside 'fields'
    JsonArray componentsArray = jsonObject.getAsJsonObject("fields").getAsJsonArray("components");
    // Iterate over the components array and extract the 'name' field
    for (int i = 0; i < componentsArray.size(); i++) {
        JsonObject component = componentsArray.get(i).getAsJsonObject();
        String name = component.get("name").getAsString();
        names.add(name);
    }
    return names;
}
}
