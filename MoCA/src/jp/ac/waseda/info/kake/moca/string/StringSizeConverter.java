package jp.ac.waseda.info.kake.moca.string;

/**
 * 文字列の全角・半角変換を行います。
 *
 * @author Tony
 *
 */
public abstract class StringSizeConverter {

	private static final char[][] alpSet = {
			{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',

			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
					'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
					'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',

					' ', '!', '"', '#', '$', '%', '&', '\'', '(', ')',

					'*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@', '\\', '^', '_', '`', '{', '|',
					'}', '~',

					'[', ']', '･' },

			{ '０', '１', '２', '３', '４', '５', '６', '７', '８', '９',

			'Ａ', 'Ｂ', 'Ｃ', 'Ｄ', 'Ｅ', 'Ｆ', 'Ｇ', 'Ｈ', 'Ｉ', 'Ｊ', 'Ｋ', 'Ｌ', 'Ｍ', 'Ｎ', 'Ｏ', 'Ｐ', 'Ｑ', 'Ｒ', 'Ｓ', 'Ｔ', 'Ｕ',
					'Ｖ', 'Ｗ', 'Ｘ', 'Ｙ', 'Ｚ', 'ａ', 'ｂ', 'ｃ', 'ｄ', 'ｅ', 'ｆ', 'ｇ', 'ｈ', 'ｉ', 'ｊ', 'ｋ', 'ｌ', 'ｍ', 'ｎ', 'ｏ',
					'ｐ', 'ｑ', 'ｒ', 'ｓ', 'ｔ', 'ｕ', 'ｖ', 'ｗ', 'ｘ', 'ｙ', 'ｚ',

					'　', '！', '”', '＃', '＄', '％', '＆', '’', '（', '）',

					'＊', '＋', '，', '‐', '．', '／', '：', '；', '＜', '＝', '＞', '？', '＠', '￥', '＾', '＿', '‘', '｛', '｜', '｝',
					'～',

					'「', '」', '・' } };

	/**
	 * 与えた文字列に含まれる半角文字を、全て全角文字に変換して返します。
	 *
	 * @param half
	 * @return
	 */
	public static String getFullString(String half) {
		return convert(half, 0, 1);
	}

	/**
	 * 与えた文字列に含まれる全角文字を、全て半角文字に変換して返します。
	 *
	 * @param full
	 * @return
	 */
	public static String getHalfString(String full) {
		return convert(full, 1, 0);
	}

	/**
	 * 与えられた文字を、半角から全角へ、あるいは全角から半角へ変換します。
	 *
	 * @param from
	 * @param hf1
	 *            0なら半角から、1なら全角から変換
	 * @param hf2
	 *            0なら半角へ、1なら全角へ変換
	 * @return
	 */
	private static char convertChar(char from, int hf1, int hf2) {
		for (int i = 0; i < alpSet[0].length; i++)
			if (alpSet[hf1][i] == from)
				return alpSet[hf2][i];

		return from;
	}

	/**
	 * 与えられた文字列を、半角から全角へ、あるいは全角から半角へ変換します。
	 *
	 * @param from
	 * @param hf1
	 *            0なら半角から、1なら全角から変換
	 * @param hf2
	 *            0なら半角へ、1なら全角へ変換
	 * @return
	 */
	private static String convert(String from, int hf1, int hf2) {
		char[] res = from.toCharArray();
		for (int i = 0; i < res.length; i++)
			res[i] = convertChar(res[i], hf1, hf2);
		return new String(res);
	}
}
