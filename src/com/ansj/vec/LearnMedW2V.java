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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import love.cq.util.MapCount;

import org.jmlp.file.utils.FileToArray;
import org.jmlp.medw2v.FileConvertFormat;
import org.jmlp.medw2v.Medw2vParms;
import org.jmlp.perceptron.FeatureIndex;
import org.jmlp.perceptron.LABEL;
import org.jmlp.perceptron.PerceptronLearn;
import org.jmlp.perceptron.StrToClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ansj.vec.domain.HiddenNeuron;
import com.ansj.vec.domain.Neuron;
import com.ansj.vec.domain.WordNeuron;
import com.ansj.vec.util.Haffman;
import org.jmlp.math.random.ArrayUtil;

public class LearnMedW2V {
    static Logger logger =  LoggerFactory.getLogger(LearnMedW2V.class);
    private Map<String, Neuron> wordMap = new HashMap<>();
    
    PerceptronLearn pl=new PerceptronLearn();
    Medw2vParms mw2vparms=new Medw2vParms();
    public double[] mu_corpus;
    
    /**
     * 向量 标记的权重矩阵
     * 行是向量的每个元素
     * 列是每个标记
     */
    //public double[] veletag_martrix;
    
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

    private Boolean isCbow = false;

    private double[] expTable = new double[EXP_TABLE_SIZE];

    private int trainWordsCount = 0;

    private int MAX_EXP = 6;
    
    public LABEL[] local_label_set ;

