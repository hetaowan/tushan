package org.jmlp.ccrf.inference;

import java.util.Map;

import org.jmlp.ccrf.optimization.AbstractStochasticCachingDiffUpdateFunction;

/**
 * 封装计算ccrf object function 的piecewise likelihood 近似 需要的各种数据、结构、方法
 * 这个类和标记好的样本集合一一对应
 * @author lq
 */
public abstract class AbstractCCRFPiecewiseObjectiveFunction extends
AbstractStochasticCachingDiffUpdateFunction{
	
	private Map<String,Feature> features;
	
	private Index<String> featureIndex;

	//该函数对应的句子或片断
	private Sentence[] sentences;

	public Map<String,Feature> getFeatures() {
		return features;
	}

	public void setFeatures(Map<String,Feature> features) {
		this.features = features;
	}

	public Index<String> getFeatureIndex() {
		return featureIndex;
	}

	public void setFeatureIndex(Index<String> featureIndex) {
		this.featureIndex = featureIndex;
	}

	public Sentence[] getSentences() {
		return sentences;
	}

	public void setSentences(Sentence[] sentences) {
		this.sentences = sentences;
	}




		
}
