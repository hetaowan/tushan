package org.jmlp.ccrf.inference;

import java.util.List;

import edu.stanford.nlp.ie.crf.CRFDatum;
import edu.stanford.nlp.ie.crf.CRFLabel;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.objectbank.ObjectBank;

/**
 * Word[] 数组 转换成  Clique[] 数组
 * @author lq
 */
public abstract class Words2Cliques {

	/**
	 * 初始化,目标是类似调用
	 * ObjectBank<List<IN>> documents = makeObjectBankFromString(sentences,
	 *	plainTextReaderAndWriter);(in AbstractSequenceClassifier classifyToString)
	 * CRFDatum<List<String>, CRFLabel> d = makeDatum(document, j,
	 *	featureFactory);(in CRFClassifier documentToDataAndLabels)	
	 * 能够像线性crf一样完整地执行	
	 */
	public abstract void init();
	
	/**
	 * 标记的word序列生成clique序列
	 * @param words
	 * @return
	 */
	public abstract Clique[] word2Cliques(Word[] words);
	
	/**
	 * 将 inner crf 的 cliques 分散到 outer cliques 上
	 * @return
	 */
	public abstract Clique[] distributeInnerCliquesOverOuterCliques(Word[] innerWords,Word[] outerWords);
	
	
	
	public abstract List<CoreLabel> labeledText2CoreLabels(String labeledText);
	
	
	
	
}
