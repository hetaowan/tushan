package org.jmlp.classify.svm_struct.source;


public class DOC {
	  public int docnum;
	  public int queryid;
	  public double costfactor;
	  public int slackid;
	  public int kernelid;
	  public SVECTOR fvec;
	  
	  public DOC copyDoc()
	  {
		  DOC ndoc=new DOC();
		  ndoc.docnum=docnum;
		  ndoc.queryid=queryid;
		  ndoc.costfactor=costfactor;
		  ndoc.slackid=slackid;
		  ndoc.kernelid=kernelid;
		  if(fvec==null)
		  {
		   ndoc.fvec=null;
		  }
		  else
		  {
			  ndoc.fvec=fvec.copySVECTOR(); 
		  }
		  return ndoc;
	  }
	  
}