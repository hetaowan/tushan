package org.jmlp.perceptron;

import java.io.Serializable;

/**
 * 参数的封装
 * @author lq
 *
 */
public class LEARNPARM implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * BeamDecoder 的类型
	 * 
	 */
	public String bd_type;
	
	/**
	 * label 的数目
	 */
	public int label_num;
	
	/**
	 * 第一层 label 的数目
	 */
	public int first_label_num;
	
	/**
	 * 第二层 label 的数目
	 */
	public int second_label_num;
	
	/**
	 * 第三层 label 的数目
	 */
	public int third_label_num;
	
	/**
	 * beam search 的best 数目
	 */
	public int top_num;
	
	public String docid;
	
	public LEARNPARM()
	{
		
	}
	public LEARNPARM(String bd_type,int label_num)
	{
		this.bd_type=bd_type;
		this.label_num=label_num;
	}
	
	
	
}
