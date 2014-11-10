package org.jmlp.str.app;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jmlp.file.utils.FileReaderUtil;

public class RegularSample {

	public void regular(File input_file, File output_file) {
		String content = FileReaderUtil.readContent(input_file);
		Pattern pat = Pattern
				.compile("<Sentiment>([^<>]*?)</Sentiment>[^<>]*?<Rev_text>([^<>]*?)</Rev_text>");
		Matcher mat = pat.matcher(content);
		String label = "";
		String text = "";

		try {
			FileWriter fw = new FileWriter(output_file);
			PrintWriter pw = new PrintWriter(fw);
			while (mat.find()) {
				label = mat.group(1).trim();
				text = mat.group(2).trim();
                pw.println(label+"\001"+"100"+"\001"+text);
			}
		} catch (Exception e) {

		}
	}

	public static void main(String[] args) {

		File input_file=new File("temp/review/c.txt");
		File output_file=new File("temp/review/train.txt");
		RegularSample rs=new RegularSample();
		rs.regular(input_file, output_file);
	}

}
