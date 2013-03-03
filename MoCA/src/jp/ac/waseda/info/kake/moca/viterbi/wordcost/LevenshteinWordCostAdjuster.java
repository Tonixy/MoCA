package jp.ac.waseda.info.kake.moca.viterbi.wordcost;

import jp.ac.waseda.info.kake.string.Levenshtein;

/**
 * レーベンシュタイン距離を用いた単語コスト調整器です。長音節最適化を行ったことで生成された単語に適用されます。
 *
 * @author Tony
 *
 */
public class LevenshteinWordCostAdjuster extends AbstractWordCostAdjuster {

	/**
	 * MoCA倍率 = 1 + 表層表現と音節修正表現のレーベンシュタイン距離
	 */
	@Override
	public double getAmplification(String surfaceForm, String fixedForm, String dictionaryForm) {
		double res = 1.0 + new Levenshtein(fixedForm, true).getDistance(surfaceForm);
		if (getNCount(surfaceForm) != getNCount(fixedForm))
			res += 2;
		if (getXTSUCount(surfaceForm) != getXTSUCount(fixedForm))
			res += 2;
		return res;
	}

	@Override
	protected double getPartOfSpeechAmplification(String[] partOfSpeech) {
		double result = 1.0;
		if (partOfSpeech[0].equals("記号"))
			result = 3.0;
		if (partOfSpeech[0].equals("名詞"))
			if (partOfSpeech[1].equals("固有名詞"))
				result = 3.0;
			else
				result = 1.5;
		return result * super.getPartOfSpeechAmplification(partOfSpeech);
	}

	private static int getNCount(String string) {
		int count = 0;
		for (char ch : string.toCharArray())
			if (ch == 'ん' || ch == 'ン')
				count++;
		return count;
	}

	private static int getXTSUCount(String string) {
		int count = 0;
		for (char ch : string.toCharArray())
			if (ch == 'ッ' || ch == 'っ')
				count++;
		return count;
	}
}
