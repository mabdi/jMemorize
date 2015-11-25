package jmemorize.tools.pearson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import jmemorize.core.Card;

public class PearsonHelper {

	private final static String URL = "https://api.pearson.com/v2/dictionaries/ldoce5/entries?headword=";

	public void applyOnCard(Card card) {

	}

	public String getWordData(String word) throws IOException {
		String url = URL + word;

		URL u = new URL(url);
		HttpURLConnection rsp = (HttpURLConnection) u.openConnection();
		rsp.setReadTimeout(15000);
		rsp.setConnectTimeout(15000);
		BufferedReader rdr = new BufferedReader(new InputStreamReader(rsp.getInputStream()));
		String s = rdr.readLine();
		JSONObject entryJSON = new JSONObject(s);
		JSONArray ress = entryJSON.getJSONArray("results");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ress.length(); i++) {
			JSONObject obj = ress.getJSONObject(i);
			buildResult(obj, sb);
		}
		return sb.toString();

	}

	private void buildResult(JSONObject obj, StringBuilder sb) throws UnsupportedEncodingException {
		sb.append(obj.getString("headword"));
		if (obj.has("part_of_speech")) {
			sb.append(" (");
			sb.append(obj.getString("part_of_speech"));
			sb.append(")");
		}
		if (obj.has("pronunciations")) {
			JSONArray pr = obj.getJSONArray("pronunciations");
			for (int i = 0; i < pr.length(); i++) {
				JSONObject pronc = pr.getJSONObject(i);
				sb.append(" /");
				sb.append(new String(pronc.getString("ipa").getBytes(), "UTF8"));
				sb.append("/");

			}
		}
		sb.append("\n");
		JSONObject sns = obj.getJSONArray("senses").getJSONObject(0);
		int i = 1;
		sb.append("Defination:\n");
		for (Object row : sns.getJSONArray("definition")) {
			sb.append("   ");
			if (sns.getJSONArray("definition").length() > 1) {
				sb.append(i);
				sb.append(". ");
			}
			sb.append(row.toString());
			sb.append("\n");
			i++;
		}
		if (sns.has("examples")) {
			i = 1;
			sb.append("Example:\n");
			for (Object row : sns.getJSONArray("examples")) {
				sb.append("   ");
				if (sns.getJSONArray("examples").length() > 1) {
					sb.append(i);
					sb.append(". ");
				}
				sb.append(((JSONObject) row).getString("text"));
				sb.append("\n");
				i++;
			}
		}
		sb.append("--------\n");
	}

}
