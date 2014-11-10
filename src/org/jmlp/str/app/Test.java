package org.jmlp.str.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Test {

	public static void main(String[] args)
	{
		//double a=-2.337299657938671;
		//System.out.println(Math.abs(a));
		List<String> c1=new ArrayList<String>();
		c1.add("a");		
		c1.add("b");
		
		List<String> c2=new ArrayList<String>();
		c2.add("c");
		c2.addAll(c1);
		
		for(int i=0;i<c2.size();i++)
		{
			System.out.println(i+":"+c2.get(i));
		}
		
		
	}
	
}
