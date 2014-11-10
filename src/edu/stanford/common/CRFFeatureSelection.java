package edu.stanford.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jmlp.sort.utils.SortStrArray;
import org.jmlp.str.basic.SSO;

import edu.stanford.nlp.ie.crf.CRFCliqueTree;
import edu.stanford.nlp.ie.crf.CRFLabel;
import edu.stanford.nlp.ie.crf.CRFLogConditionalObjectiveFunction;
import edu.stanford.nlp.ie.crf.FactorTable;
import edu.stanford.nlp.math.SloppyMath;
import edu.stanford.nlp.optimization.DiffFunction;
import edu.stanford.nlp.optimization.Evaluator;
import edu.stanford.nlp.optimization.Function;
import edu.stanford.nlp.optimization.HasEvaluators;
import edu.stanford.nlp.optimization.HybridMinimizer;
import edu.stanford.nlp.optimization.Minimizer;
import edu.stanford.nlp.optimization.QNMinimizer;
import edu.stanford.nlp.optimization.ResultStoringMonitor;
import edu.stanford.nlp.optimization.SGDMinimizer;
import edu.stanford.nlp.optimization.SGDToQNMinimizer;
import edu.stanford.nlp.optimization.SMDMinimizer;
import edu.stanford.nlp.optimization.ScaledSGDMinimizer;
import edu.stanford.nlp.optimization.StochasticInPlaceMinimizer;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.Index;
import edu.stanford.nlp.util.ReflectionLoading;

/**
 * 对crf特征使用贪婪的算法进行选择，参考 Efficently Inducing Features of Conditional Random
 * Fields
 * 
 * @author lq
 * 
 */
public class CRFFeatureSelection {

	/**
	 * Calibrated CliqueTrees 每个doc一个cliqueTree
	 */
	private final CRFCliqueTree[] cliqueTrees;

	/**
	 * 一个特征所属的doc索引，以及在doc中位置索引
	 */
	private final Map<String, List<Coordinate>> featureCoordinates;

	/**
	 * 存储所有特征的索引，与原CRF模型一致
	 */
	private final Index<String> featureIndex;

	/**
	 * 存储不同的标注
	 */
	private final Index<String> classIndex;

	/**
	 * 不同的标注的个数
	 */
	private final int numClasses;

	private final String backgroundSymbol;

	private final double sigma;

	/**
	 * 村辍所有特征所属的窗口大小，不同窗口大小的特征需要不同数量的参数来刻画
	 */
	private final int[] map;

	/**
	 * 各特征在样本中出现的次数统计
	 */
	private final double[][] Ehat;;

	private final List<int[]> labels;

	/***
	 * 不同窗口大小标注的索引例如: lableIndices[0]={[0],[1]} labelIndices[1]={[0 0],[0 1],[1
	 * 0],[1 1]}
	 */
	private final Index<CRFLabel>[] labelIndices;

	/**
	 * 标识该特征是否被选择，该activeMap用到CRFClassifier
	 * 的documentToDataAndLabels中，不活跃特征不加入到模型中
	 */
	private int[] activeMap;

	private final SeqClassifierFlags flags;

	private final int fIter;

	private double gate = 0;

	private static Logger logger = Logger.getLogger(CRFFeatureSelection.class);

	private int[] selectMap;

	public CRFFeatureSelection(CRFCliqueTree[] cliqueTrees,
			Map<String, List<Coordinate>> featureCoordinates,
			Index<String> featureIndex, Index<String> classIndex, int[] map,
			double[][] Ehat, Index<CRFLabel>[] labelIndices,
			List<int[]> labels, SeqClassifierFlags flags, int[] activeMap,
			int fIter, int[] selectMap) {

		this.cliqueTrees = cliqueTrees;
		this.featureCoordinates = featureCoordinates;
		this.featureIndex = featureIndex;
		this.classIndex = classIndex;
		this.map = map;
		this.Ehat = Ehat;
		this.labels = labels;
		this.labelIndices = labelIndices;
		this.backgroundSymbol = flags.backgroundSymbol;
		this.sigma = flags.sigma;
		this.flags = flags;
		this.activeMap = activeMap;
		this.fIter = fIter;
		this.selectMap = selectMap;
		this.numClasses = classIndex.size();
	}

