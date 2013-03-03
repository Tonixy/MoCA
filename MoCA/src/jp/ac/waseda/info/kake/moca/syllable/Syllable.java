package jp.ac.waseda.info.kake.moca.syllable;

import java.util.ArrayList;

import jp.ac.waseda.info.kake.string.KanaConverter;

/**
 * 音節です。
 *
 * @author Tony
 *
 */
public class Syllable {

	private static final char NONE = ' ';

	private static final char[][] kana_set = {
			{ NONE, 'a', 'k', 's', 't', 'n', 'h', 'm', 'y', 'r', 'w', 'g', 'z', 'd', 'b', 'p', 'a', 'y', 'w', 't' },
			{ 'a', 'ア', 'カ', 'サ', 'タ', 'ナ', 'ハ', 'マ', 'ヤ', 'ラ', 'ワ', 'ガ', 'ザ', 'ダ', 'バ', 'パ', 'ァ', 'ャ', 'ヮ', 'ヵ', NONE },
			{ 'i', 'イ', 'キ', 'シ', 'チ', 'ニ', 'ヒ', 'ミ', NONE, 'リ', NONE, 'ギ', 'ジ', 'ヂ', 'ビ', 'ピ', 'ィ', NONE, NONE, NONE,
					NONE },
			{ 'u', 'ウ', 'ク', 'ス', 'ツ', 'ヌ', 'フ', 'ム', 'ユ', 'ル', NONE, 'グ', 'ズ', 'ヅ', 'ブ', 'プ', 'ゥ', 'ュ', NONE, NONE,
					NONE },
			{ 'e', 'エ', 'ケ', 'セ', 'テ', 'ネ', 'ヘ', 'メ', NONE, 'レ', NONE, 'ゲ', 'ゼ', 'デ', 'ベ', 'ペ', 'ェ', NONE, NONE, 'ヶ',
					NONE },
			{ 'o', 'オ', 'コ', 'ソ', 'ト', 'ノ', 'ホ', 'モ', 'ヨ', 'ロ', 'ヲ', 'ゴ', 'ゾ', 'ド', 'ボ', 'ポ', 'ォ', 'ョ', NONE, NONE,
					NONE },
			{ 'n', NONE, NONE, NONE, NONE, 'ン', NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
					NONE, NONE, 'ッ' } };

	private static final char[] long_set = { 'ー', '～' };
	private static final char[] nasal_set = { 'ン', 'ッ' };

	private static final int KANA_DAN_CONSONANT = 0;// 子音段の位置
	private static final int KANA_DAN_A = 1;// あ段の位置
	private static final int KANA_DAN_O = 5;// お段の位置

	private static final int KANA_GYO_VOWEL = 0;// 母音行の位置
	private static final int KANA_GYO_A = 1;// あ行の位置
	private static final int KANA_GYO_XA = 16;// ぁ行の位置
	// private static final int KANA_GYO_XWA = 18;// ゎ行の位置
	private static final int KANA_GYO_XKA = 19;// ヵ行の位置。大文字変換する小書き文字の最後の行

	// 小書き文字の行と対応する通常文字の行との位置関係。
	// kana_set[i][kogaki[n - KANA_GYO_XA]]に、kana_set[i][n]に対応する通常文字がある
	private static final int[] kogaki = { 1, 8, 10, 3 };

	public static final char UNREADABLE = '\0';

	/**
	 * 通常の音節
	 */
	public static final byte KANA = 0;
	/**
	 * 母音で始まる音節
	 */
	public static final byte VOWEL = 1;
	/**
	 * 漢字音節
	 */
	public static final byte KANJI = 2;
	/**
	 * 長音記号で始まる音節。文頭や記号、撥音の直後に起こりうる
	 */
	public static final byte LONG = 3;
	/**
	 * 撥音や促音で始まる音節。文頭や記号、長音の直後に起こりうる
	 */
	public static final byte NASAL = 4;
	/**
	 * その他の記号等
	 */
	public static final byte SYMBOL = 5;

