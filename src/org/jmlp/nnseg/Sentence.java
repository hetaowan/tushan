package org.jmlp.nnseg;

import java.util.ArrayList;

import org.jmlp.str.basic.SSO;

public class Sentence {

	public TOKEN[] seg2tokens(String text)
	{
		if(SSO.tioe(text))
		{
			return null;
		}
		text=text.trim();
		String regexText=text.replaceAll("\\s+", " ");
		String[] seg_arr=regexText.split(" ");
		
		String word="";
		String ch="";
		ArrayList<TOKEN> token_list=new ArrayList<TOKEN>();
		token_list.add(new TOKEN("BEGIN_TOKEN",1));
		for(int i=0;i<seg_arr.length;i++)
		{
			word=seg_arr[i].trim();
		    ch=word.charAt(0)+"";
		    token_list.add(new TOKEN(ch,1));
			for(int j=1;j<word.length();j++)
			{
				  ch=word.charAt(j)+"";
				  token_list.add(new TOKEN(ch,0));  
			}
		}
		
		token_list.add(new TOKEN("END_TOKEN",1));
		TOKEN[] token_arr=new TOKEN[token_list.size()];
		for(int i=0;i<token_list.size();i++)
		{
			token_arr[i]=token_list.get(i);
		}
		
		return token_arr;
	}
	
	public TOKEN[] unseg2tokens(String text)
	{
		if(SSO.tioe(text))
		{
			return null;
		}
		text=text.trim();
        text=text.replaceAll("\\s+", "");
        String ch="";
        
		ArrayList<TOKEN> token_list=new ArrayList<TOKEN>();
		token_list.add(new TOKEN("BEGIN_TOKEN",1));
        for(int i=0;i<text.length();i++)
        {
          ch=text.charAt(i)+"";
          token_list.add(new TOKEN(ch,1));
        }
    	token_list.add(new TOKEN("END_TOKEN",1));
		TOKEN[] token_arr=new TOKEN[token_list.size()];
		for(int i=0;i<token_list.size();i++)
		{
			token_arr[i]=token_list.get(i);
		}
		
		return token_arr;
	}
	
	
	public static void main(String[] args)
	{
		String text="宏章 公务员 , 宏章 教育 广西 总校 , 广西 公务员 考试网 , 广西 人事 考试网 , 广西 公务员 考试 培训 , 2013 广西 公务员 考试 , 广西 事业 单位 考试网 , 广西 村官 考试 , 公检法司 培训 广 广西省 公务员 考试 , 广西 事业 单位 考试网 , 广西 公务员 考试 培训 , 广西 村官 考试 , 广西 宏章 教育 , “ 2013年 宏章 教育 杯 ” 广西 公务员 考试 模拟 大赛 公务员 历年 考试 真";
		Sentence s=new Sentence();
		TOKEN[] seg_tokens=s.seg2tokens(text);
		TOKEN token=null;
		for(int i=0;i<seg_tokens.length;i++)
		{
			token=seg_tokens[i];
			System.out.print(token.word+":"+token.label+" ");
		}
	}
	
}
