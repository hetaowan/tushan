package org.jmlp.perceptron;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;


/**
 * 更新权重
 * @author lq
 *
 */
public class WeightsUpdate {
	


	public  double[] update_weight(double[] weights,WORD[] words,LABEL y,LABEL yt)
	{
	   double[] nweights=new double[weights.length];
	   for(int i=0;i<weights.length;i++)
	   {
		   nweights[i]=weights[i];
	   }
	   
	   WORD w=null;
	   for(int i=0;i<words.length;i++)
	   {
		   w=words[i];
		   nweights[FeatureIndex.featIndexMap(w, yt)]+=w.count;
		   nweights[FeatureIndex.featIndexMap(w, y)]-=w.count;
                   //nweights[FeatureIndex.featIndexMap(w, yt)]=sum(nweights[FeatureIndex.featIndexMap(w, yt)],w.count);
                   //nweights[FeatureIndex.featIndexMap(w, y)]=sub(nweights[FeatureIndex.featIndexMap(w, y)],w.count);

	   }
	   
		return nweights;
	}
	
	public  double[] update_weight_three(double[] weights,WORD[] words,LABEL y,LABEL yt)
	{
          // System.out.println("in update_weight_three");
	   double[] nweights=new double[weights.length];
	   for(int i=0;i<weights.length;i++)
	   {
		   nweights[i]=weights[i];
	   }
	   
	   WORD w=null;
	   for(int i=0;i<words.length;i++)
	   {
		   w=words[i];
		   nweights[FeatureIndex.featIndexfirstMap(w, yt)]+=w.count;
		   nweights[FeatureIndex.featIndexfirstMap(w, y)]-=w.count;
		   nweights[FeatureIndex.featIndexsecondMap(w, yt)]+=w.count;
		   nweights[FeatureIndex.featIndexsecondMap(w, y)]-=w.count;
		   nweights[FeatureIndex.featIndexthirdMap(w, yt)]+=w.count;
		   nweights[FeatureIndex.featIndexthirdMap(w, y)]-=w.count;
	   }
	   
		return nweights;
	}
	
	/**
	 * 加和两数组，普通方法
	 * @param w1
	 * @param w2
	 * @return
	 */
	public  double[] plain_sum_weight(double[] w1,double[] w2)
	{
  
		double[] sumw=new double[w1.length];
		for(int i=0;i<w1.length;i++)
		{
			sumw[i]=w1[i]+w2[i];
                         //sumw[i]=sum(w1[i],w2[i]);
		}
        
                return sumw;	
	}
	
	/**
	 * 数组元素都除以pnum,普通方法
	 * @param w1
	 * @param pnum
	 * @return
	 */
	public  double[] plain_div_weight(double[] w1,double pnum)
	{
  
		double[] avgw=new double[w1.length];
		for(int i=0;i<w1.length;i++)
		{
			avgw[i]=w1[i]/pnum;
                       //avgw[i]=div(w1[i],pnum,60);
		}

               return avgw;	
	}
	
	
	
	public static double[] regular_weight(double[] w)
	{
		double sum=0;
		double[] nw=new double[w.length];
		for(int i=0;i<w.length;i++)
		{
			sum+=w[i];
                        //sum=sum(sum,w[i]);
		}
		if(sum==0)
		{
			sum=1000000;
		}
		for(int i=0;i<w.length;i++)
		{
			nw[i]=w[i]/sum;
                        //nw[i]=div(w[i],sum,60);
		}
		return nw;
	}
	
	public static double[] regular_weight_abs(double[] w)
	{
		double sum=0;
		double[] nw=new double[w.length];
		for(int i=0;i<w.length;i++)
		{
			sum+=Math.abs(w[i]);
                        // sum=sum(sum,Math.abs(w[i]));
                      
		}
		if(sum==0)
		{
			sum=1000000;
		}
		for(int i=0;i<w.length;i++)
		{
			nw[i]=(w[i]*2)/sum;
                        //nw[i]=div(mul(w[i],2.0),sum,60);
		}
		return nw;
	}


      /** 
      * double 相加 
      * @param d1 
      * @param d2 
      * @return 
      */ 
      public static double sum(double d1,double d2){ 
        BigDecimal bd1 = new BigDecimal(Double.toString(d1)); 
        BigDecimal bd2 = new BigDecimal(Double.toString(d2)); 
        return bd1.add(bd2).doubleValue(); 
     }  
	


    /** 
     * double 相减 
     * @param d1 
     * @param d2 
     * @return 
     */ 
    public static double sub(double d1,double d2){ 
        BigDecimal bd1 = new BigDecimal(Double.toString(d1)); 
        BigDecimal bd2 = new BigDecimal(Double.toString(d2)); 
        return bd1.subtract(bd2).doubleValue(); 
    } 

    /** 
     * double 乘法 
     * @param d1 
     * @param d2 
     * @return 
     */ 
    public static double mul(double d1,double d2){ 
        BigDecimal bd1 = new BigDecimal(Double.toString(d1)); 
        BigDecimal bd2 = new BigDecimal(Double.toString(d2)); 
        return bd1.multiply(bd2).doubleValue(); 
    } 


    /** 
     * double 除法 
     * @param d1 
     * @param d2 
     * @param scale 四舍五入 小数点位数 
     * @return 
     */ 
    public static double div(double d1,double d2,int scale){ 
        //  当然在此之前，你要判断分母是否为0，   
        //  为0你可以根据实际需求做相应的处理 

        BigDecimal bd1 = new BigDecimal(Double.toString(d1)); 
        BigDecimal bd2 = new BigDecimal(Double.toString(d2)); 
        return bd1.divide 
               (bd2,scale,BigDecimal.ROUND_HALF_UP).doubleValue(); 
    } 


}