	/**
	 * 音節の種類
	 */
	public final byte type;
	/**
	 * 音節の長さ
	 */
	public final int length;
	/**
	 * 長音を含む音節かどうか
	 */
	private boolean endsWithLong = false;
	/**
	 * 撥促音を含む音節かどうか。endsWithLongと同時にtrueにはならない
	 */
	private boolean endsWithNasal = false;
	/**
	 * 変換候補。0番には表層表現が入る
	 */
	private final String[] alternatives;

	/**
	 * 表層表現と開始位置から音節を生成します。
	 * <p>
	 * なお、このコンストラクタは不可視です。Syllableインスタンスは、createSyllablesを呼び出して生成してください。
	 * </p>
	 *
	 * @param surfaceForm
	 * @param start
	 */
	private Syllable(String surfaceForm) {
		type = judgeType(surfaceForm);
		length = surfaceForm.length();
		alternatives = createAlternatives(this, surfaceForm, type);
	}

	/**
	 * 表層表現と種類から変換候補を生成します。返される配列の0番目には表層表現がそのまま入ります。
	 * <p>
	 * ルール:<br>
	 * 1．表層表現は必ず候補に入る<br>
	 * 2．先頭の1文字は必ず候補に入る<br>
	 * 3．音節種類がLONGやNASALの場合、長さ0の文字列が候補に入る<br>
	 * 4．音節種類がLONGでもNASALでもない場合、先頭から2文字目までの部分文字列が候補に入る<br>
	 * 5．音節種類がKANAの場合、先頭の1文字に対応する文字1文字を加えたものが候補に入る。<br>
	 * 5．1．先頭文字の母音がaの場合、対応する文字は「あ」。<br>
	 * 5．2．先頭文字の母音がeの場合、対応する文字は「え」と「い」。<br>
	 * 5．3．先頭文字の母音がiの場合、対応する文字は「い」。<br>
	 * 5．4．先頭文字の母音がoの場合、対応する文字は「お」と「う」。<br>
	 * 5．5．先頭文字の母音がuの場合、対応する文字は「う」。<br>
	 * 5．6．先頭文字がカタカナの場合、対応する文字は全てカタカナに変換される。<br>
	 * </p>
	 *
	 * @param syllable
	 * @param surfaceForm
	 * @param type
	 *            音節の種類
	 * @return
	 */
	private static String[] createAlternatives(Syllable syllable, String surfaceForm, byte type) {
		ArrayList<String> res = new ArrayList<String>();
		res.add(surfaceForm);

		String temp = surfaceForm.substring(0, 1);
		if (!res.contains(temp))
			res.add(temp);

		switch (type) {
		case LONG:
			syllable.endsWithLong = true;
			if (!res.contains(temp = "ー")) // 長音記号で始まる音節の場合、"ー"は必ず入れておく
				res.add(temp);
		case NASAL:
			syllable.endsWithNasal = !syllable.endsWithLong;
			// if (!res.contains(temp = "")) // 長音記号や促音撥音で始まる音節の場合、空文字列を入れておく
			// res.add(temp);
			break;
		default:
			if (surfaceForm.length() == 1)
				break;

			if (!res.contains(temp = surfaceForm.substring(0, 2)))
				res.add(temp); // TODO この2行要る?

			if (!isNasal(surfaceForm.charAt(1))) {
				syllable.endsWithLong = true;
				ArrayList<String> vowels = new ArrayList<String>();
				vowels.add("ー");
				char first = surfaceForm.charAt(0);
				switch (getVowel(first)) {
				case 'a':
					if (KanaConverter.isKatakana(first)) // ここでfirstが長音記号になることはありえない
						vowels.add("ア");
					else
						vowels.add("あ");
					break;
				case 'e':
					if (KanaConverter.isKatakana(first))
						vowels.add("エ");
					else
						vowels.add("え");
					if (getVowel(surfaceForm.charAt(1)) == 'e' || getVowel(surfaceForm.charAt(1)) == UNREADABLE) {
						char altFirst = getKana(getConsonant(first), 'o');
						if (altFirst != NONE) {
							String alt = new String(new char[] { altFirst, 'イ' });
							if (KanaConverter.isHiragana(first))
								alt = KanaConverter.getHiragana(alt);
							if (!res.contains(alt))
								res.add(alt);
						}
						altFirst = getKana(getConsonant(first), 'a');
						if (altFirst != NONE) {
							String alt = new String(new char[] { altFirst, 'イ' });
							if (KanaConverter.isHiragana(first))
								alt = KanaConverter.getHiragana(alt);
							if (!res.contains(alt))
								res.add(alt);
						}
					}
					// ここはbreakしない。「えい」はありうるが「いえ」は不自然
				case 'i':
					if (KanaConverter.isKatakana(first))
						vowels.add("イ");
					else
						vowels.add("い");
					if (getVowel(first) == 'i'
							&& (getVowel(surfaceForm.charAt(1)) == 'i' || getVowel(surfaceForm.charAt(1)) == UNREADABLE)) {
						char altFirst = getKana(getConsonant(first), 'u');
						if (altFirst != NONE) {
							String alt = new String(new char[] { altFirst, 'イ' });
							if (KanaConverter.isHiragana(first))
								alt = KanaConverter.getHiragana(alt);
							if (!res.contains(alt))
								res.add(alt);
						}
					}
					break;
				case 'o':
					if (KanaConverter.isKatakana(first))
						vowels.add("オ");
					else
						vowels.add("お");
					// ここはbreakしない
				case 'u':
					if (KanaConverter.isKatakana(first))
						vowels.add("ウ");
					else
						vowels.add("う");
					break;
				}
				for (String vi : vowels)
					if (!res.contains(temp = surfaceForm.substring(0, 1) + vi))
						res.add(temp);
				// TODO 「ぢ」「づ」「じ」「ず」に対応
			} else {
				syllable.endsWithNasal = true;
			}
		}
		// 先頭にある小書き文字の変換
		if (type == KANA || type == VOWEL) {
			char first = surfaceForm.charAt(0);
			for (int i = KANA_DAN_A; i <= KANA_DAN_O; i++) {
				for (int j = KANA_GYO_XA; j <= KANA_GYO_XKA; j++) {
					if (kana_set[i][j] == NONE)
						continue;
					if (kana_set[i][j] == first) {
						first = kana_set[i][kogaki[j - KANA_GYO_XA]];
						for (String alt : res.toArray(new String[0]))
							if (alt.length() > 0)
								res.add(first + alt.substring(1));
					}
					if (KanaConverter.getHiragana(kana_set[i][j]) == first) {
						first = KanaConverter.getHiragana(kana_set[i][kogaki[j - KANA_GYO_XA]]);
						for (String alt : res.toArray(new String[0]))
							if (alt.length() > 0)
								res.add(first + alt.substring(1));
					}
				}
			}
		}
		return res.toArray(new String[0]);
	}

