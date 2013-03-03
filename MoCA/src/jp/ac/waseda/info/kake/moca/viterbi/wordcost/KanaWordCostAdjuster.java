package jp.ac.waseda.info.kake.moca.viterbi.wordcost;

import jp.ac.waseda.info.kake.moca.string.KanaConverter;

/**
 * 含まれる文字の種類を用いた単語コスト調整器です。読みによる単語検索によって得られた単語に適用されます。
 */
public class KanaWordCostAdjuster extends AbstractWordCostAdjuster {

	private static final String regex = "[" + String.valueOf(KanaConverter.getSpecialCharacters()) + "]";

	/**
	 * MoCA倍率 = H * K
	 *
	 * <ul>
	 * <li>H: ひらがなが表層表現に含まれる場合は2。そうでなければ1</li>
	 * <li>K: カタカナが表層表現に含まれる場合は2。そうでなければ1</li>
	 * </ul>
	 */
	@Override
	public double getAmplification(String surfaceForm, String fixedForm, String dictionaryForm) {
		surfaceForm = surfaceForm.replaceAll(regex, "");
		dictionaryForm = dictionaryForm.replaceAll(regex, "");
		double amp = 1.0;
		if (KanaConverter.containsHiragana(surfaceForm))
			amp *= 2;
		if (KanaConverter.containsKatakana(surfaceForm))
			amp *= 2;
		return amp;
	}

	@Override
	protected double getPartOfSpeechAmplification(String[] partOfSpeech) {
		double result = 1.0;
		if (partOfSpeech[0].equals("記号"))
			result = 2.0;
		return result*super.getPartOfSpeechAmplification(partOfSpeech);
	}
}
