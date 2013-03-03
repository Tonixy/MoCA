package jp.ac.waseda.info.kake.moca;

import java.io.IOException;

import jp.ac.waseda.info.kake.moca.MocaTokenizer.MocaMode;

import jp.ac.waseda.info.kake.moca.system.InputMain;

/**
 * MoCA - Morphological Colloquial speech Analyzer
 *
 * @author Sho
 *
 */
public class MoCA {

	public static void main(String[] args) throws IOException {
		String[] copyrights = {
				"*****************************************************************",
				"  日本語形態素解析器 MoCA(もか)",
				"    cafe.moca.jp@gmail.com",
				"",
				"  MoCA は、日本語形態素解析器 Kuromoji を基に開発されています。",
				"    http://atilika.org/",
				"*****************************************************************",
		};
		for(String ci:copyrights)
			System.out.println(ci);
		new InputMain(args, "形態素辞書の準備中…", "準備完了。文字列を入力してください") {

			MocaTokenizer moca;

			@Override
			public void prepare() {
				try {
					switch (args.length) {
					case 2:
						moca = new MocaTokenizer(args[1], MocaMode.getMocaMode(args[0]));
						return;
					case 1:
						moca = new MocaTokenizer(MocaMode.getMocaMode(args[0]));
						return;
					case 0:
						moca = new MocaTokenizer();
						return;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void run(String line) {
				// System.out.println("音節:");
				// System.out.println(new
				// jp.ac.waseda.info.kake.moca.syllable.SyllabifiedString(line));
				// System.out.println("形態素:");
				moca.printTokens(line);
				System.out.println();
			}
		}.main();
	}
}
