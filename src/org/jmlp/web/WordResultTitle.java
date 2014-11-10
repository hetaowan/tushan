package org.jmlp.web;

public class WordResultTitle extends WordResult {

	private String[] result;

	public String[] getResult() {
		return result;
	}

	public void setResult(String[] result) {
		this.result = result;
	}

	@Override
	public String toString() {
		
		String str="";
		if(result==null)
		{
			return "";
		}
		
		for(int i=0;i<result.length;i++)
		{
		  str=str+result[i]+"\n";	
		}
		
		return str;
	}
	
}
