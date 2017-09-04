package org.gj.analyzer.filter.synonym.data;

import java.io.IOException;

public interface SynonymData {
	public void reloadDic();

	String[] getSynonyms(String s) throws IOException;

}
