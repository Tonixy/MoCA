/**
 * org.atilika.kuromoji.dict.DictionariesのMoCA版
 */
package jp.ac.waseda.info.kake.moca.dict;

import org.atilika.kuromoji.dict.*;
import org.atilika.kuromoji.trie.DoubleArrayTrie;

public final class MocaDictionaries {

	private static TokenInfoDictionary dictionary;

	private static UnknownDictionary unknownDictionary;

	private static ConnectionCosts costs;

	private static DoubleArrayTrie trie;

	private static boolean initialized = false;

	static {
		load();
	}

	private static synchronized void load() {

		final String prefix = "moca_";

		if (MocaDictionaries.initialized) {
			return;
		}

		try {
			MocaDictionaries.dictionary = TokenInfoDictionary.getInstance(prefix);
			MocaDictionaries.unknownDictionary = UnknownDictionary.getInstance(prefix);
			MocaDictionaries.costs = ConnectionCosts.getInstance(prefix);
			MocaDictionaries.trie = DoubleArrayTrie.getInstance(prefix);
			MocaDictionaries.initialized = true;
		} catch (Exception ex) {
			throw new RuntimeException("MoCAの辞書を読み込めませんでした。", ex);
		}
	}

	/**
	 * @return the dictionary
	 */
	public static TokenInfoDictionary getDictionary() {
		return dictionary;
	}

	/**
	 * @param dictionary the dictionary to set
	 */
	public static void setDictionary(TokenInfoDictionary dictionary) {
		MocaDictionaries.dictionary = dictionary;
	}

	/**
	 * @return the unknownDictionary
	 */
	public static UnknownDictionary getUnknownDictionary() {
		return unknownDictionary;
	}

	/**
	 * @param unknownDictionary the unknownDictionary to set
	 */
	public static void setUnknownDictionary(UnknownDictionary unknownDictionary) {
		MocaDictionaries.unknownDictionary = unknownDictionary;
	}

	/**
	 * @return the costs
	 */
	public static ConnectionCosts getCosts() {
		return costs;
	}

	/**
	 * @param costs the costs to set
	 */
	public static void setCosts(ConnectionCosts costs) {
		MocaDictionaries.costs = costs;
	}

	/**
	 * @return the trie
	 */
	public static DoubleArrayTrie getTrie() {
		return trie;
	}

	/**
	 * @param trie the trie to set
	 */
	public static void setTrie(DoubleArrayTrie trie) {
		MocaDictionaries.trie = trie;
	}
}
