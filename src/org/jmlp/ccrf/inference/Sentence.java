package org.jmlp.ccrf.inference;

import java.util.Map;

/**
 * 每一条标记的样本，可以表示一个句子、多个句子甚至一个句子的一部分
 * 从句子中提取特征的工作不是sentence负责，因为只有在piecewise训练中，每个句子的特征权重才独立求解，为了使sentence更通用
 * 由外部类负责推导该句子(或句子集合)中的所有特征，再作为参数 传给sentence类
 * @author lq
 */
public abstract class Sentence {
 
	/**
	 * 句子编号
	 */
	private int sentenceId;
	
	/**
	 * 句子中的每个token，按照从前向后的顺序存储在数组里
	 */
	private Word[] innerWords;
	
	/**
	 * 句子的xi label(标记好的) ,长度等于句子中的单词数
	 */
	private int[] innerLabels;
	
	/**
	 * 
	 */
	private Word[] outerWords;
	
	/**
	 * 句子的delta label(标记好的),长度小于等于句子的单词数
	 */
	private int[] outerLabels;
	
	/**
	 * inner crf 的cliques 
	 */
	private Clique[][] innerCliques;
	
	/**
	 * outer crf 的cliques
	 */
	private Clique[][] outerCliques;
	
	/**
	 * inner crf 的所有可能赋值
	 */
	private Assignment[] innerPossibleLabels;

    /**
     * get innerWords, innerLabels, outerWords, outerLabels、 
     * @param text
     * @return
     */
	public abstract Sentence buildSentence(String text);
	
	/**
	 * 根据该句子和外部推导的特征生成该句子的cliques,包括innerCliques和outerCliques
	 * 在参数训练整个过程中只进行一次
	 * @param features
	 * @param featureIndex
	 */
	public abstract void makeupCliques(Map<String,Feature> features,Index<String> featureIndex);
	
	/**
	 * 在theta和alpha为当前值的情况下，计算各clique的边缘概率
	 * 在参数训练整个过程中可能要进行多次，即每一词参数更新后都要调用一次该方法
	 * @param features
	 * @param featureIndex
	 */
	public abstract void inference(Map<String,Feature> features,Index<String> featureIndex);
	
	/**
	 * 计算innerPossibleLabels 中各assignment的边缘概率
	 * 在参数训练整个过程中可能要进行多次，即每一词参数更新后都要调用一次该方法
	 */
	public abstract void computeAssignmentProbabilities();
	
	/**
	 * 根据inner crf 的label 构造outer crf 的 cliqueTree
	 * 在参数训练整个过程中只进行一次
	 * @return
	 */
	public abstract CCRFCliqueTree<CCRFLabel> getOuterCliqueTreeFromInnerLabels(int[] innerLables);
	
	/**
	 * 根据inner crf 的label 构造outer crf 的 cliqueTree
	 * 在参数训练整个过程中只进行一次
	 * @return
	 */
	public abstract CCRFCliqueTree<CCRFLabel> getOuterCliqueTreeFromInnerLabels(Assignment innerAssignment);
	
	/**
	 * 获取inner crf 所有可能的标记序列，每一隔标记序列对应一个xi
	 * 在参数训练整个过程中只进行一次
	 * @return
	 */
	public abstract Assignment[] getPossibleInnerLabels();
		
	public int getSentenceId() {
		return sentenceId;
	}

	public void setSentenceId(int sentenceId) {
		this.sentenceId = sentenceId;
	}



	public int[] getInnerLabels() {
		return innerLabels;
	}

	public void setInnerLabels(int[] innerLabels) {
		this.innerLabels = innerLabels;
	}

	public int[] getOuterLabels() {
		return outerLabels;
	}

	public void setOuterLabels(int[] outerLabels) {
		this.outerLabels = outerLabels;
	}

	public Clique[][] getInnerCliques() {
		return innerCliques;
	}

	public void setInnerCliques(Clique[][] innerCliques) {
		this.innerCliques = innerCliques;
	}

	public Clique[][] getOuterCliques() {
		return outerCliques;
	}

	public void setOuterCliques(Clique[][] outerCliques) {
		this.outerCliques = outerCliques;
	}

	public Assignment[] getInnerPossibleLabels() {
		return innerPossibleLabels;
	}

	public void setInnerPossibleLabels(Assignment[] innerPossibleLabels) {
		this.innerPossibleLabels = innerPossibleLabels;
	}

	public Word[] getInnerWords() {
		return innerWords;
	}

	public void setInnerWords(Word[] innerWords) {
		this.innerWords = innerWords;
	}

	public Word[] getOuterWords() {
		return outerWords;
	}

	public void setOuterWords(Word[] outerWords) {
		this.outerWords = outerWords;
	}


	
	
}
