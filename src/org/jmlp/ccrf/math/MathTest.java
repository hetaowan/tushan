package org.jmlp.ccrf.math;

public class MathTest {

	public static void main(String[] args)
	{
		double[] x={10000,10000,10000};
		
		double expsum=0;
		for(int i=0;i<x.length;i++)
		{
			expsum+=Math.exp(x[i]);
		}
		
		System.out.println("logexpsum:"+Math.log(expsum));
		
		System.out.println("arrayMath.logsum:"+ArrayMath.logSum(x));
	}
}
