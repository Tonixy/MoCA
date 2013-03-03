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
package org.atilika.kuromoji.viterbi;

import java.util.LinkedList;
import java.util.List;

import org.atilika.kuromoji.Tokenizer.Mode;
import org.atilika.kuromoji.dict.CharacterDefinition;
import org.atilika.kuromoji.dict.CharacterDefinition.CharacterClass;
import org.atilika.kuromoji.dict.ConnectionCosts;
import org.atilika.kuromoji.dict.Dictionary;
import org.atilika.kuromoji.dict.TokenInfoDictionary;
import org.atilika.kuromoji.dict.UnknownDictionary;
import org.atilika.kuromoji.dict.UserDictionary;
import org.atilika.kuromoji.trie.DoubleArrayTrie;
import org.atilika.kuromoji.viterbi.ViterbiNode.Type;

import tony.tonixy.util.string.StringSizeConverter;


/**
 * TONIXY バグ修正
 * TONIXY サブクラスを作りやすいよう、可視性など大幅に変更
 *
 * @author Masaru Hasegawa
 * @author Christian Moen
 */
public class Viterbi {

	// TONIXY trueにするとNode情報を一覧できる
	private static final boolean printsNodes = false;

	// TONIXY サブクラスからの参照を可能にするため、privateをprotectedに変更
	protected final DoubleArrayTrie trie;

	protected final TokenInfoDictionary dictionary;

	protected final UnknownDictionary unkDictionary;

	protected final ConnectionCosts costs;

	// TONIXY ユーザ辞書変更を可能にしたため、finalを除去。サブクラスからの参照を可能にするため、privateをprotectedに変更
	protected UserDictionary userDictionary;

	// TONIXY サブクラスからの参照を可能にするため、privateをprotectedに変更
	protected final CharacterDefinition characterDefinition;

	// TONIXY ユーザ辞書変更を可能にしたため、finalを除去
	private boolean useUserDictionary;

	// TONIXY サブクラスからの参照を可能にするため、privateをprotectedに変更
	protected final boolean searchMode;

	protected final boolean extendedMode;

	protected static final int DEFAULT_COST = 10000000;

	protected static final int SEARCH_MODE_KANJI_LENGTH_DEFAULT = 2;

	protected static final int SEARCH_MODE_OTHER_LENGTH_DEFAULT = 7;

	protected static final int SEARCH_MODE_KANJI_PENALTY_DEFAULT = 3000;

	protected static final int SEARCH_MODE_OTHER_PENALTY_DEFAULT = 1700;

	protected final int searchModeKanjiPenalty;

	protected final int searchModeOtherPenalty;

	protected final int searchModeOtherLength;

	protected final int searchModeKanjiLength;

	protected static final String BOS = "BOS";

	protected static final String EOS = "EOS";

	/**
	 * TONIXY 未知語分解モードのON/OFF。未知語の部分文字列をLatticeに追加するかどうか。trueに設定することを推奨
	 */
	protected final boolean unknownFixMode;

	/**
	 * TONIXY 全角化のON/OFF。trueに設定することを推奨
	 */
	protected final boolean convertsSize;

	// TONIXY サブクラスからの参照を可能にするため、getterを作成
	protected UserDictionary getUserDictionary() {
		return userDictionary;
	}

	protected boolean usesUserDictionary() {
		return useUserDictionary;
	}

	protected boolean isUnknownFixMode() {
		return unknownFixMode;
	}

