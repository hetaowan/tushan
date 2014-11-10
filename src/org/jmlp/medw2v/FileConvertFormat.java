package org.jmlp.medw2v;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.jmlp.file.utils.FileReaderUtil;
import org.jmlp.file.utils.FileToArray;
import org.jmlp.str.basic.SSO;
import org.jmlp.math.random.RandomGen;

/**
 * 转换文件格式到标准样本
 * 
 * @author lq
 * 
 */
public class FileConvertFormat {

	private StanfordSeg stanfseg;
	private StanfordTag stanftag;
	// **stanf or ansj
	public String seg_type = "ansj";
	public boolean isTag = false;
	public boolean isFilter=false;
	private AnsjSeg ansjseg;
	public HashMap<String, HashMap<String, Integer>> cateWords = new HashMap<String, HashMap<String, Integer>>();

	public HashMap<String, Integer> selWords = new HashMap<String, Integer>();

	public static void convertFormat(String input_file, String output_file)
			throws Exception {

	}

	public FileConvertFormat() {
		String seg_dict_file = "temp/seg_test/five_dict_uniq.txt";
		String stop_dict_file = "temp/seg_test/cn_stop_words_utf8.txt";
		HashMap<String, String> seg_dict = FileReaderUtil
				.file2Hash(seg_dict_file);
		HashMap<String, String> stop_dict = FileReaderUtil
				.file2Hash(stop_dict_file);
		if (seg_type.equals("stanf")) {
			stanfseg = new StanfordSeg();
			stanfseg.setSeg_dict(seg_dict);
			stanfseg.setStop_dict(stop_dict);
			stanfseg.load_model();
		} else {
			ansjseg = new AnsjSeg();
			ansjseg.setSeg_dict(seg_dict);
			ansjseg.setStop_dict(stop_dict);
		}
		if (isTag == true) {
			stanftag = new StanfordTag();
			//stanftag.load_model();
		}
		/*
		 * ansjseg = new AnsjSeg();
		 * 
		 * HashMap<String, String> seg_dict = FileReaderUtil
		 * .file2Hash(seg_dict_file); HashMap<String, String> stop_dict =
		 * FileReaderUtil .file2Hash(stop_dict_file);
		 * ansjseg.setSeg_dict(seg_dict); ansjseg.setStop_dict(stop_dict);
		 */
	}

	public static void convertFormatDictLabel(File input_file,
			File output_file, String label_file) throws Exception {

		HashMap<String, String> label_map = FileReaderUtil
				.getHashFromPlainFile(label_file);
		String[] samples = FileToArray.fileToDimArr(input_file);
		FileWriter fw = new FileWriter(output_file);
		PrintWriter pw = new PrintWriter(fw);

		String line = "";
		String label = "";
		String docid = "";
		String text = "";
		String segtext = "";
		String[] seg_arr = null;
		String rankey = "";
		String sam_line = "";
		String label_index = "";
		for (int i = 0; i < samples.length; i++) {
			line = samples[i];
			seg_arr = line.split("\001");
			if (seg_arr.length < 3) {
				continue;
			}
			label = seg_arr[0];
			label_index = label_map.get(label);
			if (label_index == null) {
				continue;
			}
			docid = seg_arr[1];
			text = seg_arr[2];
			segtext = seg(text);
			rankey = RandomGen.RandomString(20);
			sam_line = rankey + "\001" + label_index + "\001" + docid + "\001"
					+ segtext;
			pw.println(sam_line);
		}

		fw.close();
		pw.close();

	}

