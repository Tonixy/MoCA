package jp.ac.waseda.info.kake.moca.string;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import jp.ac.waseda.info.kake.moca.system.InputMain;

/**
 * 編集距離(レーベンシュタイン距離)を計算します。インスタンスは編集前の文字列を持ち、編集後の文字列を引数にgetDistanceを呼ぶことで計算できます。
 *
 * @author Tony
 *
 */
public class Levenshtein {

	private final String before;
	private final boolean ignoreSize;

	/**
	 * 追加/削除のコスト
	 */
	private static final int a_r = 1;

	/**
	 * 編集前文字列を与え、Levenshteinインスタンスを生成します。
	 *
	 * @param before
	 */
	public Levenshtein(String before) {
		this(before, false);
	}

	/**
	 * 編集前文字列と、半角/全角の違いを無視するか否かを設定し、Levenshteinインスタンスを生成します。
	 *
	 * @param before
	 * @param ignoreSize trueの場合、半角と全角の違いは無視され、コスト加算されなくなる
	 */
	public Levenshtein(String before, boolean ignoreSize) {
		this.before = ignoreSize ? StringSizeConverter.getFullString(before) : before;
		this.ignoreSize = ignoreSize;
	}

	/**
	 * 編集距離(レーベンシュタイン距離)を計算します。
	 *
	 * @param after
	 *            編集後文字列
	 * @return
	 */
	public int getDistance(String after) {
		if (ignoreSize)
			after = StringSizeConverter.getFullString(after);
		int[][] cost = new int[before.length() + 1][after.length() + 1];
		for (int i = 0; i < cost.length; i++)
			cost[i][0] = i;
		for (int i = 0; i < cost[0].length; i++)
			cost[0][i] = i;

		int eq;
		for (int i = 1; i < cost.length; i++)
			for (int j = 1; j < cost[i].length; j++) {
				eq = getCost(before.charAt(i - 1), after.charAt(j - 1));
				cost[i][j] = minimum(cost[i - 1][j - 1] + eq, cost[i][j - 1] + a_r, cost[i - 1][j] + a_r);
			}
		return cost[cost.length - 1][cost[0].length - 1];
	}

	/**
	 * aからbへ編集する際のコストを返します。
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	private int getCost(char a, char b) {
		if (a == b)
			return 0;
		return 1;
	}

	/**
	 * 3つの値の中で最小のものを返します。
	 *
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	private int minimum(int a, int b, int c) {
		if (a > b)
			a = b;
		if (a > c)
			a = c;
		return a;
	}

	/**
	 * とりうる編集距離(レーベンシュタイン距離)の最大値を返します。
	 *
	 * @param after
	 *            編集後文字列
	 * @return
	 */
	public int getMax(String after) {
		return -minimum(-before.length(), -after.length(), 0);
	}

	public static void main(String[] args) throws IOException {
		new InputMain(args, "レーベンシュタイン距離 計測テスト\n元の文字列を入力してください", "変形後の文字列を入力してください") {
			String before = "";
			Levenshtein lev = new Levenshtein(before, true);

			@Override
			public void prepare() {
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				try {
					before = reader.readLine();
					lev = new Levenshtein(before, true);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}

			@Override
			public void run(String line) {
				System.out.println(before + " -> " + line + ": " + lev.getDistance(line) + " / " + lev.getMax(line));
			}
		}.main();
	}
}