	/**
	 * TONIXY 非推奨。未知語分解モードがOFFになる。未知語分解モードの搭載後、従来通り動くためのコンストラクタ
	 *
	 * @param trie
	 * @param dictionary
	 * @param unkDictionary
	 * @param costs
	 * @param userDictionary
	 * @param mode
	 * @param searchModeKanjiPenalty
	 * @param searchModeOtherPenalty
	 * @param searchModeKanjiLength
	 * @param searchModeOtherLength
	 */
/*	public Viterbi(DoubleArrayTrie trie,
			   TokenInfoDictionary dictionary,
			   UnknownDictionary unkDictionary,
			   ConnectionCosts costs,
			   UserDictionary userDictionary,
			   Mode mode,
			   int searchModeKanjiPenalty,
			   int searchModeOtherPenalty,
			   int searchModeKanjiLength,
			   int searchModeOtherLength){
		this(trie, dictionary, unkDictionary, costs, userDictionary, mode, searchModeKanjiPenalty, searchModeOtherPenalty, searchModeKanjiLength, searchModeOtherLength, false, false);
	}
*/
	/**
	 * Constructor
	 * TONIXY 非推奨。未知語分解モードがOFFになる。未知語分解モードの搭載後、従来通り動くためのコンストラクタ
	 *
	 * @param trie
	 * @param targetMap
	 * @param dictionary
	 * @param unkDictionary
	 * @param costs
	 * @param userDictionary
	 */
/*	public Viterbi(DoubleArrayTrie trie,
				   TokenInfoDictionary dictionary,
				   UnknownDictionary unkDictionary,
				   ConnectionCosts costs,
				   UserDictionary userDictionary,
				   Mode mode) {
		this(trie, dictionary, unkDictionary, costs, userDictionary, mode, false, false);
	}
*/
	/**
	 * Constructor
	 * TONIXY 未知語分解モードの搭載のため、引数追加
	 *
	 * @param trie
	 * @param dictionary
	 * @param unkDictionary
	 * @param costs
	 * @param userDictionary
	 * @param mode
	 * @param searchModeKanjiPenalty
	 * @param searchModeOtherPenalty
	 * @param searchModeKanjiLength
	 * @param searchModeOtherLength
	 * @param unknownFixMode
	 * @param convertsSize
	 */
	public Viterbi(DoubleArrayTrie trie,
			   TokenInfoDictionary dictionary,
			   UnknownDictionary unkDictionary,
			   ConnectionCosts costs,
			   UserDictionary userDictionary,
			   Mode mode,
			   int searchModeKanjiPenalty,
			   int searchModeOtherPenalty,
			   int searchModeKanjiLength,
			   int searchModeOtherLength,
			   boolean unknownFixMode,
			   boolean convertsSize) {
		this.trie = trie;
		this.dictionary = dictionary;
		this.unkDictionary = unkDictionary;
		this.costs = costs;
		this.userDictionary = userDictionary;

		this.searchModeKanjiPenalty = searchModeKanjiPenalty;
		this.searchModeOtherPenalty = searchModeOtherPenalty;
		this.searchModeKanjiLength = searchModeKanjiLength;
		this.searchModeOtherLength = searchModeOtherLength;
		this.unknownFixMode = unknownFixMode;
		this.convertsSize = convertsSize;

		if (userDictionary == null) {
			this.useUserDictionary = false;
		} else {
			this.useUserDictionary = true;
		}

		switch (mode) {
		case SEARCH:
			searchMode = true;
			extendedMode = false;
			break;
		case EXTENDED:
			searchMode = true;
			extendedMode = true;
			break;
		default:
			searchMode = false;
			extendedMode = false;
			break;
		}

		this.characterDefinition = unkDictionary.getCharacterDefinition();
	}

	/**
	 * Constructor
	 * TONIXY 未知語分解モードの搭載のため、引数追加
	 *
	 * @param trie
	 * @param targetMap
	 * @param dictionary
	 * @param unkDictionary
	 * @param costs
	 * @param userDictionary
	 * @param unknownFixMode
	 * @param convertsSize
	 */
	public Viterbi(DoubleArrayTrie trie,
				   TokenInfoDictionary dictionary,
				   UnknownDictionary unkDictionary,
				   ConnectionCosts costs,
				   UserDictionary userDictionary,
				   Mode mode, boolean unknownFixMode, boolean convertsSize) {
		this(trie, dictionary, unkDictionary, costs, userDictionary, mode,
			 SEARCH_MODE_KANJI_PENALTY_DEFAULT, SEARCH_MODE_OTHER_PENALTY_DEFAULT,
			 SEARCH_MODE_KANJI_LENGTH_DEFAULT, SEARCH_MODE_OTHER_LENGTH_DEFAULT, unknownFixMode, convertsSize);
	}

