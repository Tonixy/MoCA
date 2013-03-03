package jp.ac.waseda.info.kake.moca.syllable;

import java.io.IOException;
import java.util.ArrayList;

import jp.ac.waseda.info.kake.system.InputMain;
import jp.ac.waseda.info.kake.system.PrintIntegerMaker;

/**
 * 音節をまとめて管理します。
 *
 * @author Tony
 *
 */
public class SyllabifiedString {

	private final SyllablePart[] syllables;

	/**
	 * 文字列からSyllabifiedStringインスタンスを生成します。
	 *
	 * @param string
	 */
	public SyllabifiedString(String string) {
		Syllable[] syl_temp = Syllable.createSyllables(string);
		syllables = new SyllablePart[syl_temp.length];
		int start = 0;
		for (int i = 0; i < syllables.length; i++) {
			syllables[i] = new SyllablePart(syl_temp[i], start, this);
			start += syl_temp[i].length;
		}
	}

	@Override
	public String toString() {
		StringBuffer res = new StringBuffer();
		for (SyllablePart syl : syllables)
			res.append(syl).append("\n");
		return res.toString();
	}

	public int length() {
		return syllables[syllables.length - 1].end;
	}

	/**
	 * 変換候補それぞれの部分文字列を、文字列配列として返します。beginIndex文字目が音節の先頭でない場合、beginIndex文字目が属する音節は変換されません。
	 * beginIndexとendIndexが異なる場合、結果に長さ0の文字列が含まれることはありません。
	 *
	 * 結果の先頭には、表層表現の部分文字列が入ります。
	 *
	 * @param beginIndex
	 * @param endIndex
	 * @return 部分文字列を要素に持つ文字列配列。beginIndexとendIndexが等しい場合、要素数0の文字列配列
	 * @throws IndexOutOfBoundsException
	 */
	public String[] substrings(int beginIndex, int endIndex) throws IndexOutOfBoundsException {
		return substrings(beginIndex, endIndex, false);
	}

	/**
	 * 変換候補それぞれの部分文字列を、文字列配列として返します。beginIndex文字目が省略される候補は無視されます。
	 * また、fixesBeginIndexがfalseであり、かつbeginIndex文字目が音節の先頭でない場合、beginIndex文字目が属する音節は変換されません。
	 * beginIndexとendIndexが異なる場合、結果に長さ0の文字列が含まれることはありません。
	 *
	 * 結果の先頭には、表層表現の部分文字列が入ります。
	 *
	 * @param beginIndex
	 * @param endIndex
	 * @param fixesBeginIndex
	 * @return 部分文字列を要素に持つ文字列配列。beginIndexとendIndexが等しい場合、要素数0の文字列配列
	 * @throws IndexOutOfBoundsException
	 */
	public String[] substrings(int beginIndex, int endIndex, boolean fixesBeginIndex) throws IndexOutOfBoundsException {
		ArrayList<String> res = new ArrayList<String>();
		int len = length();
		if (beginIndex < 0 || beginIndex > len)
			throw new IndexOutOfBoundsException("beginIndex: " + beginIndex);
		if (endIndex < 0 || endIndex > len)
			throw new IndexOutOfBoundsException("endIndex: " + endIndex);
		if (beginIndex > endIndex)
			throw new IndexOutOfBoundsException("beginIndex: " + beginIndex + ", endIndex: " + endIndex);
		if (beginIndex == endIndex)
			return new String[0];

		int sylBegin = getSyllableNumber(beginIndex);
		int sylEnd = getSyllableNumber(endIndex - 1);
		ArrayList<String> temp;
		int sylSubBegin;
		int sylSubEnd;
		for (int i = sylBegin; i <= sylEnd; i++) {
			sylSubBegin = beginIndex > syllables[i].start ? beginIndex : syllables[i].start;
			sylSubEnd = endIndex < syllables[i].end ? endIndex : syllables[i].end;
			if (res.size() == 0) {
				if (fixesBeginIndex || beginIndex == syllables[i].start) {
					for (String si : syllables[i].substrings(sylSubBegin, sylSubEnd))
						if (si.length() > 0) // 空文字列が含まれるのを防ぐ
							res.add(si);
				} else {
					res.add(syllables[i].substrings(sylSubBegin, sylSubEnd)[0]);
				}
				continue;
			}
			temp = res;
			res = new ArrayList<String>();
			for (String ti : temp)
				for (String si : syllables[i].substrings(sylSubBegin, sylSubEnd))
					res.add(ti + si); // tempに空文字列はないので、ここでは特に防ぐ必要はない
			// TODO この仕様だと、長音記号や促音撥音が文字列先頭にある時に対応できない
		}
		return res.toArray(new String[0]);
	}

