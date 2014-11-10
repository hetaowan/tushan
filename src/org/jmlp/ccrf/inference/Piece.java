package org.jmlp.ccrf.inference;

import java.util.ArrayList;
import java.util.Map;

import org.jmlp.str.basic.SSO;

/**
 * 句子的一个片断
 * @author lq
 */
public class Piece extends Sentence{

	
	/**
	 * text 的格式:花/1 非/0 花/0 雾/0 非雾/0 TV/1 版/0 07/1 —/1 在线/1 播放/0 —/1 《/1 花/1 非/0
	 * 花/0 雾/0 非雾/0 TV/1 版/0 》/1 —/1 电视剧/1 —/1 优酷网/1 ，/1 视频/1 高清/1 在线/1 观看/0
	 */
	@Override
	public Sentence buildSentence(String text) {
		
		
		if(SSO.tioe(text))
		{
			return null;
		}
		
		text=text.trim();
		String[] splits=text.split("\\s+");
		String split="";
		String tLabel="";
		
		ArrayList<Word> innerList=new ArrayList<>();
		ArrayList<Word> outerList=new ArrayList<>();
		
		int i=0;
		int j=0;
		
		for(i=0;i<splits.length;i++)
		{
			split=splits[i];
			tLabel=split.substring(split.lastIndexOf("/")+1,split.length());
			split=split.substring(0,split.lastIndexOf("/"));
			//System.out.println("i="+i+" split="+split+" tLabel="+tLabel);
			if(Token.isToken(split))
			{
			  	innerList.add(new Word(split,1,1));
			  	outerList.add(new Word(split,Integer.parseInt(tLabel),1));
			}
			else
			{
				innerList.add(new Word(split.charAt(0)+"",1,1));
				for(j=1;j<split.length();j++)
				{
					innerList.add(new Word(split.charAt(j)+"",0,1));
				}
				outerList.add(new Word(split,Integer.parseInt(tLabel),split.length()));
			}
				
		}
		
		Word[] innerarr=new Word[innerList.size()];
		for(int k=0;k<innerList.size();k++)
		{
		   innerarr[k]=innerList.get(k);	
		}
		
		this.setInnerWords(innerarr);
		
		Word[] outerarr=new Word[outerList.size()];
		for(int k=0;k<outerList.size();k++)
		{
			outerarr[k]=outerList.get(k);	
		}
		
		this.setOuterWords(outerarr);
		
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void makeupCliques(Map<String, Feature> features,
			Index<String> featureIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inference(Map<String, Feature> features,
			Index<String> featureIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void computeAssignmentProbabilities() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CCRFCliqueTree<CCRFLabel> getOuterCliqueTreeFromInnerLabels(
			int[] innerLables) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CCRFCliqueTree<CCRFLabel> getOuterCliqueTreeFromInnerLabels(
			Assignment innerAssignment) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Assignment[] getPossibleInnerLabels() {
		// TODO Auto-generated method stub
		return null;
	}


    public static void main(String[] args)
    {
	   String text="二手房/1 市场/1 在/1 经历/1 了/1 3/1 月份/0 的/1 成交/1 暴涨/1 之后/1 ，/1 伴随/1 着/1 3/1 月/0 底/0 地方/1 版/0 “/1 国/1 五/0 条/0 ”/1 陆续/1 出台/1 ，/1 市场/1 逐步/1 回归/1 理性/1 ，/1 成交/1 总量/0 环比/1 5/1 月/0 下降/1 6.71%/1 ，/1 26/1 城市/0 二手房/1 挂牌/1 价格/0 环比/1 24/1 升/0 2/1 降/0 ，/1 共/1 14/1 个/0 城市/1 环比/1 涨/1 跌幅/0 在/1 1%/1 内/0 ，/1 整体/1 价格/0 有所/1 震动/0 ，/1 呈/1 略微/1 上扬/1 态势/1 。/1";
    
	   Piece piece=new Piece();
	   piece.buildSentence(text);
	   
	   int innerLen=0;
	   for(int i=0;i<piece.getInnerWords().length;i++)
	   {
		   innerLen+=(piece.getInnerWords()[i].getLength());
		   System.out.print(piece.getInnerWords()[i].toString()+" ");
	   }  	
	  
	   System.out.println("\ninnerLen:"+innerLen);
	   System.out.println("innerCliqueNum:"+(piece.getInnerWords().length));
	   System.out.println("===================");
	   
	   int outerLen=0;
	   for(int i=0;i<piece.getOuterWords().length;i++)
	   {
		   outerLen+=(piece.getOuterWords()[i].getLength());
		   System.out.print(piece.getOuterWords()[i].toString()+" ");
	   }
	   System.out.println("\nouterLen:"+outerLen);
	   System.out.println("outerCliqueNum:"+(piece.getOuterWords().length));
	   System.out.println();
    }
	
	
}
