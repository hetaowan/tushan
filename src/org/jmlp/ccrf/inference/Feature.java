package org.jmlp.ccrf.inference;

public class Feature {

	private String featString;
	
	private CCRFLabel[] labels;
	
	private double[] weights;

	public String getFeatString() {
		return featString;
	}

	public void setFeatString(String featString) {
		this.featString = featString;
	}

	public CCRFLabel[] getLabels() {
		return labels;
	}

	public void setLabels(CCRFLabel[] labels) {
		this.labels = labels;
	}

	public double[] getWeights() {
		return weights;
	}

	public void setWeights(double[] weights) {
		this.weights = weights;
	}
	
}