	/**
	 * 表層表現から音節の種類を判定します。
	 *
	 * 音節の種類は先頭の文字にのみ依存します。
	 *
	 * @param surfaceForm
	 * @return
	 */
	private static byte judgeType(String surfaceForm) {
		String first = surfaceForm.substring(0, 1);
		if (isVowel(first))
			return VOWEL;
		if (isLong(first))
			return LONG;
		if (isNasal(first))
			return NASAL;
		if (KanaConverter.isKanji(first))
			return KANJI;
		if (KanaConverter.isKana(first))
			return KANA;
		if (first.equals("ヵ"))
			return KANA;
		if (first.equals("ヶ"))
			return KANA;
		return SYMBOL;
	}

	/**
	 * 文を音節のルールに従って区切り、文字列配列として返します。
	 * <p>
	 * ルール: <br>
	 * 1．先頭が撥音以外の仮名の場合、その文字の母音と同じ母音あるいは長音記号が続く限り、1つの文節として扱われる。母音の後に長音記号が現れてもよいが、
	 * 長音記号の後に母音が続く場合は1つの音節としては扱われない
	 * 。また、母音がエからイに変わった場合や、オからウに変わった場合は、1つの音節として扱われる<br>
	 * 2．先頭が仮名でなく、かつ撥音でもない文字の場合、長音記号が続く限り、1つの文節として扱われる<br>
	 * 3．先頭が長音記号以外の文字の場合、撥音が続く限り、1つの文節として扱われる
	 * </p>
	 * <p>
	 * つまり、「母音連続および長音」と「撥音」は、1つの文節内に現れることはありません。
	 * </p>
	 *
	 * @param sentence
	 * @return ルールに従って区切られた文字列配列
	 */
	public static String[] getSyllableStrings(String sentence) {
		ArrayList<String> res = new ArrayList<String>();
		int l = 0; // 音節左端の位置
		int r = 1; // 音節右端の位置。最終的に、添字l以上r未満を音節として抽出
		char vl; // 音節の母音。wh内で編集されるので注意!
		char r_temp; // 音節右側の文字置場
		boolean vowelFlag; // これがtrueになると、撥音が追加できなくなる
		boolean longFlag; // これがtrueになると、母音や撥音が追加できなくなる
		boolean nasalFlag; // これがtrueになると、長音や母音が追加できなくなる
		final int len = sentence.length();
		while (l < len) {
			vl = sentence.charAt(l);

			vowelFlag = false;// 母音フラグは、母音が連続して初めて立つ
			longFlag = isLong(vl);
			nasalFlag = isNasal(vl);
			if (judgeType(new String(new char[] { vl })) != SYMBOL) {// 記号の後にはハイフン含め何も続かない

				vl = getVowel(vl);
				wh: while (r < len) {
					r_temp = sentence.charAt(r);
					if (isVowel(r_temp) && !longFlag && !nasalFlag) {
						// 母音が続き、かつ長音フラグや撥音フラグが立ってない場合、母音の種類が合うなら、音節に含め母音フラグを立てる
						switch (getVowel(r_temp)) {
						case 'a':
							if (vl == 'a')
								break;
							break wh;
						case 'e':
							if (vl == 'e')
								break;
							break wh;
						case 'i':
							if (vl == 'e' || vl == 'i') {
								vl = 'i';// 再び「え」に戻ることができなくなる
								break;
							}
							break wh;
						case 'o':
							if (vl == 'o')
								break;
							break wh;
						case 'u':
							if (vl == 'o' || vl == 'u') {
								vl = 'u';// 再び「お」に戻ることができなくなる
								break;
							}
							break wh;
						default:
							break wh;
						}
						r++;
						vowelFlag = true;
					} else if (isLong(r_temp) && !nasalFlag) {
						// 長音記号が続き、かつ撥音フラグが立ってない場合、音節に含めて長音フラグを立てる
						r++;
						longFlag = true;
					} else if (isNasal(r_temp) && !vowelFlag && !longFlag) {
						// 撥音が続き、かつ母音フラグや長音フラグが立ってない場合、音節に含めて撥音フラグを立てる
						r++;
						nasalFlag = true;
					} else
						break;
				}
			}
			res.add(sentence.substring(l, r));
			l = r;
			r++;
		}
		return res.toArray(new String[0]);
	}

