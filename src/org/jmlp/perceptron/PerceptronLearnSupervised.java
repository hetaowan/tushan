package org.jmlp.perceptron;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;

import org.jmlp.file.utils.FileToArray;
import org.jmlp.medw2v.Medw2vParms;
import org.jmlp.str.basic.SSO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerceptronLearnSupervised {
	public double[] weights;
	public LABEL[] worst_label;
	static Logger logger = LoggerFactory.getLogger(PerceptronLearn.class);
	public double sum_precision=0;
	public int test_num=0;
    public int looptest=0;
    public int eff_start_loop=50;
	
	public void learnModel(File input_file, LABEL[] local_label_set,
			int word_num, File test_file) throws Exception {
		String[] sample = null;
		sample = FileToArray.fileToDimArr(input_file);
		LEARNPARM learn_parms = new LEARNPARM();
		learn_parms.bd_type = "plnbdns";
		learn_parms.label_num = local_label_set.length;
		weights = new double[(word_num + 1) * (learn_parms.label_num)];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = 0;
		}

		int correct_num = 0;
		int incorrect_num = 0;

		PlainBeamDecoderNoSearch bd = new PlainBeamDecoderNoSearch();
		StrToClass sc = new StrToClass();
		/*
		 * LABEL[] local_label_set = new LABEL[learn_parms.label_num]; for (int
		 * i = 1; i <= learn_parms.label_num; i++) { local_label_set[i - 1] =
		 * sc.str2label(i + "", learn_parms); }
		 */
		WeightsCut wc = new WeightsCut();
		WeightsUpdate wu = new WeightsUpdate();

		// String test_file="";
		String[] test_samples = FileToArray.fileToDimArr(test_file);

		for (int loop = 0; loop < 100; loop++) {
			incorrect_num = 0;
			correct_num = 0;
			for (int ll = 0; ll < sample.length; ll++) {
				// logger.info("ll="+ll);
				String[] seg_arr = sample[ll].split("\001");
				if ((seg_arr == null) || (seg_arr.length != 2)) {
					continue;
				}
				String did = "100";
				String label = seg_arr[0];
				// String lwo =
				// "13:1 133:3 277:3 624:3 659:3 938:3 1393:58 1395:3 1405:3 1461:3 1684:3 1839:3 2228:3 2941:3 3248:3 8260:3 26658:3";
				String lwo = seg_arr[1].trim();
				WORD[] words = sc.str2words(lwo);
				ShuffleArray.shuffle(words);

				// for (int l = 1; l < 10; l++) {
				learn_parms.docid = did;
				System.out.println("ll=" + ll);
				FEATSET best_fset = bd.beam_search_nocut(words,weights,
						local_label_set, learn_parms);
				//System.out.println("finish beam search");
				worst_label[Integer.parseInt(did)] = bd.worst_feat.label;
				LABEL yt = sc.str2label(label, learn_parms);
				did = did.trim();

				if (yt.index == best_fset.label.index) {
					correct_num++;
				} else {
					// weights=WeightsUpdate.regular_weight(weights);
					// PrintUtil.printNoZero(weights);
					weights = wu.update_weight(weights, words, best_fset.label,
							yt);
					incorrect_num++;
					// weights = WeightsUpdate.regular_weight_abs(weights);
					// PrintUtil.printNoZero(weights);

				}
				// }
			}

			double acc = ((double) (correct_num))
					/ ((double) (correct_num + incorrect_num));
			logger.info("loop=" + loop);
			logger.info("correct_num=" + correct_num);
			logger.info("incorrect_num=" + incorrect_num);
			logger.info("acc=" + acc);
		}
		predict_accurate(weights, test_samples, local_label_set, learn_parms);

	}

	public void learnModelStandalone(File input_file, File label_file,
			int word_num, File test_file) throws Exception {
		// String[] sample = null;
		// sample = FileToArray.fileToDimArr(input_file);
		FileReader fr = null;
		FileInputStream fis = null;
		InputStreamReader isr = null;

		BufferedReader br = null;
		try {
			// fr=new FileReader(input_file);
			fis = new FileInputStream(input_file.getAbsolutePath());
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);

		} catch (Exception e) {
		}

		LEARNPARM learn_parms = new LEARNPARM();
		learn_parms.bd_type = "plnbdns";

		String[] labels = FileToArray.fileToDimArr(label_file);

		learn_parms.label_num = labels.length;
		weights = new double[(word_num + 1) * (learn_parms.label_num)];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = 0;
		}

		int correct_num = 0;
		int incorrect_num = 0;

		PlainBeamDecoderNoSearch bd = new PlainBeamDecoderNoSearch();
		StrToClass sc = new StrToClass();

		LABEL[] local_label_set = new LABEL[learn_parms.label_num];
		for (int i = 1; i <= labels.length; i++) {
			local_label_set[i - 1] = sc.str2label(labels[i-1], learn_parms);
		}

		WeightsCut wc = new WeightsCut();
		WeightsUpdate wu = new WeightsUpdate();

		// String test_file="";
		String[] test_samples = FileToArray.fileToDimArr(test_file);
		String sample_line = "";

		for (int loop = 0; loop < 150; loop++) {

			incorrect_num = 0;
			correct_num = 0;
			if (loop > 0) {
				try {
					// fr=new FileReader(input_file);
					fis = new FileInputStream(input_file.getAbsolutePath());
					isr = new InputStreamReader(fis);
					br = new BufferedReader(isr);
				} catch (Exception e) {
				}
			}
			int ll = 0;
			while ((sample_line = br.readLine()) != null) {
				ll++;
				// logger.info("ll="+ll);
				String[] seg_arr = sample_line.split("\001");
				if ((seg_arr == null) || (seg_arr.length != 2)) {
					continue;
				}
				String did = "100";
				String label = seg_arr[0];
				// String lwo =
				// "13:1 133:3 277:3 624:3 659:3 938:3 1393:58 1395:3 1405:3 1461:3 1684:3 1839:3 2228:3 2941:3 3248:3 8260:3 26658:3";
				String lwo = seg_arr[1].trim();
				WORD[] words = sc.str2words(lwo);
				//ShuffleArray.shuffle(words);

				// for (int l = 1; l < 10; l++) {
				learn_parms.docid = did;
		
				FEATSET best_fset = bd.beam_search_nocut(words,weights,
						local_label_set, learn_parms);
				//System.out.println("finish beam search");
				//worst_label[Integer.parseInt(did)] = bd.worst_feat.label;
				LABEL yt = sc.str2label(label, learn_parms);
				did = did.trim();
				//if(ll%100==0)
				//{
				  System.out.println("ll=" + ll);
				//}
				if (yt.index == best_fset.label.index) {
					System.out.println("right");
					correct_num++;
				} else {
					// weights=WeightsUpdate.regular_weight(weights);
					// PrintUtil.printNoZero(weights);
					System.out.println("wrong");
					weights = wu.update_weight(weights, words, best_fset.label,
							yt);
					incorrect_num++;
					// weights = WeightsUpdate.regular_weight_abs(weights);
					// PrintUtil.printNoZero(weights);

				}
				// }
			}

			double acc = ((double) (correct_num))
					/ ((double) (correct_num + incorrect_num));
			logger.info("loop=" + loop);
			logger.info("correct_num=" + correct_num);
			logger.info("incorrect_num=" + incorrect_num);
			logger.info("acc=" + acc);
			System.out.println("loop=" + loop);
			System.out.println("correct_num=" + correct_num);
			System.out.println("incorrect_num=" + incorrect_num);
            System.out.println("acc=" + acc);
        	predict_accurate(weights, test_samples, local_label_set, learn_parms);
		}
		System.out.println("test_num:"+test_num);
		System.out.println("overall acc:"+(sum_precision/(double)test_num));
		logger.info("test_num:"+test_num);
		logger.info("overall acc:"+(sum_precision/(double)test_num));
		
		//predict_accurate(weights, test_samples, local_label_set, learn_parms);

	}

	public void learnModelthree(File input_file, int label_num, int word_num,
			File test_file, LABEL[] local_label_set, Medw2vParms mw2vparms)
			throws Exception {
		// String[] sample = null;
		// sample = FileToArray.fileToDimArr(input_file);
		FileReader fr = null;
		FileInputStream fis = null;
		InputStreamReader isr = null;

		BufferedReader br = null;
		try {
			// fr=new FileReader(input_file);
			fis = new FileInputStream(input_file.getAbsolutePath());
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);

		} catch (Exception e) {
		}

		LEARNPARM learn_parms = new LEARNPARM();
		learn_parms.bd_type = "plnbdns";
		learn_parms.label_num = label_num;
		weights = new double[(word_num + 1)
				* (mw2vparms.first_label_num + mw2vparms.second_label_num + mw2vparms.third_label_num)];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = 0;
		}
		int label_width = mw2vparms.first_label_num
				+ mw2vparms.second_label_num + mw2vparms.third_label_num;
		int correct_num = 0;
		int incorrect_num = 0;

		ThreeBeamDecoder bd = new ThreeBeamDecoder();
		StrToClass sc = new StrToClass();
		/*
		 * LABEL[] local_label_set = new LABEL[learn_parms.label_num]; for (int
		 * i = 1; i <= learn_parms.label_num; i++) { local_label_set[i - 1] =
		 * sc.str2label(i + "", learn_parms); }
		 */
		WeightsCut wc = new WeightsCut();
		WeightsUpdate wu = new WeightsUpdate();

		// String test_file="";
		// String[] test_samples=FileToArray.fileToDimArr(test_file);
		String sample_line = "";
		for (int loop = 0; loop < 2; loop++) {
			incorrect_num = 0;
			correct_num = 0;

			// for (int ll = 0; ll < sample.length; ll++) {
			int ll = -1;
			if (loop > 0) {
				try {
					// fr=new FileReader(input_file);
					fis = new FileInputStream(input_file.getAbsolutePath());
					isr = new InputStreamReader(fis);
					br = new BufferedReader(isr);
				} catch (Exception e) {
				}
			}
			while ((sample_line = br.readLine()) != null) {
				ll++;
				// logger.info("ll="+ll);
				String[] seg_arr = sample_line.split("\001");
				if ((seg_arr == null) || (seg_arr.length != 4)) {
					continue;
				}
				System.out.println("ll=" + ll);
				String did = seg_arr[2];
				String label = seg_arr[1];
				// String lwo =
				// "13:1 133:3 277:3 624:3 659:3 938:3 1393:58 1395:3 1405:3 1461:3 1684:3 1839:3 2228:3 2941:3 3248:3 8260:3 26658:3";
				String lwo = seg_arr[3].trim();
				WORD[] words = sc.str2words(lwo);
				ShuffleArray.shuffle(words);

				// for (int l = 1; l < 10; l++) {
				learn_parms.docid = did;

				FEATSETTHREE best_fset = bd.beam_search_three(words, wc
						.all2partthree(weights, words, local_label_set,
								label_width), local_label_set, learn_parms);

				worst_label[Integer.parseInt(did)] = bd.sub_worst_feat.label;
				LABEL yt = sc.str2labelthree(label, mw2vparms);
				did = did.trim();
				System.out.println("best_label:[" + best_fset.label.toStr()
						+ "] yt:[" + yt.toStr() + "]");
				if (yt.equals_three(best_fset.label)) {
					System.out.println("right label");
					correct_num++;
				} else {
					System.out.println("wrong label");
					// weights=WeightsUpdate.regular_weight(weights);
					// PrintUtil.printNoZero(weights);
					weights = wu.update_weight_three(weights, words,
							best_fset.label, yt);
					incorrect_num++;
					// weights = WeightsUpdate.regular_weight_abs(weights);
					// PrintUtil.printNoZero(weights);

				}
				// }
			}
			try {
				fis.close();
				isr.close();
				br.close();
			} catch (Exception e) {
			}
			double acc = ((double) (correct_num))
					/ ((double) (correct_num + incorrect_num));
			logger.info("loop=" + loop);
			logger.info("correct_num=" + correct_num);
			logger.info("incorrect_num=" + incorrect_num);
			logger.info("acc=" + acc);
		}
		/*
		 * try{ fr.close(); br.close(); } catch(Exception e){}
		 */
		// predict_accurate_three(weights,test_samples,local_label_set,learn_parms,label_width);

	}

	/**
	 * 在测试集测试预测准确度
	 * 
	 * @param weights
	 * @param test_sample
	 * @param learn_parms
	 * @return
	 */
	public double predict_accurate(double[] weights, String[] test_sample,
			LABEL[] label_set, LEARNPARM learn_parms) {
		double acc = 0;
		try {
			String lds = "";// 一行样本
			String lst = "";// 标记
			String lwo = "";// 单词
			String did = "";// docid
			String rankey = "";
			String[] seg_arr = null;
			WORD[] words = null;
			FEATSET best_fset = null;
			LABEL yt = null;
			int correct_num = 0;
			int incorrect_num = 0;
			PlainBeamDecoderNoSearch bd = new PlainBeamDecoderNoSearch();
			WeightsCut wc = new WeightsCut();
			StrToClass sc = new StrToClass();
			for (int i = 0; i < test_sample.length; i++) {
				lds = test_sample[i];
				// System.out.println("lds:"+lds);
				if (SSO.tioe(lds)) {
					continue;
				}
				seg_arr = lds.split("\001");
				if (seg_arr.length != 2) {
					continue;
				}

				lst = seg_arr[0].trim();
				lwo = seg_arr[1].trim();
				did = "100";
				yt = sc.str2label(lst, learn_parms);

				words = sc.str2words(lwo);
				if (words == null) {
					continue;
				}

				//ShuffleArray.shuffle(words);
				best_fset = bd.beam_search_nocut(words,weights, label_set,
						learn_parms);

				/*
				 * best_fset = beam_search(words, WeightsCut.all2part(
				 * part_weights, words, local_label_set), local_label_set,
				 * local_learn_parm);
				 */

				// }
				/** 预测标记和样本相同 ***/
				if (yt.index == best_fset.label.index) {
					System.out.println("right");
					correct_num++;
				} else {
					System.out.println("wrong");
					incorrect_num++;
				}

			}

			acc = ((double) correct_num)
					/ ((double) (correct_num + incorrect_num));
			looptest++;
			logger.info("test correct_num:" + correct_num);
			logger.info("test incorrect_num:" + incorrect_num);
			logger.info("test acc:" + acc);
			if(looptest>50)
			{
			  sum_precision+=acc;
			  test_num+=1;
			}
			System.out.println("test correct_num:" + correct_num);
			System.out.println("test incorrect_num:" + incorrect_num);
			System.out.println("test acc:" + acc);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return acc;

	}

	/**
	 * 在测试集测试预测准确度
	 * 
	 * @param weights
	 * @param test_sample
	 * @param learn_parms
	 * @return
	 */
	public double predict_accurate_three(double[] weights,
			String[] test_sample, LABEL[] label_set, LEARNPARM learn_parms,
			int label_width) {
		double acc = 0;
		try {
			String lds = "";// 一行样本
			String lst = "";// 标记
			String lwo = "";// 单词
			String did = "";// docid
			String rankey = "";
			String[] seg_arr = null;
			WORD[] words = null;
			// FEATSET best_fset = null;
			LABEL yt = null;
			int correct_num = 0;
			int incorrect_num = 0;
			// BeamDecoder bd = BeamDecoderFactory.create(learn_parms);
			ThreeBeamDecoder bd = new ThreeBeamDecoder();
			WeightsCut wc = new WeightsCut();
			StrToClass sc = new StrToClass();
			for (int i = 0; i < test_sample.length; i++) {
				lds = test_sample[i];
				// System.out.println("lds:"+lds);
				if (SSO.tioe(lds)) {
					continue;
				}
				seg_arr = lds.split("\001");
				if (seg_arr.length != 4) {
					continue;
				}
				rankey = seg_arr[0].trim();
				lst = seg_arr[1].trim();
				lwo = seg_arr[3].trim();
				did = seg_arr[2].trim();
				yt = sc.str2label(lst, learn_parms);

				words = sc.str2words(lwo);
				if (words == null) {
					continue;
				}

				ShuffleArray.shuffle(words);
				// best_fset = bd.beam_search(words, wc.all2part(weights, words,
				// label_set), label_set,learn_parms);

				FEATSETTHREE best_fset = bd.beam_search_three(words, wc
						.all2partthree(weights, words, label_set, label_width),
						label_set, learn_parms);
				/*
				 * best_fset = beam_search(words, WeightsCut.all2part(
				 * part_weights, words, local_label_set), local_label_set,
				 * local_learn_parm);
				 */

				// }
				/** 预测标记和样本相同 ***/
				if (yt.equals_three(best_fset.label)) {
					correct_num++;
				} else {
					incorrect_num++;
				}

			}

			acc = ((double) correct_num)
					/ ((double) (correct_num + incorrect_num));

			logger.info("test correct_num:" + correct_num);
			logger.info("test incorrect_num:" + incorrect_num);
			logger.info("test acc:" + acc);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return acc;

	}
	
	public static void main(String[] args) throws Exception
	{
		File trvecFile = new File("temp/medw2v/trvec.txt");
		File tevecFile = new File("temp/medw2v/tevec.txt");
		File labelFile = new File("temp/medw2v/b.txt");
		PerceptronLearnSupervised pls=new PerceptronLearnSupervised();
		pls.learnModelStandalone(trvecFile, labelFile, 200, tevecFile);
		
	}

}
