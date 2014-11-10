package com.ansj.vec;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import love.cq.util.MapCount;

import org.jmlp.file.utils.FileSplit;
import org.jmlp.file.utils.FileToArray;
import org.jmlp.math.random.ArrayUtil;
import org.jmlp.medw2v.FileConvertFormat;
import org.jmlp.perceptron.LABEL;
import org.jmlp.perceptron.PerceptronLearnSupervised;
import org.jmlp.perceptron.StrToClass;
import org.jmlp.str.basic.SSO;
import org.jmlp.str.basic.StdReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ansj.vec.domain.HiddenNeuron;
import com.ansj.vec.domain.Neuron;
import com.ansj.vec.domain.WordNeuron;
import com.ansj.vec.util.Haffman;

/**
 * 
 * @author lq
 * 
 */
public class LearnSupervised {

	static Logger logger = LoggerFactory.getLogger(Learn.class);
	private Map<String, Neuron> wordMap = new HashMap<>();

	private Map<String, Neuron> labelMap = new HashMap<>();

	private Map<String, Integer> labelIndex = new HashMap<>();

	/**
	 * 训练多少个特征
	 */
	private int layerSize = 200;

	/**
	 * 上下文窗口大小
	 */
	private int window = 5;

	private double sample = 1e-3;
	private double alpha = 0.025;
	private double startingAlpha = alpha;

	public int EXP_TABLE_SIZE = 1000;

	private Boolean isCbow = true;

	private double[] expTable = new double[EXP_TABLE_SIZE];

	private int trainWordsCount = 0;

	private int MAX_EXP = 6;

	private long nextRandom = 5;
	private int wordCount = 0;

	public HashMap<String, String> occHash = new HashMap<>();
	
	int lastWordCount = 0;
	int wordCountActual = 0;

	public LearnSupervised(Boolean isCbow, Integer layerSize, Integer window,
			Double alpha, Double sample) {
		createExpTable();
		if (isCbow != null) {
			this.isCbow = isCbow;
		}
		if (layerSize != null)
			this.layerSize = layerSize;
		if (window != null)
			this.window = window;
		if (alpha != null)
			this.alpha = alpha;
		if (sample != null)
			this.sample = sample;
	}

	public LearnSupervised() {
		createExpTable();
	}