	/**
	 * Find best path from input lattice.
	 * @param lattice the result of build method
	 * @return	List of ViterbiNode which consist best path
	 */
	public List<ViterbiNode> search(ViterbiNode[][][] lattice) {
		ViterbiNode[][] startIndexArr = lattice[0];
		ViterbiNode[][] endIndexArr = lattice[1];

		// ここからコスト計算とパス設定
		for (int i = 1; i < startIndexArr.length; i++){

			if (startIndexArr[i] == null || endIndexArr[i] == null){	// continue since no array which contains ViterbiNodes exists. Or no previous node exists.
				continue;
			}

			// 現在地から始まるViterbiNodeを順番に見る
			for (ViterbiNode node : startIndexArr[i]) {
				if (node == null){	// If array doesn't contain ViterbiNode any more, continue to next index
					break;
				}

				int backwardConnectionId = node.getLeftId();
				int wordCost = node.getWordCost();
				int leastPathCost = DEFAULT_COST;

				// ひとつ前となりうるViterbiNodeを順番に見る
				for (ViterbiNode leftNode : endIndexArr[i]) {
					if (leftNode == null){ // If array doesn't contain ViterbiNode any more, continue to next index
						break;
					}

					int pathCost = leftNode.getPathCost() + costs.get(leftNode.getRightId(), backwardConnectionId) + wordCost;	// cost = [total cost from BOS to previous node] + [connection cost between previous node and current node] + [word cost]

					// "Search mode". Add extra costs if it is long node.
					// ここからサーチモードの処理
					if (searchMode) {
//						System.out.print(""); // If this line exists, kuromoji runs faster for some reason when searchMode == false.
						String surfaceForm = node.getSurfaceForm();
						int length = surfaceForm.length();
						if (length > searchModeKanjiLength) {
							boolean allKanji = true;
							// check if node consists of only kanji
							for (int pos = 0; pos < length; pos++) {
								if (!characterDefinition.isKanji(surfaceForm.charAt(pos))){
									allKanji = false;
									break;
								}
							}

							if (allKanji) {	// Process only Kanji keywords
								pathCost += (length - searchModeKanjiLength) * searchModeKanjiPenalty;
							} else if (length > searchModeOtherLength) {
								pathCost += (length - searchModeOtherLength) * searchModeOtherPenalty;
//								pathCost += searchModePenalty;
							}
						}
					}
					// ここまでサーチモードの処理

					// 現時点での最小コストとなったなら、ひとつ前のViterbiNodeを記憶しておく
					if (pathCost < leastPathCost){	// If total cost is lower than before, set current previous node as best left node (previous means left).
						leastPathCost = pathCost;
						node.setPathCost(leastPathCost);
						node.setLeftNode(leftNode);
					}
				}
			}
		}
		// ここまでコスト計算とパス設定

		// track best path
		// バックトラック
		ViterbiNode node = endIndexArr[0][0];	// EOS
		LinkedList<ViterbiNode> result = new LinkedList<ViterbiNode>();
		result.add(node);// 最小コストのパスはEOSへとつながっているので、ベストパスの最後は必ずEOSになっている
		while (true) {
			ViterbiNode leftNode = node.getLeftNode();
			if (leftNode == null) {
				break;
			}

			// EXTENDED mode convert unknown word into unigram node
			// EXTENDEDモードの処理。未知語を1文字ずつ分割?
			if (extendedMode && leftNode.getType() == Type.UNKNOWN) {
				int unigramWordId = CharacterClass.NGRAM.getId();
				int unigramLeftId = unkDictionary.getLeftId(unigramWordId); // isn't required
				int unigramRightId = unkDictionary.getLeftId(unigramWordId); // isn't required
				int unigramWordCost = unkDictionary.getWordCost(unigramWordId); // isn't required
				String surfaceForm = leftNode.getSurfaceForm();
				for (int i = surfaceForm.length(); i > 0; i--) {
					ViterbiNode uniGramNode = new ViterbiNode(unigramWordId, surfaceForm.substring(i - 1, i), unigramLeftId, unigramRightId, unigramWordCost, leftNode.getStartIndex() + i - 1, Type.UNKNOWN);
					result.addFirst(uniGramNode);
				}
				// ここまでEXTENDEDモードの処理
			} else {
				result.addFirst(leftNode);
			}
			node = leftNode;
		}

		return result;
	}


