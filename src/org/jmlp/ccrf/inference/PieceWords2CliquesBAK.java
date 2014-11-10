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
import edu.stanford.nlp.sequences.ObjectBankWrapper;
import edu.stanford.nlp.objectbank.ResettableReaderIteratorFactory;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;

import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.wordseg.ChineseBeqReaderAndWriter;
import edu.stanford.nlp.wordseg.Sighan2005DocumentReaderAndWriter;

public class PieceWords2CliquesBAK extends Words2Cliques {

	@Override
	public void init() {
		// TODO Auto-generated method stub

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

	public static void main(String[] args) {
		Set<String> knownLCWords = Collections
				.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		Properties props = new Properties();
		try {
			FileInputStream fis = new FileInputStream("train.prop");
			props.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		SeqClassifierFlags flags = new SeqClassifierFlags(props);
		DocumentReaderAndWriter<CoreLabel> plainTextReaderAndWriter = new ChineseBeqReaderAndWriter();
		plainTextReaderAndWriter.init(flags);
		String sentences = "二手房/1 市场/1 在/1 经历/1 了/1 3/1 月份/0 的/1 成交/1 暴涨/1 之后/1 ，/1 伴随/1 着/1 3/1 月/0 底/0 地方/1 版/0 “/1 国/1 五/0 条/0 ”/1 陆续/1 出台/1 ，/1 市场/1 逐步/1 回归/1 理性/1 ，/1 成交/1 总量/0 环比/1 5/1 月/0 下降/1 6.71%/1 ，/1 26/1 城市/0 二手房/1 挂牌/1 价格/0 环比/1 24/1 升/0 2/1 降/0 ，/1 共/1 14/1 个/0 城市/1 环比/1 涨/1 跌幅/0 在/1 1%/1 内/0 ，/1 整体/1 价格/0 有所/1 震动/0 ，/1 呈/1 略微/1 上扬/1 态势/1 。/1";
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

	}

	@Override
	public List<CoreLabel> labeledText2CoreLabels(String labeledText) {
		// TODO Auto-generated method stub
		return null;
	}

}
