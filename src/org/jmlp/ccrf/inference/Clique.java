package org.jmlp.ccrf.inference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.stanford.nlp.math.ArrayMath;
import edu.stanford.nlp.math.SloppyMath;
import edu.stanford.nlp.util.Index;

/**
 * crf 的一个clique,即 crf graph的完全连通子图
 * 
 * @author lq
 */
public class Clique {

	/**
	 * clique所跨越的窗口大小
	 */
	private int windowSize;

	/**
	 * 每个label能够取值的个数
	 */
	private int numClasses;

	/**
	 * 对于每个label的特征数组 =======argue=============== 每个clique的特征与label是无关系的
	 * ====================== 所以修改旧的特征定义 private int[][] features 为下面的新定义
	 * 每个clique的特征与clique的windowSize相关 features.size()==windowSize
	 */
	private ArrayList<List<String>> features = new ArrayList<List<String>>();

	/**
	 * clique 所有可能的label
	 */
	private CCRFLabel[] labels;

	/**
	 * 对应每个label的边缘概率
	 */
	private double[] table;

	/**
	 * inner crf or outer crf 的 clique
	 */
	private int type;

	public Clique(int numClasses, int windowSize) {
		this.numClasses = numClasses;
		this.windowSize = windowSize;

		table = new double[SloppyMath.intPow(numClasses, windowSize)];
		Arrays.fill(table, Double.NEGATIVE_INFINITY);
	}

	public int indexOfLabel(CCRFLabel[] label) {
		// to do
		return 0;
	}

	public CCRFLabel[] getLabels() {
		return labels;
	}