	/**
	 * Build lattice from input text
	 * @param text
	 * @return
	 */
	public ViterbiNode[][][] build(String text) {
		int textLength = text.length();
		// startIndexArr[n]には、textのn文字目(1<=n<=text.length)から始まるViterbiNodeが入ってる。[0][0]にはBOS,[text.length()+1][0]にはEOS
		ViterbiNode[][] startIndexArr = new ViterbiNode[textLength + 2][];  // text length + BOS and EOS
		// endIndexArr[n]には、textのn-1文字目(2<=n<=text.length()+1)で終わるViterbiNodeが入ってる。
		ViterbiNode[][] endIndexArr = new ViterbiNode[textLength + 2][];  // text length + BOS and EOS
		// startSizeArr[n]は、startIndexArr[n]の長さ
		int[] startSizeArr = new int[textLength + 2]; // array to keep ViterbiNode count in startIndexArr
		// endSizeArr[n]は、endIndexArr[n]の長さ
		int[] endSizeArr = new int[textLength + 2];   // array to keep ViterbiNode count in endIndexArr

		ViterbiNode bosNode = new ViterbiNode(-1, BOS, 0, 0, 0, -1, Type.KNOWN);
		addToArrays(bosNode, 0, 1, startIndexArr, endIndexArr, startSizeArr, endSizeArr);

		// Process user dictionary;
		// 先にユーザ辞書の単語をすべて登録
		if (useUserDictionary) {
			processUserDictionary(text, startIndexArr, endIndexArr, startSizeArr, endSizeArr);
		}

		int unknownWordEndIndex = -1;	// index of the last character of unknown word

		for (int startIndex = 0; startIndex < textLength; startIndex++) {
			// If no token ends where current token starts, skip this index
			if (endSizeArr[startIndex + 1] == 0) {
				continue;
			}

			String suffix = text.substring(startIndex);

			boolean found = false;
			for (int endIndex = 1; endIndex < suffix.length() + 1; endIndex++) {
				String prefix = suffix.substring(0, endIndex); // startIndex～endIndexの部分文字列になる

				int result;
				if(convertsSize)
					result = trie.lookup(StringSizeConverter.getFullString(prefix));
				else
					result = trie.lookup(prefix);

				if (result > 0) {	// Found match in double array trie
					found = true;	// Don't produce unknown word starting from this index
					for (int wordId : dictionary.lookupWordIds(result)) {
						ViterbiNode node = new ViterbiNode(wordId, prefix, dictionary.getLeftId(wordId), dictionary.getRightId(wordId), dictionary.getWordCost(wordId), startIndex, Type.KNOWN);
						addToArrays(node, startIndex + 1, startIndex + 1 + endIndex, startIndexArr, endIndexArr, startSizeArr, endSizeArr);
					}
				} else if(result < 0) {	// If result is less than zero, continue to next position
						break;
				}
			}

			// In the case of normal mode, it doesn't process unknown word greedily.
			if(!searchMode && unknownWordEndIndex > startIndex){
				continue;
			}

			// Process Unknown Word
			int unknownWordLength = 0;
			char firstCharacter = suffix.charAt(0);
			boolean isInvoke = characterDefinition.isInvoke(firstCharacter);
			if (isInvoke){	// Process "invoke"
				unknownWordLength = unkDictionary.lookup(suffix);
			} else if (found == false){	// Process not "invoke"
				unknownWordLength = unkDictionary.lookup(suffix);
			}

			if (unknownWordLength > 0) {      // found unknown word
				String unkWord = suffix.substring(0, unknownWordLength);
				int characterId = characterDefinition.lookup(firstCharacter);
				int[] wordIds = unkDictionary.lookupWordIds(characterId); // characters in input text are supposed to be the same

				for (int wordId : wordIds) {
					// TONIXY 未知語の部分文字列をとって、すべて辞書に追加するよう変更。これによって、未知語部分をできるだけ短くする。
					// また、未知語直後にユーザ辞書内単語が来た時、ユーザ辞書内単語以前の結果が消滅する現象も修正できる。
					if (unknownFixMode)
						for (int i = 1; i <= unknownWordLength; i++) {
							ViterbiNode node = new ViterbiNode(wordId, unkWord.substring(0, i),
									unkDictionary.getLeftId(wordId), unkDictionary.getRightId(wordId),
									unkDictionary.getWordCost(wordId), startIndex, Type.UNKNOWN);
							addToArrays(node, startIndex + 1, startIndex + 1 + i, startIndexArr, endIndexArr,
									startSizeArr, endSizeArr);
						}
					else{
						ViterbiNode node = new ViterbiNode(wordId, unkWord, unkDictionary.getLeftId(wordId), unkDictionary.getRightId(wordId), unkDictionary.getWordCost(wordId), startIndex, Type.UNKNOWN);
						addToArrays(node, startIndex + 1, startIndex + 1 + unknownWordLength, startIndexArr, endIndexArr, startSizeArr, endSizeArr);
					}
				}
				unknownWordEndIndex = startIndex + unknownWordLength;
			}
		}

		ViterbiNode eosNode = new ViterbiNode(-1, EOS, 0, 0, 0, textLength + 1, Type.KNOWN);
		addToArrays(eosNode, textLength + 1, 0, startIndexArr, endIndexArr, startSizeArr, endSizeArr); //Add EOS node to endIndexArr at index 0

		ViterbiNode[][][] result = new ViterbiNode[][][]{startIndexArr, endIndexArr};

		return result;
	}

