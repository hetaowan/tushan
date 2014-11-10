package org.jmlp.web;

public abstract class ConfigureFactory {

	public abstract int getWordResultType();

	public abstract String getPrefixSE(); 

	public abstract String getWordAnalyticOutputDir();
	
	public abstract WordAnalytic getWordAnalytic();
	
}