	/**
	 * 文から音節の配列を生成します。
	 *
	 * @param sentence
	 * @return
	 */
	public static Syllable[] createSyllables(String sentence) {
		String[] sylStrings = getSyllableStrings(sentence);
		Syllable[] res = new Syllable[sylStrings.length];
		for (int i = 0; i < res.length; i++)
			res[i] = new Syllable(sylStrings[i]);
		return res;
	}

	/**
	 * この音節の表層表現のうち、先頭の文字の母音を返します。
	 *
	 * @return 'a', 'i', 'u', 'e', 'o', UNREADABLE のいずれか
	 */
	public char getVowel() {
		return getVowel(alternatives[0].charAt(0));
	}

	/**
	 * この音節が長音を含むか否かを返します。
	 *
	 * @return
	 */
	public boolean endsWithLong() {
		return endsWithLong;
	}

	/**
	 * この音節が撥音や促音を含むか否かを返します。
	 *
	 * @return
	 */
	public boolean endsWithNasal() {
		return endsWithNasal;
	}

	/**
	 * 文字の母音を返します。
	 *
	 * @param reading
	 * @return 'a', 'i', 'u', 'e', 'o', UNREADABLE のいずれか
	 */
	public static char getVowel(char reading) {
		char last = KanaConverter.getKatakana(reading);
		if (!KanaConverter.isKana(last))
			return UNREADABLE;
		for (int i = KANA_DAN_A; i <= KANA_DAN_O; i++)
			for (char cj : kana_set[i])
				if (last == cj)
					return kana_set[i][KANA_GYO_VOWEL];
		return UNREADABLE;
	}