    public LearnMedW2V(Boolean isCbow, Integer layerSize, Integer window, Double alpha, Double sample) {
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

    public LearnMedW2V() {
        createExpTable();
    }

    /**
     * 训练样本格式,line .=. <key>\001<tag>\001<docid>\001<seg line>
     * trainModel
     * @throws IOException 
     */
    private void trainModel(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(new FileInputStream(file)))) {
            String temp = null;
            long nextRandom = 5;
            int wordCount = 0;
            int lastWordCount = 0;
            int wordCountActual = 0;
            
            String[] seg_arr=null;
            String   seg_text="";
            String label_str="";
            LABEL tlabel=null;
            int docid=0;
            while ((temp = br.readLine()) != null) {
                if (wordCount - lastWordCount > 10000) {
                    System.out
                        .println("alpha:" + alpha + "\tProgress: "
                                 + (int) (wordCountActual / (double) (trainWordsCount + 1) * 100)
                                 + "%");
                    wordCountActual += wordCount - lastWordCount;
                    lastWordCount = wordCount;
                    alpha = startingAlpha * (1 - wordCountActual / (double) (trainWordsCount + 1));
                    if (alpha < startingAlpha * 0.0001) {
                        alpha = startingAlpha * 0.0001;
                    }
                }
                
                seg_arr=temp.split("\001");
                if(seg_arr.length<4)
                {
                	continue;
                }
                label_str=seg_arr[1].trim();
                tlabel=StrToClass.str2labelSimple(label_str, mw2vparms.label_num);
                
                seg_text=seg_arr[3];
                docid=Integer.parseInt(seg_arr[2]);
                
                String[] strs = seg_text.split(" ");
                wordCount += strs.length;
                List<WordNeuron> sentence = new ArrayList<WordNeuron>();
                for (int i = 0; i < strs.length; i++) {
                    Neuron entry = wordMap.get(strs[i]);
                    if (entry == null) {
                        continue;
                    }
                    // The subsampling randomly discards frequent words while keeping the ranking same
                    if (sample > 0) {
                        double ran = (Math.sqrt(entry.freq / (sample * trainWordsCount)) + 1)
                                     * (sample * trainWordsCount) / entry.freq;
                        nextRandom = nextRandom * 25214903917L + 11;
                        if (ran < (nextRandom & 0xFFFF) / (double) 65536) {
                            continue;
                        }
                    }
                    sentence.add((WordNeuron) entry);
                }
                double[] eta_doc=matrix2vec(tlabel,docid);
                
                for (int index = 0; index < sentence.size(); index++) {
                    nextRandom = nextRandom * 25214903917L + 11;
                    if (isCbow) {
                        cbowGram(index, sentence, (int) nextRandom % window,eta_doc,docid);
                    } else {
                        skipGram(index, sentence, (int) nextRandom % window,eta_doc,docid);
                    }
                }
            }
            System.out.println("Vocab size: " + wordMap.size());
            System.out.println("Words in train file: " + trainWordsCount);
            System.out.println("sucess train over!");
        }
    }
    
    
    
    /**
     * 训练样本格式,line .=. <key>\001<tag>\001<docid>\001<seg line>
     * trainModel
     * @throws IOException 
     */
    private void trainModelThree(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(new FileInputStream(file)))) {
            String temp = null;
            long nextRandom = 5;
            int wordCount = 0;
            int lastWordCount = 0;
            int wordCountActual = 0;
            
            String[] seg_arr=null;
            String   seg_text="";
            String label_str="";
            LABEL tlabel=null;
            int docid=0;
            while ((temp = br.readLine()) != null) {
                if (wordCount - lastWordCount > 10000) {
                    System.out
                        .println("alpha:" + alpha + "\tProgress: "
                                 + (int) (wordCountActual / (double) (trainWordsCount + 1) * 100)
                                 + "%");
                    wordCountActual += wordCount - lastWordCount;
                    lastWordCount = wordCount;
                    alpha = startingAlpha * (1 - wordCountActual / (double) (trainWordsCount + 1));
                    if (alpha < startingAlpha * 0.0001) {
                        alpha = startingAlpha * 0.0001;
                    }
                }
                
                seg_arr=temp.split("\001");
                if(seg_arr.length<4)
                {
                	continue;
                }
                label_str=seg_arr[1].trim();
                //tlabel=StrToClass.str2labelSimple(label_str, mw2vparms.label_num);
                tlabel=StrToClass.str2labelthree(label_str, mw2vparms);
                seg_text=seg_arr[3];
                docid=Integer.parseInt(seg_arr[2]);
                
                String[] strs = seg_text.split(" ");
                wordCount += strs.length;
                List<WordNeuron> sentence = new ArrayList<WordNeuron>();
                for (int i = 0; i < strs.length; i++) {
                    Neuron entry = wordMap.get(strs[i]);
                    if (entry == null) {
                        continue;
                    }
                    // The subsampling randomly discards frequent words while keeping the ranking same
                    if (sample > 0) {
                        double ran = (Math.sqrt(entry.freq / (sample * trainWordsCount)) + 1)
                                     * (sample * trainWordsCount) / entry.freq;
                        nextRandom = nextRandom * 25214903917L + 11;
                        if (ran < (nextRandom & 0xFFFF) / (double) 65536) {
                            continue;
                        }
                    }
                    sentence.add((WordNeuron) entry);
                }
                double[] eta_doc=matrix2vecthree(tlabel,docid);
                
                for (int index = 0; index < sentence.size(); index++) {
                    nextRandom = nextRandom * 25214903917L + 11;
                    if (isCbow) {
                        cbowGram(index, sentence, (int) nextRandom % window,eta_doc,docid);
                    } else {
                        skipGram(index, sentence, (int) nextRandom % window,eta_doc,docid);
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
     * @param sentence
     * @param neu1 
     */
    private void skipGram(int index, List<WordNeuron> sentence, int b,double[] eta_doc,int docid) {
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

            double[] neu1e = new double[layerSize];//误差项
            //HIERARCHICAL SOFTMAX
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
                // double g = f * (1 - f) * (word.codeArr[i] - f) * alpha;
                //double g =  (word.codeArr[i] - f) * alpha;
                // Propagate errors output -> hidden
                for (c = 0; c < layerSize; c++) {
                    neu1e[c] += g * out.syn1[c];
                }
                // Learn weights hidden -> output
                for (c = 0; c < layerSize; c++) {
                    out.syn1[c] += g * we.syn0[c];
                   // out.syn1[c] += mu_corpus[docid]*eta_doc[c];
                
                }
            }

            // Learn weights input -> hidden
            for (int j = 0; j < layerSize; j++) {
                we.syn0[j] += neu1e[j];
                //we.syn0[j] +=mu_corpus[docid]*eta_doc[j];
            }
        }
           /*
            for (int cc = 0; cc < layerSize; cc++)
            {
                    
                    word.syn0[cc]+=mu_corpus[docid]*eta_doc[cc];
            }
          */
           int cc;
           for (int aa = b; aa < window * 2 + 1 - b; aa++) {
            if (aa == window) {
                continue;
            }
            cc = index - window + aa;
            if (cc < 0 || cc >= sentence.size()) {
                continue;
            }
            WordNeuron wwe = sentence.get(cc);
            
            for (int ll = 0; ll < layerSize; ll++)
            {

                    wwe.syn0[ll]+=(mu_corpus[docid]*eta_doc[ll]/(double)window);
            }
           }

    }

    /**
     * 词袋模型
     * syn0:M 要修改
     * syn1:M''
     * @param index
     * @param sentence
     * @param b
     */
    private void cbowGram(int index, List<WordNeuron> sentence, int b,double[] eta_doc,int docid) {
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
                f = expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];
            // 'g' is the gradient multiplied by the learning rate
            //            double g = (1 - word.codeArr[d] - f) * alpha;
            double g = ( word.codeArr[d] - f) * alpha;
            //double g = f * (1 - f) * (word.codeArr[d] - f) * alpha;
           
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
                //logger.info("layerSize:"+layerSize);
                for (c = 0; c < layerSize; c++)
                {
                    last_word.syn0[c] += neu1e[c];
                    //logger.info("c:"+c);
                    last_word.syn0[c]+=mu_corpus[docid]*eta_doc[c];
                }
            }

        }
    }

    /**
     * 统计词频
     * @param file
     * @throws IOException
     */
    private void readVocab(File file) throws IOException {
        MapCount<String> mc = new MapCount<>();
        HashMap<String,Integer> label_hash=new HashMap<String,Integer>();
        int temp_docid;
        int max_docid=-1;
        int doc_num=0;
        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(new FileInputStream(file)))) {
            String temp = null;
            String[] seg_arr=null;
            String seg_text="";
            String label_str="";
            while ((temp = br.readLine()) != null) {
            	seg_arr=temp.split("\001");
            	if(seg_arr.length<4)
            	{
            		continue;
            	}
            	label_str=seg_arr[1].trim();
            	if(!(label_hash.containsKey(label_str)))
            	{
            		label_hash.put(label_str, 1);
            	}
            	
            	seg_text=seg_arr[3];
            	temp_docid=Integer.parseInt(seg_arr[2]);
            	if(temp_docid>max_docid)
            	{
            		max_docid=temp_docid;
            	}
            	doc_num++;
                String[] split = seg_text.split(" ");
                trainWordsCount += split.length;
                for (String string : split) {
                    mc.add(string);
                }
            }
        }
        for (Entry<String, Integer> element : mc.get().entrySet()) {
        	//logger.info("ekey:"+element.getKey()+"eval:"+element.getValue());
            wordMap.put(element.getKey(), new WordNeuron(element.getKey(), element.getValue(),
                layerSize));
        }
        mw2vparms.label_num=label_hash.size();
        mw2vparms.doc_num=doc_num;
        
        mu_corpus=new double[max_docid+1];
        pl.worst_label=new LABEL[max_docid+1];
        System.out.println("max_docid:"+max_docid);
        for(int i=0;i<mu_corpus.length;i++)
        {
        	mu_corpus[i]=((double)1)/((double)doc_num);
        }
        
        for(int i=0;i<pl.worst_label.length;i++)
        {
        	pl.worst_label[i]=StrToClass.str2labelSimple("1",mw2vparms.label_num );
        }
        local_label_set = new LABEL[mw2vparms.label_num];

	for (int i = 1; i <= local_label_set.length; i++) {
		local_label_set[i - 1] = StrToClass.str2labelSimple(i+"", mw2vparms.label_num);
	}
        
    }

    /**
     * 统计词频
     * @param file
     * @throws IOException
     */
    private void readVocabThree(File file) throws IOException {
    	
        MapCount<String> mc = new MapCount<>();
        HashMap<String,Integer> label_hash=new HashMap<String,Integer>();
        HashMap<String,Integer> first_label_hash=new HashMap<String,Integer>();
        HashMap<String,Integer> second_label_hash=new HashMap<String,Integer>();
        HashMap<String,Integer> third_label_hash=new HashMap<String,Integer>();
        
        String[] label_seg=null;
        String first_label;
        String second_label;
        String third_label;
        String label_g_index="";
        int temp_docid;
        int max_docid=-1;
        int doc_num=0;
        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(new FileInputStream(file)))) {
            String temp = null;
            String[] seg_arr=null;
            String seg_text="";
            String label_str="";
            while ((temp = br.readLine()) != null) {
            	seg_arr=temp.split("\001");
            	if(seg_arr.length<4)
            	{
            		continue;
            	}
            	label_str=seg_arr[1].trim();
              	label_seg=label_str.split("\\|");
            	if(label_seg.length!=4)
            	{
            		continue;
            	}
            	
            	if(!(label_hash.containsKey(label_str)))
            	{
            		label_hash.put(label_str, 1);
            	}
            	
          
            	first_label=label_seg[0].trim();
            	second_label=label_seg[1].trim();
            	third_label=label_seg[2].trim();
            	label_g_index=label_seg[3].trim();
            	if(!(first_label_hash.containsKey(first_label)))
            	{
            		first_label_hash.put(first_label,1);
            	}
           
            	if(!(second_label_hash.containsKey(second_label)))
            	{
            		second_label_hash.put(second_label,1);
            	}
            	
            	if(!(third_label_hash.containsKey(third_label)))
            	{
            		third_label_hash.put(third_label,1);
            	}
            	
            	seg_text=seg_arr[3];
            	temp_docid=Integer.parseInt(seg_arr[2]);
            	if(temp_docid>max_docid)
            	{
            		max_docid=temp_docid;
            	}
            	doc_num++;
                String[] split = seg_text.split(" ");
                trainWordsCount += split.length;
                for (String string : split) {
                    mc.add(string);
                }
            }
        }
        
        for (Entry<String, Integer> element : mc.get().entrySet()) {
        	//logger.info("ekey:"+element.getKey()+"eval:"+element.getValue());
                wordMap.put(element.getKey(), new WordNeuron(element.getKey(), element.getValue(),
                layerSize));
        }
        mw2vparms.label_num=label_hash.size();
        mw2vparms.first_label_num=first_label_hash.size();
        mw2vparms.second_label_num=second_label_hash.size();
        mw2vparms.third_label_num=third_label_hash.size();
        
        mw2vparms.doc_num=doc_num;
        
        mu_corpus=new double[max_docid+1];
        pl.worst_label=new LABEL[max_docid+1];
        System.out.println("max_docid:"+max_docid);
        for(int i=0;i<mu_corpus.length;i++)
        {
        	mu_corpus[i]=((double)1)/((double)doc_num);
        }
        /***
        for(int i=0;i<pl.worst_label.length;i++)
        {
        	//pl.worst_label[i]=StrToClass.str2labelSimple("1",mw2vparms.label_num );
                pl.worst_label[i]=StrToClass.str2labelthree("1",mw2vparms.label_num );
        }
        ****/
        local_label_set = new LABEL[mw2vparms.label_num];
		
	Set<String> label_enum=label_hash.keySet();
	Iterator<String> label_it=label_enum.iterator();
	String temp_label_str="";
		
        String first_label_str="";
	int it_index=0;
	while(label_it.hasNext())
	{   
		temp_label_str=label_it.next();

                if(it_index==0)
                {
                   first_label_str=temp_label_str;
                }
		local_label_set[it_index++]=StrToClass.str2labelthree(temp_label_str, mw2vparms);
	}


        for(int i=0;i<pl.worst_label.length;i++)
        {
                pl.worst_label[i]=StrToClass.str2labelthree(first_label_str,mw2vparms );
        }		
	/*
	for (int i = 1; i <= local_label_set.length; i++) {
		local_label_set[i - 1] = StrToClass.str2labelSimple(i+"", mw2vparms.label_num);
	}
        */
    }
    
    /**
     * Precompute the exp() table
     * f(x) = x / (x + 1)
     */
    private void createExpTable() {
        for (int i = 0; i < EXP_TABLE_SIZE; i++) {
            expTable[i] = Math.exp(((i / (double) EXP_TABLE_SIZE * 2 - 1) * MAX_EXP));
            expTable[i] = expTable[i] / (expTable[i] + 1);
        }
    }

    /**
     * 根据文件学习
     * @param file
     * @throws IOException 
     */
    public void learnFile(File file,File origin_test_file) throws Exception {
    	layerSize=200;
    	File formatFile=new File("temp/medw2v/format.txt");
        File testformatFile=new File("temp/medw2v/test_format.txt");
    	String label_file="temp/medw2v/label.txt";
        
        //label_name,docid,text转换成rankey,label_index,docid,segtext
    	FileConvertFormat.convertFormatDictLabel(file, formatFile, label_file);
        FileConvertFormat.convertFormatDictLabel(origin_test_file, testformatFile, label_file);  
         	
        readVocab(formatFile);
        new Haffman(layerSize).make(wordMap.values());
        
        //查找每个神经元
        for (Neuron neuron : wordMap.values()) {
            ((WordNeuron)neuron).makeNeurons() ;
        }
        
        /*****改成medw2v train*******/
        //trainModel(file);
        
        trainMedw2vModel(formatFile,testformatFile);    
    }

    
    /**
     * 根据文件学习
     * @param file
     * @throws IOException 
     */
    public void learnFileThree(File file,File origin_test_file) throws Exception {
    	layerSize=200;
    	File formatFile=new File("temp/medw2v/format.txt");
        File testformatFile=new File("temp/medw2v/test_format.txt");
    	String label_file="temp/medw2v/label.txt";
    	FileConvertFormat.convertFormatDictLabelThree(file, formatFile, label_file);
        FileConvertFormat.convertFormatDictLabelThree(origin_test_file, testformatFile, label_file);   	
        readVocabThree(formatFile);
        new Haffman(layerSize).make(wordMap.values());
        
        //查找每个神经元
        for (Neuron neuron : wordMap.values()) {
            ((WordNeuron)neuron).makeNeurons() ;
        }
        
        /*****改成medw2v train*******/
        //trainModel(file);
        
        trainMedw2vModelThree(formatFile,testformatFile);    
    }
    
    /**
     * 保存模型
     */
    public void saveModel(File file) {
        // TODO Auto-generated method stub

        try (DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(
            new FileOutputStream(file)))) {
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

    /**
     * input_file
     * 格式:<key>\001<tag>\001<docid>\001\001<seg_line>
     * output_file
     * 格式:<key>\001<tag>\001<docid>\001<n dim vec>
     */
    public void vecTagFile(File input_file,File output_file) throws Exception
    {
       String[] samples=FileToArray.fileToDimArr(input_file);
       FileWriter fw=new FileWriter(output_file);	
       PrintWriter pw=new PrintWriter(fw);
       String[] seg_arr=null;
       String dockey="";
       String label_str="";
       String docid="";
       String seg_text="";
       String line="";
       String[] word_seg=null;
       double[] vec = new double[layerSize];
       for(int i=0;i<vec.length;i++)
       {
    	   vec[i]=0;
       }
      
       
       double[] syn0vec=null;
       String word="";
       WordNeuron wn;
       Neuron nn;       
       String vec_sam_line="";
       String vec_str="";
       for(int i=0;i<samples.length;i++)
       {
    	   line=samples[i];
    	   seg_arr=line.split("\001");
    	   if(seg_arr.length<4)
    	   {
    		   continue;
    	   }
    	   dockey=seg_arr[0].trim();
    	   label_str=seg_arr[1].trim();
    	   docid=seg_arr[2].trim();
    	   seg_text=seg_arr[3].trim();
    	   word_seg=seg_text.split("\\s+");
    	
    	   for(int j=0;j<vec.length;j++)
    	   {
    	    	   vec[j]=0;
    	   }

    	   for(int j=0;j<word_seg.length;j++)
    	   {
    		   word=word_seg[j].trim();
                   nn=wordMap.get(word);
                   if(nn==null)
                   {
                     continue;
                   }  
    		   //wn=(WordNeuron)wordMap.get(word);
                   wn=(WordNeuron)nn;
    		   syn0vec=wn.syn0;
    		   vec=ArrayUtil.sum_weight(vec,syn0vec);		   
    	   }
    	   vec_str=ArrayUtil.arrayToSamStr(vec);
    	   vec_sam_line=dockey+"\001"+label_str+"\001"+docid+"\001"+vec_str;
    	   pw.println(vec_sam_line);
       }
       fw.close();
       pw.close();
       
    }

    public void trainMedw2vModel(File input_file,File test_file) throws Exception
    {
        pl.weights = new double[(layerSize + 1) * (mw2vparms.label_num)];
        for (int i = 0; i < pl.weights.length; i++) {
	   pl.weights[i] = 0;
        }
        for(int l=0;l<50;l++)
	{
           logger.info("outer loop:"+l);			
    	   /***训练word2vec***/
    	   trainModel(input_file);
    	   File vectag_file=new File("temp/medw2v/vectag.txt");
    	   File vectag_testfile=new File("temp/medw2v/vectag_test.txt");
           //格式:<key>\001<tag>\001<docid>\001\001<seg_line>  转换成 格式:<key>\001<tag>\001<docid>\001<n dim vec>
    
    	   vecTagFile(input_file,vectag_file);
           vecTagFile(test_file,vectag_testfile);
 	   pl.weights = new double[(layerSize + 1) * (mw2vparms.label_num)];
           for (int i = 0; i < pl.weights.length; i++) {
 			pl.weights[i] = 0;
           }
    	   /**训练perceptron模型**/
    	   pl.learnModel(vectag_file,mw2vparms.label_num,layerSize,vectag_testfile);
         }
    }
    
    public void trainMedw2vModelThree(File input_file,File test_file) throws Exception
    {
        pl.weights = new double[(layerSize + 1) * (mw2vparms.first_label_num+mw2vparms.second_label_num+mw2vparms.third_label_num)];
        for (int i = 0; i < pl.weights.length; i++) {
	   pl.weights[i] = 0;
        }
        for(int l=0;l<50;l++)
	{
           logger.info("outer loop:"+l);			
    	   /***训练word2vec***/
    	   trainModelThree(input_file);
    	   File vectag_file=new File("temp/medw2v/vectag.txt");
    	   File vectag_testfile=new File("temp/medw2v/vectag_test.txt");
    	   vecTagFile(input_file,vectag_file);
           vecTagFile(test_file,vectag_testfile);
 	   pl.weights = new double[(layerSize + 1) * (mw2vparms.first_label_num+mw2vparms.second_label_num+mw2vparms.third_label_num)];
           for (int i = 0; i < pl.weights.length; i++) {
 			 pl.weights[i] = 0;
           }
    	   /**训练perceptron模型**/
    	   pl.learnModelthree(vectag_file,mw2vparms.label_num,layerSize,vectag_testfile,local_label_set,mw2vparms);
         }
    }
    
    public double[] matrix2vec(LABEL y,int docid)
    {
    	double[] vec=new double[layerSize];
       	for(int i=0;i<vec.length;i++)
    	{
       		vec[i]=0;
    	}
       	
       	LABEL temp_label;
    	for(int i=0;i<vec.length;i++)
    	{
    		
    		for(int j=0;j<local_label_set.length;j++)
    		{
    		   temp_label=local_label_set[j];
    		   if(temp_label.index==pl.worst_label[docid].index)
    		   {
    		     vec[i]+=(pl.weights[FeatureIndex.featIndexNoWordMap((i+1), y)]-pl.weights[FeatureIndex.featIndexNoWordMap((i+1), temp_label)]);
    		   }
    		}
    	}
        /*
        for(int i=0;i<vec.length;i++)
        {
           vec[i]=vec[i]/((double)mw2vparms.doc_num);
        }
        */
    	vec=ArrayUtil.normalize(vec);
    	return vec;
    }
    


    public double[] matrix2vecthree(LABEL y,int docid)
    {
        double[] vec=new double[layerSize];
        for(int i=0;i<vec.length;i++)
        {
                vec[i]=0;
        }

        LABEL temp_label;
        for(int i=0;i<vec.length;i++)
        {

                for(int j=0;j<local_label_set.length;j++)
                {
                   temp_label=local_label_set[j];
                   if(temp_label.equals_three(pl.worst_label[docid]))
                   {
                     
                      vec[i]+=(pl.weights[FeatureIndex.featIndexNoWordFirstMap((i+1), y)]-pl.weights[FeatureIndex.featIndexNoWordFirstMap((i+1), temp_label)]);
                      vec[i]+=(pl.weights[FeatureIndex.featIndexNoWordSecondMap((i+1), y)]-pl.weights[FeatureIndex.featIndexNoWordSecondMap((i+1), temp_label)]);
                      vec[i]+=(pl.weights[FeatureIndex.featIndexNoWordThirdMap((i+1), y)]-pl.weights[FeatureIndex.featIndexNoWordThirdMap((i+1), temp_label)]);
                   }
                }
        }
        vec=ArrayUtil.normalize(vec);
        return vec;
    }
    
    public static void main(String[] args) throws Exception {
    	
    	/*
        Learn learn = new Learn();
        long start = System.currentTimeMillis() ;
        learn.learnFile(new File("library/xh.txt"));
        System.out.println("use time "+(System.currentTimeMillis()-start));
        learn.saveModel(new File("library/javaVector"));
        */
    	LearnMedW2V lean = new LearnMedW2V() ;
        File corpusFile=new File("temp/medw2v/tr.txt");
        File test_corpusFile=new File("temp/medw2v/te.txt");
        
        lean.learnFileThree(corpusFile,test_corpusFile) ;
        
    }
}