	/**
	 * charPos文字目( 0<=charPos<length() )を含む音節の、syllablesにおける添字を返します。
	 *
	 * @param charPos
	 * @return
	 * @throws IndexOutOfBoundsException
	 *             0<=charPos<length() を満たさなかった場合
	 */
	private int getSyllableNumber(int charPos) throws IndexOutOfBoundsException {
		if (charPos < 0)
			throw new IndexOutOfBoundsException("charPos: " + charPos);
		for (int i = 0; i < syllables.length; i++)
			if (charPos < syllables[i].end)
				return i;
		throw new IndexOutOfBoundsException("charPos: " + charPos);
	}

	/**
	 * charPos文字目( 0<=charPos<length() )を含む音節を返します。
	 *
	 * @param charPos
	 * @return
	 * @throws IndexOutOfBoundsException
	 *             0<=charPos<length() を満たさなかった場合
	 */
	public Syllable getSyllable(int charPos) throws IndexOutOfBoundsException {
		if (charPos < 0)
			throw new IndexOutOfBoundsException("charPos: " + charPos);
		for (int i = 0; i < syllables.length; i++)
			if (charPos < syllables[i].end)
				return syllables[i].syllable;
		throw new IndexOutOfBoundsException("charPos: " + charPos);
	}

	/**
	 * Syllableに開始/終了位置情報を追加したものです。SyllabifiedString内で使用します。
	 *
	 * @author Tony
	 *
	 */
	class SyllablePart {
		public final SyllabifiedString parent;
		public final Syllable syllable;
		public final int start;
		public final int end;

		public SyllablePart(Syllable syllable, int start, SyllabifiedString parent) {
			this.syllable = syllable;
			this.start = start;
			this.parent = parent;
			end = start + syllable.length;
		}

		@Override
		public String toString() {
			return new StringBuffer(
					new PrintIntegerMaker(parent.syllables[parent.syllables.length - 1].start).getPrintInteger(start))
					.append(": ").append(syllable).toString();
		}

		/**
		 * SyllabifiedString内での文字位置を利用し、変換候補それぞれの部分文字列を、文字列配列として返します。
		 *
		 * 結果の先頭には、表層表現の部分文字列が入ります。
		 *
		 * @param beginIndex
		 * @param endIndex
		 * @return
		 * @throws IndexOutOfBoundsException
		 */
		public String[] substrings(int beginIndex, int endIndex) throws IndexOutOfBoundsException {
			return syllable.substrings(beginIndex - start, endIndex - start);
		}
	}

	public static void main(String[] args) throws IOException {
		new InputMain(args, "文字列を入力してください") {
			@Override
			public void run(String line) {
				SyllabifiedString syl = new SyllabifiedString(line);
				System.out.println(syl);
				StringBuffer space = new StringBuffer();
				for (int i = 0; i < syl.length(); i++) {
					for (int j = i + 1; j <= syl.length(); j++) {
						String[] subs = syl.substrings(i, j);
						for (String si : subs) {
							System.out.println(space + si + " (" + i + ", " + j + ")");
						}
					}
					space.append("  ");
				}
				System.out.println();
			}
		}.main();
	}
}
