package jp.ac.waseda.info.kake.moca.viterbi.wordcost;

import jp.ac.waseda.info.kake.string.KanaConverter;

public class UnknownWordCostAdjuster extends AbstractWordCostAdjuster {

	@Override
	public double getAmplification(String surfaceForm, String fixedForm, String dictionaryForm) {
		if(KanaConverter.isKatakana(surfaceForm))
			return 1.5;
		return 2.0;
	}
}
