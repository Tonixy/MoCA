package jp.ac.waseda.info.kake.moca.viterbi;

import jp.ac.waseda.info.kake.moca.MocaTokenizer;
import jp.ac.waseda.info.kake.moca.MocaTokenizer.MocaMode;
import jp.ac.waseda.info.kake.moca.syllable.SyllabifiedString;
import jp.ac.waseda.info.kake.moca.syllable.Syllable;
import jp.ac.waseda.info.kake.moca.viterbi.wordcost.AbstractWordCostAdjuster;
import jp.ac.waseda.info.kake.moca.viterbi.wordcost.KanaWordCostAdjuster;
import jp.ac.waseda.info.kake.moca.viterbi.wordcost.LevenshteinWordCostAdjuster;
import jp.ac.waseda.info.kake.moca.viterbi.wordcost.BaseWordCostAdjuster;
import jp.ac.waseda.info.kake.moca.viterbi.wordcost.MultipliedWordCostAdjuster;
import jp.ac.waseda.info.kake.moca.viterbi.wordcost.UnknownWordCostAdjuster;
import jp.ac.waseda.info.kake.string.KanaConverter;
import jp.ac.waseda.info.kake.string.StringSizeConverter;

import org.atilika.kuromoji.dict.ConnectionCosts;
import org.atilika.kuromoji.dict.TokenInfoDictionary;
import org.atilika.kuromoji.dict.UnknownDictionary;
import org.atilika.kuromoji.dict.UserDictionary;
import org.atilika.kuromoji.trie.DoubleArrayTrie;
import org.atilika.kuromoji.viterbi.Viterbi;
import org.atilika.kuromoji.viterbi.ViterbiNode;
import org.atilika.kuromoji.viterbi.ViterbiNode.Type;


/**
 * ViterbiをMoCA用に改変したものです。SyllabifiedStringを用い、音節変形を考慮した形態素解析を行います。
 *
 * @author Tony
 *
 */
public class MocaViterbi extends Viterbi {

	private MocaMode mode;

	/**
	 * 音節連結をした場合のコスト倍率
	 */
	private static final int AMPLIFICATION_CONNECT = 2;

	private static enum PrefixType {
		LITERARY(new BaseWordCostAdjuster()), SYLLABIFIED(new LevenshteinWordCostAdjuster()), KANACONVERTED(
				new KanaWordCostAdjuster()), SYLLABIFIED_AND_KANACONVERTED(new MultipliedWordCostAdjuster(
				SYLLABIFIED.adjuster, KANACONVERTED.adjuster));

		public final AbstractWordCostAdjuster adjuster;

		private PrefixType(AbstractWordCostAdjuster adjuster) {
			this.adjuster = adjuster;
		}
	};

	private static UnknownWordCostAdjuster unkAdjuster = new UnknownWordCostAdjuster();

	public MocaViterbi(DoubleArrayTrie trie, TokenInfoDictionary dictionary, UnknownDictionary unkDictionary,
			ConnectionCosts costs, UserDictionary userDictionary, MocaMode mode) {
		super(trie, dictionary, unkDictionary, costs, userDictionary, MocaTokenizer.kuroMode,
				MocaTokenizer.unknownFixMode, MocaTokenizer.convertsSize);
		this.mode = mode;
	}

