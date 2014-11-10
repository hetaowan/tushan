package edu.stanford.common;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.stanford.nlp.ie.crf.CRFCliqueTree;
import edu.stanford.nlp.ie.crf.CRFLabel;
import edu.stanford.nlp.ie.crf.FactorTable;
import edu.stanford.nlp.math.SloppyMath;
import edu.stanford.nlp.optimization.AbstractStochasticCachingDiffUpdateFunction;
import edu.stanford.nlp.util.Index;

/**
 * 
 * @author lq
 */
public class CRFLogConditionalSelectionFunction extends
		AbstractStochasticCachingDiffUpdateFunction {

    public static final int NO_PRIOR = 0;
	public static final int QUADRATIC_PRIOR = 1;
	/* Use a Huber robust regression penalty (L1 except very near 0) not L2 */
	public static final int HUBER_PRIOR = 2;
	public static final int QUARTIC_PRIOR = 3;
	  
	private final int prior;
	private final double sigma;
	private final double epsilon = 0.1; // You can't actually set this at present
	private final String backgroundSymbol;
	  
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

	/**
	 * 村辍所有特征所属的窗口大小，不同窗口大小的特征需要不同数量的参数来刻画
	 */
	private final int[] map;

	/**
	 * 各特征在样本中出现的次数统计
	 */
	private final double[][] Ehat;;

	/***
	 * 不同窗口大小标注的索引例如: lableIndices[0]={[0],[1]} labelIndices[1]={[0 0],[0 1],[1
	 * 0],[1 1]}
	 */
	private final Index<CRFLabel>[] labelIndices;
	
	private final List<int[]> labels;  
	
	/**
	 * 所要优化参数的特征
	 */
	private final String feature;
	
	/**
	 * 所要优化参数的特征的索引
	 */
	private final int fIndex;
	
	/**
	 * 要优化特征所属的窗口大小
	 */
	private final int fSize;
	
	private static Logger logger = Logger.getLogger(CRFLogConditionalSelectionFunction.class);
	
	private ProbTable ptc;

	public CRFLogConditionalSelectionFunction(CRFCliqueTree[] cliqueTrees, Map<String, List<Coordinate>> featureCoordinates,List<int[]> labels, Index featureIndex,  Index<String> classIndex, Index[] labelIndices, int[] map, String backgroundSymbol, double sigma,String feature,int fIndex,int fSize, double[][] Ehat)
	{
		this.cliqueTrees=cliqueTrees;
		this.featureCoordinates=featureCoordinates;
	    this.featureIndex = featureIndex;
	    this.classIndex = classIndex;
	    this.numClasses = classIndex.size();
	    this.labelIndices = labelIndices;
	    this.map = map;
	    this.labels = labels;
	    this.prior = QUADRATIC_PRIOR;
	    this.backgroundSymbol = backgroundSymbol;
	    this.sigma = sigma;
	    this.feature=feature;
	    this.fIndex=fIndex;
	    this.Ehat= Ehat;
	    this.fSize=fSize;
	}
	
	@Override
	public int domainDimension() {
		// TODO Auto-generated method stub
		return SloppyMath.intPow(numClasses, fSize);
	}

	@Override
	public double valueAt(double[] x, double xScale, int[] batch) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double calculateStochasticUpdate(double[] x, double xScale,
			int[] batch, double gain) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void calculateStochastic(double[] x, double[] v, int[] batch) {
		// TODO Auto-generated method stub

	}

	@Override
	public int dataDimension() {
		// TODO Auto-generated method stub
		return 0;
	}

	  
	@Override
	protected void calculate(double[] x) {
		// TODO Auto-generated method stub
		
		double prob = 0.0; // the log prob of the sequence given the model, which is the negation of value at this point
		
		Index labelIndex = labelIndices[fSize-1];
			
		int[] fLabel=new int[fSize];
		
		double[] E=new double[SloppyMath.intPow(numClasses, fSize)];
		//首先构建该feature的FactorTable
		FactorTable ft = new FactorTable(numClasses, fSize);
		for (int k = 0, liSize = labelIndex.size(); k < liSize; k++) {
		        int[] label = ((CRFLabel) labelIndex.get(k)).getLabel();
		        double cliquePotential = Math.exp(x[k]);
		        ft.setValue(label, cliquePotential);
		}
		
		//获得该特征所有出现的坐标
		List<Coordinate> fCoordinates=featureCoordinates.get(feature);
		
		Coordinate coordinate;
		//该坐标的factor table,元素值是labelIndex中各label的边缘概率
		FactorTable ft_margin =null ;
		ProbTable pt=null;
		
		int lk=0;
		for(int i=0;i<fCoordinates.size();i++)
		{
			
			fLabel=new int[fSize];
			lk=0;
			coordinate=fCoordinates.get(i);
			
			for(int k=coordinate.y-fSize+1;k<=coordinate.y;k++)
			{
				//logger.info("coordinate.x:"+coordinate.x+" coordinate.y:"+coordinate.y);	
				//logger.info("labels.get(coordinate.x):"+labels.get(coordinate.x).length);
				if(k>=0)
				fLabel[lk++]=labels.get(coordinate.x)[k];
			}
			
			//参考  CRFLogConditionalObjectiveFunction calculate 计算labelIndex中每个label的概率
			//对于fSize==1的情况，对  ft_margin sumOutFront() 加和左边的label 
			ft_margin=cliqueTrees[coordinate.x].getFactorTables()[coordinate.y];
			
			
			//使ft_margin的大小与ft一致
			for(int j=fSize,lSize=labelIndices.length;j<lSize;j++)
			{
				ft_margin=ft_margin.sumOutFront();
			}	
			
			pt=new ProbTable(numClasses, fSize);
			for (int k = 0, liSize = labelIndex.size(); k < liSize; k++) {
		        int[] label = ((CRFLabel) labelIndex.get(k)).getLabel();
		        double potential = ft.getValue(label)*ft_margin.prob(label);
		        pt.setValue(label, potential);
		    }
			
			prob+=Math.log(ft.getValue(fLabel)/pt.totalMass());
			
			for (int k = 0, liSize = labelIndex.size(); k < liSize; k++) {
		        int[] label = ((CRFLabel) labelIndex.get(k)).getLabel();
		        double p=pt.prob(label);
		        E[k]+=p;
		     }
			
		}
		
		
	    if (Double.isNaN(prob)) { // shouldn't be the case
	        throw new RuntimeException("Got NaN for prob in CRFLogConditionalObjectiveFunction.calculate()" +
	                " - this may well indicate numeric underflow due to overly long documents.");
	      }
		
		
	    value = -prob;
	    
	     logger.debug(" method calculate : value is " + value);
	    
		
	    for (int i = 0; i < E.length; i++) {
	          derivative[i] = (E[i] - Ehat[fIndex][i]); 
	    }
	    
	    
	    // incorporate priors
	    if (prior == QUADRATIC_PRIOR) {
	      double sigmaSq = sigma * sigma;
	      for (int i = 0; i < x.length; i++) {
	        double k = 1.0;
	        double w = x[i];
	        value += k * w * w / 2.0 / sigmaSq;
	        derivative[i] += k * w / sigmaSq;
	      }
	    } else if (prior == HUBER_PRIOR) {
	      double sigmaSq = sigma * sigma;
	      for (int i = 0; i < x.length; i++) {
	        double w = x[i];
	        double wabs = Math.abs(w);
	        if (wabs < epsilon) {
	          value += w * w / 2.0 / epsilon / sigmaSq;
	          derivative[i] += w / epsilon / sigmaSq;
	        } else {
	          value += (wabs - epsilon / 2) / sigmaSq;
	          derivative[i] += ((w < 0.0) ? -1.0 : 1.0) / sigmaSq;
	        }
	      }
	    } else if (prior == QUARTIC_PRIOR) {
	      double sigmaQu = sigma * sigma * sigma * sigma;
	      for (int i = 0; i < x.length; i++) {
	        double k = 1.0;
	        double w = x[i];
	        value += k * w * w * w * w / 2.0 / sigmaQu;
	        derivative[i] += k * w / sigmaQu;
	      }
	    }
	    
	    if (ClassifierCommon.verbosity_level >= ClassifierCommon.DEBUG) {
	       setPtc(pt);
	    }
	    
	}

	public CRFCliqueTree[] getCliqueTrees() {
		return cliqueTrees;
	}


	public Map<String, List<Coordinate>> getFeatureCoordinates() {
		return featureCoordinates;
	}


	public Index<String> getFeatureIndex() {
		return featureIndex;
	}


	public Index<String> getClassIndex() {
		return classIndex;
	}


	public int getNumClasses() {
		return numClasses;
	}


	public double[][] getEhat() {
		return Ehat;
	}


	public Index<CRFLabel>[] getLabelIndices() {
		return labelIndices;
	}


	public String getFeature() {
		return feature;
	}


	public int getfSize() {
		return fSize;
	}


	public List<int[]>  getLabels() {
		return labels;
	}

	public ProbTable getPtc() {
		return ptc;
	}

	public void setPtc(ProbTable ptc) {
		this.ptc = ptc;
	}

}
