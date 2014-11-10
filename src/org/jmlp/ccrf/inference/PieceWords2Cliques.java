package org.jmlp.ccrf.inference;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.objectbank.ResettableReaderIteratorFactory;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;
import edu.stanford.nlp.sequences.ObjectBankWrapper;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.wordseg.ChineseBeqReaderAndWriter;




public class PieceWords2Cliques extends Words2Cliques {
	
	private DocumentReaderAndWriter<CoreLabel> plainTextReaderAndWriter;
	
	private Set<String> knownLCWords;

	private SeqClassifierFlags flags;
	
	@Override
	public void init() {
		
		knownLCWords = Collections
				.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

		Properties props = new Properties();
		 props.setProperty("serDictionary", "data/dict-chris6.ser.gz");
		try {
			FileInputStream fis = new FileInputStream("config/train_beq2.prop");
			props.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		flags = new SeqClassifierFlags(props);
		
		plainTextReaderAndWriter = new ChineseBeqReaderAndWriter();
		plainTextReaderAndWriter.init(flags);
		
	}

	@Override
	public Clique[] word2Cliques(Word[] words) {
		// TODO Auto-generated method stub

		return null;
	}

	@Override
	public Clique[] distributeInnerCliquesOverOuterCliques(Word[] innerWords,
			Word[] outerWords) {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public List<CoreLabel> labeledText2CoreLabels(String labeledText) {
		
		ObjectBank<List<CoreLabel>> documents = new ObjectBankWrapper<CoreLabel>(
				flags, new ObjectBank<List<CoreLabel>>(
						new ResettableReaderIteratorFactory(labeledText),
						plainTextReaderAndWriter), knownLCWords);

		Iterator<List<CoreLabel>> it = documents.iterator();
		List<CoreLabel> cl;
		if (it.hasNext()) {
			cl = it.next();
			
		}
		else
		{
			cl=null;
		}
		return cl;
	}

	public DocumentReaderAndWriter<CoreLabel> getPlainTextReaderAndWriter() {
		return plainTextReaderAndWriter;
	}

	public void setPlainTextReaderAndWriter(DocumentReaderAndWriter<CoreLabel> plainTextReaderAndWriter) {
		this.plainTextReaderAndWriter = plainTextReaderAndWriter;
	}

	public Set<String> getKnownLCWords() {
		return knownLCWords;
	}

	public void setKnownLCWords(Set<String> knownLCWords) {
		this.knownLCWords = knownLCWords;
	}

	public SeqClassifierFlags getFlags() {
		return flags;
	}

	public void setFlags(SeqClassifierFlags flags) {
		this.flags = flags;
	}
	
	public static void main(String[] args) {
		/*
		Set<String> knownLCWords = Collections
				.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		Properties props = new Properties();
		 props.setProperty("serDictionary", "data/dict-chris6.ser.gz");
		try {
			FileInputStream fis = new FileInputStream("conf/train_beq2.prop");
			props.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		SeqClassifierFlags flags = new SeqClassifierFlags(props);
		DocumentReaderAndWriter<CoreLabel> plainTextReaderAndWriter = new ChineseBeqReaderAndWriter();
		plainTextReaderAndWriter.init(flags);
		*/
		
		PieceWords2Cliques pc=new PieceWords2Cliques();
		pc.init();
		
		String sentences = "再/1 看/0 新/1 白娘子/0 传奇/0 ，/1 才/1 明白/1 小青/1 和/1 张公子/1 的/1 爱情/0 故事/1 那么/1 悲情/1 。/1";
		List<CoreLabel> cl=pc.labeledText2CoreLabels(sentences);
		
		for (int j = 0; j < cl.size(); j++) {
			System.out.println(cl.get(j).toString() + " ");
		}
		
		/*
		ObjectBank<List<CoreLabel>> documents = new ObjectBankWrapper<CoreLabel>(
				flags, new ObjectBank<List<CoreLabel>>(
						new ResettableReaderIteratorFactory(sentences),
						plainTextReaderAndWriter), knownLCWords);
		Iterator<List<CoreLabel>> it = documents.iterator();
		while (it.hasNext()) {
			List<CoreLabel> cl = it.next();
			for (int j = 0; j < cl.size(); j++) {
				System.out.println(cl.get(j).toString() + " ");
			}
			System.out.println();
		}
		*/

		
	}



}