	public void readcatewords(String inputdir) {
		File dir = new File(inputdir);
		File[] catefiles = dir.listFiles();
		String fn = "";
		String[] samples = null;
		String line = "";
		String[] seg_arr = null;
		String word = "";
		try {
			for (int i = 0; i < catefiles.length; i++) {
				fn = catefiles[i].getName();
				fn = fn.replace("\\.txt", "");
				// HashMap<String,Integer> tempMap=new
				// HashMap<String,Integer>();
				System.out.println("fn:" + fn);
				samples = FileToArray.fileToDimArr(catefiles[i]);
				for (int j = 0; j < samples.length; j++) {
					line = samples[j];
					if (SSO.tioe(line)) {
						continue;
					}
					line = line.trim();
					seg_arr = line.split("\\s+");
					for (int k = 0; k < seg_arr.length; k++) {
						word = seg_arr[k].trim();
						if (!(selWords.containsKey(word))) {
							selWords.put(word, 1);
						}
					}
				}

				// cateWords.put(fn, tempMap);
			}

		} catch (Exception e) {

		}

	}

	public void convertFormatSupervise(File input_file, File output_file) {
		try {
			String[] samples = FileToArray.fileToDimArr(input_file);
			FileWriter fw = new FileWriter(output_file);
			PrintWriter pw = new PrintWriter(fw);

			String line = "";
			String label = "";
			String docid = "";
			String text = "";
			String segtext = "";
			String[] seg_arr = null;
			String sam_line = "";
			String filter_text = "";
			String tag_text="";
            String noun_text="";
            String post_text="";
			for (int i = 0; i < samples.length; i++) {
				try {
					line = samples[i];
					seg_arr = line.split("\001");
					if (seg_arr.length < 3) {
						continue;
					}
					label = seg_arr[0];
					docid = seg_arr[1];
					text = seg_arr[2];
					if(seg_type.equals("stanf"))
					{
						segtext = stanfseg.seg(text);
					}
					else
					{
						segtext = ansjseg.seg(text);
					}
					
					if(isTag==true)
					{
						//tag_text=stanftag.tag(segtext);
						//noun_text=stanftag.noun_text(tag_text);
						post_text=noun_text;
					}
					else
					{
						post_text=segtext;
					}
					
					if(isFilter==true)
					{
						post_text=filternoise(label,post_text);
					}
					
					sam_line = label + "\001" + post_text;
					/*
					 * filter_text=filternoise(label,segtext);
					 * if(SSO.tioe(filter_text)) { continue; }
					 */
					
					
					
					pw.println(sam_line);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}

			fw.close();
			pw.close();
		} catch (Exception e) {
		}

	}

	public String filternoise(String label, String segtext) {
		if (SSO.tioe(segtext)) {
			return "";
		}
		String[] seg_arr = segtext.split("\\s+");

		String word = "";
		// HashMap<String,Integer> tempMap=cateWords.get(label);
		String filter_text = "";
		int wn = 0;
		for (int i = 0; i < seg_arr.length; i++) {
			word = seg_arr[i].trim();
			if (selWords.containsKey(word)) {
				filter_text += (word + " ");
				wn++;
			}
		}

		filter_text = filter_text.trim();

		if (wn < 3) {
			return "";
		}
		return filter_text;
	}

	public void convertFormatSuperviseNounText(File input_file, File output_file) {
		try {
			String[] samples = FileToArray.fileToDimArr(input_file);
			FileWriter fw = new FileWriter(output_file);
			PrintWriter pw = new PrintWriter(fw);

			String line = "";
			String label = "";
			String docid = "";
			String text = "";
			String segtext = "";
			String[] seg_arr = null;
			String sam_line = "";
			String tagtext = "";
			String nountext = "";
			for (int i = 0; i < samples.length; i++) {
				try {
					line = samples[i];
					seg_arr = line.split("\001");
					if (seg_arr.length < 3) {
						continue;
					}
					label = seg_arr[0];
					docid = seg_arr[1];
					text = seg_arr[2];
					segtext = stanfseg.seg(text);
					// tagtext=stanftag.tag(segtext);
					// nountext=stanftag.noun_text(tagtext);
					if (SSO.tioe(nountext)) {
						continue;
					}
					sam_line = label + "\001" + segtext;
					pw.println(sam_line);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}

			fw.close();
			pw.close();
		} catch (Exception e) {
		}

	}

	public void convertFormatSuperviseTrTe(File train_file, File test_file,
			File output_file) {
		try {
			String[] samples = FileToArray.fileToDimArr(train_file);
			FileWriter fw = new FileWriter(output_file);
			PrintWriter pw = new PrintWriter(fw);

			String line = "";
			String label = "";
			String docid = "";
			String text = "";
			String segtext = "";
			String[] seg_arr = null;
			String sam_line = "";
			String filter_text = "";
			String tag_text="";
            String noun_text="";
            String post_text="";
			for (int i = 0; i < samples.length; i++) {
				try {
					line = samples[i];
					seg_arr = line.split("\001");
					if (seg_arr.length < 3) {
						continue;
					}
					label = seg_arr[0];
					docid = seg_arr[1];
					text = seg_arr[2];
					if(seg_type.equals("stanf"))
					{
						segtext = stanfseg.seg(text);
					}
					else
					{
						segtext = ansjseg.seg(text);
					}
					
					if(isTag==true)
					{
						//tag_text=stanftag.tag(segtext);
						//noun_text=stanftag.noun_text(tag_text);
						post_text=noun_text;
					}
					else
					{
						post_text=segtext;
					}
					
					if(isFilter==true)
					{
						post_text=filternoise(label,post_text);
					}
					
					sam_line = label + "\001" + post_text;
					pw.println(sam_line);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}

			String[] testSamples = FileToArray.fileToDimArr(test_file);

			for (int i = 0; i < testSamples.length; i++) {
				try {
					line = testSamples[i];
					seg_arr = line.split("\001");
					if (seg_arr.length < 3) {
						continue;
					}
					label = seg_arr[0];
					docid = seg_arr[1];
					text = seg_arr[2];
					segtext = stanfseg.seg(text);
					if(seg_type.equals("stanf"))
					{
						segtext = stanfseg.seg(text);
					}
					else
					{
						segtext = ansjseg.seg(text);
					}
					
					if(isTag==true)
					{
						//tag_text=stanftag.tag(segtext);
						//noun_text=stanftag.noun_text(tag_text);
						post_text=noun_text;
					}
					else
					{
						post_text=segtext;
					}
					
					if(isFilter==true)
					{
						post_text=filternoise(label,post_text);
					}
					sam_line = label + "\001" + post_text;
					pw.println(sam_line);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}

			fw.close();
			pw.close();
		} catch (Exception e) {
		}

	}

	public static void convertFormatDictLabelThree(File input_file,
			File output_file, String label_file) throws Exception {

		HashMap<String, String> label_map = FileReaderUtil
				.getHashFromPlainFile(label_file);
		String[] samples = FileToArray.fileToDimArr(input_file);
		FileWriter fw = new FileWriter(output_file);
		PrintWriter pw = new PrintWriter(fw);

		String line = "";
		String label = "";
		String docid = "";
		String text = "";
		String segtext = "";
		String[] seg_arr = null;
		String rankey = "";
		String sam_line = "";
		String label_index = "";
		for (int i = 0; i < samples.length; i++) {
			line = samples[i];
			seg_arr = line.split("\001");
			if (seg_arr.length < 3) {
				continue;
			}
			label = seg_arr[0];
			label_index = label_map.get(label);
			if (label_index == null) {
				continue;
			}
			docid = seg_arr[1];
			text = seg_arr[2];
			text = text.replaceAll("\\s+", "");
			segtext = seg(text);
			rankey = RandomGen.RandomString(20);
			if ((SSO.tioe(label_index)) || (SSO.tioe(segtext))) {
				continue;
			}
			sam_line = rankey + "\001" + label_index + "\001" + docid + "\001"
					+ segtext;
			pw.println(sam_line);
		}

		fw.close();
		pw.close();

	}

	public static String seg(String text) {
		String segs = "";
		List<Term> parse2 = ToAnalysis.parse(text);
		StringBuilder sb = new StringBuilder();
		for (Term term : parse2) {
			sb.append(term.getName());
			sb.append(" ");
		}

		segs = sb.toString();
		segs = segs.trim();
		return segs;
	}

}
