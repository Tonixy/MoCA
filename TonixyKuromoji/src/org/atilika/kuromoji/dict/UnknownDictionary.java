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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.atilika.kuromoji.dict.CharacterDefinition.CharacterClass;

/**
 * 0.7.5版から変更あり。getBaseformが追加(必ずnullを返す)
 *
 * @author Masaru Hasegawa
 * @author Christian Moen
 */
public class UnknownDictionary extends TokenInfoDictionary {

	public static final String FILENAME = "unk.dat";

	public static final String TARGETMAP_FILENAME = "unk_map.dat";

	public static final String CHARDEF_FILENAME = "cd.dat";

	private CharacterDefinition characterDefinition;

	/**
	 * Constructor
	 */
    public UnknownDictionary() {
    }

    public UnknownDictionary(int size) {
    	super(size);
		characterDefinition = new CharacterDefinition();
    }

    @Override
    public int put(String[] entry) {
    	// Get wordId of current entry
    	int wordId = buffer.position();

    	// Put entry
		int result = super.put(entry);

		// Put entry in targetMap
		int characterId = CharacterClass.valueOf(entry[0]).getId();
		addMapping(characterId, wordId);
		return result;
    }

    public int lookup(String text) {
    	if(!characterDefinition.isGroup(text.charAt(0))) {
    		return 1;
    	}

    	// Extract unknown word. Characters with the same character class are considered to be part of unknown word
    	int characterIdOfFirstCharacter = characterDefinition.lookup(text.charAt(0));
    	int length = 1;
    	for (int i = 1; i < text.length(); i++) {
    		if (characterIdOfFirstCharacter == characterDefinition.lookup(text.charAt(i))){
        		length++;
    		} else {
    			break;
    		}
    	}

    	return length;
    }

	/**
	 * Put mapping from unicode code point to character class.
	 *
	 * @param codePoint code point
	 * @param class character class name
	 */
	public void putCharacterCategory(int codePoint, String characterClassName) {
		characterDefinition.putCharacterCategory(codePoint, characterClassName);
	}

	public void putInvokeDefinition(String characterClassName, int invoke, int group, int length) {
		characterDefinition.putInvokeDefinition(characterClassName, invoke, group, length);
	}


	public CharacterDefinition getCharacterDefinition() {
		return characterDefinition;
	}

	/**
	 * Write dictionary in file
	 * Dictionary format is:
	 * [Size of dictionary(int)], [entry:{left id(short)}{right id(short)}{word cost(short)}{length of pos info(short)}{pos info(char)}], [entry...], [entry...].....
	 * @param filename
	 * @throws IOException
	 */
	public void write(String directoryname) throws IOException {
		writeDictionary(directoryname + File.separator + FILENAME);
		writeTargetMap(directoryname + File.separator + TARGETMAP_FILENAME);
		writeCharDef(directoryname + File.separator + CHARDEF_FILENAME);
	}

	protected void writeCharDef(String filename) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
		oos.writeObject(characterDefinition);
		oos.close();
	}

	// TONIXY ファイル名を指定するための引数追加
	public static UnknownDictionary getInstance(String fileNamePrefix) throws IOException, ClassNotFoundException {
		UnknownDictionary dictionary = new UnknownDictionary();
		ClassLoader loader = dictionary.getClass().getClassLoader();
		dictionary.loadDictionary(loader.getResourceAsStream(fileNamePrefix + FILENAME));
		dictionary.loadTargetMap(loader.getResourceAsStream(fileNamePrefix + TARGETMAP_FILENAME));
		dictionary.loadCharDef(loader.getResourceAsStream(fileNamePrefix + CHARDEF_FILENAME));
		return dictionary;
	}

	public static UnknownDictionary getInstance() throws IOException, ClassNotFoundException {
		return getInstance("");
	}

	protected void loadCharDef(InputStream is) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(is));
		characterDefinition = (CharacterDefinition) ois.readObject();
		ois.close();
	}

	@Override
	public String getReading(int wordId) {
		return null;
	}

	@Override
	public String getBaseForm(int wordId) {
		return null;
	}
}
