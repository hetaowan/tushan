package org.jmlp.web;

import java.net.URLEncoder;
import java.util.ArrayList;

import org.jmlp.file.utils.FileWriterUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 网络解析该单词的信息
 * 
 * @author lq
 * 
 */
public class WordAnalyticSE extends WordAnalytic{
	
	private String prefixSE;
	
	private Fetcher fetcher;
	
	private ConfigureFactory confFactory;
	
	private int wrType=0;
	
	ArrayList<String> revWords=new ArrayList<String>();

	public String[] getSETopTitles(String word) {
		
		//String prefix = "http://www.so.com/s?&q=";
		String url = prefixSE + URLEncoder.encode(word);
		Document doc=null;
		try {

			//////String content=fetcher.getSourceEasyProxy(url,getProxy());
			//////doc=Jsoup.parse(content);
			doc = Jsoup.connect(url).get();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//doc.toString();
		
		//FileWriterUtil.writeContent(doc.toString(), "sohtml.txt");
		
		Elements ress=doc.getElementsByClass("res-title");
		Element res=null;
		
		String title="";
		
		String[] titles=new String[ress.size()];
		
        for(int i=0;i<ress.size();i++)
        {
        	res=ress.get(i);
        	title=res.getElementsByTag("a").text();	
        	titles[i]=title;
        	//System.out.println(title);
        }
		
        
        Elements resswords=doc.getElementsByClass("mh-name");
        Element resword=null;
        String rword="";
        revWords=new ArrayList<String>();
        for(int i=0;i<resswords.size();i++)
        {
        	resword=resswords.get(i);
        	rword=resword.getElementsByTag("a").text();	
        	revWords.add(rword);
        	//System.out.println(title);
        }
        
        
		return titles;
	}

	public int getEffectNum(String word)
	{
		int en=0;
		
		String[] titles=getSETopTitles(word);
		if(titles.length<2)
		{
		  return 0;	
		}
		
		for(int i=0;i<titles.length;i++)
		{
			if((titles[i].indexOf(word)!=-1)&&(titles[i].indexOf("百科")==-1)&&(titles[i].indexOf("地图")==-1)&&(titles[i].indexOf("问答")==-1)&&(titles[i].indexOf("知道")==-1)&&(titles[i].indexOf("360")==-1)&&(titles[i].indexOf("百度")==-1)&&(titles[i].indexOf("搜狗")==-1))
			{
				en++;
			}
		}
		
		System.out.println("pa:"+word+" :" +en);
				
		
		return en;
	}
	
	
	public static void main(String[] args)
	{
		String word="侯卫东官场笔记";
		WordAnalyticSE wa=new WordAnalyticSE();
		wa.init();
		//wa.getSETopTitles(word);
		int en=wa.getEffectNum(word);
		FileWriterUtil.writeContent(word+":"+en, "to.txt");
		System.out.println("en="+en);
	}

	@Override
	public void init() {
		
		confFactory=ConfigureFactoryInstantiate.getConfigureFactory();
		wrType=confFactory.getWordResultType();
		prefixSE=confFactory.getPrefixSE();
		fetcher=new Fetcher();
		
	}

	@Override
	public WordResult parse(String word) {
	
		//System.out.println("parsing word:"+word);
		
		if(wrType==0)
		{
			WordResultTitle wr=new WordResultTitle();
			String[] titles=getSETopTitles(word);
			wr.setResult(titles);
			return wr;
			
		}
		else if(wrType==1)
		{
			WordResultEN wren=new WordResultEN();
			int en=getEffectNum(word);
			wren.setEn(en);
		    return wren;		
		}
		
		return null;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public int getWrType() {
		return wrType;
	}

	public void setWrType(int wrType) {
		this.wrType = wrType;
	}

	@Override
	public ArrayList<String> getRevWords() {

		return revWords;
	}
	
}
