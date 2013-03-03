package jp.ac.waseda.info.kake.moca.viterbi.wordcost;

/**
 * 単語コスト調整器2つを掛け合わせた単語コスト調整器です。
 *
 * @author Tony
 *
 */
public class MultipliedWordCostAdjuster extends AbstractWordCostAdjuster {

	private final AbstractWordCostAdjuster adjusterA;
	private final AbstractWordCostAdjuster adjusterB;

	/**
	 * 掛け合わせる2つの単語コスト調整器を与え、MultipliedWordCostAdjusterを生成します。
	 *
	 * @param adjusterA
	 * @param adjusterB
	 */
	public MultipliedWordCostAdjuster(AbstractWordCostAdjuster adjusterA, AbstractWordCostAdjuster adjusterB) {
		this.adjusterA = adjusterA;
		this.adjusterB = adjusterB;
	}

	/**
	 * MoCA倍率 = 2つの単語コスト調整器によるMoCA倍率を掛け算した結果
	 */
	@Override
	public double getAmplification(String surfaceForm, String fixedForm, String dictionaryForm) {
		return adjusterA.getAmplification(surfaceForm, fixedForm, dictionaryForm)
				* adjusterB.getAmplification(surfaceForm, fixedForm, dictionaryForm);
	}

	@Override
	protected double getPartOfSpeechAmplification(String[] partOfSpeech) {
		return Math.sqrt(adjusterA.getPartOfSpeechAmplification(partOfSpeech)
				* adjusterB.getPartOfSpeechAmplification(partOfSpeech));
	}
}