	/**
	 * Find token(s) in input text and set found token(s) in arrays as normal tokens
	 * ユーザ辞書にある単語を探してstartIndexArr, endIndexArrに登録
	 *
	 * TONIXY サブクラスからの参照を可能にするため、privateをprotectedに変更
	 *
	 * @param text
	 * @param startIndexArr
	 * @param endIndexArr
	 * @param startSizeArr
	 * @param endSizeArr
	 */
	protected void processUserDictionary(String text, ViterbiNode[][] startIndexArr, ViterbiNode[][] endIndexArr, int[] startSizeArr, int[] endSizeArr) {
		int[][] result = userDictionary.lookup(text);
		for(int[] segmentation : result) {
			int wordId = segmentation[0];
			int index = segmentation[1];
			int length = segmentation[2];
			ViterbiNode node = new ViterbiNode(wordId, text.substring(index, index + length), userDictionary.getLeftId(wordId), userDictionary.getRightId(wordId), userDictionary.getWordCost(wordId), index, Type.USER);
			addToArrays(node, index + 1, index + 1 + length, startIndexArr, endIndexArr, startSizeArr, endSizeArr);
		}
	}

	/**
	 * Add node to arrays and increment count in size array
	 * startIndexArrとendIndexArrにnodeを追加する。
	 *
	 * TONIXY サブクラスからの参照を可能にするため、privateをprotectedに変更
	 *
	 * @param node
	 * @param startIndex
	 * @param endIndex
	 * @param startIndexArr
	 * @param endIndexArr
	 * @param startSizeArr
	 * @param endSizeArr
	 */
	protected void addToArrays(ViterbiNode node, int startIndex, int endIndex, ViterbiNode[][] startIndexArr, ViterbiNode[][] endIndexArr, int[] startSizeArr, int[] endSizeArr ) {
		if(printsNodes) {
		Dictionary dic = null;
			try {
				switch (node.getType()) {
				case KNOWN:
					dic = dictionary;
					break;
				case UNKNOWN:
					dic = unkDictionary;
					break;
				case USER:
					dic = userDictionary;
					break;
				}
			} catch (Exception e) {
			}
			String[] features = { "?" };
			String pos = "";
			int wordId = node.getWordId();
			if(dic != null){
				features = dic.getAllFeaturesArray(wordId);
				pos = dic.getPartOfSpeech(wordId);
			}
			System.out.println(" * " + node.getSurfaceForm() + " <" + features[features.length - 1] + ">【" + pos + "】(" + startIndex
					+ ", " + endIndex + ") " + node.getWordCost());
		}
		int startNodesCount = startSizeArr[startIndex];
		int endNodesCount = endSizeArr[endIndex];

		// 必要に応じて初期化
		if (startNodesCount == 0) {
			startIndexArr[startIndex] = new ViterbiNode[10];
		}

		if (endNodesCount == 0) {
			endIndexArr[endIndex] = new ViterbiNode[10];
		}

		// 必要に応じて配列を長くする
		if (startIndexArr[startIndex].length <= startNodesCount){
			startIndexArr[startIndex] = extendArray(startIndexArr[startIndex]);
		}

		if (endIndexArr[endIndex].length <= endNodesCount){
			endIndexArr[endIndex] = extendArray(endIndexArr[endIndex]);
		}

		// 格納
		startIndexArr[startIndex][startNodesCount] = node;
		endIndexArr[endIndex][endNodesCount] = node;

		// 配列の長さを修正
		startSizeArr[startIndex] = startNodesCount + 1;
		endSizeArr[endIndex] = endNodesCount + 1;
	}


	/**
	 * Return twice as big array which contains value of input array
	 *
	 * @param array
	 * @return
	 */
	private ViterbiNode[] extendArray(ViterbiNode[] array) {
		// extend array
		ViterbiNode[] newArray = new ViterbiNode[array.length * 2];
		System.arraycopy(array, 0, newArray, 0, array.length);
		return newArray;
	}

	/**
	 * ユーザー辞書を変更します。TONIXY とにぃによる追記。
	 *
	 * @param userDictionary
	 */
	public void setUserDictionary(UserDictionary userDictionary) {
		this.userDictionary = userDictionary;
		if (userDictionary == null) {
			this.useUserDictionary = false;
		} else {
			this.useUserDictionary = true;
		}
	}
}
