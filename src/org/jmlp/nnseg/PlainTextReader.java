package org.jmlp.nnseg;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.jmlp.file.utils.FileToArray;
import org.jmlp.str.basic.SSO;

public class PlainTextReader {

	public static void plain2label(File plain_file, File label_file) {
		try {
			String[] samples = FileToArray.fileToDimArr(plain_file);
			FileWriter fw = new FileWriter(label_file);
			PrintWriter pw = new PrintWriter(fw);

			String line = "";
			Sentence s = new Sentence();
			String token_line;
			for (int i = 0; i < samples.length; i++) {
				token_line = "";
				//token_line = "BEGIN_TOKEN:1";
				try {
					line = samples[i];
					if (SSO.tioe(line)) {
						continue;
					}

					TOKEN[] seg_tokens = s.seg2tokens(line);
					TOKEN token = null;
					for (int j = 0; j < seg_tokens.length; j++) {
						token = seg_tokens[j];
						token_line += (token.word + ":" + token.label + " ");
					}
					token_line = token_line.trim();
					if (SSO.tioe(token_line)) {
						continue;
					}
					//token_line+=" END_TOKEN:1";
					pw.println(token_line);
				} catch (Exception e) {

				}

			}

			pw.close();
			fw.close();

		} catch (Exception e) {

		}
	}

}
