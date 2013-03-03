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
package jp.ac.waseda.info.kake.moca.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.atilika.kuromoji.dict.TokenInfoDictionary;
import org.atilika.kuromoji.util.CSVUtil;
import org.atilika.kuromoji.util.DictionaryBuilder.DictionaryFormat;

import jp.ac.waseda.info.kake.string.StringSizeConverter;

/**
 * TODO UniDicで辞書作成を試す
 *
 * @author Sho
 *
 */
public class MocaTokenInfoDictionaryBuilder {

	/**
	 * Internal word id - incrementally assigned as entries are read and added.
	 * This will be byte offset of dictionary file
	 */
	private int offset = 0;

	private TreeMap<Integer, String> dictionaryEntries; // wordId, surface form

	private String encoding = "euc-jp";

	private boolean normalizeEntries = false;

	private DictionaryFormat format = DictionaryFormat.IPADIC;

	public MocaTokenInfoDictionaryBuilder() {
		this.dictionaryEntries = new TreeMap<Integer, String>();
	}

	public MocaTokenInfoDictionaryBuilder(DictionaryFormat format, String encoding, boolean normalizeEntries) {
		this.format = format;
		this.encoding = encoding;
		this.dictionaryEntries = new TreeMap<Integer, String>();
		this.normalizeEntries = normalizeEntries;
	}

	public TokenInfoDictionary build(String dirname) throws IOException {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".csv");
			}
		};
		ArrayList<File> csvFiles = new ArrayList<File>();
		for (File file : new File(dirname).listFiles(filter)) {
			csvFiles.add(file);
		}
		return buildDictionary(csvFiles);
	}

	public TokenInfoDictionary buildDictionary(List<File> csvFiles) throws IOException {
		TokenInfoDictionary dictionary = new TokenInfoDictionary(10 * 1024 * 1024);

		for (File file : csvFiles) {
			FileInputStream inputStream = new FileInputStream(file);
			InputStreamReader streamReader = new InputStreamReader(inputStream, encoding);
			BufferedReader reader = new BufferedReader(streamReader);

			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] entry = CSVUtil.parse(line);
				if (entry.length < 13) {
					System.out.println("Entry in CSV is not valid: " + line);
					continue;
				}
				String[] formatEntry = formatEntry(entry);
				int next = dictionary.put(formatEntry);

				if (next == offset) {
					System.out.println("Failed to process line: " + line);
					continue;
				}
				dictionaryEntries.put(offset, StringSizeConverter.getFullString(entry[0]));
				offset = next;
				if (!(formatEntry[0].equals(formatEntry[11])/* || formatEntry[0].equals(KanaConverter
						.getHiragana(formatEntry[11])))*/)) {//TODO 辞書軽量化すべき? しかしコメント外すと一部単語が入ってないっぽい
					next = dictionary.put(formatEntry);
					if (next == offset) {
						System.out.println("Failed to process line: " + line);
						continue;
					}
					dictionaryEntries.put(offset, StringSizeConverter.getFullString(formatEntry[11]));
					offset = next;// TONIXY 読みで追加
				}

				// NFKC normalize dictionary entry
				if (normalizeEntries) {
					if (entry[0].equals(Normalizer.normalize(entry[0], Normalizer.Form.NFKC))) {
						continue;
					}
					String[] normalizedEntry = new String[entry.length];
					for (int i = 0; i < entry.length; i++) {
						normalizedEntry[i] = Normalizer.normalize(entry[i], Normalizer.Form.NFKC);
					}

					formatEntry = formatEntry(normalizedEntry);
					next = dictionary.put(formatEntry);
					dictionaryEntries.put(offset, StringSizeConverter.getFullString(normalizedEntry[0]));
					offset = next;

					if (!(formatEntry[0].equals(formatEntry[11])/* || formatEntry[0].equals(KanaConverter
							.getHiragana(formatEntry[11]))*/)) {//TODO 同上
						next = dictionary.put(formatEntry);
						dictionaryEntries.put(offset, StringSizeConverter.getFullString(formatEntry[11]));
						offset = next;// TONIXY 読みで追加
					}
				}
			}
		}
		return dictionary;
	}

	/*
	 * IPADIC features TONIXY 末尾[13]に表層表現を追加
	 *
	 * 0 - surface 1 - left cost 2 - right cost 3 - word cost 4-9 - pos 10 -
	 * base form 11 - reading 12 - pronounciation
	 *
	 * UniDic features
	 *
	 * 0 - surface 1 - left cost 2 - right cost 3 - word cost 4-9 - pos 10 -
	 * base form reading 11 - base form 12 - surface form 13 - surface reading
	 */
	public String[] formatEntry(String[] features) {
		String[] features2 = new String[14];
		if (this.format == DictionaryFormat.IPADIC) {
			for (int i = 0; i < features.length; i++)
				features2[i] = features[i];
		} else {
			features2[0] = features[0];
			features2[1] = features[1];
			features2[2] = features[2];
			features2[3] = features[3];
			features2[4] = features[4];
			features2[5] = features[5];
			features2[6] = features[6];
			features2[7] = features[7];
			features2[8] = features[8];
			features2[9] = features[9];
			features2[10] = features[11];

			// If the surface reading is non-existent, use surface form for
			// reading and pronunciation.
			// This happens with punctuation in UniDic and there are possibly
			// other cases as well
			if (features[13].length() == 0) {
				features2[11] = features[0];
				features2[12] = features[0];
			} else {
				features2[11] = features[13];
				features2[12] = features[13];
			}
		}
		features2[13] = features[0];
		return features2;
	}

	public Set<Entry<Integer, String>> entrySet() {
		return dictionaryEntries.entrySet();
	}
}
