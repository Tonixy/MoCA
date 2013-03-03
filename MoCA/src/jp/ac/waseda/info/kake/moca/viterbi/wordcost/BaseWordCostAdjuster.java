package jp.ac.waseda.info.kake.moca.viterbi.wordcost;


public class BaseWordCostAdjuster extends AbstractWordCostAdjuster {

	public double getAmplification(String surfaceForm, String fixedForm, String dictionaryForm) {
		return 1.0;
	}
}
