package org.jmlp.perceptron;

/**
 * 根据word 和  label 返回特征编号
 * @author lq
 *
 */
public class FeatureIndex {

	/**
	 * 根据word 和  label 返回特征编号
	 * @param word
	 * @param label
	 * @return feat index
	 */
	public static int featIndexMap(WORD word,LABEL label)
	{
		int index;
		//index=(word.index-1)+(label.index-1)*label.label_num;
		index=(label.index-1)+(word.index-1)*(label.label_num);
		return index;
	}
	
	/**
	 * 根据word 和  label 返回特征编号
	 * @param word
	 * @param label
	 * @return feat index
	 */
	public static int featIndexNoWordMap(int index,LABEL label)
	{
		int index_map;
		//index=(word.index-1)+(label.index-1)*label.label_num;
		index_map=(label.index-1)+(index-1)*(label.label_num);
		return index_map;
	}
	
	/**
	 * 根据word 和  label 返回第一层特征编号
	 * @param word
	 * @param label
	 * @return feat index
	 */
	public static int featIndexfirstMap(WORD word,LABEL label)
	{
		int index;
		//index=(word.index-1)+(label.index-1)*label.label_num;
		index=(label.hier_labels[0]-1)+(word.index-1)*(label.hier_labels_num[0]+label.hier_labels_num[1]+label.hier_labels_num[2]);
		return index;
	}
	
	/**
	 * 根据word 和  label 返回第二层特征编号
	 * @param word
	 * @param label
	 * @return feat index
	 */
	public static int featIndexsecondMap(WORD word,LABEL label)
	{
		int index;
		//index=(word.index-1)+(label.index-1)*label.label_num;
		index=(label.hier_labels[1]-1)+label.hier_labels_num[0]+(word.index-1)*(label.hier_labels_num[0]+label.hier_labels_num[1]+label.hier_labels_num[2]);
		return index;
	}
	
	/**
	 * 根据word 和  label 返回第三层特征编号
	 * @param word
	 * @param label
	 * @return feat index
	 */
	public static int featIndexthirdMap(WORD word,LABEL label)
	{
		int index;
		//index=(word.index-1)+(label.index-1)*label.label_num;
		index=(label.hier_labels[2]-1)+label.hier_labels_num[0]+label.hier_labels_num[1]+(word.index-1)*(label.hier_labels_num[0]+label.hier_labels_num[1]+label.hier_labels_num[2]);
		return index;
	}
	
	
	
	/**
	 * 根据word 和  label 返回特征编号
	 * @param word
	 * @param label
	 * @return feat index
	 */
	public static int featIndexNoWordFirstMap(int index,LABEL label)
	{
		int index_map;
		//index=(word.index-1)+(label.index-1)*label.label_num;
		//index_map=(label.index-1)+(index-1)*(label.label_num);
		index_map=(label.hier_labels[0]-1)+(index-1)*(label.hier_labels_num[0]+label.hier_labels_num[1]+label.hier_labels_num[2]);
		return index_map;
	}
	
	/**
	 * 根据word 和  label 返回特征编号
	 * @param word
	 * @param label
	 * @return feat index
	 */
	public static int featIndexNoWordSecondMap(int index,LABEL label)
	{
		int index_map;
		//index=(word.index-1)+(label.index-1)*label.label_num;
		//index_map=(label.index-1)+(index-1)*(label.label_num);
		index_map=(label.hier_labels[1]-1)+label.hier_labels_num[0]+(index-1)*(label.hier_labels_num[0]+label.hier_labels_num[1]+label.hier_labels_num[2]);
		return index_map;
	}
	
	/**
	 * 根据word 和  label 返回特征编号
	 * @param word
	 * @param label
	 * @return feat index
	 */
	public static int featIndexNoWordThirdMap(int index,LABEL label)
	{
		int index_map;
		//index=(word.index-1)+(label.index-1)*label.label_num;
		//index_map=(label.index-1)+(index-1)*(label.label_num);
		index_map=(label.hier_labels[2]-1)+label.hier_labels_num[0]+label.hier_labels_num[1]+(index-1)*(label.hier_labels_num[0]+label.hier_labels_num[1]+label.hier_labels_num[2]);
		return index_map;
	}
	
}