	/**
	 * trainModel
	 * 
	 * @throws IOException
	 */
	private void trainModel(File file) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file)))) {
			String temp = null;
			// //long nextRandom = 5;
			// //int wordCount = 0;

			while ((temp = br.readLine()) != null) {
				if (wordCount - lastWordCount > 10000) {
					System.out.println("alpha:"
							+ alpha
							+ "\tProgress: "
							+ (int) (wordCountActual
									/ (double) (trainWordsCount + 1) * 100)
							+ "%");
					wordCountActual += wordCount - lastWordCount;
					lastWordCount = wordCount;
					alpha = startingAlpha
							* (1 - wordCountActual
									/ (double) (trainWordsCount + 1));
					if (alpha < startingAlpha * 0.0001) {
						alpha = startingAlpha * 0.0001;
					}
				}

				// **********replace with trainOneDoc**********************

				trainOneDoc(temp);

				// *******************************

			}
			System.out.println("Vocab size: " + wordMap.size());
			System.out.println("Words in train file: " + trainWordsCount);
			System.out.println("sucess train over!");
		}
	}

	/**
	 * line.=.<label>\001<seg_text>
	 * 
	 * @param recLine
	 */
	public void trainOneDoc(String recLine) {
		String label_text = "";
		String seg_text = "";
		String[] seg_arr = null;
		seg_arr = recLine.split("\001");

		if (seg_arr.length != 2) {
			return;
		}
		label_text = seg_arr[0].trim();
		seg_text = seg_arr[1].trim();

		String[] strs = seg_text.split(" ");
		wordCount += strs.length;
		List<WordNeuron> sentence = new ArrayList<WordNeuron>();
		for (int i = 0; i < strs.length; i++) {
			Neuron entry = wordMap.get(strs[i]);
			if (entry == null) {
				continue;
			}
			// The subsampling randomly discards frequent words while keeping
			// the ranking same
			if (sample > 0) {
				double ran = (Math
						.sqrt(entry.freq / (sample * trainWordsCount)) + 1)
						* (sample * trainWordsCount) / entry.freq;
				nextRandom = nextRandom * 25214903917L + 11;
				if (ran < (nextRandom & 0xFFFF) / (double) 65536) {
					continue;
				}
			}
			sentence.add((WordNeuron) entry);
		}

		WordNeuron labelWN = (WordNeuron) labelMap.get(label_text);

		for (int index = 0; index < sentence.size(); index++) {
			nextRandom = nextRandom * 25214903917L + 11;
			if (isCbow) {
				cbowGram(index, sentence, labelWN, (int) nextRandom % window);
			} else {
				skipGram(index, sentence, labelWN, (int) nextRandom % window);
			}
		}

		// cbowGramdoc( sentence, labelWN,(int) nextRandom % window);

	}

	/**
	 * skip gram 模型训练
	 * 
	 * @param sentence
	 * @param neu1
	 */
	private void skipGram(int index, List<WordNeuron> sentence,
			WordNeuron labelWN, int b) {
		// TODO Auto-generated method stub
		WordNeuron word = sentence.get(index);
                if(word.name.equals("NA"))
                {
                       return;
                }
		int a, c = 0;
		for (a = b; a < window * 2 + 1 - b; a++) {
			if (a == window) {
				continue;
			}
			c = index - window + a;
			if (c < 0 || c >= sentence.size()) {
				continue;
			}

			double[] neu1e = new double[layerSize];// 误差项
			// HIERARCHICAL SOFTMAX
			List<Neuron> neurons = word.neurons;
			WordNeuron we = sentence.get(c);
			if(we.name.equals("NA"))
			{
				continue;
			}
			for (int i = 0; i < neurons.size(); i++) {
				HiddenNeuron out = (HiddenNeuron) neurons.get(i);
				double f = 0;
				// Propagate hidden -> output
				for (int j = 0; j < layerSize; j++) {
					f += we.syn0[j] * out.syn1[j];
				}
				if (f <= -MAX_EXP || f >= MAX_EXP) {
					continue;
				} else {
					f = (f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2);
					f = expTable[(int) f];
				}
				// 'g' is the gradient multiplied by the learning rate
				double g = (1 - word.codeArr[i] - f) * alpha;
				// Propagate errors output -> hidden
				for (c = 0; c < layerSize; c++) {
					neu1e[c] += g * out.syn1[c];
				}
				// Learn weights hidden -> output
				for (c = 0; c < layerSize; c++) {
					out.syn1[c] += g * we.syn0[c];
				}
			}

			// Learn weights input -> hidden
			for (int j = 0; j < layerSize; j++) {
				we.syn0[j] += neu1e[j];
			}
		}
	}

	/**
	 * 词袋模型 syn0:M syn1:M''
	 * 
	 * @param index
	 * @param sentence
	 * @param b
	 */
	private void cbowGram(int index, List<WordNeuron> sentence,
			WordNeuron labelWN, int b) {
		//alpha=0.025;
		WordNeuron word = sentence.get(index);
                if(word.name.equals("NA"))
                {
                       return;
                }
		int a, c = 0;

		List<Neuron> neurons = word.neurons;
		double[] neu1e = new double[layerSize];// 误差项
		double[] neu1 = new double[layerSize];// 误差项
		WordNeuron last_word;

		for (a = b; a < window * 2 + 1 - b; a++)
			if (a != window) {
				c = index - window + a;
				if (c < 0)
					continue;
				if (c >= sentence.size())
					continue;
				last_word = sentence.get(c);
                                if(last_word.name.equals("NA"))
                                {
                                   continue;
                                }
				if (last_word == null)
					continue;
				for (c = 0; c < layerSize; c++)
					neu1[c] += last_word.syn0[c];
			}

		double[] neu1d = new double[layerSize];// 误差项
		WordNeuron current_word;
		for (int w = 0; w < sentence.size(); w++) {
			current_word = sentence.get(w);
                        if(current_word.name.equals("NA"))
                        {
                                   continue;
                        }
			for (c = 0; c < layerSize; c++)
				neu1d[c] += current_word.syn0[c];
		}

		List<Neuron> lneurons = labelWN.neurons;
		// double[] lambdad=new double[lneurons.size()];
		// System.out.println("layerSize1:"+layerSize);
		double[] madd = new double[layerSize];
		// for(int i=0;i<madd.length;i++)
		// {
		// madd[i]=0;
		// }

		boolean isF = false;
		if (!(occHash.containsKey(labelWN.name))) {
			isF = true;
			occHash.put(labelWN.name, "1");
		}

		for (int d = 0; d < lneurons.size(); d++) {
			HiddenNeuron dout = (HiddenNeuron) lneurons.get(d);
			if (isF == true) {
				dout.syn1 = ArrayUtil.initialRandom(layerSize);
			}
			double f = 0;
			for (c = 0; c < layerSize; c++)
				f += neu1d[c] * dout.syn1[c];

			if (f <= -MAX_EXP)
				continue;
			else if (f >= MAX_EXP)
				continue;
			else
			{
				f = (f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2);
				f = expTable[(int) f];
			//	f = expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];
			//	f = expTable[(int) f];
			}
			// lambdad[d]=f;

			double g = (1 - labelWN.codeArr[d] - f) * alpha;

			for (c = 0; c < layerSize; c++) {
				madd[c] += g * dout.syn1[c];

			}

			for (c = 0; c < layerSize; c++) {
				dout.syn1[c] += neu1d[c] * g;
			}

		}

		// HIERARCHICAL SOFTMAX
		for (int d = 0; d < neurons.size(); d++) {
			HiddenNeuron out = (HiddenNeuron) neurons.get(d);
			double f = 0;
			// Propagate hidden -> output
			for (c = 0; c < layerSize; c++)
				f += neu1[c] * out.syn1[c];
			if (f <= -MAX_EXP)
				continue;
			else if (f >= MAX_EXP)
				continue;
			else
			{
				f = (f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2);
				f = expTable[(int) f];
				//f = expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];
				//f = expTable[(int) f];
			}
			// 'g' is the gradient multiplied by the learning rate
			// double g = (1 - word.codeArr[d] - f) * alpha;
			// double g = f*(1-f)*( word.codeArr[i] - f) * alpha;
			// double g = f * (1 - f) * (word.codeArr[d] - f) * alpha;
			double g = (1 - word.codeArr[d] - f) * alpha;
			for (c = 0; c < layerSize; c++) {
				neu1e[c] += g * out.syn1[c];
			}
			// Learn weights hidden -> output

			for (c = 0; c < layerSize; c++) {
				out.syn1[c] += g * neu1[c];
			}
		}

		for (a = b; a < window * 2 + 1 - b; a++) {
			if (a != window) {
				c = index - window + a;
				if (c < 0)
					continue;
				if (c >= sentence.size())
					continue;
				last_word = sentence.get(c);
                                if(last_word.name.equals("NA"))
                                {
                                   continue;
                                }
				if (last_word == null)
					continue;
				// System.out.println("layerSize:"+layerSize);
				for (c = 0; c < layerSize; c++) {
					last_word.syn0[c] += neu1e[c];
					last_word.syn0[c] += madd[c];
				}
			}
		}

	}

	  private void cbowGramUn(int index, List<WordNeuron> sentence, WordNeuron labelWN, int b) {
		  //alpha=0.025;
	        WordNeuron word = sentence.get(index);
	        int a, c = 0;

	        List<Neuron> neurons = word.neurons;
	        double[] neu1e = new double[layerSize];//误差项
	        double[] neu1 = new double[layerSize];//误差项
	        WordNeuron last_word;

	        for (a = b; a < window * 2 + 1 - b; a++)
	            if (a != window) {
	                c = index - window + a;
	                if (c < 0)
	                    continue;
	                if (c >= sentence.size())
	                    continue;
	                last_word = sentence.get(c);
	                if (last_word == null)
	                    continue;
	                for (c = 0; c < layerSize; c++)
	                    neu1[c] += last_word.syn0[c];
	            }

	        //HIERARCHICAL SOFTMAX
	        for (int d = 0; d < neurons.size(); d++) {
	            HiddenNeuron out = (HiddenNeuron) neurons.get(d);
	            double f = 0;
	            // Propagate hidden -> output
	            for (c = 0; c < layerSize; c++)
	                f += neu1[c] * out.syn1[c];
	            if (f <= -MAX_EXP)
	                continue;
	            else if (f >= MAX_EXP)
	                continue;
	            else
	            {
	            	f = (f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2);
					f = expTable[(int) f];
	                //f = expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];
	            }
	                
	            // 'g' is the gradient multiplied by the learning rate
	            //            double g = (1 - word.codeArr[d] - f) * alpha;
	            //              double g = f*(1-f)*( word.codeArr[i] - f) * alpha;
	            //double g = f * (1 - f) * (word.codeArr[d] - f) * alpha;
	            double g = (1 - word.codeArr[d] - f) * alpha;
	            for (c = 0; c < layerSize; c++) {
	                neu1e[c] += g * out.syn1[c];
	            }
	            // Learn weights hidden -> output
	            for (c = 0; c < layerSize; c++) {
	                out.syn1[c] += g * neu1[c];
	            }
	        }
	        
	        for (a = b; a < window * 2 + 1 - b; a++) {
	            if (a != window) {
	                c = index - window + a;
	                if (c < 0)
	                    continue;
	                if (c >= sentence.size())
	                    continue;
	                last_word = sentence.get(c);
	                if (last_word == null)
	                    continue;
	                for (c = 0; c < layerSize; c++)
	                    last_word.syn0[c] += neu1e[c];
	            }

	        }
	    }
	  
	/**
	 * 词袋模型 syn0:M syn1:M''
	 * 
	 * @param index
	 * @param sentence
	 * @param b
	 */
	private void cbowGramdoc(List<WordNeuron> sentence, WordNeuron labelWN,
			int b) {

		int c = 0;
		double[] neu1d = new double[layerSize];// 误差项
		WordNeuron current_word;
		for (int w = 0; w < sentence.size(); w++) {
			current_word = sentence.get(w);
			for (c = 0; c < layerSize; c++)
				neu1d[c] += current_word.syn0[c];
		}

		List<Neuron> lneurons = labelWN.neurons;
		double[] madd = new double[layerSize];
		boolean isF = false;
		if (!(occHash.containsKey(labelWN.name))) {
			isF = true;
			occHash.put(labelWN.name, "1");
		}
		for (int d = 0; d < lneurons.size(); d++) {
			HiddenNeuron dout = (HiddenNeuron) lneurons.get(d);
			if (isF == true) {
				dout.syn1 = ArrayUtil.initialRandom(layerSize);
			}
			double f = 0;
			for (c = 0; c < layerSize; c++)
				f += neu1d[c] * dout.syn1[c];

			if (f <= -MAX_EXP)
				continue;
			else if (f >= MAX_EXP)
				continue;
			else
				f = expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];
			// lambdad[d]=f;
			double g = (1 - labelWN.codeArr[d] - f) * alpha;

			for (c = 0; c < layerSize; c++) {
				dout.syn1[c] += neu1d[c] * g;
			}

		}

	}

	/**
	 * 统计词频 文件格式:line.=.<label>\001<seg_text>
	 * 
	 * @param file
	 * @throws IOException
	 */
	private void readVocab(File file) throws IOException {
		MapCount<String> mc = new MapCount<>();
		MapCount<String> lmc = new MapCount<>();
		String seg_text = "";
		String label_text = "";
		String[] seg_arr = null;
		int label2index = 1;

		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file)))) {
			String temp = null;
			while ((temp = br.readLine()) != null) {
				seg_arr = temp.split("\001");
				if (seg_arr.length != 2) {
					continue;
				}
				label_text = seg_arr[0].trim();
				seg_text = seg_arr[1].trim();
				if (SSO.tioe(label_text)) {
					continue;
				}
				lmc.add(label_text);
				if (!(labelIndex.containsKey(label_text))) {
					labelIndex.put(label_text, label2index++);
				}

				String[] split = seg_text.split(" ");
				trainWordsCount += split.length;
				for (String string : split) {
					mc.add(string);
				}
			}
		}

		for (Entry<String, Integer> element : mc.get().entrySet()) {
			logger.info("ekey:" + element.getKey() + "eval:"
					+ element.getValue());
			wordMap.put(element.getKey(), new WordNeuron(element.getKey(),
					element.getValue(), layerSize));
		}

		for (Entry<String, Integer> element : lmc.get().entrySet()) {
			logger.info("label_ekey:" + element.getKey() + "label_eval:"
					+ element.getValue());
			labelMap.put(element.getKey(), new WordNeuron(element.getKey(),
					element.getValue(), layerSize));
		}

	}

	/**
	 * Precompute the exp() table f(x) = x / (x + 1)
	 */
	private void createExpTable() {
		for (int i = 0; i < EXP_TABLE_SIZE; i++) {
			expTable[i] = Math
					.exp(((i / (double) EXP_TABLE_SIZE * 2 - 1) * MAX_EXP));
			expTable[i] = expTable[i] / (expTable[i] + 1);
		}
	}

	/**
	 * 根据文件学习
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void learnFile(File trainfile,File trfile, File tefile) throws IOException {
	
		/*
		File trainformatFile = new File("temp/medw2v/trainformat.txt");
		try {
			FileConvertFormat fcf = new FileConvertFormat();
			fcf.convertFormatSupervise(trfile, trainformatFile);
		} catch (Exception e) {
		}
		*/
		File trformatFile = new File("temp/medw2v/trformat.txt");
	
		try {
			//FileConvertFormat fcf = new FileConvertFormat();
			//fcf.convertFormatSuperviseNounText(trfile, trformatFile);
		} catch (Exception e) {
		}		
		readVocab(trformatFile);
		new Haffman(layerSize).make(wordMap.values());

		// 查找每个神经元
		for (Neuron neuron : wordMap.values()) {
			((WordNeuron) neuron).makeNeurons();
		}

		new Haffman(layerSize).make(labelMap.values());

		// 查找每个神经元
		for (Neuron neuron : labelMap.values()) {
			((WordNeuron) neuron).makeNeurons();
		}
		/*
		 * WordNeuron lwn=null;
		 * 
		 * for (Neuron neuron : labelMap.values()) { lwn=(WordNeuron) neuron;
		 * List<Neuron> lneurons = lwn.neurons;
		 * 
		 * for(int d=0;d<lneurons.size();d++) { HiddenNeuron dout =
		 * (HiddenNeuron) lneurons.get(d);
		 * dout.syn1=ArrayUtil.initialRandom(layerSize); } }
		 */

		
		File teformatFile = new File("temp/medw2v/teformat.txt");
		try {
			//FileConvertFormat fcf = new FileConvertFormat();
			//fcf.convertFormatSuperviseNounText(tefile, teformatFile);
		} catch (Exception e) {
		}
		try{
		//FileSplit.splitRandomSim(0.7,trainformatFile.getAbsolutePath(),trformatFile.getAbsolutePath(),teformatFile.getAbsolutePath());
		}
		catch(Exception e)
		{
			
		}
		

		trainModel(trformatFile);
		
		
		File trvecFile = new File("temp/medw2v/trvec.txt");
		File tevecFile = new File("temp/medw2v/tevec.txt");
		try {
			vecTagFile(trformatFile, trvecFile);
			vecTagFile(teformatFile, tevecFile);
		} catch (Exception e) {

		}
                /*
		LABEL[] label_set = new LABEL[labelIndex.size()];
		int lin = 0;
		StrToClass sc = new StrToClass();
		for (Integer tl : labelIndex.values()) {
			label_set[lin++] = sc.str2labelSimple(tl.intValue() + "",
					labelIndex.size());
		}
               
		PerceptronLearnSupervised pls = new PerceptronLearnSupervised();
		try {
			pls.learnModel(trvecFile, label_set, layerSize, tevecFile);
		} catch (Exception e) {

		}

               */
            
		
	}

	/**
	 * input_file 格式:<key>\001<tag>\001<docid>\001\001<seg_line> output_file
	 * 格式:<key>\001<tag>\001<docid>\001<n dim vec>
	 */
	public void vecTagFile(File input_file, File output_file) throws Exception {
		String[] samples = FileToArray.fileToDimArr(input_file);
		FileWriter fw = new FileWriter(output_file);
		PrintWriter pw = new PrintWriter(fw);
		String[] seg_arr = null;

		String label_str = "";
		String seg_text = "";
		String line = "";
		String[] word_seg = null;
		double[] vec = new double[layerSize];
		for (int i = 0; i < vec.length; i++) {
			vec[i] = 0;
		}

		double[] syn0vec = null;
		String word = "";
		WordNeuron wn;
		Neuron nn;
		String vec_sam_line = "";
		String vec_str = "";
		int label2index = 0;
		for (int i = 0; i < samples.length; i++) {
			line = samples[i];
			seg_arr = line.split("\001");
			if (seg_arr.length < 2) {
				continue;
			}
			label_str = seg_arr[0].trim();
			if (SSO.tioe(label_str)) {
				continue;
			}

			if (labelIndex.get(label_str) == null) {
				continue;
			}
			label2index = labelIndex.get(label_str);
			seg_text = seg_arr[1].trim();
			word_seg = seg_text.split("\\s+");

			for (int j = 0; j < vec.length; j++) {
				vec[j] = 0;
			}

			for (int j = 0; j < word_seg.length; j++) {
                                
				word = word_seg[j].trim();
                                if(word.equals("NA"))
                                {
                                   continue;
                                }
				nn = wordMap.get(word);
				if (nn == null) {
					continue;
				}
				// wn=(WordNeuron)wordMap.get(word);
				wn = (WordNeuron) nn;
				syn0vec = wn.syn0;
				vec = ArrayUtil.sum_weight(vec, syn0vec);
			}
			vec_str = ArrayUtil.arrayToSamStr(vec);
			vec_sam_line = label2index + "\001" + vec_str;
			pw.println(vec_sam_line);
		}
		fw.close();
		pw.close();

	}

	public void classifyFile(File trfile, File tefile) throws IOException {
		ClassifySupervised cs = new ClassifySupervised();
		cs.setLabelMap(labelMap);
		cs.setWordMap(wordMap);
		cs.setLayerSize(layerSize);
		cs.EXP_TABLE_SIZE = EXP_TABLE_SIZE;
		cs.MAX_EXP = MAX_EXP;
		cs.expTable = expTable;
		// System.out.println("accuracy is:"+cs.classifyFile(file));
	}

	/**
	 * 保存模型
	 */
	public void saveModel(File file) {
		// TODO Auto-generated method stub

		try (DataOutputStream dataOutputStream = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(file)))) {
			dataOutputStream.writeInt(wordMap.size());
			dataOutputStream.writeInt(layerSize);
			double[] syn0 = null;
			for (Entry<String, Neuron> element : wordMap.entrySet()) {
				dataOutputStream.writeUTF(element.getKey());
				syn0 = ((WordNeuron) element.getValue()).syn0;
				for (double d : syn0) {
					dataOutputStream.writeFloat(((Double) d).floatValue());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getLayerSize() {
		return layerSize;
	}

	public void setLayerSize(int layerSize) {
		this.layerSize = layerSize;
	}

	public int getWindow() {
		return window;
	}

	public void setWindow(int window) {
		this.window = window;
	}

	public double getSample() {
		return sample;
	}

	public void setSample(double sample) {
		this.sample = sample;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
		this.startingAlpha = alpha;
	}

	public Boolean getIsCbow() {
		return isCbow;
	}

	public void setIsCbow(Boolean isCbow) {
		this.isCbow = isCbow;
	}

	public static void main(String[] args) throws IOException {
		LearnSupervised learn = new LearnSupervised();
		long start = System.currentTimeMillis();
		learn.learnFile(new File("temp/medw2v/train.txt"),new File("temp/medw2v/tr.txt"), new File(
				"temp/medw2v/te.txt"));
		System.out.println("use time " + (System.currentTimeMillis() - start));
		// learn.classifyFile(new File("temp/medw2v/te.txt"));
	
                /*
		learn.saveModel(new File("temp/medw2v/vector.mod"));
               
		Word2VEC w2v = new Word2VEC();

		w2v.loadJavaModel("temp/medw2v/vector.mod");
		System.out.println(w2v.distance("衣服"));

		StdReader stdreader = new StdReader();
		System.out.println("input one word:");
		String line = stdreader.readLine();
		line = line.trim();
		while (!(line.equals("quit"))) {

			System.out.println(w2v.distance(line));
			System.out.println("input one word:");
			line = stdreader.readLine();
			line = line.trim();
		}
                */

	}

}