	public void selection_bat() {

		int n = featureIndex.size() / 50;

		double gain = 0;
		if (fIter == 0) {
			gate = 0.5;
		} else {
			gate = 2.0E-3;
		}
		/*
		 * for(int j=fIter*n;(j<(fIter+1)*n)&&(j<featureIndex.size());j++) {
		 * try{ gain=gainOfFeature(featureIndex.get(j)); } catch(Exception e) {
		 * e.printStackTrace(); }
		 * logger.info("j="+j+" feature:"+featureIndex.get(j)+" gain:"+gain);
		 * if(gain>gate) { activeMap[j]=1; } }
		 */

		for (int j = 0; (j < featureIndex.size()); j++) {

			if (Math.random() > 0.1 || selectMap[j] == 1) {
				continue;
			}

			try {
				gain = gainOfFeature(featureIndex.get(j));
			} catch (Exception e) {
				e.printStackTrace();
			}
			selectMap[j] = 1;

			logger.info("j=" + j + " feature:" + featureIndex.get(j) + " gain:"
					+ gain);
			if (gain > gate) {
				activeMap[j] = 1;
				logger.info("sel feature:" + featureIndex.get(j) + " gain:"
						+ gain);
			}

		}

		/*
		 * String[] sortarr=SortStrArray.sort_List(featureScores, 1, "dou", 2,
		 * "\001"); String[] seg_arr=null; for(int
		 * i=0;i<(sortarr.length/10);i++) { if(SSO.tioe(sortarr[i])) { continue;
		 * } seg_arr=sortarr[i].split("\001"); if(seg_arr.length!=2) { continue;
		 * }
		 * 
		 * activeMap[Integer.parseInt(seg_arr[0])]=1;
		 * logger.info("sel feature:"+sortarr[i]); }
		 */

	}

	public double gainOfFeature(String feature) {
		double gain = 0;

		// 该特征的索引
		int fIndex = 0;
		fIndex = featureIndex.indexOf(feature);

		// 该特征所属窗口的大小
		int fSize = 0;
		fSize = map[fIndex] + 1;

		// 该特征对应窗口内不同label的权重
		double[] weights = new double[SloppyMath.intPow(numClasses, fSize)];
		Arrays.fill(weights, 0.0);

		CRFLogConditionalSelectionFunction func = new CRFLogConditionalSelectionFunction(
				this.cliqueTrees, this.featureCoordinates, this.labels,
				this.featureIndex, this.classIndex, this.labelIndices,
				this.map, this.backgroundSymbol, this.sigma, feature, fIndex,
				fSize, this.Ehat);
		Evaluator[] evaluators = null;

		Minimizer minimizer = getMinimizer(0, evaluators);
		QNMinimizer qnminimizer = (QNMinimizer) minimizer;
		qnminimizer.setisFeatureSelection(true);
		double[] ws = qnminimizer.minimize(func, flags.tolerance, weights);

		gain = -func.valueAt(ws);

		if (ClassifierCommon.verbosity_level >= ClassifierCommon.DEBUG) {
			String EStr = "";
			String PStr = "";

			Index labelIndex = labelIndices[fSize - 1];
			for (int k = 0, liSize = labelIndex.size(); k < liSize; k++) {
				int[] label = ((CRFLabel) labelIndex.get(k)).getLabel();
				double p = func.getPtc().prob(label);
				EStr += (k + " " + Ehat[fIndex][k] + " ");
				PStr += (k + " " + p + " ");
			}

			logger.info("feature:" + feature + "  featureIndex:" + fIndex
					+ " EStr:" + EStr + " PStr:" + PStr);

		}
		
		return gain;
	}

