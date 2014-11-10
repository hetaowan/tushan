package com.ansj.vec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jmlp.math.random.ArrayUtil;
import org.jmlp.math.random.SimFunc;
import org.jmlp.medw2v.FileConvertFormat;
import org.jmlp.perceptron.WeightsUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ansj.vec.domain.HiddenNeuron;
import com.ansj.vec.domain.Neuron;
import com.ansj.vec.domain.WordNeuron;

public class ClassifySupervised {

    private Map<String, Neuron> labelMap = new HashMap<>();
    private Map<String, Neuron> wordMap = new HashMap<>();
    private int layerSize=0;
    private int correctNum=0;
    private int incorrectNum=0;
    
    public int EXP_TABLE_SIZE = 1000;

    public double[] expTable = new double[EXP_TABLE_SIZE];

    public int MAX_EXP = 6;
    
	static Logger logger =  LoggerFactory.getLogger(ClassifySupervised.class);
    
	public double classifyFile(File file) throws IOException
	{
		File testFormatFile=new File("temp/medw2v/test_format.txt");
    	try{
    		FileConvertFormat fcf=new FileConvertFormat();
    	    fcf.convertFormatSupervise(file, testFormatFile);
    	    predictFile(testFormatFile);
    	}
    	catch(Exception e){}
    	
    	double acc=(double)correctNum/(double)(correctNum+incorrectNum);
    	
    	return acc;
	}
	
	
	public void predictFile(File file) throws IOException 
	{
	     try (BufferedReader br = new BufferedReader(
	             new InputStreamReader(new FileInputStream(file)))) {
	             String temp = null;

	             while ((temp = br.readLine()) != null) {                
	            	 predictOneLine(temp);                              
	             }
	         }
	}
	
	public void predictOneLine(String recLine) throws IOException 
	{
		try{
    	String label_text="";
    	String seg_text="";
    	String[] seg_arr=null;
    	seg_arr=recLine.split("\001");
    	
    	if(seg_arr.length!=2)
    	{
    		return;
    	}
    	label_text=seg_arr[0].trim();
    	seg_text=seg_arr[1].trim();
    	logger.info("label_text:"+label_text+" seg_text:"+seg_text);
    	//System.out.println("label_text:"+label_text);
    	String[] strs = seg_text.split(" ");
    	WordNeuron wn=null;
    	 
    	double[] docvec=new double[layerSize];
    	for(int i=0;i<docvec.length;i++)
    	{
    		docvec[i]=0;
    	}
    	WeightsUpdate wc=new WeightsUpdate();
    	
    
        for (int i = 0; i < strs.length; i++) {
            wn = (WordNeuron)wordMap.get(strs[i]);
            //System.out.print(wn.name+" ");
            if(wn==null)
            {
            	continue;
            }
            
            docvec=wc.plain_sum_weight(docvec, wn.syn0);
        }
    	
    	String possLabel="";
        WordNeuron lwn=null;
        
        double maxSumk=-100000000;
        String maxLabel="";
        
        double sumk=0;
        
        
        double mm=0;
        double g=0;
        double f=0;
        for (Entry<String,Neuron> len : labelMap.entrySet()) {
        	possLabel=len.getKey();
        	possLabel=possLabel.trim();
            lwn=(WordNeuron)len.getValue();
            List<Neuron> lneurons = lwn.neurons;
           
            sumk=0;
            String mm_str="";
            for(int d=0;d<lneurons.size();d++)
            {            	
            	HiddenNeuron dout = (HiddenNeuron) lneurons.get(d);
                mm=ArrayUtil.dotProduct(dout.syn1, docvec);
                mm_str+=d+":"+mm+" ";
            	f=mm;
                MAX_EXP=100;                           
                if (f <= -MAX_EXP)
                    continue;
                else if (f >= MAX_EXP)
                    continue;
                /*
                else
                    f = expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];  
                */
                f=Math.exp(f)/(1+Math.exp(f));
                
            	// tempf=(double)1/(double)(1+Math.exp(tempf));
                //g=(lwn.codeArr[d]+f-1)*mm-SimFunc.entropy(f);
                if(lwn.codeArr[d]==0)
                {
                 f=1-f;
                }
            	sumk+=Math.log(f);
            }
            logger.info("mm:"+mm_str);
           logger.info("sumk:"+sumk+"  possLabel:"+possLabel);
            if(sumk>maxSumk)
            {
            	maxSumk=sumk;
            	maxLabel=possLabel;
            }     
        }
        
        logger.info("maxLabel:"+maxLabel+" maxSumk:"+maxSumk);
        
        if(maxLabel.equals(label_text))
        {
        	System.out.println("right");
        	correctNum++;
        }
        else
        {
        	System.out.println("false");
        	incorrectNum++;
        }
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}


	public Map<String, Neuron> getLabelMap() {
		return labelMap;
	}


	public void setLabelMap(Map<String, Neuron> labelMap) {
		this.labelMap = labelMap;
	}
	
	public Map<String, Neuron> getWordMap() {
		return wordMap;
	}

	public void setWordMap(Map<String, Neuron> wordMap) {
		this.wordMap = wordMap;
	}


	public int getLayerSize() {
		return layerSize;
	}


	public void setLayerSize(int layerSize) {
		this.layerSize = layerSize;
	}
	
}
