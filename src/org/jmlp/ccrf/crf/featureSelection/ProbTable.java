package org.jmlp.ccrf.crf.featureSelection;

import java.util.Arrays;

import edu.stanford.nlp.math.ArrayMath;
import edu.stanford.nlp.math.SloppyMath;

/**
 * probabilistic table
 * 
 * @author lq
 */
public class ProbTable {

	private final int numClasses;
	private final int windowSize;

	private final double[] table;

	public ProbTable(int numClasses, int windowSize) {
		this.numClasses = numClasses;
		this.windowSize = windowSize;

		table = new double[SloppyMath.intPow(numClasses, windowSize)];
		Arrays.fill(table, 0);
	}

	private int indexOf(int[] entry) {
		int index = 0;
		for (int i = 0; i < entry.length; i++) {
			index *= numClasses;
			index += entry[i];
		}
		return index;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public double getValue(int index) {
		return table[index];
	}

	public double getValue(int[] label) {
		return table[indexOf(label)];
	}

	public void setValue(int index, double value) {
		table[index] = value;
	}

	public void setValue(int[] label, double value) {
		table[indexOf(label)] = value;
	}

	public double totalMass() {
		return ArrayMath.sum(table);
	}

	public double prob(int[] label) {
		return getValue(label) / totalMass();
	}

}
