package org.jmlp.str.app;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.jmlp.file.utils.FileToArray;
import org.jmlp.str.basic.SSO;

public class SVMSample {
	public static void main(String[] args) throws Exception
	{
		FileWriter fw=new FileWriter(new File("temp/medw2v/trvec_svm.txt"));
		PrintWriter pw=new PrintWriter(fw);
		
		String[] samples=FileToArray.fileToDimArr("temp/medw2v/trvec.txt");
		String line="";
		
		String[] seg_arr=null;
		String label="";
		String docid="";
		String text="";
		
		String[] text_seg=null;
		String title="";
		
		for(int i=0;i<samples.length;i++)
		{
			line=samples[i];
			if(SSO.tioe(line))
			{
				continue;
			}
			line=line.trim();	
	        if(SSO.tioe(line))
	        {
	        	continue;
	        }
	        
	        line=line.replaceAll("\001", " ");
            pw.println(line);
            
		}
		
		fw.close();
		pw.close();
		
	}
}