	public void setLabels(CCRFLabel[] labels) {
		this.labels = labels;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public int getNumClasses() {
		return numClasses;
	}

	public void setNumClasses(int numClasses) {
		this.numClasses = numClasses;
	}

	public double[] getTable() {
		return table;
	}

	public void setTable(double[] table) {
		this.table = table;
	}

	public Clique(Clique t) {
		numClasses = t.numClasses();
		windowSize = t.windowSize();
		table = new double[t.size()];
		System.arraycopy(t.table, 0, table, 0, t.size());
	}

	public boolean hasNaN() {
		return ArrayMath.hasNaN(table);
	}

	public String toProbString() {
		StringBuilder sb = new StringBuilder("{\n");
		for (int i = 0; i < table.length; i++) {
			sb.append(Arrays.toString(toArray(i)));
			sb.append(": ");
			sb.append(prob(toArray(i)));
			sb.append("\n");
		}
		sb.append("}");
		return sb.toString();
	}

	public String toNonLogString() {
		StringBuilder sb = new StringBuilder("{\n");
		for (int i = 0; i < table.length; i++) {
			sb.append(Arrays.toString(toArray(i)));
			sb.append(": ");
			sb.append(Math.exp(getValue(i)));
			sb.append("\n");
		}
		sb.append("}");
		return sb.toString();
	}

	public <L> String toString(Index<L> classIndex) {
		StringBuilder sb = new StringBuilder("{\n");
		for (int i = 0; i < table.length; i++) {
			sb.append(toString(toArray(i), classIndex));
			sb.append(": ");
			sb.append(getValue(i));
			sb.append("\n");
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{\n");
		for (int i = 0; i < table.length; i++) {
			sb.append(Arrays.toString(toArray(i)));
			sb.append(": ");
			sb.append(getValue(i));
			sb.append("\n");
		}
		sb.append("}");
		return sb.toString();
	}

	private static <L> String toString(int[] array, Index<L> classIndex) {
		List<L> l = new ArrayList<L>(array.length);
		for (int i = 0; i < array.length; i++) {
			l.add(classIndex.get(array[i]));
		}
		return l.toString();
	}

	public int[] toArray(int index) {
		int[] indices = new int[windowSize];
		for (int i = indices.length - 1; i >= 0; i--) {
			indices[i] = index % numClasses;
			index /= numClasses;
		}
		return indices;
	}

	private int indexOf(int[] entry) {
		int index = 0;
		for (int i = 0; i < entry.length; i++) {
			index *= numClasses;
			index += entry[i];
		}
		// if (index < 0) throw new RuntimeException("index=" + index +
		// " entry=" + Arrays.toString(entry)); // only if overflow
		return index;
	}

	private int indexOf(int[] front, int end) {
		int index = 0;
		for (int i = 0; i < front.length; i++) {
			index *= numClasses;
			index += front[i];
		}
		index *= numClasses;
		index += end;
		return index;
	}

	private int indexOf(int front, int[] end) {
		int index = front;
		for (int i = 0; i < end.length; i++) {
			index *= numClasses;
			index += end[i];
		}
		return index;
	}

	private int[] indicesEnd(int[] entries) {
		int index = 0;
		for (int i = 0; i < entries.length; i++) {
			index *= numClasses;
			index += entries[i];
		}
		int[] indices = new int[SloppyMath.intPow(numClasses, windowSize
				- entries.length)];
		final int offset = SloppyMath.intPow(numClasses, entries.length);
		for (int i = 0; i < indices.length; i++) {
			indices[i] = index;
			index += offset;
		}
		// System.err.println("indicesEnd returning: " +
		// Arrays.toString(indices));
		return indices;
	}

	/**
	 * This now returns the first index of the requested entries. The run of
	 * numClasses ^ (windowSize - entries.length) successive entries will give
	 * all of them.
	 * 
	 * @param entries
	 *            The class indices of size windowsSize
	 * @return First index of requested entries
	 */
	private int indicesFront(int[] entries) {
		int start = 0;
		for (int entry : entries) {
			start *= numClasses;
			start += entry;
		}
		int offset = SloppyMath.intPow(numClasses, windowSize - entries.length);
		return start * offset;
	}

	public int windowSize() {
		return windowSize;
	}

	public int numClasses() {
		return numClasses;
	}

	public int size() {
		return table.length;
	}

	public double totalMass() {
		return ArrayMath.logSum(table);
	}

	/** Returns a single clique potential. */
	public double unnormalizedLogProb(int[] label) {
		return getValue(label);
	}

	public double logProb(int[] label) {
		return unnormalizedLogProb(label) - totalMass();
	}

	public double prob(int[] label) {
		return Math.exp(unnormalizedLogProb(label) - totalMass());
	}

	/**
	 * Computes the probability of the tag OF being at the end of the table
	 * given that the previous tag sequence in table is GIVEN. given is at the
	 * beginning, of is at the end.
	 * 
	 * @return the probability of the tag OF being at the end of the table
	 */
	public double conditionalLogProbGivenPrevious(int[] given, int of) {
		if (given.length != windowSize - 1) {
			throw new IllegalArgumentException(
					"conditionalLogProbGivenPrevious requires given one less than clique size ("
							+ windowSize + ") but was "
							+ Arrays.toString(given));
		}
		// Note: other similar methods could be optimized like this one, but
		// this is the one the CRF uses....
		/*
		 * int startIndex = indicesFront(given); int numCellsToSum =
		 * SloppyMath.intPow(numClasses, windowSize - given.length); double z =
		 * ArrayMath.logSum(table, startIndex, startIndex + numCellsToSum); int
		 * i = indexOf(given, of); System.err.printf(
		 * "startIndex is %d, numCellsToSum is %d, i is %d (of is %d)%n",
		 * startIndex, numCellsToSum, i, of);
		 */
		int startIndex = indicesFront(given);
		// System.out.println("numClasses:"+numClasses);
		double z = ArrayMath.logSum(table, startIndex, startIndex + numClasses);

		int i = startIndex + of;
		// System.err.printf("startIndex is %d, numCellsToSum is %d, i is %d (of is %d)%n",
		// startIndex, numClasses, i, of);

		return table[i] - z;
	}

	// public double conditionalLogProbGivenPreviousForPartial(int[] given, int
	// of) {
	// if (given.length != windowSize - 1) {
	// System.err.println("error computing conditional log prob");
	// System.exit(0);
	// }
	// // int[] label = indicesFront(given);
	// // double[] masses = new double[label.length];
	// // for (int i = 0; i < masses.length; i++) {
	// // masses[i] = table[label[i]];
	// // }
	// // double z = ArrayMath.logSum(masses);
	//
	// int i = indexOf(given, of);
	// // if (SloppyMath.isDangerous(z) || SloppyMath.isDangerous(table[i])) {
	// // System.err.println("z="+z);
	// // System.err.println("t="+table[i]);
	// // }
	//
	// return table[i];
	// }

	/**
	 * Computes the probabilities of the tag at the end of the table given that
	 * the previous tag sequence in table is GIVEN. given is at the beginning,
	 * position in question is at the end
	 * 
	 * @return the probabilities of the tag at the end of the table
	 */
	public double[] conditionalLogProbsGivenPrevious(int[] given) {
		if (given.length != windowSize - 1) {
			throw new IllegalArgumentException(
					"conditionalLogProbsGivenPrevious requires given one less than clique size ("
							+ windowSize + ") but was "
							+ Arrays.toString(given));
		}
		double[] result = new double[numClasses];
		for (int i = 0; i < numClasses; i++) {
			int index = indexOf(given, i);
			result[i] = table[index];
		}
		ArrayMath.logNormalize(result);
		return result;
	}

	/**
	 * Computes the probability of the sequence OF being at the end of the table
	 * given that the first tag in table is GIVEN. given is at the beginning, of
	 * is at the end
	 * 
	 * @return the probability of the sequence of being at the end of the table
	 */
	public double conditionalLogProbGivenFirst(int given, int[] of) {
		if (of.length != windowSize - 1) {
			throw new IllegalArgumentException(
					"conditionalLogProbGivenFirst requires of one less than clique size ("
							+ windowSize + ") but was " + Arrays.toString(of));
		}
		// compute P(given, of)
		int[] labels = new int[windowSize];
		labels[0] = given;
		System.arraycopy(of, 0, labels, 1, windowSize - 1);
		// double probAll = logProb(labels);
		double probAll = unnormalizedLogProb(labels);

		// compute P(given)
		// double probGiven = logProbFront(given);
		double probGiven = unnormalizedLogProbFront(given);

		// compute P(given, of) / P(given)
		return probAll - probGiven;
	}

	/**
	 * Computes the probability of the sequence OF being at the end of the table
	 * given that the first tag in table is GIVEN. given is at the beginning, of
	 * is at the end.
	 * 
	 * @return the probability of the sequence of being at the end of the table
	 */
	public double unnormalizedConditionalLogProbGivenFirst(int given, int[] of) {
		if (of.length != windowSize - 1) {
			throw new IllegalArgumentException(
					"unnormalizedConditionalLogProbGivenFirst requires of one less than clique size ("
							+ windowSize + ") but was " + Arrays.toString(of));
		}
		// compute P(given, of)
		int[] labels = new int[windowSize];
		labels[0] = given;
		System.arraycopy(of, 0, labels, 1, windowSize - 1);
		// double probAll = logProb(labels);
		double probAll = unnormalizedLogProb(labels);

		// compute P(given)
		// double probGiven = logProbFront(given);
		// double probGiven = unnormalizedLogProbFront(given);

		// compute P(given, of) / P(given)
		// return probAll - probGiven;
		return probAll;
	}

	/**
	 * Computes the probability of the tag OF being at the beginning of the
	 * table given that the tag sequence GIVEN is at the end of the table. given
	 * is at the end, of is at the beginning
	 * 
	 * @return the probability of the tag of being at the beginning of the table
	 */
	public double conditionalLogProbGivenNext(int[] given, int of) {
		if (given.length != windowSize - 1) {
			throw new IllegalArgumentException(
					"conditionalLogProbGivenNext requires given one less than clique size ("
							+ windowSize + ") but was "
							+ Arrays.toString(given));
		}
		int[] label = indicesEnd(given);
		double[] masses = new double[label.length];
		for (int i = 0; i < masses.length; i++) {
			masses[i] = table[label[i]];
		}
		double z = ArrayMath.logSum(masses);

		return table[indexOf(of, given)] - z;
	}

	public double unnormalizedLogProbFront(int[] labels) {
		int startIndex = indicesFront(labels);
		int numCellsToSum = SloppyMath.intPow(numClasses, windowSize
				- labels.length);
		// double[] masses = new double[labels.length];
		// for (int i = 0; i < masses.length; i++) {
		// masses[i] = table[labels[i]];
		// }
		return ArrayMath.logSum(table, startIndex, startIndex + numCellsToSum);
	}

	public double logProbFront(int[] label) {
		return unnormalizedLogProbFront(label) - totalMass();
	}

	public double unnormalizedLogProbFront(int label) {
		int[] labels = { label };
		return unnormalizedLogProbFront(labels);
	}

	public double logProbFront(int label) {
		return unnormalizedLogProbFront(label) - totalMass();
	}

	public double unnormalizedLogProbEnd(int[] labels) {
		labels = indicesEnd(labels);
		double[] masses = new double[labels.length];
		for (int i = 0; i < masses.length; i++) {
			masses[i] = table[labels[i]];
		}
		return ArrayMath.logSum(masses);
	}

	public double logProbEnd(int[] labels) {
		return unnormalizedLogProbEnd(labels) - totalMass();
	}

	public double unnormalizedLogProbEnd(int label) {
		int[] labels = { label };
		return unnormalizedLogProbEnd(labels);
	}

	public double logProbEnd(int label) {
		return unnormalizedLogProbEnd(label) - totalMass();
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
		// try{
		table[indexOf(label)] = value;
		// } catch (Exception e) {
		// e.printStackTrace();
		// System.err.println("Table length: " + table.length +
		// " indexOf(label): "
		// + indexOf(label));
		// throw new ArrayIndexOutOfBoundsException(e.toString());
		// // System.exit(1);
		// }
	}

	public void incrementValue(int[] label, double value) {
		incrementValue(indexOf(label), value);
	}

	public void incrementValue(int index, double value) {
		table[index] += value;
	}

	void logIncrementValue(int index, double value) {
		table[index] = SloppyMath.logAdd(table[index], value);
	}

	public void logIncrementValue(int[] label, double value) {
		logIncrementValue(indexOf(label), value);
	}

	public void multiplyInFront(Clique other) {
		int divisor = SloppyMath.intPow(numClasses,
				windowSize - other.windowSize());
		for (int i = 0; i < table.length; i++) {
			table[i] += other.getValue(i / divisor);
		}
	}

	public void multiplyInEnd(Clique other) {
		int divisor = SloppyMath.intPow(numClasses, other.windowSize());
		// System.out.println("divisor:"+divisor);
		for (int i = 0; i < table.length; i++) {
			table[i] += other.getValue(i % divisor);
		}
	}

	public Clique sumOutEnd() {
		Clique ft = new Clique(numClasses, windowSize - 1);
		for (int i = 0, sz = ft.size(); i < sz; i++) {
			ft.table[i] = ArrayMath.logSum(table, i * numClasses, (i + 1)
					* numClasses);
		}
		/*
		 * for (int i = 0; i < table.length; i++) { ft.logIncrementValue(i /
		 * numClasses, table[i]); }
		 */
		return ft;
	}

	public Clique sumOutFront() {
		Clique ft = new Clique(numClasses, windowSize - 1);
		int stride = ft.size();
		for (int i = 0; i < stride; i++) {
			ft.setValue(i, ArrayMath.logSum(table, i, table.length, stride));
		}
		return ft;
	}

	public void divideBy(Clique other) {
		for (int i = 0; i < table.length; i++) {
			if (table[i] != Double.NEGATIVE_INFINITY
					|| other.table[i] != Double.NEGATIVE_INFINITY) {
				table[i] -= other.table[i];
			}
		}
	}

	public static void main(String[] args) {
		int numClasses = 6;
		final int cliqueSize = 3;
		System.err
				.printf("Creating factor table with %d classes and window (clique) size %d%n",
						numClasses, cliqueSize);
		Clique ft = new Clique(numClasses, cliqueSize);

		/**
		 * for (int i = 0; i < 2; i++) { for (int j = 0; j < 2; j++) { for (int
		 * k = 0; k < 2; k++) { int[] a = new int[]{i, j, k};
		 * System.out.print(ft.toString(a)+": "+ft.indexOf(a)); } } } for (int i
		 * = 0; i < 2; i++) { int[] b = new int[]{i};
		 * System.out.print(ft.toString
		 * (b)+": "+ft.toString(ft.indicesFront(b))); } for (int i = 0; i < 2;
		 * i++) { for (int j = 0; j < 2; j++) { int[] b = new int[]{i, j};
		 * System
		 * .out.print(ft.toString(b)+": "+ft.toString(ft.indicesFront(b))); } }
		 * for (int i = 0; i < 2; i++) { int[] b = new int[]{i};
		 * System.out.print(ft.toString(b)+": "+ft.toString(ft.indicesBack(b)));
		 * } for (int i = 0; i < 2; i++) { for (int j = 0; j < 2; j++) { int[] b
		 * = new int[]{i, j}; ft2.setValue(b, (i*2)+j); } } for (int i = 0; i <
		 * 2; i++) { for (int j = 0; j < 2; j++) { int[] b = new int[]{i, j};
		 * System.out.print(ft.toString(b)+": "+ft.toString(ft.indicesBack(b)));
		 * } }
		 * 
		 * System.out.println("##########################################");
		 **/

		for (int i = 0; i < numClasses; i++) {
			for (int j = 0; j < numClasses; j++) {
				for (int k = 0; k < numClasses; k++) {
					int[] b = { i, j, k };
					ft.setValue(b, (i * 4) + (j * 2) + k);
				}
			}
		}

		System.err.println(ft);
		double normalization = 0.0;
		for (int i = 0; i < numClasses; i++) {
			for (int j = 0; j < numClasses; j++) {
				for (int k = 0; k < numClasses; k++) {
					normalization += ft
							.unnormalizedLogProb(new int[] { i, j, k });
				}
			}
		}
		System.err.println("Normalization Z = " + normalization);

		System.err.println(ft.sumOutFront());

		Clique ft2 = new Clique(numClasses, 2);
		for (int i = 0; i < numClasses; i++) {
			for (int j = 0; j < numClasses; j++) {
				int[] b = { i, j };
				ft2.setValue(b, i * numClasses + j);
			}
		}

		System.err.println(ft2);
		// Clique ft3 = ft2.sumOutFront();
		// System.err.println(ft3);

		for (int i = 0; i < numClasses; i++) {
			for (int j = 0; j < numClasses; j++) {
				int[] b = { i, j };
				double t = 0;
				for (int k = 0; k < numClasses; k++) {
					t += Math.exp(ft.conditionalLogProbGivenPrevious(b, k));
					System.err
							.println(k
									+ "|"
									+ i
									+ ","
									+ j
									+ " : "
									+ Math.exp(ft
											.conditionalLogProbGivenPrevious(b,
													k)));
				}
				System.err.println(t);
			}
		}

		System.err.println("conditionalLogProbGivenFirst");
		for (int j = 0; j < numClasses; j++) {
			for (int k = 0; k < numClasses; k++) {
				int[] b = { j, k };
				double t = 0.0;
				for (int i = 0; i < numClasses; i++) {
					t += ft.unnormalizedConditionalLogProbGivenFirst(i, b);
					System.err
							.println(i
									+ "|"
									+ j
									+ ","
									+ k
									+ " : "
									+ ft.unnormalizedConditionalLogProbGivenFirst(
											i, b));
				}
				System.err.println(t);
			}
		}

		System.err.println("conditionalLogProbGivenFirst");
		for (int i = 0; i < numClasses; i++) {
			for (int j = 0; j < numClasses; j++) {
				int[] b = { i, j };
				double t = 0.0;
				for (int k = 0; k < numClasses; k++) {
					t += ft.conditionalLogProbGivenNext(b, k);
					System.err.println(i + "," + j + "|" + k + " : "
							+ ft.conditionalLogProbGivenNext(b, k));
				}
				System.err.println(t);
			}
		}

		numClasses = 2;
		Clique ft3 = new Clique(numClasses, cliqueSize);
		ft3.setValue(new int[] { 0, 0, 0 }, Math.log(0.25));
		ft3.setValue(new int[] { 0, 0, 1 }, Math.log(0.35));
		ft3.setValue(new int[] { 0, 1, 0 }, Math.log(0.05));
		ft3.setValue(new int[] { 0, 1, 1 }, Math.log(0.07));
		ft3.setValue(new int[] { 1, 0, 0 }, Math.log(0.08));
		ft3.setValue(new int[] { 1, 0, 1 }, Math.log(0.16));
		ft3.setValue(new int[] { 1, 1, 0 }, Math.log(1e-50));
		ft3.setValue(new int[] { 1, 1, 1 }, Math.log(1e-50));

		Clique ft4 = ft3.sumOutFront();
		System.err.println(ft4.toNonLogString());
		Clique ft5 = ft3.sumOutEnd();
		System.err.println(ft5.toNonLogString());
	} // end main

	public ArrayList<List<String>> getFeatures() {
		return features;
	}

	public void setFeatures(ArrayList<List<String>> features) {
		this.features = features;
	}

}
