package org.jmlp.classify.svm_struct.source;

import java.math.BigDecimal;

public class WU {

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
