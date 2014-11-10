package org.jmlp.web;

public class EasyConfigureFactory extends ConfigureFactory{

	@Override
	public String getPrefixSE() {
		
		return "http://www.so.com/s?&q=";
	}

	/**
	 * 0  获取title
	 * 1 获取有效数量
	 */
	@Override
	public int getWordResultType() {
		
		return 1;
	}

	@Override
	public String getWordAnalyticOutputDir() {
		// TODO Auto-generated method stub
		return "wordAnalytic";
	}

	@Override
	public WordAnalytic getWordAnalytic() {
		// TODO Auto-generated method stub
		return new WordAnalyticSE();
	}

	

}
