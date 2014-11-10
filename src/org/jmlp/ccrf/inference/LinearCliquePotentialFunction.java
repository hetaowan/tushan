package org.jmlp.ccrf.inference;

/**
 * @author Mengqiu Wang
 */

public class LinearCliquePotentialFunction implements CliquePotentialFunction {

  double[][] weights;

  LinearCliquePotentialFunction(double[][] weights) {
    this.weights = weights;
  }

  @Override
  public double computeCliquePotential(int cliqueSize, int labelIndex, int[] cliqueFeatures) {
    double output = 0.0;
    for (int m = 0; m < cliqueFeatures.length; m++) {
     //System.out.println("cliqueFeatures["+m+"]="+cliqueFeatures[m]+" labelIndex"+labelIndex+" weight="+ weights[cliqueFeatures[m]][labelIndex]);
      output += weights[cliqueFeatures[m]][labelIndex];
    }
    return output;
  }
  
  
}
