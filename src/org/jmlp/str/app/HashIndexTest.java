package org.jmlp.str.app;

import edu.stanford.nlp.util.HashIndex;
import edu.stanford.nlp.util.Index;

public class HashIndexTest {

	public static void main(String[] args)
	{
		Index<String> featureIndex = new HashIndex<String>();
		featureIndex.add("a");
		featureIndex.add("b");
		featureIndex.add("c");
		featureIndex.add("d");
		featureIndex.add("e");
		
		System.out.println(featureIndex.toString());
		
		
	}
	
	
}
