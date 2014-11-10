package org.jmlp.perceptron;

import java.util.Properties;


/**
 * 根据输入的参数返回合适的BeamDecoder解码器
 * @author lq
 *
 */
public class BeamDecoderFactory {

	public static BeamDecoder create(LEARNPARM learn_parms)
	{
	   BeamDecoder bd=null;
	   if(learn_parms.bd_type.equals("sfsbd"))
	   {
		  // System.out.println("creating SparkFeatSearchBD");
		  // bd=new SparkFeatSearchBD();
	   }
	   else if(learn_parms.bd_type.equals("plnbd"))
	   {
		   //System.out.println("creating PlainBeamDecoder");
		   bd=new PlainBeamDecoder();
	   }
	   else if(learn_parms.bd_type.equals("plnbdns"))
	   {
		   //System.out.println("creating PlainBeamDecoder");
		   bd=new PlainBeamDecoderNoSearch();
	   }
	   return bd;
	}

}
