package org.jmlp.perceptron;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ThreeBeamDecoder extends BeamDecoder{

	public PrintWriter pw = null;
	public static int log_level = 0;
        public FEATSETTHREE sub_worst_feat;
	public ThreeBeamDecoder()
	{
		if (log_level > 3) {
		   try {
		       FileWriter fw = new FileWriter(new File("temp/mark/plainbeamdecoder.txt"));
		       pw=new PrintWriter(fw);
		   } catch (Exception e) {
                       System.out.println(e.getMessage());
		   }
		}
	}
	
	public FEATSETTHREE beam_search_three(WORD[] words, FEAT[] sub_weights,
			LABEL[] label_set, LEARNPARM learn_parms) {
		ArrayList<FEATSETTHREE> src = null;
		src = new ArrayList<FEATSETTHREE>();
		FEATSETTHREE ifs = null;// 初始feat set
		//pw.println("process word 0");
		for (int i = 0; i < label_set.length; i++) {
			// System.out.println("word[0]="+words[0].index);
			ifs = new FEATSETTHREE(words[0], label_set[i], new WEI(sub_weights[FeatureIndex.featIndexNoWordFirstMap(1, label_set[i])].weight),new WEI(sub_weights[FeatureIndex.featIndexNoWordSecondMap(1, label_set[i])].weight),new WEI(sub_weights[FeatureIndex.featIndexNoWordThirdMap(1, label_set[i])].weight));
			for(int j=1;j<words.length;j++)
			{
				ifs.addWord(words[j], new WEI(sub_weights[FeatureIndex.featIndexNoWordFirstMap(j+1, ifs.label)].weight),new WEI(sub_weights[FeatureIndex.featIndexNoWordSecondMap(j+1, ifs.label)].weight),new WEI(sub_weights[FeatureIndex.featIndexNoWordThirdMap(j+1, ifs.label)].weight));
			}	
			
			// System.out.println("score="+ifs.score);
			ifs.calScore();
			//pw.println(ifs.toString()+"  score="+ifs.score);
			src.add(ifs);
		}

		// TODO Auto-generated method stub
		TopList tl=new TopList();
		FEATSETTHREE best_feat=tl.getBestFromListThree(src);
		sub_worst_feat=tl.worst_three_feat;
		return best_feat;
	}

	@Override
	public FEATSET beam_search(WORD[] words, FEAT[] init_weights,
			LABEL[] label_set, LEARNPARM learn_parms) {
		// TODO Auto-generated method stub
		return null;
	}
}
