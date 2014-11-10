package org.jmlp.perceptron;

import java.util.ArrayList;

public class FEATSETTHREE {


	public ArrayList<WORD> words = null;

	/**
	 * words对应的权重
	 */
	public ArrayList<WEI> first_weights = null;
	
	/**
	 * words对应的权重
	 */
	public ArrayList<WEI> second_weights = null;
	
	/**
	 * words对应的权重
	 */
	public ArrayList<WEI> third_weights = null;
	
	public LABEL label;

	/**
	 * 此 feat set 的得分
	 */
	public double score;

	public FEATSETTHREE() {
		words = new ArrayList<WORD>();
		first_weights = new ArrayList<WEI>();
		second_weights = new ArrayList<WEI>();
		third_weights = new ArrayList<WEI>();
	}

	public FEATSETTHREE(WORD word, LABEL label, WEI first_weight,WEI second_weight,WEI third_weight) {
		words = new ArrayList();
		words.add(word);
		first_weights = new ArrayList<WEI>();
		first_weights.add(first_weight);

                second_weights = new ArrayList<WEI>();
		second_weights.add(second_weight);

                third_weights = new ArrayList<WEI>();
		third_weights.add(third_weight);
		
		this.label = label;
	}

	public void addWord(WORD word, WEI first_weight,WEI second_weight,WEI third_weight) {
		words.add(word);
		first_weights.add(first_weight);
		second_weights.add(second_weight);
		third_weights.add(third_weight);
		
	}

	public void calScore() {
		WORD w;
		WEI first_we;
		WEI second_we;
		WEI third_we;
		
		double sum = 0;
		for (int i = 0; i < words.size(); i++) {
			w = words.get(i);
			first_we = first_weights.get(i);
			second_we=second_weights.get(i);
			third_we=third_weights.get(i);
			
			sum += (w.count * first_we.val+w.count*second_we.val+w.count*third_we.val);
		}
		score = sum;
	}

	public String toString() {
		String str = "label=" + this.label.index + " word=[";
		String word_str = "";
		for (int j = 0; j < this.words.size(); j++) {
			word_str += ((this.words.get(j)).index + ":"
					+ (this.words.get(j)).count + " ");
		}
		word_str = word_str.trim();
        
		str += word_str;
        str+="]";
		
		return str;
	}
	
	/**
	 * 复制FEATSETTHREE 对象,值传递
	 * @param src
	 * @return
	 */
	public static FEATSETTHREE copy(FEATSETTHREE src)
	{
		FEATSETTHREE nfset=new FEATSETTHREE();
		nfset.label=new LABEL(src.label.index,src.label.label_num);
		nfset.words=new ArrayList<WORD>();
		WORD w=null;
		for(int i=0;i<src.words.size();i++)
		{
			w=new WORD(src.words.get(i).index,src.words.get(i).count);
			nfset.words.add(w);
		}
		
		nfset.first_weights=new ArrayList<WEI>();
		nfset.second_weights=new ArrayList<WEI>();
		nfset.third_weights=new ArrayList<WEI>();
		
		WEI first_we=null;
		for(int i=0;i<src.first_weights.size();i++)
		{
			first_we=new WEI(src.first_weights.get(i).val);
			nfset.first_weights.add(first_we);
		}
		
		WEI second_we=null;
		for(int i=0;i<src.second_weights.size();i++)
		{
			second_we=new WEI(src.second_weights.get(i).val);
			nfset.second_weights.add(second_we);
		}
		
		WEI third_we=null;
		for(int i=0;i<src.third_weights.size();i++)
		{
			third_we=new WEI(src.third_weights.get(i).val);
			nfset.third_weights.add(third_we);
		}
		
		return nfset;
	}
	
	
}
