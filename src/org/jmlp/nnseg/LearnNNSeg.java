package org.jmlp.nnseg;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import love.cq.util.MapCount;

import org.jmlp.math.random.ArrayUtil;
import org.jmlp.str.basic.SSO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ansj.vec.Learn;
import com.ansj.vec.domain.HiddenNeuron;
import com.ansj.vec.domain.Neuron;
import com.ansj.vec.domain.WordNeuron;
import com.ansj.vec.util.Haffman;

public class LearnNNSeg {

	static Logger logger = LoggerFactory.getLogger(LearnNNSeg.class);
	private Map<String, Neuron> wordMap = new HashMap<>();
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

	//private double[] posweight = new double[layerSize * (2 * window + 1)];
	//private double[] negweight = new double[layerSize * (2 * window + 1)];

	public LearnNNSeg(Boolean isCbow, Integer layerSize, Integer window,
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

	public LearnNNSeg() {
		createExpTable();
		
	   // posweight = ArrayUtil.initialRandom(layerSize * (2 * window + 1));
	   // negweight = ArrayUtil.initialRandom(layerSize * (2 * window + 1));
		 //posweight = ArrayUtil.initialRandom(layerSize );
		 //negweight = ArrayUtil.initialRandom(layerSize );
		
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
			long nextRandom = 5;
			int wordCount = 0;
			int lastWordCount = 0;
			int wordCountActual = 0;
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
				String[] strs = temp.split("\\s+");
				wordCount += strs.length;
				List<WordNeuron> sentence = new ArrayList<WordNeuron>();

				String token = "";
				String word = "";
				String label = "";
				WordNeuron tempwn;
				for (int i = 0; i < strs.length; i++) {
					token = strs[i];
					if (SSO.tioe(token)) {
						continue;
					}
					word = token.substring(0, token.lastIndexOf(":"));
					label = token.substring(token.lastIndexOf(":") + 1,
							token.length());

					Neuron entry = wordMap.get(word);
					if (entry == null) {
						continue;
					}

					tempwn = (WordNeuron) entry;
					tempwn.label = Integer.parseInt(label);

					// The subsampling randomly discards frequent words while
					// keeping the ranking same
					/*
					 * if (sample > 0) { double ran = (Math.sqrt(entry.freq /
					 * (sample * trainWordsCount)) + 1) (sample *
					 * trainWordsCount) / entry.freq; nextRandom = nextRandom *
					 * 25214903917L + 11; if (ran < (nextRandom & 0xFFFF) /
					 * (double) 65536) { continue; } }
					 */

					sentence.add(tempwn);
				}

				for (int index = 1; index < sentence.size() - 1; index++) {
					nextRandom = nextRandom * 25214903917L + 11;
					if (isCbow) {
						cbowGram(index, sentence, (int) nextRandom % window);
					} else {
						skipGram(index, sentence, (int) nextRandom % window);
					}
				}
			}
			System.out.println("Vocab size: " + wordMap.size());
			System.out.println("Words in train file: " + trainWordsCount);
			System.out.println("sucess train over!");
		}
	}

	/**
	 * skip gram 模型训练
	 * 
	 * @param sentence
	 * @param neu1
	 */
	private void skipGram(int index, List<WordNeuron> sentence, int b) {
		// TODO Auto-generated method stub
		WordNeuron word = sentence.get(index);
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
	private void cbowGram(int index, List<WordNeuron> sentence, int b) {
		WordNeuron word = sentence.get(index);
		int a, c = 0;
		int k = 0;
		//List<Neuron> neurons = word.neurons;
		double[] neu1e = new double[layerSize * (window * 2 + 1)];// 误差项
		double[] neu1 = new double[layerSize * (window * 2 + 1)];// 误差项
		WordNeuron last_word;

		/*
		for (a = 0; a < b; a++) {
			for (c = 0; c < layerSize; c++) {
				neu1[c + a * layerSize] = 0;
			}
		}
        */
		for (a = 0; a < window * 2 + 1; a++) {

			//System.out.println("a=" + a);

			// if (a != window) {
			c = index - window + a;
			if (c < 0 || c >= sentence.size()) {
				for (k = 0; k < layerSize; k++) {
					neu1[k + a * layerSize] = 0;
				}
				continue;
			}
			// if (c >= sentence.size())
			// continue;

			last_word = sentence.get(c);
			if (last_word != null) {
				for (k = 0; k < layerSize; k++) {
					// System.out.println("k + a * layerSize="+(c + a *
					// layerSize));
					neu1[k + a * layerSize] += last_word.syn0[k + a * layerSize];
				}
			} else {
				for (k = 0; k < layerSize; k++) {
					neu1[k + a * layerSize] = 0;
				}
			}
			// }
		}

		/*
		for (a = window * 2 + 1 - b; a < (window * 2 + 1); a++) {
			c = index - window + a;
			for (k = 0; k < layerSize; k++) {
				neu1[k + a * layerSize] = 0;
			}
		}
       */
		double g = 0;
		if (word.label == 1) {
			g=0;
			double f = 0;
			//posweight=ArrayUtil.normalize(posweight);
			for (k = 0; k < layerSize * (window * 2 + 1); k++) {
				f += neu1[k] * word.syn21[k];
			}
			//System.out.println("fpos="+f);
			if (f <= -MAX_EXP)
				return;
			else if (f >= MAX_EXP)
				return;
			else {
				//f = expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];
				f=Math.exp(f)/(1+Math.exp(f));
			}

			
			g = (1-word.label-f) * alpha;

			for (k = 0; k < layerSize * (window * 2 + 1); k++) {
				neu1e[k] += g *  word.syn21[k];
			}

			for (k = 0; k < layerSize * (window * 2 + 1); k++) {
				word.syn21[k] += g * neu1[k];
				//posweight=ArrayUtil.normalize(posweight);
			}

		} else {
			g=0;
			double f = 0;
			//negweight=ArrayUtil.normalize(negweight);
			for (k = 0; k < layerSize * (window * 2 + 1); k++) {
				f += neu1[k] * word.syn20[k];
			}
			//System.out.println("fneg="+f);
			if (f <= -MAX_EXP)
				return;
			else if (f >= MAX_EXP)
				return;
			else {
			//	f = expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];
				f=Math.exp(f)/(1+Math.exp(f));
			}

			g = (1-word.label- f) * alpha;

			for (k = 0; k < layerSize * (window * 2 + 1); k++) {
				neu1e[k] += g * word.syn20[k];
			}

			for (k = 0; k < layerSize * (window * 2 + 1); k++) {
				word.syn20[k] += g * neu1[k];
				//negweight=ArrayUtil.normalize(negweight);
			}

		}

		for (a = 0; a < window * 2 + 1 ; a++) {
			//if (a != window) {
				c = index - window + a;
				if (c < 0)
					continue;
				if (c >= sentence.size())
					continue;
				last_word = sentence.get(c);
				if (last_word == null)
					continue;
				for (c = 0; c < layerSize; c++)
				{
					last_word.syn0[c + a* layerSize] += neu1e[c + a* layerSize];
				}
				//last_word.syn0=ArrayUtil.normalize(last_word.syn0);
			//}

		}
	}

	/*
	private void cbowGramTest(int index, List<WordNeuron> sentence, int b) {
		
		WordNeuron word = sentence.get(index);
		int a, c = 0;
		int k = 0;
		//List<Neuron> neurons = word.neurons;
		double[] neu1e = new double[layerSize ];// 误差项
		double[] neu1 = new double[layerSize ];// 误差项
		WordNeuron last_word;


		for (a = 0; a < window * 2 + 1; a++) {

			//System.out.println("a=" + a);

			// if (a != window) {
			c = index - window + a;
			if ((c < 0) ||( c >= sentence.size())) {
				continue;
			}
			// if (c >= sentence.size())
			// continue;

			last_word = sentence.get(c);
			if (last_word != null) {
				for (k = 0; k < layerSize; k++) {
					neu1[k] += last_word.syn0[k];
				}
			}
			// }
		}


		double g = 0;
		if (word.label == 1) {
			g=0;
			double f = 0;
			posweight=ArrayUtil.normalize(posweight);
			for (int i = 0; i < layerSize ; i++) {
				f += neu1[i] * posweight[i];
			}
			//System.out.println("fpos="+f);
			if (f <= -MAX_EXP)
				return;
			else if (f >= MAX_EXP)
				return;
			else {
				f = expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];
			}

			
			g = (1-f) * alpha;

			for (int i = 0; i < layerSize ; i++) {
				neu1e[i] += g * posweight[i];
			}

			for (int i = 0; i < layerSize ; i++) {
				posweight[i] += g * neu1[i];
				//posweight=ArrayUtil.normalize(posweight);
			}

		} else {
			g=0;
			double f = 0;
			negweight=ArrayUtil.normalize(negweight);
			for (int i = 0; i < layerSize ; i++) {
				f += neu1[i] * negweight[i];
			}
			//System.out.println("fneg="+f);
			if (f <= -MAX_EXP)
				return;
			else if (f >= MAX_EXP)
				return;
			else {
				f = expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];
			}

			g = (1 - f) * alpha;

			for (int i = 0; i < layerSize ; i++) {
				neu1e[i] += g * negweight[i];
			}

			for (int i = 0; i < layerSize ; i++) {
				negweight[i] += g * neu1[i];
				//negweight=ArrayUtil.normalize(negweight);
			}

		}

		for (a = 0; a < window * 2 + 1 ; a++) {
			//if (a != window) {
				c = index - window + a;
				if (c < 0)
					continue;
				if (c >= sentence.size())
					continue;
				last_word = sentence.get(c);
				if (last_word == null)
					continue;
				for (c = 0; c < layerSize; c++)
				{
					last_word.syn0[c] += neu1e[c ];
				}
				//last_word.syn0=ArrayUtil.normalize(last_word.syn0);
			//}

		}
		
	}
	*/
	
	/**
	 * 统计词频
	 * 
	 * @param file
	 * @throws IOException
	 */
	private void readVocab(File file) throws IOException {
		MapCount<String> mc = new MapCount<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file)))) {
			String temp = null;
			String[] seg_arr = null;
			String word = "";

			while ((temp = br.readLine()) != null) {
				String[] split = temp.split("\\s+");
				trainWordsCount += split.length;
				for (String token : split) {
					if (token.indexOf(":") == -1) {
						continue;
					}
					word = token.substring(0, token.lastIndexOf(":"));

					mc.add(word);

				}
			}
		}
		for (Entry<String, Integer> element : mc.get().entrySet()) {
			logger.info("ekey:" + element.getKey() + "eval:"
					+ element.getValue());
			wordMap.put(element.getKey(), new WordNeuron(element.getKey(),
					element.getValue(), layerSize*(2*window+1) ));
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
	public void learnFile(File file) throws IOException {

		File format_file = new File("nnseg/format.txt");
		PlainTextReader.plain2label(file, format_file);
		readVocab(format_file);
		new Haffman(layerSize).make(wordMap.values());

		// 查找每个神经元
		for (Neuron neuron : wordMap.values()) {
			((WordNeuron) neuron).makeNeurons();
		}

		trainModel(format_file);
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

	/**
	 * 保存模型
	 */
	public String seg(String text) {

		String segtext;
		if (SSO.tioe(text)) {
			return "";
		}

		text = text.replaceAll("\\s+", "");
		Sentence s = new Sentence();
		TOKEN[] seg_tokens = s.seg2tokens(text);

		List<WordNeuron> sentence = new ArrayList<WordNeuron>();
		TOKEN token;
		WordNeuron tempwn = null;
		for (int i = 0; i < seg_tokens.length; i++) {
			token = seg_tokens[i];

			Neuron entry = wordMap.get(token.word);
			if (entry == null) {
				continue;
			}

			tempwn = (WordNeuron) entry;

			sentence.add(tempwn);
		}
		long nextRandom = 5;
		segtext = "";

		for (int index = 1; index < sentence.size() - 1; index++) {
			nextRandom = nextRandom * 25214903917L + 11;
			tempwn = sentence.get(index);
			segtext += tempwn.name + ":"
					+ predict(index, sentence, (int) nextRandom % window) + " ";
		}
		segtext = segtext.trim();
		return segtext;
	}

	public int predict(int index, List<WordNeuron> sentence, int b) {

		double[] neu1e = new double[layerSize * (window * 2 + 1)];// 误差项
		double[] neu1 = new double[layerSize * (window * 2 + 1)];// 误差项
		WordNeuron last_word;
		WordNeuron word = sentence.get(index);
		int a, c,k = 0;


		for (a = 0; a < window * 2 + 1 ; a++) {

			// if (a != window) {
			c = index - window + a;
			if (c < 0||c >= sentence.size())
			{
				for (c = 0; c < layerSize; c++) {
					neu1[c + a * layerSize] = 0;
				}
				continue;
			}
			
			last_word = sentence.get(c);
			if (last_word != null) {
				for (c = 0; c < layerSize; c++) {
					neu1[c + a * layerSize] += last_word.syn0[c + a * layerSize];
				}
			} else {
				for (c = 0; c < layerSize; c++) {
					neu1[c + a * layerSize] = 0;
				}
			}
			// }
		}



		double fpos = 0;
		for (k = 0; k < layerSize * (window * 2 + 1); k++) {
			fpos += neu1[k] * word.syn21[k];
		}
		if (fpos <= -MAX_EXP)
			return -1;
		else if (fpos >= MAX_EXP)
			return -1;
		else {
			//fpos = expTable[(int) ((fpos + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];
			fpos=Math.exp(fpos)/(1+Math.exp(fpos));
		}

		double fneg = 0;
		for (k = 0; k < layerSize * (window * 2 + 1); k++) {
			fneg += neu1[k] * word.syn20[k];
		}
		if (fneg <= -MAX_EXP)
			return -1;
		else if (fneg >= MAX_EXP)
			return -1;
		else {
			//fneg = expTable[(int) ((fneg + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];
			fneg=Math.exp(fneg)/(1+Math.exp(fneg));
		}

		if (fpos > fneg) {
			return 1;
		}

		return 0;
	}
	/*
	public int predictTest(int index, List<WordNeuron> sentence, int b) {
		

		double[] neu1e = new double[layerSize ];// 误差项
		double[] neu1 = new double[layerSize ];// 误差项
		WordNeuron last_word;
		WordNeuron word = sentence.get(index);
		int a, c = 0;


		for (a = 0; a < window * 2 + 1 ; a++) {

			// if (a != window) {
			c = index - window + a;
			if ((c < 0)||(c >= sentence.size()))
			{
				continue;
			}
			
			last_word = sentence.get(c);
			if (last_word != null) {
				for (c = 0; c < layerSize; c++) {
					neu1[c ] += last_word.syn0[c];
				}
			} 
			// }
		}



		double fpos = 0;
		for (int i = 0; i < layerSize; i++) {
			fpos += neu1[i] * posweight[i];
		}
		if (fpos <= -MAX_EXP)
			return -1;
		else if (fpos >= MAX_EXP)
			return -1;
		else {
			fpos = expTable[(int) ((fpos + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];
		}

		double fneg = 0;
		for (int i = 0; i < layerSize; i++) {
			fneg += neu1[i] * negweight[i];
		}
		if (fneg <= -MAX_EXP)
			return -1;
		else if (fneg >= MAX_EXP)
			return -1;
		else {
			fneg = expTable[(int) ((fneg + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];
		}

		if (fpos > fneg) {
			return 1;
		}
       
		return 0;
		
	}
 */
	
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
		LearnNNSeg learn = new LearnNNSeg();
		long start = System.currentTimeMillis();
		learn.learnFile(new File("nnseg/ctb6.train.seg"));

		//String text = "7月14日下午，部分中央企业、地方国企和民营企业负责人与部分国务院相关部委负责人走进中南海会议室。下午3点，国务院总理李克强准时走进会议室，他把这些人邀请进中南海，正是想听听大家对上半年经济形势的看法，同时讨论一下下半年经济发展的意见建议。";
		String text="据 初步 统计 ， 目前 在 中国 境内 承包 工程 的 国外 承包商 已 有 一百三十七 家 ， 承包 的 工程 达 一百四十一 项 ， 其中 最大 规模 的 项目 达 二十七点七亿 元 ； 中外 合资 合作 的 建筑 企业 近 二千 家 。中国 建筑业 对 外 开放 始于 八十年代 。";
		System.out.println(learn.seg(text));
		// System.out.println("use time " + (System.currentTimeMillis() -
		// start));
		// learn.saveModel(new File("library/javaVector"));

	}

}
