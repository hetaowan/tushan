package org.jmlp.cluster;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jmlp.code.MD5Code;
import org.jmlp.file.utils.FileToArray;
import org.jmlp.math.random.ArrayUtil;
import org.jmlp.sort.utils.SortStrArray;
import org.jmlp.str.basic.DS2STR;
import org.jmlp.str.basic.SSO;
import org.jmlp.str.basic.STR2DS;

/***
 * 输入n个向量，输出m个聚类
 * 
 * @author zkyz
 * 
 */
public class CRP {

	public HashMap<String, double[]> input_vecs = new HashMap<String, double[]>();
	public HashMap<String, String> md5decmap = new HashMap<String, String>();
	public ArrayList<ArrayList<double[]>> clusterVec = new ArrayList<ArrayList<double[]>>();
	public ArrayList<ArrayList<String>> clusterIdx = new ArrayList<ArrayList<String>>();
	
	public int vecsize = 200;
	public HashMap<String, double[]> wordMap = new HashMap<String, double[]>();

	public void crp() {
		int ncluster = 0;

		double pnew = 1.00 / (double) (1 + ncluster);
		double maxSim = Double.NEGATIVE_INFINITY;
		int maxIdx = 0;
		double[] v;
		int j, i;
		double sim = 0;
		double[] rands = new double[input_vecs.size()];
		for (i = 0; i < rands.length; i++) {
			rands[i] = Math.random();
		}

		i = 0;
		for (Entry<String, double[]> word : input_vecs.entrySet()) {
			// System.out.println("word:"+word.getKey());
			maxSim = Double.NEGATIVE_INFINITY;
			maxIdx = 0;
			v = word.getValue();
			// System.out.println("v:"+DS2STR.doublearr2str(v));
			for (j = 0; j < ncluster; j++) {
				if (clusterVec.size() < 1) {
					continue;
				}
				sim = cosine_similarity(v, clusterVec.get(j));
				// System.out.println("j:"+j+"sim:"+sim);
				if (sim > maxSim) {
					maxIdx = j;
					maxSim = sim;
				}
			}

			if (maxSim < pnew) {
				if (rands[i++] < pnew) {
					// System.out.println("create new cluster");
					ArrayList<double[]> newcluster = new ArrayList<double[]>();
					newcluster.add(v);
					clusterVec.add(newcluster);
					ArrayList<String> newidxs = new ArrayList<String>();
					newidxs.add(word.getKey());
					clusterIdx.add(newidxs);
					ncluster++;
					pnew = 1.00 / (double) (1 + ncluster);
				}
			}

			if (clusterVec.size() < 1) {
				continue;
			}
			clusterVec.get(maxIdx).add(v);
			clusterIdx.get(maxIdx).add(word.getKey());
		}

	}

	public void init() {
		input_vecs = new HashMap<String, double[]>();
		md5decmap = new HashMap<String, String>();
		clusterVec = new ArrayList<ArrayList<double[]>>();
		clusterIdx = new ArrayList<ArrayList<String>>();
	}

	public double cosine_similarity(double[] v, ArrayList<double[]> cluster) {
		if (cluster.size() < 1) {
			return Double.NEGATIVE_INFINITY;
		}
		double sum = 0;
		for (int i = 0; i < cluster.size(); i++) {
			sum += ArrayUtil.dotProduct(v, cluster.get(i));
		}

		return sum / (double) cluster.size();
	}

	public double cosine_similarity_other(double[] v, ArrayList<double[]> cluster) {
		if (cluster.size() < 1) {
			return Double.NEGATIVE_INFINITY;
		}
		double sum = 0;
		double[] sumvec=new double[v.length];
		
		for (int i = 0; i < cluster.size(); i++) {
			sumvec = ArrayUtil.sum_weight(sumvec, cluster.get(i));
		}
		sumvec=ArrayUtil.sub_weight(sumvec, v);
		sum=ArrayUtil.dotProduct(sumvec, v);
		if(cluster.size()<2)
		{
			return 0;
		}
		return sum / (double) (cluster.size()-1);
	}
	
	public void read_input(String file) {
		String[] samples = null;
		String line = "";
		String[] seg_arr = null;
		String word = "";
		String vstr = "";
		double[] v;
		String md5word = "";
		try {
			samples = FileToArray.fileToDimArr(file);
			for (int i = 0; i < samples.length; i++) {
				line = samples[i];
				if (SSO.tioe(line)) {
					continue;
				}

				seg_arr = line.split("\001");
				if (seg_arr.length != 2) {
					continue;
				}
				word = seg_arr[0];
				vstr = seg_arr[1];
				// System.out.println("word:"+word+"  vstr:"+vstr);
				v = STR2DS.str2douarr(vstr);
				if (v.length != vecsize) {
					continue;
				}

				md5word = MD5Code.Md5(word);
				input_vecs.put(md5word, v);
				md5decmap.put(md5word, word);
			}
		} catch (Exception e) {

		}
	}

