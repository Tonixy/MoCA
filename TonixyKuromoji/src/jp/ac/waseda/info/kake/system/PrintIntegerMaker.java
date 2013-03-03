package jp.ac.waseda.info.kake.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 整数を出力するとき、右揃えで出力するためにスペースを加えた文字列を生成します。
 *
 * @author Sho
 *
 */
public class PrintIntegerMaker {

	public final int length;

	/**
	 * 表示しうる最大値を指定してPrintIntegerMakerを生成します。
	 *
	 * @param max
	 */
	public PrintIntegerMaker(int max) {
		length = getLength(max);
	}

	/**
	 * 数値にスペースを加えた文字列を生成します。
	 *
	 * @param num
	 * @return
	 */
	public String getPrintInteger(int num) {
		StringBuffer res = new StringBuffer(Integer.toString(num));
		while (res.length() < length)
			res.insert(0, " ");
		return res.toString();
	}

	/**
	 * 指定した数値を表示する際の文字列幅を返します。数値の正負にかかわらず、-記号の分の幅が確保されます。
	 *
	 * @param max
	 * @return
	 */
	public static int getLength(int max) {
		int res = 1;
		while (max != 0) {
			max /= 10;
			res++;
		}
		if(res <= 1)
			return 2;
		return res;
	}

	public static void main(String[] args) throws IOException {
		System.out.println("数値を入力してください(空行入力で終了)");
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		String line;
		while ((line = reader.readLine()) != null && line.length() > 0) {
			System.out.println(getLength(Integer.parseInt(line)));
		}
	}

}
