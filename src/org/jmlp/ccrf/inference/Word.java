package org.jmlp.ccrf.inference;

/**
 * 单词，可以表示一个单词(token)、也可以指多个单词(token)
 * @author lq
 *
 */
public class Word {

	/**
	 * token的字面值
	 */
	private String value;
	 
	/**
	 * token的各个label
	 */
	private int label;
	
	/**
	 * token的个数
	 */
	private int length;
	
	public Word(String value,int label,int length)
	{
		this.value=value;
		this.label=label;
		this.length=length;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}
	
	public String toString()
	{
		String str="";
		str="["+value+","+label+","+length+"]";
		return str;
	}
}
