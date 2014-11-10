package org.jmlp.str.app;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.jmlp.file.utils.FileToArray;
import org.jmlp.str.basic.SSO;

public class FilterTitle {

	public static void main(String[] args) throws Exception
	{
		FileWriter fw=new FileWriter(new File("temp/supervised/tr_title.txt"));
		PrintWriter pw=new PrintWriter(fw);
		
		String[] samples=FileToArray.fileToDimArr("temp/supervised/tr.txt");
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
			seg_arr=line.split("\001");
			if(seg_arr.length!=3)
			{
				continue;
			}
			
		    label=seg_arr[0];	
			docid=seg_arr[1];
			text=seg_arr[2];
            text_seg=text.split("\\s+");
            if(text_seg.length<1)
            {
            	continue;
            }
			title=text_seg[0];
			title=title.trim();
            pw.println(label+"\001"+docid+"\001"+title);
            
		}
		
		fw.close();
		pw.close();
		
	}
	
	
}
