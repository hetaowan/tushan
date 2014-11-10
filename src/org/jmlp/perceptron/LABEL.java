package org.jmlp.perceptron;

import java.io.Serializable;

/**
 * 标记的封装
 * @author lq
 *
 */
public class LABEL implements Serializable{
	
   /**
    * label 的编号	，从1开始的整数
    */
   public int index;
   
   /**
    * 多层的label
    * hier_labels依次存储每个层对应的标记编号
    * hier_labels[0]:第一层
    * hier_labels[1]:第二层
    * .....
    * 每层的标记编号从1开始
    */
   public int[] hier_labels;
   
   /**
    * 多层的label
    * hier_labels依次存储各层的标记数目
    * hier_labels[0]:第一层
    * hier_labels[1]:第二层
    * .....
    * 每层的标记编号从1开始
    */
   public int[] hier_labels_num;
   
   /**
    * 层数
    */
   public int hier_height;
   /**
    * 所有类别的数目
    */
   public int label_num;
   
   public LABEL(int index,int label_num)
   {
	   this.index=index;
	   this.label_num=label_num;
   }
   public LABEL(int index,int first_index,int second_index,int third_index, int first_num,int second_num,int third_num, int label_num)
   {
	   this.hier_height=3;
	   this.hier_labels=new int[3];
	   this.hier_labels[0]=first_index;
	   this.hier_labels[1]=second_index;
	   this.hier_labels[2]=third_index;
	   
	   this.hier_labels_num=new int[3];
	   this.hier_labels_num[0]=first_num;
	   this.hier_labels_num[1]=second_num;
	   this.hier_labels_num[2]=third_num;
	   
	   this.index=index;
	   this.label_num=label_num;
   }
   
   public boolean equals_three(LABEL other)
   {
	 if((this.hier_labels[0]==other.hier_labels[0])&&(this.hier_labels[1]==other.hier_labels[1])&&(this.hier_labels[2]==other.hier_labels[2]))
	 {
		 return true;
	 }
	 return false;
   }

  public String toStr()
  {
         String str="frist:"+this.hier_labels[0]+"second:"+this.hier_labels[1]+"third:"+this.hier_labels[2];
         return str;
  }   
}