	/**
	 * 文字の子音を返します。
	 *
	 * @param reading
	 * @return 'a', 'k', 's', 't', 'n', 'h', 'm', 'y', 'r', 'w', 'g', 'z', 'd',
	 *         'b', 'p', UNREADABLE のいずれか
	 */
	public static char getConsonant(char reading) {
		char last = KanaConverter.getKatakana(reading);
		if (!KanaConverter.isKana(last))
			return UNREADABLE;
		for (int i = KANA_DAN_A; i <= KANA_DAN_O; i++)
			for (int j = KANA_GYO_A; j <= KANA_GYO_XKA; j++)
				if (last == kana_set[i][j])
					return kana_set[KANA_DAN_CONSONANT][j];
		return UNREADABLE;
	}

	/**
	 * 与えられた文字列が全て母音であるときtrueを返します。
	 *
	 * 母音とは、「あ」行あるいは「ぁ」行の、ひらがなまたはカタカナを指します。
	 *
	 * @param reading
	 * @return
	 */
	public static boolean isVowel(String reading) {
		for (char ri : reading.toCharArray())
			if (!isVowel(ri))
				return false;
		return true;
	}

	/**
	 * 与えられた文字が母音であるか否かを返します。
	 *
	 * 母音とは、「あ」行あるいは「ぁ」行の、ひらがなまたはカタカナを指します。
	 *
	 * @param reading
	 * @return
	 */
	public static boolean isVowel(char reading) {
		reading = KanaConverter.getKatakana(reading);
		for (int j = KANA_DAN_A; j <= KANA_DAN_O; j++)
			if (reading == kana_set[j][KANA_GYO_A] || reading == kana_set[j][KANA_GYO_XA])
				return true;
		return false;
	}

	/**
	 * 与えられた文字列が全て長音記号であるときtrueを返します。
	 *
	 * @param string
	 * @return
	 */
	public static boolean isLong(String string) {
		for (char si : string.toCharArray())
			if (!isLong(si))
				return false;
		return true;
	}

	/**
	 * 与えられた文字が長音記号であるときtrueを返します。
	 *
	 * @param ch
	 * @return
	 */
	public static boolean isLong(char ch) {
		for (char li : long_set)
			if (ch == li)
				return true;
		return false;
	}

	/**
	 * 与えられた文字列が全て撥音であるときtrueを返します。
	 *
	 * @param string
	 * @return
	 */
	public static boolean isNasal(String string) {
		for (char si : string.toCharArray())
			if (!isNasal(si))
				return false;
		return true;
	}

