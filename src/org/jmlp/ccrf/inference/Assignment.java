package org.jmlp.ccrf.inference;

/**
 * sequence label 的一个赋值
 * @author lq
 *
 */
public class Assignment {
	
	/**
	 * sequence 的 赋值label
	 */
	private int[] label;
	
	/**
	 * sequence 在该赋值下的未正规化概率，可能存在sequence 计算的值过大的问题
	 */
	private double unnormalizedLogProb;
	
	/**
	 * sequence 在该赋值下的边缘概率
	 */
	private double prob;
	
	private CCRFCliqueTree<CCRFLabel> outerCliqueTree;

	public int[] getLabel() {
		return label;
	}

	public void setLabel(int[] label) {
		this.label = label;
	}

	public double getUnnormalizedLogProb() {
		return unnormalizedLogProb;
	}

	public void setUnnormalizedLogProb(double unnormalizedLogProb) {
		this.unnormalizedLogProb = unnormalizedLogProb;
	}

	public CCRFCliqueTree<CCRFLabel> getOuterCliqueTree() {
		return outerCliqueTree;
	}

	public void setOuterCliqueTree(CCRFCliqueTree<CCRFLabel> outerCliqueTree) {
		this.outerCliqueTree = outerCliqueTree;
	}

	public double getProb() {
		return prob;
	}

	public void setProb(double prob) {
		this.prob = prob;
	}


	
	

}
