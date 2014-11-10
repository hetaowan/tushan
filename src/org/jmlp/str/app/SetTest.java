package org.jmlp.str.app;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;
import edu.stanford.nlp.sequences.PlainTextDocumentReaderAndWriter;

public class SetTest {
	 public static void main(String[] args) throws Exception {


		    Properties props = new Properties();
            FileInputStream fis = new FileInputStream("train.prop");
            props.load(fis);
            props.setProperty("serDictionary", "data/dict-chris6.ser.gz");
            props.setProperty("sighanCorporaDict", "data");          
            
            System.out.println("props:"+props.toString());
            
            
            //props.setProperty("dictionary2", "baidu_short_dict.txt");           
            
		   // props.setProperty("sighanCorporaDict", "data");
            /*
		     props.setProperty("map", "char=0,answer=1");
		     props.setProperty("sighanCorporaDict", "data");
		     props.setProperty("sighanCorporaDict", "data");
		     props.setProperty("NormalizationTable", "data/norm.simp.utf8");
		     props.setProperty("normTableEncoding", "UTF-8");
		     props.setProperty("normTableEncoding", "edu.stanford.nlp.wordseg.Sighan2005DocumentReaderAndWriter");
		     props.setProperty("featureFactory", "edu.stanford.nlp.wordseg.Gale2007ChineseSegmenterFeatureFactory");
		     */
            
		    // below is needed because CTBSegDocumentIteratorFactory accesses it
		   // props.setProperty("serDictionary","data/dict-chris6.ser.gz");
		    //props.setProperty("serDictionary","data/dict-chris6.ser.gz");
//		    props.setProperty("testFile", args[0]);
		    //props.setProperty("inputEncoding", "UTF-8");
		    //props.setProperty("sighanPostProcessing", "true");

            
            //*************************************
		    /*
            props.setProperty("sighanCorporaDict", "data");
             props.setProperty("NormalizationTable", "data/norm.simp.utf8");
             props.setProperty("normTableEncoding", "UTF-8");
            // below is needed because CTBSegDocumentIteratorFactory accesses it
           // props.setProperty("serDictionary","data/dict-chris6.ser.gz");
            props.setProperty("serDictionary","data/dict-chris6.ser.gz");
//          props.setProperty("testFile", args[0]);
            props.setProperty("inputEncoding", "UTF-8");
            props.setProperty("sighanPostProcessing", "true");
		   */
		    
		    
		    
		   
		    
		    CRFClassifier<CoreLabel> classifier = new CRFClassifier<CoreLabel>(props);
		    System.out.println(" before load the seg_external_dict.gz");
		    Thread.sleep(10000);
		    classifier.loadClassifierNoExceptions("seg_external_dict.gz", props);
		    System.out.println(" after load the seg_external_dict.gz");
		    // flags must be re-set after data is loaded
		    classifier.flags.setProperties(props);
		   
		    PrintStream myout;
		    
		    String  segOutFileName="sstest.txt";
                

         //   BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
         //   System.out.println("input one line");
          //  br.readLine();
		  //  myout = new PrintStream(new FileOutputStream(new File(segOutFileName)));       
		 //   System.setOut(myout); 
		   // classifier.classifyStdin();
		    String s="凤凰网是中国领先的综合门户网站，提供含文图音视频的全方位综合新闻资讯、深度访谈、观点评论、财经产品、互动应用、分享社区等服务，同时与凤凰无线、凤凰宽频形成三屏联动，为全球主流华人提供互联网、无线通信、电视网三网融合无缝衔接的新媒体优质体验\n";
		    String s2="这部电影甩《变形金刚》的距离大致和《金刚》甩《金刚狼》的距离差不多。动作场面单一个静中有动慢中有快就令人叹为观止。ps，大韩民国好歹也是pacific rim国家吧，被无视至此人家只好说怪兽是他们用泡菜养大的了。。";
		    String s3="话说，这次《环太平洋》的字幕，仍然是看不下去啊，我已经刻意不看字幕了，但即使如此都还是发现了好多错误，打击颇大。 《环太平洋》的预告片和花絮我几个月前就有听译了，我对影片也非常期待，可没想到这个片子居然又栽到贾翻译的手里了。唉，我越来越觉得贾翻译实际是用来打击进口影片票房的秘密武器了。 这个片子的对白其实非常简单，最复杂的词无非也就是complia";
		    String s4="数控振荡器 (NCO)在 CDMA基站的软件无线电收发机中 ，起着非常重要的作用。";
		    String s5="据中国人民银行统计，截至去年底，中国金融机构对三资企业的人民币贷款余额已达九百九十五点六亿元，这一数字比上年末增加二百零三点三亿元，增长百分之二十七点六";
		    System.out.println("s5:"+s5.length());
		    String res=classifier.classifyToString(s5);		    
		    System.out.println("res:"+res);
		    /*
		    String s6="天似穹庐，草原辽阔，牛羊成群，牧歌回荡，一座座毡帐坐落在草原上，迎来中国贵客。习近平的车队在蒙古骑兵护卫下抵达那达慕现场。习近平和夫人彭丽媛受到额勒贝格道尔吉总统和夫人热情迎接。两国元首夫妇一同走进营帐入座。";
		    String res2=classifier.classifyToString(s6);		    
		    System.out.println("res2:"+res2);
		    
		    res2=classifier.classifyToString(s);		    
		    System.out.println("res2:"+res2);
		    res2=classifier.classifyToString(s2);		    
		    System.out.println("res2:"+res2);
		    res2=classifier.classifyToString(s3);		    
		    System.out.println("res2:"+res2);
		    res2=classifier.classifyToString(s4);		    
		    System.out.println("res2:"+res2);
		    
		    */
		    
		    /*
		    List resList=classifier.classify(s);
		    System.out.println("list.size:"+resList.size());
		    for(int i=0;i<resList.size();i++)
		    {
		    	List<CoreLabel> coreList=(List<CoreLabel>)resList.get(i);
		    	System.out.println("coreList.size:"+coreList.size());
		    	for(int j=0;j<coreList.size();j++)
		    	{
		    		CoreLabel cl=coreList.get(j);
		    		Set ks=cl.keySet();
		    		Iterator it=ks.iterator();
		    		while(it.hasNext())
		    		{
		    			System.out.println(it.next());
		    		}
		    		System.out.println(cl.word());
		    	}
		    }
		    */
		  //  String toSegFileName="tt.txt";
		   // classifier.classifyAndWriteAnswers(toSegFileName);
		 //  myout.close();
		
		   	 
		 //   }
		    
		   
		 //   System.setErr(myout); 
		  
		  }
}
