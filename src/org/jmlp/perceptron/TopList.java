package org.jmlp.perceptron;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 从array list 选择出得分最高的k个元素
 * @author lq
 *
 */
public class TopList {
	public FEATSET worst_feat;
	
	public FEATSETTHREE worst_three_feat;
	
	public static  ArrayList<FEATSET> getTopFromList(int num, ArrayList<FEATSET> all_list)
	{
	
        Collections.sort(all_list, new Comparator<FEATSET>() {
              @Override
	               public int compare(FEATSET t1, FEATSET t2) {
	                    if(t1.score < t2.score) {
	                         return 1;
	                    } else if(t1.score > t2.score) {
	                         return -1;
	                    }
	                    return 0;
	               }
	          });
        
        ArrayList<FEATSET> small_list=new ArrayList<FEATSET>();
        for(int i=0;i<num;i++)
        {
        	if(i>(all_list.size()-1))
        	{
        		break;
        	}
        	small_list.add(all_list.get(i));
        }
        
      
		return small_list;
	}
	
	/**
	 * 获得得分最高的元素
	 * @param all_list
	 * @return
	 */
	public FEATSET getBestFromList(ArrayList<FEATSET> all_list)
	{
	
        Collections.sort(all_list, new Comparator<FEATSET>() {
              @Override
	               public int compare(FEATSET t1, FEATSET t2) {
	                    if(t1.score < t2.score) {
	                         return 1;
	                    } else if(t1.score > t2.score) {
	                         return -1;
	                    }
	                    return 0;
	               }
	          });
        
    /*
		for(int i=0;i<all_list.size();i++)
		{
			if(all_list.get(i).score!=0)
			{
			 System.out.println("ns:"+all_list.get(i).toString()+"   score["+i+"]:"+all_list.get(i).score);
			}
		}	
     */ 
        worst_feat=all_list.get(all_list.size()-1);
		return all_list.get(0);
	}
	
	

	/**
	 * 获得得分最高的元素
	 * @param all_list
	 * @return
	 */
	public FEATSETTHREE getBestFromListThree(ArrayList<FEATSETTHREE> all_list)
	{
	
		/*
        Collections.sort(all_list, new Comparator<FEATSETTHREE>() {
              @Override
	               public int compare(FEATSETTHREE t1, FEATSETTHREE t2) {
	                    if(t1.score < t2.score) {
	                         return 1;
	                    } else if(t1.score > t2.score) {
	                         return -1;
	                    }
	                    return 0;
	               }
	          });
	    */
        
    /*
		for(int i=0;i<all_list.size();i++)
		{
			if(all_list.get(i).score!=0)
			{
			 System.out.println("ns:"+all_list.get(i).toString()+"   score["+i+"]:"+all_list.get(i).score);
			}
		}	
     */ 
		FEATSETTHREE temp_best_feat=all_list.get(0);
		FEATSETTHREE temp_worst_feat=all_list.get(0);
		FEATSETTHREE temp_feat=null;
		for(int i=1;i<all_list.size();i++)
		{
			temp_feat=all_list.get(i);
			if(temp_feat.score>temp_best_feat.score)
			{
				temp_best_feat=temp_feat;
			}
			
			if(temp_feat.score<temp_worst_feat.score)
			{
				temp_worst_feat=temp_feat;
			}
		}
		
        ////worst_three_feat=all_list.get(all_list.size()-1);
		worst_three_feat=temp_worst_feat;
		
		////return all_list.get(0);
		return temp_best_feat;
	}
	
}
