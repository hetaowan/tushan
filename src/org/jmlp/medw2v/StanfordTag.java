package org.jmlp.medw2v;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Properties;

import org.jmlp.file.utils.FileReaderUtil;
import org.jmlp.str.basic.SSO;




public class StanfordTag {
	/*
	public MaxentTagger tagger;
	public void load_model()
	{	
		String model_file = "models/chinese-distsim.tagger";
		Properties props = new Properties();
		try{
		 FileInputStream fis = new FileInputStream(model_file + ".props");
		 props.load(fis);
		 tagger = new MaxentTagger(model_file);
		}
		catch(Exception e)
		{
			
		}
	}
	
	public String tag(String text)
	{
		String res="";
		res = tagger.tagString(text);
		return res;
	}
	
	public String noun_text(String text)
	{
		if(SSO.tioe(text))
		{
			return "";
		}
		
		String[] seg_arr=text.split("\\s+");
		String item="";
		
		String ntext="";
		for(int i=0;i<seg_arr.length;i++)
		{
			item=seg_arr[i].trim();
			if((item.indexOf("/NN")>=0))
			{
				item=item.replaceAll("/NN","");
				ntext+=(item+" ");
			}
			else if((item.indexOf("/NR")>=0))
			{
				item=item.replaceAll("/NR","");
				ntext+=(item+" ");
			}
			else
			{
				ntext+=("NA ");
			}
		}
		ntext=ntext.trim();	
		return ntext;
	}
	
	public static void main(String[] args)
	{
		StanfordTag stanftag=new StanfordTag();
		stanftag.load_model();
		String seg_dict_file="temp/seg_test/five_dict_uniq.txt";
		String stop_dict_file="temp/seg_test/cn_stop_words_utf8.txt";
		StanfordSeg stanfseg=new StanfordSeg();
		stanfseg.load_model();
		HashMap<String,String> seg_dict=FileReaderUtil.file2Hash(seg_dict_file);
		HashMap<String,String> stop_dict=FileReaderUtil.file2Hash(stop_dict_file);
		stanfseg.setSeg_dict(seg_dict);
		stanfseg.setStop_dict(stop_dict);
		
		String text="秀团网_中国最大的精品团购网站_走秀网 秀团网,团购网,团购网站,聚划算,走秀网 秀团是走秀网旗下中国最大的聚划算精品团购网站，秀团以超低的折扣，百分百的正品保障，受到越的消费者青睐。秀团汇聚了众多世界知名品牌，支持货到付款，全国包邮";
		String seg_text=stanfseg.seg(text);
		String tag_text=stanftag.tag(seg_text);
		String ntext=stanftag.noun_text(tag_text);
		System.out.println("tag_text:"+tag_text);
		System.out.println("ntext:"+ntext);
	}
	
	*/
	
}
