package jp.ac.waseda.info.kake.moca.viterbi.wordcost;

import org.atilika.kuromoji.dict.Dictionary;

/**
 * <p>
 * 単語コスト調整器です。辞書と単語ID、表層表現、音節修正表現を用いて、単語コストを計算します。
 *
 * 単語の修正方法にあわせてMoCA倍率計算式(getAmplification)を実装し使用します。
 * </p>
 *
 * <p>
 * 以下に、用語の意味を示します。
 * </p>
 *
 * <ul>
 * <li>表層表現: 入力された文字列</li>
 * <li>音節修正表現: 表層表現を、MoCAの長音節最適化機能によって修正してできた文字列</li>
 * <li>辞書上表現: 辞書に登録された単語の、(読みではない)本来の表記</li>
 * </ul>
 *
 * @author Tony
 *
 */
public abstract class AbstractWordCostAdjuster {

	/**
	 * 標準文字列長。表層表現の長さと辞書上表現の長さの平均がこの値より大きくなると、コストが下がります。
	 */
	public static final double standardLength = 1.5;
	/**
	 * 単語コスト倍率。連接コストとの大きさ調整に用います。
	 */
	private static final double defaultAmplification = 1;

	/**
	 * 単語コストを返します。
	 *
	 * 単語コストは Kuromojiコスト * 品詞依存倍率 * MoCA倍率 * 単語コスト倍率 / 文字列長 で表されます。
	 *
	 * @param surfaceForm
	 *            表層表現
	 * @param fixedForm
	 *            長音節最適化のみ行った後の表現
	 * @param dictionary
	 *            辞書上表現
	 * @param wordId
	 * @return
	 */
	public final int getWordCost(String surfaceForm, String fixedForm, Dictionary dictionary, int wordId) {
		String[] features = dictionary.getAllFeaturesArray(wordId);
		String[] pos = getPartOfSpeech(dictionary, wordId);
		return (int) (getDefaultWordCost(dictionary, wordId) * getPartOfSpeechAmplification(pos) * defaultAmplification
				* getAmplification(surfaceForm, fixedForm, features[features.length - 1]) / getDenominator(surfaceForm,
					fixedForm, features[features.length - 1], pos));
	}

	public static final String[] getPartOfSpeech(Dictionary dictionary, int wordId) {
		return dictionary.getPartOfSpeech(wordId).split(",");
	}

	/**
	 * 品詞依存倍率を返します。
	 *
	 * @param partOfSpeech
	 * @return
	 */
	protected double getPartOfSpeechAmplification(String[] partOfSpeech) {
		return 1.0;
	}

	/**
	 * 文字列長(コスト計算式の分母)を返します。
	 *
	 * 表層表現の長さと辞書上表現の長さの平均Lが標準文字列長Lsを上回る場合、 L / 2 / Ls となります。<br>
	 * LがLsと等しいか下回る場合、 1 となります。
	 *
	 * @param surfaceForm
	 *            表層表現
	 * @param fixedForm
	 *            音節修正表現
	 * @param dictionaryForm
	 *            辞書上表現
	 * @return
	 */
	public double getDenominator(String surfaceForm, String fixedForm, String dictionaryForm, String[] partOfSpeech) {
		double length = (surfaceForm.length() + dictionaryForm.length()) / 2.0 / standardLength;
		double min = 1.0;
		if (partOfSpeech[0].equals("助詞"))
			min = 1.2;
		if (partOfSpeech[0].equals("助動詞"))
			min = 1.2;
		return length > min ? length : min;
	}

	/**
	 * Kuromojiコストを返します。
	 *
	 * @param dictionary
	 * @param wordId
	 * @return
	 */
	public final int getDefaultWordCost(Dictionary dictionary, int wordId) {
		return dictionary.getWordCost(wordId);
	}

	/**
	 * MoCA倍率を返します。
	 *
	 * @param surfaceForm
	 * @param fixedForm
	 * @param dictionaryForm
	 * @param partOfSpeech
	 * @return
	 */
	public abstract double getAmplification(String surfaceForm, String fixedForm, String dictionaryForm);
}
