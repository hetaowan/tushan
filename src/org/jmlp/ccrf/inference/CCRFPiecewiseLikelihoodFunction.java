package org.jmlp.ccrf.inference;

/**
 * ccrf object function 的  piecewise likelihood 形式,即 partition function 是针对每个样本中一个句子的一部分(片断)，而不是整个句子
 * 一个特征样本频率和期望的计算分散每个片断上进行
 * 综合所有sentence 的 innerCliques 和 outerCliques 计算出每个特征的在当前参数下的期望   
 * @author lq
 *
 */
public class CCRFPiecewiseLikelihoodFunction extends AbstractCCRFPiecewiseObjectiveFunction{

	@Override
	public int domainDimension() {
		// TODO Auto-generated method stub
		return 0;
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
		
	}

}