	/**
	 * 与えられた文字が撥音であるときtrueを返します。
	 *
	 * @param ch
	 * @return
	 */
	public static boolean isNasal(char ch) {
		for (char li : nasal_set)
			if (KanaConverter.getKatakana(ch) == li)
				return true;
		return false;
	}

	/**
	 * 変換候補それぞれの部分文字列を、文字列配列として返します。beginIndexが範囲外となる候補は無視されます。
	 * ただし、変換候補に長さ0の文字列がある場合(長音記号や促音撥音で始まる音節の場合)、結果に長さ0の文字列が含まれます。
	 * endIndexが範囲外となる候補がある場合、長さがendIndex-beginIndex未満となる文字列が結果に含まれます。
	 *
	 * なお、返される文字列配列に同一のものが複数含まれることはありません。 また、結果の先頭には、表層表現の部分文字列が入ります。
	 *
	 * @param beginIndex
	 * @param endIndex
	 * @return
	 * @throws IndexOutOfBoundsException
	 *             beginIndexやendIndexが、0未満または(音節の)length以上の場合
	 */
	public String[] substrings(int beginIndex, int endIndex) throws IndexOutOfBoundsException {
		ArrayList<String> res = new ArrayList<String>();
		if (beginIndex < 0 || beginIndex > length)
			throw new IndexOutOfBoundsException("beginIndex: " + beginIndex);
		if (endIndex < 0 || endIndex > length)
			throw new IndexOutOfBoundsException("endIndex: " + endIndex);
		if (beginIndex > endIndex)
			throw new IndexOutOfBoundsException("beginIndex: " + beginIndex + ", endIndex: " + endIndex);

		int temp_end;
		int temp_len;
		String temp_sub;
		for (String ai : alternatives) {
			temp_len = ai.length();
			if (temp_len == 0) {
				if (!res.contains(ai))
					res.add(ai);
				continue;
			}
			if (beginIndex >= temp_len)
				continue;
			temp_end = endIndex;
			if (endIndex > temp_len)
				temp_end = temp_len;
			if (!res.contains(temp_sub = ai.substring(beginIndex, temp_end)))
				res.add(temp_sub);
		}
		return res.toArray(new String[0]);
	}

	/**
	 * 音節の変換候補を文字列配列で返します。
	 *
	 * @return
	 */
	public String[] getAlternatives() {
		return alternatives.clone();
	}

	/**
	 * 子音がconsonant、母音がvowelである文字を返します。
	 *
	 * 小書き文字は返されません。また該当文字がない場合、NONEが返されます。
	 *
	 * @param consonant
	 * @param vowel
	 * @return
	 */
	public static char getKana(char consonant, char vowel) {
		int i = KANA_DAN_A;
		for (; i <= KANA_DAN_O; i++)
			if (kana_set[i][KANA_GYO_VOWEL] == vowel)
				break;
		for (int j = KANA_GYO_A; j <= KANA_GYO_XKA; j++)
			if (kana_set[KANA_DAN_CONSONANT][j] == consonant)
				return kana_set[i][j];
		return NONE;
	}

	@Override
	public String toString() {
		StringBuffer res = new StringBuffer(alternatives[0]);
		res.append(" <");
		switch (type) {
		case KANA:
			res.append("子音");
			break;
		case VOWEL:
			res.append("母音");
			break;
		case KANJI:
			res.append("漢字");
			break;
		case LONG:
			res.append("長音");
			break;
		case NASAL:
			res.append("撥促");
			break;
		case SYMBOL:
			res.append("記号");
			break;
		default:
			res.append("*");
		}
		res.append(", ").append(length).append("> [ ");
		int i = 1;
		for (; i < alternatives.length; i++)
			res.append(alternatives[i].length() == 0 ? "*" : alternatives[i]).append(", ");
		if (i > 1)
			res.delete(res.length() - 2, res.length() - 1).append("]");
		else
			res.delete(res.length() - 3, res.length());
		return res.toString();
	}
}