	public void readline(String line) {
		init();
		String[] seg_arr = null;
		String label = "";
		String wstr = "";
		double[] v;
		String[] word_seg = null;
		String md5word = "";
		String word = "";
		try {

			if (SSO.tioe(line)) {
				return;
			}

			seg_arr = line.split("\001");
			if (seg_arr.length != 2) {
				return;
			}
			label = seg_arr[0];
			wstr = seg_arr[1];

			word_seg = wstr.split("\\s+");
			for (int j = 0; j < word_seg.length; j++) {
				word = word_seg[j].trim();
				if (SSO.tioe(word)) {
					continue;
				}
				// System.out.println("word:"+word+"  vstr:"+vstr);
				v = wordMap.get(word);
				if (v == null) {
					continue;
				}
				if (v.length != vecsize) {
					return;
				}

				md5word = MD5Code.Md5(word);
				input_vecs.put(md5word, v);
				md5decmap.put(md5word, word);
			}

		} catch (Exception e) {

		}
	}

	public void readModel(String file) {
		String[] samples = null;
		String line = "";
		String[] seg_arr = null;
		String word = "";
		String vstr = "";
		double[] v;

		try {
			samples = FileToArray.fileToDimArr(file);
			for (int i = 0; i < samples.length; i++) {
				line = samples[i];
				if (SSO.tioe(line)) {
					continue;
				}

				seg_arr = line.split("\001");
				if (seg_arr.length != 2) {
					continue;
				}
				word = seg_arr[0];
				vstr = seg_arr[1];
				// System.out.println("word:"+word+"  vstr:"+vstr);
				v = STR2DS.str2douarr(vstr);
				if (v.length != vecsize) {
					continue;
				}
				wordMap.put(word, v);
			}
		} catch (Exception e) {

		}
	}

	public void print_result(String file) {
		FileWriter fw = null;
		PrintWriter pw = null;
		try {
			fw = new FileWriter(new File(file));
			pw = new PrintWriter(fw);
			ArrayList<String> cluster = null;
			ArrayList<double[]> clustervecs=null;
			String wstr = "";
			String md5word = "";
			String word = null;
			double similarity=0;
			double[] v=null;
			ArrayList<String> crpwords=null;
	
			for (int i = 0; i < clusterIdx.size(); i++) {
				cluster = clusterIdx.get(i);
				clustervecs=clusterVec.get(i);
				wstr = "";
				crpwords=new ArrayList<String>();
				for (int j = 0; j < cluster.size(); j++) {
					
					md5word = cluster.get(j).trim();
					v=input_vecs.get(md5word);
					if(v==null)
					{
						continue;
					}
					similarity=cosine_similarity_other(v,clustervecs);
					
					word = md5decmap.get(md5word);
					if (SSO.tioe(word)) {
						continue;
					}
					crpwords.add(word+"\001"+similarity);				
				}
				
				String[] narr=SortStrArray.sort_List(crpwords, 0, "dou", 2, "\001");
				wstr=DS2STR.trimfield(narr, "\001", 2, 0);
				wstr = wstr.trim();
				pw.println("cluster[" + i + "]:" + wstr);
			}

			pw.close();
			fw.close();
		} catch (Exception e) {

		}

	}

	public String print_result() {
		String res = "";
		try {

			ArrayList<String> cluster = null;
			String wstr = "";
			String md5word = "";
			String word = null;
			for (int i = 0; i < clusterIdx.size(); i++) {
				cluster = clusterIdx.get(i);
				wstr = "";
				for (int j = 0; j < cluster.size(); j++) {
					md5word = cluster.get(j).trim();
					word = md5decmap.get(md5word);
					if (SSO.tioe(word)) {
						continue;
					}
					wstr += word + " ";
				}
				wstr = wstr.trim();
				res += "cluster[" + i + "]:" + wstr + " ";
			}
		} catch (Exception e) {

		}

		return res;

	}

	public static void main(String[] args) {
		String input_file = "";
		String output_file = "";
		if (args.length < 2) {
			System.err.println("Usage:<input_file> <output_file>");
			System.exit(1);
		}
		input_file = args[0];
		output_file = args[1];

		/*
		 * CRP crp=new CRP(); crp.read_input(input_file); crp.crp();
		 * crp.print_result(output_file);
		 */

		CRP crp = new CRP();

		File dir = new File(input_file);
		File[] catefiles = dir.listFiles();
		String fn = "";

		try {

			for (int i = 0; i < catefiles.length; i++) {

				fn = catefiles[i].getName();
				System.out.println("fn:" + fn);
				fn = fn.replace("\\.txt", "");
				crp.init();
				crp.read_input(catefiles[i].getAbsolutePath());
				crp.crp();
				crp.print_result(output_file + "/" + fn + ".txt");
			}

		} catch (Exception e) {

		}
        /*
		crp.readModel("temp/medw2v/model.txt");

		try {
			FileWriter fw = new FileWriter(new File(output_file));
			PrintWriter pw = new PrintWriter(fw);

			String[] samples = FileToArray.fileToDimArr(input_file);
			String line = "";
			for (int i = 0; i < samples.length; i++) {
				line = samples[i];
				crp.readline(line);
				crp.crp();
				pw.println(line);
				pw.println(crp.print_result());
				pw.flush();
			}
			pw.close();
		} catch (Exception e) {

		}
		*/
	}

}