	@Override
	public ViterbiNode[][][] build(String text) {
		int textLength = text.length();
		// startIndexArr[n]には、textのn文字目(1<=n<=text.length)から始まるViterbiNodeが入ってる。[0][0]にはBOS,[text.length()+1][0]にはEOS
		ViterbiNode[][] startIndexArr = new ViterbiNode[textLength + 2][];
		// endIndexArr[n]には、textのn-1文字目(2<=n<=text.length()+1)で終わるViterbiNodeが入ってる。
		ViterbiNode[][] endIndexArr = new ViterbiNode[textLength + 2][];
		// startSizeArr[n]は、startIndexArr[n]の長さ
		int[] startSizeArr = new int[textLength + 2];
		// endSizeArr[n]は、endIndexArr[n]の長さ
		int[] endSizeArr = new int[textLength + 2];

		ViterbiNode bosNode = new ViterbiNode(-1, BOS, 0, 0, 0, -1, Type.KNOWN);
		addToArrays(bosNode, 0, 1, startIndexArr, endIndexArr, startSizeArr, endSizeArr);

		// Process user dictionary;
		// 先にユーザ辞書の単語をすべて登録
		if (usesUserDictionary()) {
			processUserDictionary(text, startIndexArr, endIndexArr, startSizeArr, endSizeArr);
		}

		SyllabifiedString syls = new SyllabifiedString(text);

		int unknownWordEndIndex = -1; // index of the last character of unknown
										// word
		int katakanaIndex = -1;// カタカナ語登録に使用

		// 開始文字位置のループ
		for (int startIndex = 0; startIndex < textLength; startIndex++) {
			// If no token ends where current token starts, skip this index
			if (endSizeArr[startIndex + 1] == 0) {
				continue;
			}

			int result;
			boolean alreadyFound = false; // この開始文字位置で既に単語が見つかっているか否か

			// 終了文字位置のループ
			for (int endIndex = startIndex + 1; endIndex <= textLength; endIndex++) {
				boolean found = false; // 一致があるかどうか
				boolean prefixFound = false; // 前方一致があるかどうか
				boolean readingPrefixFound = false; // 読み仮名検索による前方一致があるかどうか

				String[] prefixes;
				if (mode.analysesSyllables)
					prefixes = syls.substrings(startIndex, endIndex); // startIndex～endIndexの部分文字列になる
				else
					prefixes = new String[] { text.substring(startIndex, endIndex) };

				// 変換候補のループ
				for (String prefix : prefixes) {
					prefix = StringSizeConverter.getFullString(prefix);
					PrefixType prefixType = PrefixType.SYLLABIFIED;
					if (prefix.equals(prefixes[0]))
						prefixType = PrefixType.LITERARY;
					result = trie.lookup(prefix);
					if (result >= 0) {
						readingPrefixFound = true;
						if (!KanaConverter.isKatakana(prefix))
							prefixFound = true;
					}
					// 表層表現がヒットしなかったら、読み検索
					if (mode.analysesReading && result <= 0) {
						result = trie.lookup(KanaConverter.getKatakana(prefix));
						if (result >= 0) {
							readingPrefixFound = true;
							if (prefixType == PrefixType.SYLLABIFIED)
								prefixType = PrefixType.SYLLABIFIED_AND_KANACONVERTED;
							else
								prefixType = PrefixType.KANACONVERTED;
						}
					}

					if (result > 0) { // Found match in double array trie
						for (int wordId : dictionary.lookupWordIds(result)) {
							String[] features = dictionary.getAllFeaturesArray(wordId);

							// 読み検索用の単語でないかチェック
							PrefixType wordType = prefixType;
							if (!prefix.equals(features[features.length - 1])) {
								if (wordType == PrefixType.SYLLABIFIED)
									wordType = PrefixType.SYLLABIFIED_AND_KANACONVERTED;
								else if (wordType == PrefixType.LITERARY)
									wordType = PrefixType.KANACONVERTED;
							}
							// MoCAの設定が読み仮名検索を許可しているかチェック
							if ((wordType == PrefixType.KANACONVERTED || wordType == PrefixType.SYLLABIFIED_AND_KANACONVERTED)
									&& !mode.analysesReading) {
								continue;
							}

							found = true;
							prefixFound = true;
							readingPrefixFound = true;
							alreadyFound = true;

							// 形態素の表層表現設定
							String surface = prefixes[0];
							if (!mode.returnsColloquial) {
								surface = features[features.length - 1];
							}

							ViterbiNode node = new ViterbiNode(wordId, surface, dictionary.getLeftId(wordId),
									dictionary.getRightId(wordId), wordType.adjuster.getWordCost(prefixes[0], prefix,
											dictionary, wordId), startIndex, Type.KNOWN);
							addToArrays(node, startIndex + 1, endIndex + 1, startIndexArr, endIndexArr, startSizeArr,
									endSizeArr);

							// 音節連結処理
							connect: if (mode.analysesSyllables && endIndex < textLength) {
								Syllable before = syls.getSyllable(endIndex - 1);
								Syllable next = syls.getSyllable(endIndex);
								check: switch (next.type) {
								case Syllable.VOWEL:
									// 漢字音節後に母音音節が続くとき、漢字音節の読みによって挙動を変化させる
									if (before.type != Syllable.KANJI || before.endsWithNasal()
											|| before.endsWithLong())
										break connect;// 漢字直後に撥音がついている場合は連結処理しない
									// TODO かなの時に合わせ、長音記号が挟まる場合連結してない。これでいい?
									Syllable[] reading = Syllable.createSyllables(dictionary.getReading(wordId));
									Syllable readLast = reading[reading.length - 1];
									if (readLast.endsWithNasal())
										break connect;// 万が一、読みの最後に撥音がついている場合は連結処理しない
									switch (readLast.getVowel()) {
									case Syllable.UNREADABLE:
										break connect;
									case 'e':
										if (next.getVowel() == 'e')
											break;
										// 漢字の読みの最後音節がエ/オ母音かつ長音節である場合のみ、イ/ウ母音への連結を許す
										if ((before.endsWithLong() || readLast.endsWithLong())
												&& next.getVowel() == 'i')
											// beforeは、漢字直後に長音記号が来た時も長音節であると扱うために必要
											break;
										break connect;
									case 'o':
										if ((before.endsWithLong() || readLast.endsWithLong())
												&& next.getVowel() == 'u')
											break;
									case 'u':
									case 'i':
									case 'a':
										if (next.getVowel() == readLast.getVowel())
											break;
									default:
										break connect;
									}
									break check;
								case Syllable.LONG:
									// LONGは、記号以外のものなら繋がりうる。記号以外にLONG音節が作られる条件=直前に撥促音があること
									if (before.endsWithNasal())
										break check;
									break connect;
								case Syllable.NASAL:
									// NASALは、記号以外のものなら繋がりうる。記号以外にNASAL音節が作られる条件=直前に長音があること
									// TODO どっちをとる?
									// 今のルールは顔文字内の「っ」が記号とくっつかなくなるが、「、。」等の後のが省略されない
									// break check;
									if (before.endsWithLong())
										break check;
									break connect;
								default:
									break connect;
								}

								// コスト計算、表層表現を修正する必要がある
								String substring = text.substring(startIndex, endIndex + next.length);
								surface = substring;
								if (!mode.returnsColloquial) {
									surface = features[features.length - 1];
								}
								node = new ViterbiNode(wordId, surface, dictionary.getLeftId(wordId),
										dictionary.getRightId(wordId), prefixType.adjuster.getWordCost(substring,
												prefix, dictionary, wordId) * AMPLIFICATION_CONNECT, startIndex,
										Type.KNOWN);
								addToArrays(node, startIndex + 1, endIndex + next.length + 1, startIndexArr,
										endIndexArr, startSizeArr, endSizeArr);
							}
							// ここまで、漢字音節→母音音節の連結処理
						}
					}
				}

				// 前方一致すらない場合は、未知語処理して終了文字位置ループを抜ける
				if (!(found || (prefixFound && endIndex < textLength))) {
					if (mode.analysesColloquial && !alreadyFound)
						// MoCA版未知語処理。
						for (int wordId : unkDictionary
								.lookupWordIds(characterDefinition.lookup(prefixes[0].charAt(0))))
							for (int i = 1; i <= prefixes[0].length(); i++) {
								ViterbiNode node = new ViterbiNode(wordId, prefixes[0].substring(0, i),
										unkDictionary.getLeftId(wordId), unkDictionary.getRightId(wordId),
										unkDictionary.getWordCost(wordId) * i, startIndex, Type.UNKNOWN);
								addToArrays(node, startIndex + 1, startIndex + i + 1, startIndexArr, endIndexArr,
										startSizeArr, endSizeArr);
							}
					if (!readingPrefixFound)
						break;
				}
			}

			// 辞書にないカタカナ語を無条件で未知語として追加

			// 全体を登録するパターン
			k: if (KanaConverter.isKatakana(text.charAt(startIndex))
					&& (!KanaConverter.isSpecialCharacter(text.charAt(startIndex))) && startIndex >= katakanaIndex) {
				katakanaIndex = startIndex + 1;
				while (katakanaIndex < textLength && KanaConverter.isKatakana(text.charAt(katakanaIndex))) {
					katakanaIndex++;
				}
				String katakana = text.substring(startIndex, katakanaIndex);
				result = trie.lookup(katakana);

				// 辞書に一致する単語があったら登録しない。読み検索用の単語なら見つかっても無視。
				if (result > 0) {
					for (int wordId : dictionary.lookupWordIds(result)) {
						String[] features = dictionary.getAllFeaturesArray(wordId);
						if (katakana.equals(features[features.length - 1]))
							break k;
					}
				}
				for (int wordId : unkDictionary.lookupWordIds(characterDefinition.lookup(katakana.charAt(0)))) {
					ViterbiNode node = new ViterbiNode(wordId, katakana, unkDictionary.getLeftId(wordId),
							unkDictionary.getRightId(wordId), unkAdjuster.getWordCost(katakana, katakana,
									unkDictionary, wordId), startIndex, Type.UNKNOWN);
					addToArrays(node, startIndex + 1, katakanaIndex + 1, startIndexArr, endIndexArr, startSizeArr,
							endSizeArr);
				}
			}
			// 全体を登録するパターン ここまで
			/*
			//部分文字列全部登録するパターン
			katakanaIndex = startIndex;
			w: while (katakanaIndex < textLength)
				if (KanaConverter.isKatakana(text.charAt(katakanaIndex))) {
					katakanaIndex++;
					String katakana = text.substring(startIndex, katakanaIndex);
					result = trie.lookup(katakana);

					// 辞書に一致する単語があったら登録しない。読み検索用の単語なら見つかっても無視。
					if (result > 0) {
						for (int wordId : dictionary.lookupWordIds(result)) {
							String[] features = dictionary.getAllFeaturesArray(wordId);
							if (katakana.equals(features[features.length - 1]))
								continue w;
						}
					}
					for (int wordId : unkDictionary.lookupWordIds(characterDefinition.lookup(katakana.charAt(0)))) {
						ViterbiNode node = new ViterbiNode(wordId, katakana, unkDictionary.getLeftId(wordId),
								unkDictionary.getRightId(wordId), unkAdjuster.getWordCost(katakana, katakana,
										unkDictionary, wordId), startIndex, Type.UNKNOWN);
						addToArrays(node, startIndex + 1, katakanaIndex + 1, startIndexArr, endIndexArr, startSizeArr,
								endSizeArr);
					}
				} else
					break;
			//部分文字列全部登録するパターン ここまで
			 */

			// Kuromoji版未知語処理。コスト以外は完全にViterbiのコピペ

			// TODO Kuromoji版未知語処理を常に使うべきか決める
			// if (true) {
			if (!mode.analysesColloquial) {

				if (unknownWordEndIndex > startIndex) {
					continue;
				}
				// Process Unknown Word
				int unknownWordLength = 0;
				String suffix = text.substring(startIndex);
				char firstCharacter = suffix.charAt(0);
				boolean isInvoke = characterDefinition.isInvoke(firstCharacter);
				if (isInvoke) { // Process "invoke"
					unknownWordLength = unkDictionary.lookup(suffix);
				} else if (alreadyFound == false) { // Process not "invoke"
					unknownWordLength = unkDictionary.lookup(suffix);
				}

				if (unknownWordLength > 0) { // found unknown word
					String unkWord = suffix.substring(0, unknownWordLength);
					int characterId = characterDefinition.lookup(firstCharacter);
					int[] wordIds = unkDictionary.lookupWordIds(characterId);

					for (int wordId : wordIds) {
						if (unknownFixMode)
							for (int i = 1; i <= unknownWordLength; i++) {
								ViterbiNode node = new ViterbiNode(wordId, unkWord.substring(0, i),
										unkDictionary.getLeftId(wordId), unkDictionary.getRightId(wordId),
										unkDictionary.getWordCost(wordId), startIndex, Type.UNKNOWN);
								addToArrays(node, startIndex + 1, startIndex + 1 + i, startIndexArr, endIndexArr,
										startSizeArr, endSizeArr);
							}
						else {
							ViterbiNode node = new ViterbiNode(wordId, unkWord, unkDictionary.getLeftId(wordId),
									unkDictionary.getRightId(wordId), unkDictionary.getWordCost(wordId), startIndex,
									Type.UNKNOWN);
							addToArrays(node, startIndex + 1, startIndex + 1 + unknownWordLength, startIndexArr,
									endIndexArr, startSizeArr, endSizeArr);
						}
					}
					unknownWordEndIndex = startIndex + unknownWordLength;
				}
			}
		}
		ViterbiNode eosNode = new ViterbiNode(-1, EOS, 0, 0, 0, textLength + 1, Type.KNOWN);
		addToArrays(eosNode, textLength + 1, 0, startIndexArr, endIndexArr, startSizeArr, endSizeArr);

		return new ViterbiNode[][][] { startIndexArr, endIndexArr };
	}

	@Override
	protected void addToArrays(ViterbiNode node, int startIndex, int endIndex, ViterbiNode[][] startIndexArr,
			ViterbiNode[][] endIndexArr, int[] startSizeArr, int[] endSizeArr) {
		/*
				String[] features = { "?" };
				try {
					switch (node.getType()) {
					case KNOWN:
						features = dictionary.getAllFeaturesArray(node.getWordId());
					case UNKNOWN:
						features = unkDictionary.getAllFeaturesArray(node.getWordId());
					case USER:
						features = userDictionary.getAllFeaturesArray(node.getWordId());
					}
				} catch (Exception e) {
				}
				System.out.println(" * " + node.getSurfaceForm() + " <" + features[features.length - 1] + "> (" + startIndex
						+ ", " + endIndex + ") " + node.getWordCost());
		*/
		// ↑を有効にすると、単語候補が一覧される
		super.addToArrays(node, startIndex, endIndex, startIndexArr, endIndexArr, startSizeArr, endSizeArr);
	}

	/**
	 * MoCAモードの切り替えを行います。
	 *
	 * @param mode
	 */
	public void setMocaMode(MocaMode mode) {
		this.mode = mode;
	}
}
