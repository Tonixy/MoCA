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
package org.atilika.kuromoji;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.atilika.kuromoji.dict.Dictionaries;
import org.atilika.kuromoji.dict.Dictionary;
import org.atilika.kuromoji.dict.TokenInfoDictionary;
import org.atilika.kuromoji.dict.UnknownDictionary;
import org.atilika.kuromoji.dict.UserDictionary;
import org.atilika.kuromoji.viterbi.Viterbi;
import org.atilika.kuromoji.viterbi.ViterbiNode;
import org.atilika.kuromoji.viterbi.ViterbiNode.Type;

/**
 * Tokenizer main class.
 * Thread safe.
 *
 * @author Masaru Hasegawa
 * @author Christian Moen
 */
public class Tokenizer {
	public enum Mode {
		NORMAL, SEARCH, EXTENDED
	}

	/**
	 * TONIXY protectedに変更
	 */
	protected final Viterbi viterbi;

	private final EnumMap<Type, Dictionary> dictionaryMap = new EnumMap<Type, Dictionary>(Type.class);

	private final boolean split;

	/**
	 * Constructor
	 * TONIXY 非推奨。未知語分解モードがOFFになる。未知語分解モードの搭載後、従来通り動くためのコンストラクタ
	 *
	 * @param dictionary
	 * @param costs
	 * @param trie
	 * @param unkDictionary
	 * @param userDictionary
	 * @param mode
	 */
/*	protected Tokenizer(UserDictionary userDictionary, Mode mode, boolean split) {
		this(userDictionary, mode, split, false);
	}
*/
	/**
	 * TONIXY 未知語分解モードの搭載のため、引数追加
	 *
	 * @param dictionary
	 * @param costs
	 * @param trie
	 * @param unkDictionary
	 * @param userDictionary
	 * @param mode
	 */
	protected Tokenizer(UserDictionary userDictionary, Mode mode, boolean split, boolean unknownFixMode, boolean convertsSize) {
		this(new Viterbi(Dictionaries.getTrie(), Dictionaries.getDictionary(),
                Dictionaries.getUnknownDictionary(), Dictionaries.getCosts(), userDictionary,
                mode, unknownFixMode, convertsSize), Dictionaries.getDictionary(), Dictionaries.getUnknownDictionary(),
                userDictionary, mode, split, unknownFixMode);
	}

	/**
	 * Constructor
	 * TONIXY Viterbiや辞書の差し替えを可能に
	 *
	 * @param viterbi
	 * @param dictionary
	 * @param unkDictionary
	 * @param userDictionary
	 * @param mode
	 * @param split
	 * @param unknownFixMode
	 */
	protected Tokenizer(Viterbi viterbi, TokenInfoDictionary dictionary, UnknownDictionary unkDictionary,
			UserDictionary userDictionary, Mode mode, boolean split, boolean unknownFixMode) {

		this.viterbi = viterbi;
		this.split = split;

		dictionaryMap.put(Type.KNOWN, dictionary);
		dictionaryMap.put(Type.UNKNOWN, unkDictionary);
		dictionaryMap.put(Type.USER, userDictionary);
	}

	/**
	 * Tokenize input text
	 * @param text
	 * @return list of Token
	 */
	public List<Token> tokenize(String text) {

		// 。、を先に処理しない場合
		if (!split) {
			return doTokenize(0, text);
		}

		// 。、の位置を取得
		List<Integer> splitPositions = getSplitPositions(text);

		if(splitPositions.size() == 0) {
			return doTokenize(0, text);
		}

		// 。、の後で区切って処理していく(前で区切って。、のみのトークンを作ったりはしない)
		ArrayList<Token> result = new ArrayList<Token>();
		int offset = 0;
		for(int position : splitPositions) {
			result.addAll(doTokenize(offset, text.substring(offset, position + 1)));
			offset = position + 1;
		}

		// 最後の。、以降の解析
		if(offset < text.length()) {
			result.addAll(doTokenize(offset, text.substring(offset)));
		}

		return result;
	}

	/**
	 * Split input text at 句読点, which is 。 and 、
	 * @param text
	 * @return list of split position
	 */
	private List<Integer> getSplitPositions(String text) {
		ArrayList<Integer> splitPositions = new ArrayList<Integer>();

		int position = 0;
		int currentPosition = 0;

		while(true) {
			int indexOfMaru = text.indexOf("。", currentPosition);
			int indexOfTen = text.indexOf("、", currentPosition);

			if(indexOfMaru < 0 || indexOfTen < 0) {
				position = Math.max(indexOfMaru, indexOfTen);;
			} else {
				position = Math.min(indexOfMaru, indexOfTen);
			}

			if(position >= 0) {
				splitPositions.add(position);
				currentPosition = position + 1;
			} else {
				break;
			}
		}

		return splitPositions;
	}

