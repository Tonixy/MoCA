package jp.ac.waseda.info.kake.moca.string;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 文字列内の全角カタカナ・ひらがな変換
 *
 * @author Tony
 *
 */
public abstract class KanaConverter {

	// 参考: http://ja.wikipedia.org/wiki/Unicode%E4%B8%80%E8%A6%A7_3000-3FFF

	private static final char start = 'ぁ';
	private static final char end = 'ん';
	private static final char dist = 'ァ' - 'ぁ';

	/**
	 * 特別に仮名として扱う文字
	 */
	private static final char[] kana = { 'ー' };
	/**
	 * 特別にカタカナとして扱う文字
	 */
	private static final char[] katakana = { 'ヴ', 'ヵ', 'ヶ' };
	/**
	 * 特別にひらがなとして扱う文字
	 */
	private static final char[] hiragana = {};

	// TODO ヴ は変換には対応してない。Unicodeには ゔ がある。どうする?文字コード設定をつける?

	/**
	 * 与えた文字列に含まれるひらがなを、全て全角カタカナに変換して返します。
	 *
	 * @param str
	 * @return
	 */
	public static String getKatakana(String str) {
		return convert(str, (char) 0, dist);
	}

	/**
	 * 与えた文字がひらがなであれば、全角カタカナに変換して返します。そうでなければ、そのまま返します。
	 *
	 * @param ch
	 * @return
	 */
	public static char getKatakana(char ch) {
		return convertChar(ch, (char) 0, dist);
	}

	/**
	 * 与えた文字列に含まれる全角カタカナを、全てひらがなに変換して返します。
	 *
	 * @param str
	 * @return
	 */
	public static String getHiragana(String str) {
		return convert(str, dist, -dist);
	}

	/**
	 * 与えた文字が全角カタカナであれば、ひらがなに変換して返します。そうでなければ、そのまま返します。
	 *
	 * @param ch
	 * @return
	 */
	public static char getHiragana(char ch) {
		return convertChar(ch, dist, -dist);
	}

	/**
	 * 与えられた文字を、半角から全角へ、あるいは全角から半角へ変換します。
	 *
	 * @param from
	 * @param pos
	 *            ひらがな→カタカナなら0、逆ならdistを指定
	 * @param plus
	 *            ひらがな→カタカナならdist、逆なら-distを指定
	 * @return
	 */
	private static char convertChar(char from, char pos, int plus) {
		if (from >= start + pos && from <= end + pos)
			return (char) (from + plus);
		return from;
	}

	/**
	 * 与えられた文字列を、半角から全角へ、あるいは全角から半角へ変換します。
	 *
	 * @param from
	 * @param pos
	 *            ひらがな→カタカナなら0、逆ならdistを指定
	 * @param plus
	 *            ひらがな→カタカナならdist、逆なら-distを指定
	 * @return
	 */
	private static String convert(String from, char pos, int plus) {
		if (from == null)
			return null;
		char[] res = from.toCharArray();
		for (int i = 0; i < res.length; i++)
			res[i] = convertChar(res[i], pos, plus);
		return new String(res);
	}

	/**
	 * 文字列内の文字全てがひらがなか特別仮名である場合のみtrueを返します。
	 *
	 * @param string
	 * @return
	 */
	public static boolean isHiragana(String string) {
		for (char ch : string.toCharArray())
			if (!isHiragana(ch))
				return false;
		return true;
	}

	/**
	 * 文字列がひらがなか特別仮名を含む場合にtrueを返します。
	 *
	 * @param string
	 * @return
	 */
	public static boolean containsHiragana(String string) {
		for (char ch : string.toCharArray())
			if (isHiragana(ch))
				return true;
		return false;
	}

	/**
	 * 文字がひらがなか特別仮名である場合のみtrueを返します。
	 *
	 * @param ch
	 * @return
	 */
	public static boolean isHiragana(char ch) {
		for (char k : kana)
			if (k == ch)
				return true;
		for (char k : hiragana)
			if (k == ch)
				return true;
		return start <= ch && ch <= end;
	}

	/**
	 * 文字列内の文字全てがカタカナか特別仮名である場合のみtrueを返します。
	 *
	 * @param string
	 * @return
	 */
	public static boolean isKatakana(String string) {
		for (char ch : string.toCharArray())
			if (!isKatakana(ch))
				return false;
		return true;
	}

	/**
	 * 文字列がカタカナか特別仮名を含む場合にtrueを返します。
	 *
	 * @param string
	 * @return
	 */
	public static boolean containsKatakana(String string) {
		for (char ch : string.toCharArray())
			if (isKatakana(ch))
				return true;
		return false;
	}

	/**
	 * 文字がカタカナか特別仮名である場合のみtrueを返します。
	 *
	 * @param ch
	 * @return
	 */
	public static boolean isKatakana(char ch) {
		for (char k : kana)
			if (k == ch)
				return true;
		for (char k : katakana)
			if (k == ch)
				return true;
		return start + dist <= ch && ch <= end + dist;
	}

	/**
	 * 文字が特別仮名(ひらがなでもカタカナでもある文字)である場合のみtrueを返します。
	 *
	 * @param ch
	 * @return
	 */
	public static boolean isSpecialCharacter(char ch) {
		for (char sp : kana)
			if (ch == sp)
				return true;
		return false;
	}

	/**
	 * 文字列が特別仮名(ひらがなでもカタカナでもある文字)を含む場合にtrueを返します。
	 *
	 * @param string
	 * @return
	 */
	public static boolean containsSpecialCharacter(String string) {
		for (char ch : string.toCharArray())
			if (isSpecialCharacter(ch))
				return true;
		return false;
	}

	/**
	 * 文字列内の文字全てがひらがなまたはカタカナであればtrueを返します。
	 *
	 * @param string
	 * @return
	 */
	public static boolean isKana(String string) {
		for (char ch : string.toCharArray())
			if (!isKana(ch))
				return false;
		return true;
	}

	/**
	 * 指定した文字がひらがなまたはカタカナであればtrueを返します。
	 *
	 * @param ch
	 * @return
	 */
	public static boolean isKana(char ch) {
		return isHiragana(ch) || isKatakana(ch);
	}

	/**
	 * 文字列内の文字全てが漢字であればtrueを返します。
	 *
	 * @param string
	 * @return
	 */
	public static boolean isKanji(String string) {
		return string.matches("^[一-龠]*$");
	}

	/**
	 * 文字列内に漢字が含まれればtrueを返します。
	 *
	 * @param string
	 * @return
	 */
	public static boolean containsKanji(String string) {
		return string.matches(".*[一-龠].*");
	}

	/**
	 * 特別仮名をすべて含んだ配列を返します。
	 *
	 * @return
	 */
	public static char[] getSpecialCharacters() {
		return kana.clone();
	}

	public static void main(String[] args) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(System.in));
			String input;
			while (true) {
				System.err.println("変換したい文を入力してください。(空行入力で終了)");
				input = reader.readLine();
				if (input.length() <= 0)
					break;
				System.out.println("ひらがな: " + getHiragana(input));
				System.out.println("カタカナ: " + getKatakana(input));
				System.out.println(containsKanji(input));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
}
