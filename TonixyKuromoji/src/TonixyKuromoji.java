import java.io.IOException;
import java.util.List;

import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;
import org.atilika.kuromoji.Tokenizer.Mode;

import jp.ac.waseda.info.kake.system.InputMain;
import jp.ac.waseda.info.kake.system.PrintIntegerMaker;

public class TonixyKuromoji {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new InputMain(args, "kuromoji 準備中...", "準備完了。文を入力してください") {

			Tokenizer tokenizer;

			@Override
			public void prepare() {
				if (args.length == 1) {
					Mode mode = Mode.valueOf(args[0].toUpperCase());
					tokenizer = Tokenizer.builder().unknownFixMode(true).mode(mode).build();
				} else if (args.length == 2) {
					Mode mode = Mode.valueOf(args[0].toUpperCase());
					try {
						tokenizer = Tokenizer.builder().unknownFixMode(true).mode(mode).userDictionary(args[1]).build();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					tokenizer = Tokenizer.builder().unknownFixMode(true).build();
					/*
					 * try { String userDict = "（＾＾）,（＾＾）,（＾＾）,顔文字\n" +
					 * "日本経済新聞,日本 経済 新聞,ニホン ケイザイ シンブン,カスタム名詞\n" +
					 * "関西国際空港,関西 国際 空港,カンサイ コクサイ クウコウ,テスト名詞\n" +
					 * "朝青龍,朝青龍,アサショウリュウ,カスタム人名"; tokenizer =
					 * Tokenizer.builder().unknownFixMode(false
					 * ).userDictionary(new StringReader(userDict)).build(); }
					 * catch (IOException e) { e.printStackTrace(); }
					 */
				}
			};

			@Override
			public void run(String line) {
				printTokens(tokenizer.tokenize(line));
			}

			/**
			 * デモ用。形態素解析結果を出力します。
			 *
			 * @param line
			 */
			public void printTokens(List<Token> tokens) {
				PrintIntegerMaker pim = new PrintIntegerMaker(tokens.get(tokens.size() - 1).getPosition());
				for (Token token : tokens) {
					System.out.println(pim.getPrintInteger(token.getPosition()) + ": " + token.getSurfaceForm() + "\t"
							+ token.getAllFeatures());
				}
			}
		}.main();
	}
}