	/**
	 * Tokenize input sentence.
	 * 実際にはsearchで得たViterbiNode列からTokenを作ってArrayListに入れて返す
	 *
	 * @param offset offset of sentence in original input text
	 * @param sentence sentence to tokenize
	 * @return list of Token
	 */
	private List<Token> doTokenize(int offset, String sentence) {
		ArrayList<Token> result = new ArrayList<Token>();

		ViterbiNode[][][] lattice = viterbi.build(sentence);
		List<ViterbiNode> bestPath = viterbi.search(lattice);
		for (ViterbiNode node : bestPath) {
			int wordId = node.getWordId();
			if (node.getType() == Type.KNOWN && wordId == -1) { // Do not include BOS/EOS
				continue;
			}
			Token token = new Token(wordId, node.getSurfaceForm(), node.getType(), offset + node.getStartIndex(), dictionaryMap.get(node.getType()));	// Pass different dictionary based on the type of node
			result.add(token);
		}

		return result;
	}

	/**
	 * Get Builder to create Tokenizer instance.
	 * @return Builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * ユーザー辞書を変更します。TONIXY とにぃによる追記。
	 *
	 * @param userDictionary
	 */
	public void setUserDictionary(UserDictionary userDictionary) {
		dictionaryMap.put(Type.USER, userDictionary);
		viterbi.setUserDictionary(userDictionary);
	}

	/**
	 * Builder class used to create Tokenizer instance.
	 */
	public static class Builder {

		private Mode mode = Mode.NORMAL;

		private boolean split = true;

		/**
		 * TONIXY 未知語分解モードのON/OFF。未知語の部分文字列をLatticeに追加するかどうか。trueに設定することを推奨
		 */
		private boolean unknownFixMode = false;

		/**
		 * TONIXY 全角化のON/OFF。trueに設定することを推奨
		 */
		private boolean convertsSize = false;

		private UserDictionary userDictionary = null;

		/**
		 * Set tokenization mode
		 * Default: NORMAL
		 *
		 * モード設定 Default: NORMAL
		 *
		 * @param mode tokenization mode
		 * @return Builder
		 */
		public synchronized Builder mode(Mode mode) {
			this.mode = mode;
			return this;
		}

		/**
		 * Set if tokenizer should split input string at "。" and "、" before tokenize to increase performance.
		 * Splitting shouldn't change the result of tokenization most of the cases.
		 * Default: true
		 *
		 * 先に、。で区切ってパフォーマンスを上げるか否か
		 *
		 * @param split whether tokenizer should split input string
		 * @return Builder
		 */
		public synchronized Builder split(boolean split) {
			this.split = split;
			return this;
		}

		/**
		 * TONIXY 全角化のON/OFFを設定します。Kuromojiの挙動を保持するためデフォルトはOFFですが、ONにしておくことを推奨します。
		 *
		 * ON(true)にすると、辞書から単語を検索する際に、自動的に全角文字に変換して検索します。アルファベットや記号を認識しやすくなります。
		 *
		 * @param convertsSize
		 * @return
		 */
		public synchronized Builder convertsSize(boolean convertsSize) {
			this.convertsSize = convertsSize;
			return this;
		}

		/**
		 * TONIXY 未知語分解モードのON/OFFを設定します。Kuromojiの挙動を保持するためデフォルトはOFFですが、ONにしておくことを推奨します。
		 *
		 * ON(true)にすると、未知語の部分文字列をLatticeに追加することで、未知語部分をできるだけ短くすることができます。
		 * また、未知語直後にユーザ辞書内単語が来た時、ユーザ辞書内単語以前の結果が消滅する現象も回避できます。
		 *
		 * @param unknownFixMode
		 * @return
		 */
		public synchronized Builder unknownFixMode(boolean unknownFixMode) {
			this.unknownFixMode = unknownFixMode;
			return this;
		}

		/**
		 * Set user dictionary input stream
		 * @param userDictionaryInputStream dictionary file as input stream
		 * @return Builder
		 * @throws IOException
		 */
		public synchronized Builder userDictionary(InputStream userDictionaryInputStream) throws IOException {
			this.userDictionary = UserDictionary.read(userDictionaryInputStream);
			return this;
		}

		/**
		 * TONIXY ユーザ辞書読込の自由度を上げるため、Reader型版を追加
		 *
		 * @param userDictionaryReader
		 * @return
		 * @throws IOException
		 */
		public synchronized Builder userDictionary(Reader userDictionaryReader)
				throws IOException {
			this.userDictionary = UserDictionary.read(userDictionaryReader);
			return this;
		}

		/**
		 * Set user dictionary path
		 * @param userDictionaryPath path to dictionary file
		 * @return Builder
		 * @throws IOException
		 * @throws FileNotFoundException
		 */
		public synchronized Builder userDictionary(String userDictionaryPath) throws FileNotFoundException, IOException {
			if (userDictionaryPath != null && ! userDictionaryPath.isEmpty()) {
				this.userDictionary(new BufferedInputStream(new FileInputStream(userDictionaryPath)));
			}
			return this;
		}

		/**
		 * Create Tokenizer instance
		 * @return Tokenizer
		 */
		public synchronized Tokenizer build() {
			return new Tokenizer(userDictionary, mode, split, unknownFixMode, convertsSize);
		}
	}
}
