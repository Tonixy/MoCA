package jp.ac.waseda.info.kake.moca.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * メインメソッドでよく行う、標準入力からの入力を受け取って処理する過程を繰り返す手順を簡単に実現します。事前準備をprepareメソッドに、
 * 入力を受け取るたびに行う処理をrunメソッドに記述し、mainメソッドを呼び出して使用します。
 *
 * @author Sho
 *
 */
public abstract class InputMain {

	protected final String[] args;
	protected final String messagePrepare;
	protected final String messageStart;

	/**
	 * コマンドライン引数と、準備中および準備完了のメッセージを指定し、InputMainを生成します。
	 *
	 * @param args
	 *            コマンドライン引数
	 * @param messagePrepare
	 *            準備中メッセージ
	 * @param messageStart
	 *            準備完了メッセージ
	 */
	public InputMain(String[] args, String messagePrepare, String messageStart) {
		this.args = args;
		this.messagePrepare = messagePrepare;
		this.messageStart = messageStart + "(空行入力で終了)";
	}

	/**
	 * コマンドライン引数と準備完了メッセージを指定し、InputMainを生成します。
	 *
	 * @param args
	 *            コマンドライン引数
	 * @param messageStart
	 *            準備完了メッセージ
	 */
	public InputMain(String[] args, String messageStart) {
		this(args, null, messageStart);
	}

	/**
	 * prepareメソッドを呼び出した後、標準入力からの入力を繰り返し求め、その入力を引数にしてrunメソッドを呼び出します。
	 * 空行が入力されるまで、処理は続行されます。
	 *
	 * @throws IOException
	 */
	public void main() throws IOException {
		if (messagePrepare != null)
			System.out.println(messagePrepare);
		prepare();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		String line;
		System.out.println(messageStart);
		while ((line = reader.readLine()) != null && line.length() > 0) {
			run(line);
		}
	}

	/**
	 * 必要であれば、準備完了メッセージを出す前に1度だけ行う処理を書いてください。
	 */
	public void prepare() {
	}

	/**
	 * 入力を受け取るたびに行う処理を書いてください。
	 *
	 * @param line
	 *            標準入力からの1行の入力
	 */
	public abstract void run(String line);
}
