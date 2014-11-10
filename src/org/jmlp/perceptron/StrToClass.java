package org.jmlp.perceptron;

import java.util.ArrayList;

import org.jmlp.medw2v.Medw2vParms;
import org.jmlp.str.basic.SSO;

/**
 * 字符串转对象数组
 * @author lq
 *
 */
public class StrToClass {

	
	/**
	 * 字符串转单词数组
	 * @param str
	 * @return
	 */
	public  WORD[] str2words(String str)
	{
		str=str.trim();
		if(SSO.tioe(str))
		{
			return null;
		}
		
		String[] seg_arr=str.split("\\s+");
		String item="";
		String[] item_arr=null;
		String index="";
		String count="";
		ArrayList<WORD> wl=new ArrayList<WORD>();
		WORD w=null;
		/*
		double sum_count=0;
		for(int i=0;i<seg_arr.length;i++)
		{
			item=seg_arr[i].trim();
			item_arr=item.split(":");
		    if(item_arr.length<2)
		    {
		    	continue;
		    }
		    count=item_arr[1].trim();
		    sum_count+=Double.parseDouble(count);
		}
		*/
		
		for(int i=0;i<seg_arr.length;i++)
		{
			item=seg_arr[i].trim();
			item_arr=item.split(":");
		    if(item_arr.length<2)
		    {
		    	continue;
		    }
		    index=item_arr[0].trim();
		    count=item_arr[1].trim();
		   // w=new WORD(Integer.parseInt(index),Integer.parseInt(count));
		    w=new WORD(Integer.parseInt(index),Double.parseDouble(count));
		    wl.add(w);
		}
		
		WORD[] warr=new WORD[wl.size()];
		for(int i=0;i<warr.length;i++)
		{
			warr[i]=wl.get(i);
		}
		//System.out.println("warr.len:"+warr.length);
		return warr;
	}
	
	/**
	 * 字符串转label
	 * @param str
	 * @return
	 */
	public  LABEL str2label(String str,LEARNPARM learn_parm)
	{
		LABEL l=new LABEL(Integer.parseInt(str),learn_parm.label_num);		
		return l;
	}
	
	/**
	 * 字符串转label
	 * @param str
	 * @return
	 */
	public static LABEL str2labelSimple(String str,int n)
	{
		LABEL l=new LABEL(Integer.parseInt(str),n);		
		return l;
	}
	
	/**
	 * 字符串转label
	 * @param str
	 * @return
	 */
	public static LABEL str2labelthree(String str,Medw2vParms learn_parm)
	{
		String[] label_seg=null;
		label_seg=str.split("\\|");
		
		String first_label="";
		String second_label="";
		String third_label="";
		String label="";

		first_label=label_seg[0];
		second_label=label_seg[1];
		third_label=label_seg[2];
		label=label_seg[3];

		int first_index=Integer.parseInt(first_label);
		int second_index=Integer.parseInt(second_label);
		int third_index=Integer.parseInt(third_label); 
                int label_index=Integer.parseInt(label);		
		LABEL l=new LABEL(label_index,first_index,second_index,third_index,learn_parm.first_label_num,learn_parm.second_label_num,learn_parm.third_label_num,learn_parm.label_num);		
		return l;
	}
	
}