	protected Minimizer getMinimizer(int featurePruneIteration,
			Evaluator[] evaluators) {
		Minimizer minimizer = null;
		if (flags.useQN) {

			int QNmem;
			if (featurePruneIteration == 0) {
				QNmem = flags.QNsize;
			} else {
				QNmem = flags.QNsize2;
			}

			if (flags.interimOutputFreq != 0) {
				Function monitor = new ResultStoringMonitor(
						flags.interimOutputFreq, flags.serializeTo);
				minimizer = new QNMinimizer(monitor, QNmem, flags.useRobustQN);
			} else {
				minimizer = new QNMinimizer(QNmem, flags.useRobustQN);
			}
		} else if (flags.useInPlaceSGD) {
			StochasticInPlaceMinimizer<DiffFunction> sgdMinimizer = new StochasticInPlaceMinimizer<DiffFunction>(
					flags.sigma, flags.SGDPasses, flags.tuneSampleSize);
			if (flags.useSGDtoQN) {
				QNMinimizer qnMinimizer;
				int QNmem;
				if (featurePruneIteration == 0) {
					QNmem = flags.QNsize;
				} else {
					QNmem = flags.QNsize2;
				}
				if (flags.interimOutputFreq != 0) {
					Function monitor = new ResultStoringMonitor(
							flags.interimOutputFreq, flags.serializeTo);
					qnMinimizer = new QNMinimizer(monitor, QNmem,
							flags.useRobustQN);
				} else {
					qnMinimizer = new QNMinimizer(QNmem, flags.useRobustQN);
				}
				minimizer = new HybridMinimizer(sgdMinimizer, qnMinimizer,
						flags.SGDPasses);
			} else {
				minimizer = sgdMinimizer;
			}
		} else if (flags.useSGDtoQN) {
			minimizer = new SGDToQNMinimizer(flags.initialGain,
					flags.stochasticBatchSize, flags.SGDPasses, flags.QNPasses,
					flags.SGD2QNhessSamples, flags.QNsize,
					flags.outputIterationsToFile);
		} else if (flags.useSMD) {
			minimizer = new SMDMinimizer(flags.initialGain,
					flags.stochasticBatchSize, flags.stochasticMethod,
					flags.SGDPasses);
		} else if (flags.useSGD) {
			minimizer = new SGDMinimizer(flags.initialGain,
					flags.stochasticBatchSize);
		} else if (flags.useScaledSGD) {
			minimizer = new ScaledSGDMinimizer(flags.initialGain,
					flags.stochasticBatchSize, flags.SGDPasses,
					flags.scaledSGDMethod);
		} else if (flags.l1reg > 0.0) {
			minimizer = ReflectionLoading
					.loadByReflection(
							"edu.stanford.nlp.optimization.OWLQNMinimizer",
							flags.l1reg);
		}

		if (minimizer instanceof HasEvaluators) {
			((HasEvaluators) minimizer).setEvaluators(flags.evaluateIters,
					evaluators);
		}
		if (minimizer == null) {
			throw new RuntimeException("No minimizer assigned!");
		}

		return minimizer;
	}

	public CRFCliqueTree[] getCliqueTrees() {
		return cliqueTrees;
	}

	public Index<String> getFeatureIndex() {
		return featureIndex;
	}

	public int[] getMap() {
		return map;
	}

	public Index<CRFLabel>[] getLabelIndices() {
		return labelIndices;
	}

	public Map<String, List<Coordinate>> getFeatureCoordinate() {
		return featureCoordinates;
	}

	public int[] getActiveMap() {
		return activeMap;
	}

	public void setActiveMap(int[] activeMap) {
		this.activeMap = activeMap;
	}

	public double[][] getEhat() {
		return Ehat;
	}

	public Index<String> getClassIndex() {
		return classIndex;
	}

}
