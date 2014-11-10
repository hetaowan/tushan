package org.jmlp.web;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;

import org.jmlp.file.utils.FileReaderUtil;
import org.jmlp.file.utils.FileToArray;
import org.jmlp.str.basic.SSO;

public class TrunkBat {

	public void trunk(String input,String trunk,String output)
	{
		try{
	     HashMap<String,String> wordMap=FileReaderUtil.file2Hash(trunk);
	     BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(input)));
	     boolean left=false;
	     String line="";
	     
	     PrintWriter pw=new PrintWriter(new FileWriter(output));
	     while((line=br.readLine())!=null)
	     {
	    	 if(SSO.tioe(line))
	    	 {
	    		
	    		 continue;
	    	 }
	    	 line=line.trim();
	    	 if(wordMap.containsKey(line))
	    	 {
	    		left=true; 
	    	 }
	    	 if(left==true)
	    	 {
	    		 pw.println(line);
	    	 }
	    	 
	     }
	     
	     
	     br.close();
	     pw.close();
	     
	     
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public static  void main(String[] args)
	{
		if(args.length!=3)
		{
			System.err.println("Usage:<input> <trunk> <output>");
			System.exit(1);
		}
		
		TrunkBat tb=new TrunkBat();
		String input=args[0];
		String trunk=args[1];
		String output=args[2];
		
		tb.trunk(input, trunk, output);
		
	}
	
}
