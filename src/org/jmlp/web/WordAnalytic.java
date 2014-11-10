package org.jmlp.web;

import java.util.ArrayList;

/**
 * 输入某个单词
 * 进行一系列处理
 * 生成某种输出
 * @author lq
 *
 */
public abstract class WordAnalytic {

	private int proxy=0;
	
	public abstract ArrayList<String> getRevWords();
	/**
	 * 初始化word 分析 所用的数据配置
	 */
	public abstract void init();
	
	/**
	 * 对单词进行解析，结果存入WordResult中
	 * @param word
	 * @return
	 */
	public abstract WordResult parse(String word);
	
	/**
	 * 释放解析程序所用的内存空间
	 */
	public abstract void destroy();

	public int getProxy() {
		return proxy;
	}

	public void setProxy(int proxy) {
		this.proxy = proxy;
	}
	
	
	
}
