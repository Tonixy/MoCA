package jp.ac.waseda.info.kake.moca;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import jp.ac.waseda.info.kake.moca.dict.MocaDictionaries;
import jp.ac.waseda.info.kake.moca.viterbi.MocaViterbi;

import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;
import org.atilika.kuromoji.dict.UserDictionary;

import jp.ac.waseda.info.kake.moca.system.PrintIntegerMaker;

public class MocaTokenizer extends Tokenizer {

	public static final boolean split = true;
	// TODO splitはtrueでいい? trueにしてると、崩れた表記解析で邪魔になることはない? 例: あ、は、は、は、、、。
	public static final boolean unknownFixMode = true;
	public static final boolean convertsSize = true;
	public static final Mode kuroMode = Mode.NORMAL; // TODO サーチモードは使わなくていい?

	/**
	 * MoCAの挙動を示します。
	 *
	 * @author Tony
	 */
	public enum MocaMode {
		/**
		 * くだけた表現解析を行わず、基本的にKuromojiと同様の結果を返します。
		 */
		ESPRESSO(false, false),
		/**
		 * くだけた表現解析を行います。返される形態素の表層表現は、辞書上の表記に訂正されます。
		 */
		LATTE(true, false),
		/**
		 * くだけた表現解析を行います。返される形態素の表層表現は、元の文の表層表現のままです。
		 */
		MOCHA(true, true),

		/**
		 * 長音節最適化機能のみを有効化します。
		 */
		SYLLABLES(true, false, true),
		/**
		 * 読み仮名検索機能のみを有効化します。
		 */
		READING(false, true, true);

		/**
		 * 何らかの崩れた表記解析を行うか否か。
		 *
		 * 必ず analysesSyllables || analysesReading と同値になります。
		 */
		public final boolean analysesColloquial;
		/**
		 * 長音節最適化を行うか否か
		 */
		public final boolean analysesSyllables;
		/**
		 * 読み仮名検索を行うか否か
		 */
		public final boolean analysesReading;
		/**
		 * trueの場合、形態素解析結果を、崩れた表記のまま(元の文の表記のまま)返します。
		 *
		 * falseの場合、形態素解析結果を、辞書上の表記に直して返します。
		 */
		public final boolean returnsColloquial;

		private MocaMode(boolean analyses, boolean returns) {
			this(analyses, analyses, returns);
		}

		private MocaMode(boolean analysesSyllables, boolean analysesReading, boolean returns) {
			this.analysesSyllables = analysesSyllables;
			this.analysesReading = analysesReading;
			this.analysesColloquial = analysesSyllables || analysesReading;
			returnsColloquial = (analysesSyllables || analysesReading) && returns;
		}

		/**
		 * 与えられた文字列に対応するMocaModeを返します。valueOfよりも柔軟性があります(大文字/小文字非依存、MOCHA / MOCA
		 * のどちらの表記も可)。基本的に、valueOfではなくこちらを用いることを推奨します。
		 *
		 * @param value
		 * @return
		 * @throws IllegalArgumentException
		 */
		public static MocaMode getMocaMode(String value) throws IllegalArgumentException {
			value = value.toUpperCase();
			try {
				return valueOf(value);
			} catch (IllegalArgumentException e) {
				if (value.equals("MOCA"))
					return MOCHA;
				throw e;
			}
		}
	}

	public MocaTokenizer() throws IOException {
		this(MocaMode.MOCHA);
	}

	public MocaTokenizer(String userDictionaryPath) throws IOException {
		this(userDictionaryPath, MocaMode.MOCHA);
	}

	public MocaTokenizer(UserDictionary userDictionary) {
		this(userDictionary, MocaMode.MOCHA);
	}

	public MocaTokenizer(MocaMode mode) {
		this((UserDictionary) null, mode);
	}

	public MocaTokenizer(String userDictionaryPath, MocaMode mode) throws IOException {
		this(UserDictionary.read(new BufferedInputStream(new FileInputStream(userDictionaryPath))), mode);
	}

	public MocaTokenizer(UserDictionary userDictionary, MocaMode mode) {
		super(getMocaViterbi(userDictionary, mode), MocaDictionaries.getDictionary(), MocaDictionaries.getUnknownDictionary(),
				userDictionary, kuroMode, split, unknownFixMode);
	}

	private static MocaViterbi getMocaViterbi(UserDictionary userDictionary, MocaMode mode) {
		return new MocaViterbi(MocaDictionaries.getTrie(), MocaDictionaries.getDictionary(),
				MocaDictionaries.getUnknownDictionary(), MocaDictionaries.getCosts(), userDictionary, mode);
	}

	/**
	 * MoCAモードの切り替えを行います。
	 *
	 * @param mode
	 */
	public void setMocaMode(MocaMode mode) {
		if(viterbi instanceof MocaViterbi)
			((MocaViterbi) viterbi).setMocaMode(mode);
	}

	/**
	 * デモ用。形態素解析結果を出力します。
	 *
	 * @param line
	 */
	public void printTokens(String line) {
		List<Token> tokens = tokenize(line);
		PrintIntegerMaker pim = new PrintIntegerMaker(tokens.get(tokens.size() - 1).getPosition());
		for (Token token : tokens) {
			System.out.println(pim.getPrintInteger(token.getPosition()) + ": " + token.getSurfaceForm() + "\t"
					+ token.getAllFeatures());
		}
	}
}
