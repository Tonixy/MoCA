/**
 * Copyright © 2010-2012 Atilika Inc.  All rights reserved.
 *
 * Atilika Inc. licenses this file to you under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with
 * the License.  A copy of the License is distributed with this work in the
 * LICENSE.txt file.  You may also obtain a copy of the License from
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.atilika.kuromoji.dict;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.atilika.kuromoji.util.CSVUtil;

/**
 * @author Masaru Hasegawa
 * @author Christian Moen
 */
public class UserDictionary implements Dictionary {

	private TreeMap<String, int[]> entries = new TreeMap<String, int[]>();

	private HashMap<Integer, String> featureEntries = new HashMap<Integer, String>();

	private static final int CUSTOM_DICTIONARY_WORD_ID_OFFSET = 100000000;

	public static final int WORD_COST = -100000;

	public static final int LEFT_ID = 5;

	public static final int RIGHT_ID = 5;

	public UserDictionary() {

	}

	/**
	 * Lookup words in text
	 * ユーザ辞書に登録された単語の位置を全て探し出し、 {wordId, position, length} の形の2次元配列で返す
	 *
	 * @param text
	 * @return array of {wordId, position, length}
	 */
	public int[][] lookup(String text) {
		TreeMap<Integer, int[]> result = new TreeMap<Integer, int[]>(); // index, [length, length...]

		for (String keyword : entries.descendingKeySet()) {
			int offset = 0;
			int position = text.indexOf(keyword, offset);
			while (offset < text.length() && position >= 0) {
				if(!result.containsKey(position)){
					result.put(position, entries.get(keyword));
				}
				offset += position + keyword.length();
				position = text.indexOf(keyword, offset);
			}
		}

		return toIndexArray(result);
	}

	/**
	 * Convert Map of index and wordIdAndLength to array of {wordId, index, length}
	 *
	 * @param input
	 * @return array of {wordId, index, length}
	 */
	private int[][] toIndexArray(Map<Integer, int[]> input) {
		ArrayList<int[]> result = new ArrayList<int[]>();
		for (int i : input.keySet()) {
			int[] wordIdAndLength = input.get(i);
			int wordId = wordIdAndLength[0];
			// convert length to index
			int current = i;
			for (int j = 1; j < wordIdAndLength.length; j++) { // first entry is wordId offset
				int[] token = { wordId + j - 1, current, wordIdAndLength[j] };
				result.add(token);
				current += wordIdAndLength[j];
			}
		}
		return result.toArray(new int[result.size()][]);
	}

	@Override
	public int getLeftId(int wordId) {
		return LEFT_ID;
	}

	@Override
	public int getRightId(int wordId) {
		return RIGHT_ID;
	}

	/**
	 * ユーザ辞書のWORD_COST = -100000を返す
	 */
	@Override
	public int getWordCost(int wordId) {
		return WORD_COST;
	}

	@Override
	public String getReading(int wordId) {
		return getFeature(wordId, 0);
	}

	@Override
	public String getBaseForm(int wordId) {
		return null; // NOTE: Currently unsupported TODO ユーザ辞書のgetBaseFormはnullでいい?
	}

	@Override
	public String getPartOfSpeech(int wordId) {
		return getFeature(wordId, 1);
	}

	@Override
	public String getAllFeatures(int wordId) {
		return getFeature(wordId);
	}

	@Override
	public String[] getAllFeaturesArray(int wordId) {
		String allFeatures = featureEntries.get(wordId);
		if(allFeatures == null) {
			return null;
		}

		return allFeatures.split(INTERNAL_SEPARATOR);
	}


	@Override
	public String getFeature(int wordId, int... fields) {
		String[] allFeatures = getAllFeaturesArray(wordId);
		if (allFeatures == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		if (fields.length == 0) { // All features
			for (String feature : allFeatures) {
				sb.append(CSVUtil.quoteEscape(feature)).append(",");
			}
		} else if (fields.length == 1) { // One feature doesn't need to escape value
			sb.append(allFeatures[fields[0]]).append(",");
		} else {
			for (int field : fields){
				sb.append(CSVUtil.quoteEscape(allFeatures[field])).append(",");
			}
		}
		return sb.deleteCharAt(sb.length() - 1).toString();
	}

	public static UserDictionary read(String filename) throws IOException {
		return read(new FileInputStream(filename));
	}

	// TONIXY 下の引数型変更に伴う追加。InputStreamのままでも使えるように。
	public static UserDictionary read(InputStream is) throws IOException {
		return read(new InputStreamReader(is));
	}

	// TONIXY ユーザ辞書読込の自由度を上げるため、引数をReader型に変更。
	public static UserDictionary read(Reader is) throws IOException {
		UserDictionary dictionary = new UserDictionary();
		BufferedReader reader = new BufferedReader(is);
		String line = null;
		int wordId = CUSTOM_DICTIONARY_WORD_ID_OFFSET;
		while ((line = reader.readLine()) != null) {
			// Remove comments TONIXY 登録できる文字の自由度を上げるため、Kuromojiユーザ辞書コメントに非対応にした。
			// line = line.replaceAll("#.*$", "");
			// TODO Kuromojiユーザ辞書コメントに対応させる。全角半角非依存にし、必ず全角登録する仕様にすれば問題ない

			// Skip empty lines or comment lines
			if (line.trim().length() == 0) {
				continue;
			}
			String[] values = CSVUtil.parse(line);
			String[] segmentation = values[1].replaceAll("  *", " ").split(" ");
			String[] readings = values[2].replaceAll("  *", " ").split(" ");
			String pos = values[3];

			if (segmentation.length != readings.length) {
				// FIXME: Should probably deal with this differently.  Exception?
				System.out.println("This entry is not properly formatted : " + line);
			}

			int[] wordIdAndLength = new int[segmentation.length + 1]; // wordId offset, length, length....
			wordIdAndLength[0] = wordId;
			for (int i = 0; i < segmentation.length; i++) {
				wordIdAndLength[i + 1] = segmentation[i].length();
				dictionary.featureEntries.put(wordId, readings[i] + INTERNAL_SEPARATOR + pos);
				wordId++;
			}
			dictionary.entries.put(values[0], wordIdAndLength);
		}
		reader.close();
		return dictionary;
	}
}
