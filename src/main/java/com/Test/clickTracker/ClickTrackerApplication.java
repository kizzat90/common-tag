package com.Test.clickTracker;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClickTrackerApplication {

	public static void main(String[] args) {
		track();
	}

	private static void track() {
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader("data.json"));
			JSONObject jsonObject = (JSONObject)obj;
			JSONArray recipients = (JSONArray)jsonObject.get("recipients");
			Map<String, List<String>> recipientTagMap = new HashMap<>();

			// Extracts data from JON
			for (Object recipient : recipients) {
				JSONObject recipientObject = (JSONObject)recipient;
				long id = (long)recipientObject.get("id");
				String name = (String)recipientObject.get("name");
				JSONArray tags = (JSONArray)recipientObject.get("tags");
				if (!tags.isEmpty()){
					for(Object tag : tags) {
						mapTagsToRecipient(name, tag.toString(), recipientTagMap);
					}
				}
			}

			// Construct output
			Map<Map<String, String>, List<String>> commonTag = findCommonTag(recipientTagMap);
			StringBuilder stringBuilder = new StringBuilder();
			for (Map.Entry<Map<String, String>, List<String>> common : commonTag.entrySet()) {
				common.getKey().forEach((key, value) -> stringBuilder.append(key).append(", ").append(value).append(" - "));
				stringBuilder.append(common.getValue()).append(" | ");
			}
			System.out.println(stringBuilder.delete(stringBuilder.length() - 3, stringBuilder.length()));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
	}

	// Maps tags to Individual Recipient
	private static void mapTagsToRecipient(String name, String tag, Map<String, List<String>> recipientTag) {
		List<String> tagList = new ArrayList<>();
		if (recipientTag.containsKey(name)){
			tagList = recipientTag.get(name);
		}
		tagList.add(tag);
		recipientTag.put(name, tagList);
	}

	// Find common tags between recipient and pairs them
	private static Map<Map<String, String>, List<String>> findCommonTag(Map<String, List<String>> recipientTagMap) {
		Map<String, String> recipientPair = new HashMap<>();
		Map<Map<String, String>, List<String>> commonMap = new HashMap<>();

		// Create Name Pair keys
		for (Map.Entry<String, List<String>> firstMap : recipientTagMap.entrySet()) {
			for (Map.Entry<String, List<String>> secondMap : recipientTagMap.entrySet()) {
				if (!firstMap.getKey().equals(secondMap.getKey())) {
					recipientPair.put(firstMap.getKey(), secondMap.getKey());
				}
			}
		}

		// Find common tag
		for (Map.Entry<String, String> pair : recipientPair.entrySet()) {
			List<String> recipient1Tags = recipientTagMap.get(pair.getKey()).stream().toList();
			List<String> recipient2Tags = recipientTagMap.get(pair.getValue()).stream().toList();
			List<String> commonArray = recipient1Tags.stream().filter(recipient2Tags::contains).collect(Collectors.toList());

			if (!commonArray.isEmpty() && commonArray.size() >= 2){
				Map<String, String> localPair = new HashMap<>();
				localPair.put(pair.getKey(), pair.getValue());
				commonMap.put(localPair, commonArray);
			}
		}

		return commonMap;
	}
}



