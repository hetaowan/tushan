package org.jmlp.ccrf.inference;

import java.util.regex.Pattern;

/**
 * 一个token，例如一个标点符号、一个汉子、一个英语单词等
 * 
 * @author lq
 */
public class Token {

	public static boolean isToken(String str) {
		if (str.length() == 1)
			return true;
		else if (Pattern.matches("[0-9A-Za-z\\.\\-%]*", str))
			return true;
		else
			return false;
	}

}
