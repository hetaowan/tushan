package edu.stanford.nlp.wordseg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.io.EncodingPrintWriter;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.CharAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.D2_LBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.D2_LEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.D2_LMiddleAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LMiddleAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.OriginalCharAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.ShapeAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.UBlockAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.UTypeAnnotation;
import edu.stanford.nlp.sequences.Clique;
import edu.stanford.nlp.sequences.FeatureFactory;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.trees.international.pennchinese.RadicalMap;
import edu.stanford.nlp.util.PaddedList;


/**
 * A Chinese segmenter Feature Factory for GALE project. (modified from Sighan Bakeoff 2005.)
 * <p>
 * c is Chinese character ("char").  c means current, n means next and p means previous.
 * </p>
 *
 * <table>
 * <tr>
 * <th>Feature</th><th>Templates</th>
 * </tr>
 * <tr>
 * <tr>
 * <th></th><th>Current position clique</th>
 * </tr>
 * <tr>
 * <td>useWord1</td><td>CONSTANT, cc, nc, pc, pc+cc, if (As|Msr|Pk|Hk) cc+nc, pc,nc </td>
 * </tr>
 * </table>
 *
 * @author Huihsin Tseng
 * @author Pichuan Chang
 * @author Christopher Manning
 */
public class Gale2007ChineseSegmenterFeatureFactory<IN extends CoreLabel> extends FeatureFactory<IN> {

  private static final int DEBUG = 0;

  private transient TagAffixDetector taDetector; // = null;
  private transient CorpusDictionary outDict; // = null;

  @Override
  public void init(SeqClassifierFlags flags) {
    super.init(flags);
  }
  
  synchronized private void createTADetector() {
    if (taDetector == null) {
      taDetector = new TagAffixDetector(flags);
    }
  }

  synchronized private void createOutDict() {
    if (outDict == null) {
      System.err.println("reading "+flags.outDict2+" as a seen lexicon");
      outDict = new CorpusDictionary(flags.outDict2);
    }
  }    

  
  /**
   * Extracts all the features from the input data at a certain index.
   *
   * @param cInfo The complete data set as a List of WordInfo 一篇文档objectBank形式
   * @param loc  The index at which to extract features. 该文档的第loc个position
   * @param clique The form of the clique we extract features for 例如窗口大小为1的clique 形式为[0],窗口大小为2的clique形式为[-1,0]
   */
  @Override
  public Collection<String> getCliqueFeatures(PaddedList<IN> cInfo, int loc, Clique clique) {
    Collection<String> features = new HashSet<String>();

    if (clique == cliqueC) {
      addAllInterningAndSuffixing(features, featuresC(cInfo, loc), "C");
    } else if (clique == cliqueCpC) {
      addAllInterningAndSuffixing(features, featuresCpC(cInfo, loc), "CpC");
      addAllInterningAndSuffixing(features, featuresCnC(cInfo, loc-1), "CnC");
    } else if (clique == cliqueCpCp2Cp3C) {
      addAllInterningAndSuffixing(features, featuresCpCp2Cp3C(cInfo, loc), "CpCp2Cp3C");
    }

    if (DEBUG > 0) {
      EncodingPrintWriter.err.println("For " + cInfo.get(loc) +
              ", features: " + features, "UTF-8");
    }
    return features;
  }



  private static final Pattern patE = Pattern.compile("[a-z]");
  private static final Pattern patEC = Pattern.compile("[A-Z]");

  private static String isEnglish(String chp, String chc) {
    Matcher mp = patE.matcher(chp);   // previous char is [a-z]
    Matcher mc = patE.matcher(chc);   //  current char is [a-z]
    Matcher mpC = patEC.matcher(chp); // previous char is [A-Z]
    Matcher mcC = patEC.matcher(chc); //  current char is [A-Z]
    if (mp.matches() && mcC.matches()){
      return "BND"; // [a-z][A-Z]
    } else if (mp.matches() && mc.matches()){
      return "ENG"; // [a-z][a-z]
    } else if (mpC.matches() && mcC.matches()){
      return "BCC"; // [A-Z][A-Z]
    } else if (mp.matches() && !mc.matches() && !mcC.matches()){
      return "e1";  // [a-z][^A-Za-z]
    } else if (mc.matches() && !mp.matches() && !mpC.matches()) {
      return "e2";  // [^A-Za-z][a-z]
    } else if (mpC.matches() && !mc.matches() && !mcC.matches()){
      return "e3";  // [A-Z][^A-Za-z]
    } else if (mcC.matches() && !mp.matches() && !mpC.matches()) {
      return "e4";  // [^A-Za-z][A-Z]
    } else {
      return "";
    }
  } // end isEnglish

  // the pattern used to be [\u00b7\\-\\.] which AFAICS matched only . because - wasn't escaped. CDM Nov 2007
  private static final Pattern patP = Pattern.compile("[-\u00b7.]");

  private static String isEngPU(String Ep) {
    Matcher mp = patP.matcher(Ep);
    if (mp.matches()) {
      return "1:EngPU";
    } else {
      return "";
    }
  } //is EnglishPU

/**
 * 窗口大小为1的clique(形式为[0])加入词典特征,其实是描述   p+c+c2 在词典中的存在形式
 * @param lbeginFieldName CoreAnnotation对象，作为CoreLabel索引获取具体值
 * @param lmiddleFieldName CoreAnnotation对象，作为CoreLabel索引获取具体值
 * @param lendFieldName CoreAnnotation对象，作为CoreLabel索引获取具体值
 * @param dictSuffix 不同词典得出的特征采用不同的后缀
 * @param features 提取出特征存放的地方
 * @param p 前一个CoreLabel
 * @param c 当前CoreLabel
 * @param c2 下一个CoreLabel
 */
  private static void dictionaryFeaturesC(Class<? extends CoreAnnotation<String>> lbeginFieldName,
                                   Class<? extends CoreAnnotation<String>> lmiddleFieldName,
                                   Class<? extends CoreAnnotation<String>> lendFieldName,
                                   String dictSuffix, Collection<String> features, CoreLabel p, CoreLabel c, CoreLabel c2) {
      String lbegin = c.getString(lbeginFieldName);
      String lmiddle = c.getString(lmiddleFieldName);
      String lend = c.getString(lendFieldName);
      features.add(lbegin+dictSuffix+"-lb");
      features.add(lmiddle+dictSuffix+"-lm");
      features.add(lend+dictSuffix+"-le");

      lbegin = p.getString(lbeginFieldName);
      lmiddle = p.getString(lmiddleFieldName);
      lend = p.getString(lendFieldName);
      features.add(lbegin+dictSuffix+"-plb");
      features.add(lmiddle+dictSuffix+"-plm");
      features.add(lend+dictSuffix+"-ple");

      lbegin = c2.getString(lbeginFieldName);
      lmiddle = c2.getString(lmiddleFieldName);
      lend = c2.getString(lendFieldName);
      features.add(lbegin+dictSuffix+"-c2lb");
      features.add(lmiddle+dictSuffix+"-c2lm");
      features.add(lend+dictSuffix+"-c2le");
  }

/**
 * 窗口大小为1 所获取的特征 即clique形式是[0]
 * @param cInfo objectbank 形式的文档
 * @param loc 该文档的第loc个position
 * @return
 */
  protected Collection<String> featuresC(PaddedList<? extends CoreLabel> cInfo, int loc) {
    Collection<String> features = new ArrayList<String>();
   // System.out.println("loc:"+loc);
    CoreLabel c = cInfo.get(loc);
   // System.out.println("c:"+c.toString());
    CoreLabel c2 = cInfo.get(loc + 1);
   // System.out.println("c2:"+c2.toString());
    CoreLabel c3 = cInfo.get(loc + 2);
   // System.out.println("c3:"+c3.toString());
    CoreLabel p = cInfo.get(loc - 1);
   // System.out.println("p:"+p.toString());
    CoreLabel p2 = cInfo.get(loc - 2);
   // System.out.println("p2:"+p2.toString()); 
    CoreLabel p3 = cInfo.get(loc - 3);
   // System.out.println("p3:"+p3.toString());
    String charc = c.getString(CharAnnotation.class);
    String charc2 = c2.getString(CharAnnotation.class);
    String charc3 = c3.getString(CharAnnotation.class);
    String charp = p.getString(CharAnnotation.class);
    String charp2 = p2.getString(CharAnnotation.class);
    String charp3 = p3.getString(CharAnnotation.class);
    Integer cI = c.get(UTypeAnnotation.class);
    String uTypec = (cI != null ? cI.toString() : "");
    Integer c2I = c2.get(UTypeAnnotation.class);
    String uTypec2 = (c2I != null ? c2I.toString() : "");
    Integer c3I = c3.get(UTypeAnnotation.class);
    String uTypec3 = (c3I != null ? c3I.toString() : "");
    Integer pI = p.get(UTypeAnnotation.class);
    String uTypep = (pI != null ? pI.toString() : "");
    Integer p2I = p2.get(UTypeAnnotation.class);
    String uTypep2 = (p2I != null ? p2I.toString() : "");

    /* N-gram features. N is upto 2. */
    if (flags.useWord1) {
      features.add(charc +"c");//当前字符
      features.add(charc2+"c2");//下一个字符
      features.add(charp +"p");//前一个字符
      features.add(charp + charc  +"pc");//2-gram <前一个字符  + 当前字符>
      features.add(charc + charc2  +"cc2");//2-gram <当前字符  + 下一字符>
      // cdm: need hyphen so you can see which of charp or charc2 is null....
      features.add(charp + "-" + charc2 + "pc2");//2-gram <前一个字符  + 下一个字符>
    }
   //为什么使用两个词典
    if (flags.dictionary != null || flags.serializedDictionary != null) {
      dictionaryFeaturesC(LBeginAnnotation.class,LMiddleAnnotation.class,LEndAnnotation.class,"",features, p, c, c2);
    }

    if (flags.dictionary2 != null) {
      dictionaryFeaturesC(D2_LBeginAnnotation.class, D2_LMiddleAnnotation.class, D2_LEndAnnotation.class,"-D2-",features, p, c, c2);
    }

    if (flags.useFeaturesC4gram || flags.useFeaturesC5gram || flags.useFeaturesC6gram) {
      features.add(charp2 + charp  +"p2p");
      features.add(charp2 + "p2");
    }
    if (flags.useFeaturesC5gram || flags.useFeaturesC6gram) {
      features.add(charc3+"c3");
      features.add(charc2 + charc3 + "c2c3");
    }
    if (flags.useFeaturesC6gram) {
      features.add(charp3 + "p3");
      features.add(charp3 + charp2 + "p3p2");
    }

    if (flags.useUnicodeType || flags.useUnicodeType4gram || flags.useUnicodeType5gram) {
      features.add(uTypep + "-" + uTypec + "-" + uTypec2 + "-uType3");
    }
    if (flags.useUnicodeType4gram || flags.useUnicodeType5gram) {
      features.add(uTypep2 + "-" + uTypep + "-" + uTypec + "-" + uTypec2 + "-uType4");
    }
    if (flags.useUnicodeType5gram) {
      features.add(uTypep2 + "-" + uTypep + "-" + uTypec + "-" + uTypec2 + "-" + uTypec3 + "-uType5");
    }
    if (flags.useUnicodeBlock) {
      features.add(p.getString(UBlockAnnotation.class) + "-" + c.getString(UBlockAnnotation.class) + "-" + c2.getString(UBlockAnnotation.class) + "-uBlock");
    }
    if (flags.useShapeStrings) {
      if (flags.useShapeStrings1) {
        features.add(p.getString(ShapeAnnotation.class) + "ps");
        features.add(c.getString(ShapeAnnotation.class) + "cs");
        features.add(c2.getString(ShapeAnnotation.class) + "c2s");
      }
      if (flags.useShapeStrings3) {
        features.add(p.getString(ShapeAnnotation.class) + c.getString(ShapeAnnotation.class) + c2.getString(ShapeAnnotation.class) + "pscsc2s");
      }
      if (flags.useShapeStrings4) {
        features.add(p2.getString(ShapeAnnotation.class) + p.getString(ShapeAnnotation.class) + c.getString(ShapeAnnotation.class) + c2.getString(ShapeAnnotation.class) + "p2spscsc2s");
      }
      if (flags.useShapeStrings5) {
        features.add(p2.getString(ShapeAnnotation.class) + p.getString(ShapeAnnotation.class) + c.getString(ShapeAnnotation.class) + c2.getString(ShapeAnnotation.class) + c3.getString(ShapeAnnotation.class) + "p2spscsc2sc3s");
      }
    }

    features.add("cliqueC");

    return features;
  }
/**
 * 获取窗口大小为2的clique(形式为[-1 0])对应的词典特征
 * @param lbeginFieldName CoreAnnotation对象 用来获取CoreLabel的具体值
 * @param lmiddleFieldName CoreAnnotation对象 用来获取CoreLabel的具体值
 * @param lendFieldName CoreAnnotation对象 用来获取CoreLabel的具体值
 * @param dictSuffix 不同的词典对应不同的后缀
 * @param features 提取出特征存放的地方
 * @param p2 前推第二个CoreLabel
 * @param p 前一个CoreLabel
 * @param c 当前CoreLabel
 * @param c2 下一个CoreLabel
 */
  private void dictionaryFeaturesCpC(Class<? extends CoreAnnotation<String>> lbeginFieldName,
                                     Class<? extends CoreAnnotation<String>> lmiddleFieldName,
                                     Class<? extends CoreAnnotation<String>> lendFieldName,
                                     String dictSuffix, Collection<String> features, CoreLabel p2, CoreLabel p, CoreLabel c, CoreLabel c2) {
    String lbegin = c.getString(lbeginFieldName);
    String lmiddle = c.getString(lmiddleFieldName);
    String lend = c.getString(lendFieldName);
    features.add(lbegin+dictSuffix+"-lb");
    features.add(lmiddle+dictSuffix+"-lm");
    features.add(lend+dictSuffix+"-le");

    lbegin = p.getString(lbeginFieldName);
    lmiddle = p.getString(lmiddleFieldName);
    lend = p.get(lendFieldName);
    features.add(lbegin+dictSuffix+"-plb");
    features.add(lmiddle+dictSuffix+"-plm");
    features.add(lend+dictSuffix+"-ple");

    lbegin = c2.getString(lbeginFieldName);
    lmiddle = c2.getString(lmiddleFieldName);
    lend = c2.getString(lendFieldName);
    features.add(lbegin+dictSuffix+"-c2lb");
    features.add(lmiddle+dictSuffix+"-c2lm");
    features.add(lend+dictSuffix+"-c2le");

    if (flags.useDictionaryConjunctions) {
      String p2Lend = p2.getString(lendFieldName);
      String pLend = p.getString(lendFieldName);
      String pLbegin = p.getString(lbeginFieldName);
      String cLbegin = c.getString(lbeginFieldName);
      String cLmiddle = c.getString(lmiddleFieldName);
      if (flags.useDictionaryConjunctions3) {
        features.add(pLend + cLbegin + cLmiddle + dictSuffix + "-pcLconj1");
      }
      features.add(p2Lend + pLend + cLbegin + cLmiddle + dictSuffix + "-p2pcLconj1");
      features.add(p2Lend + pLend + pLbegin + cLbegin + cLmiddle + dictSuffix + "-p2pcLconj2");
    }
  }

/**
 * 获取窗口大小为2的clique(即形式为[-1,0])的特征
 * @param cInfo objectBank形式的文档
 * @param loc 该文档第loc个postion
 * @return
 */
  protected Collection<String> featuresCpC(PaddedList<? extends CoreLabel> cInfo, int loc) {
    Collection<String> features = new ArrayList<String>();
    CoreLabel c = cInfo.get(loc);
    CoreLabel c2 = cInfo.get(loc + 1);
    CoreLabel c3 = cInfo.get(loc + 2);
    CoreLabel p = cInfo.get(loc - 1);
    CoreLabel p2 = cInfo.get(loc - 2);
    CoreLabel p3 = cInfo.get(loc - 3);
    String charc = c.getString(CharAnnotation.class);
    String charc2 = c2.getString(CharAnnotation.class);
    String charc3 = c3.getString(CharAnnotation.class);
    String charp = p.getString(CharAnnotation.class);
    String charp2 = p2.getString(CharAnnotation.class);
    String charp3 = p3.getString(CharAnnotation.class);
    Integer cI = c.get(UTypeAnnotation.class);
    String uTypec = (cI != null ? cI.toString() : "");
    Integer c2I = c2.get(UTypeAnnotation.class);
    String uTypec2 = (c2I != null ? c2I.toString() : "");
    Integer c3I = c3.get(UTypeAnnotation.class);
    String uTypec3 = (c3I != null ? c3I.toString() : "");
    Integer pI = p.get(UTypeAnnotation.class);
    String uTypep = (pI != null ? pI.toString() : "");
    Integer p2I = p2.get(UTypeAnnotation.class);
    String uTypep2 = (p2I != null ? p2I.toString() : "");

    if (flags.dictionary != null || flags.serializedDictionary != null) {
      dictionaryFeaturesCpC(LBeginAnnotation.class, LMiddleAnnotation.class, LEndAnnotation.class,"",features, p2, p, c, c2);//不同于窗口大小为1的clique 多了一个p2
    }
    if (flags.dictionary2 != null) {
      dictionaryFeaturesCpC(D2_LBeginAnnotation.class, D2_LMiddleAnnotation.class,D2_LEndAnnotation.class,"-D2-",features, p2, p, c, c2);//不同于窗口大小为1的clique 多了一个p2
    }

    /*
     * N-gram features. N is upto 2.
     */
    if (flags.useWord2) {//和窗口大小为1的clique一样，是否重复？不重复，因为该特征与相应窗口大小为1的特征对应的 参数指示特征函数 不一样，例如该特征字面值为xxx，则窗口为1(即clique大小为1)对应的 参数指示特征函数 不一样 窗口大小为1时 参数指示特征函数 有两个 f(XXX|c:[0])和f(XXX|c:[1])
    	//而窗口大小为2(即clique大小为2)时 参数指示特征函数 有4个 f(XXX|cpc:[0 0]) f(XXX|cpc:[0 1]) f(XXX|cpc:[1 0]) 和 f(XXX|cpc:[1 1]) (注:虽然字面都为XXX但是不同大小窗口的clique后缀不一样，所以在feantureIndex里对应不同的索引)
      features.add(charc +"c");
      features.add(charc2+"c2");
      features.add(charp +"p");
      features.add(charp + charc  +"pc");
      features.add(charc + charc2  +"cc2");
      // cdm: need hyphen so you can see which of charp or charc2 is null....
      features.add(charp + "-" + charc2 + "pc2");
    }
    if (flags.useFeaturesCpC4gram || flags.useFeaturesCpC5gram || flags.useFeaturesCpC6gram) {
      features.add(charp2 + charp  +"p2p");
      features.add(charp2 + "p2");
    }
    if (flags.useFeaturesCpC5gram || flags.useFeaturesCpC6gram) {
      features.add(charc3+"c3");
      features.add(charc2 + charc3 + "c2c3");
    }
    if (flags.useFeaturesCpC6gram) {
      features.add(charp3 + "p3");
      features.add(charp3 + charp2 + "p3p2");
    }
    if (flags.useGoodForNamesCpC) {//该特征窗口大小为1的clique不提取
      // these 2 features should be distinctively good at biasing from
      // picking up a Chinese family name in the p2 or p3 positions:
      // familyName X X startWord AND familyName X startWord
      // But actually they seem to have negative value.
      features.add(charp2 + "p2");
      features.add(charp3 + "p3");
    }

    if (flags.useUnicodeType || flags.useUnicodeType4gram || flags.useUnicodeType5gram) {
      features.add(uTypep + "-" + uTypec + "-" + uTypec2 + "-uType3");
    }
    if (flags.useUnicodeType4gram || flags.useUnicodeType5gram) {
      features.add(uTypep2 + "-" + uTypep + "-" + uTypec + "-" + uTypec2 + "-uType4");
    }
    if (flags.useUnicodeType5gram) {
      features.add(uTypep2 + "-" + uTypep + "-" + uTypec + "-" + uTypec2 + "-" + uTypec3 + "-uType5");
    }
    if (flags.useWordUTypeConjunctions2) {
      features.add(uTypep + charc + "putcc");
      features.add(charp + uTypec + "pccut");
    }
    if (flags.useWordUTypeConjunctions3) {
      features.add(uTypep2 + uTypep + charc + "p2utputcc");
      features.add(uTypep + charc + uTypec2 + "putccc2ut");
      features.add(charc + uTypec2 + uTypec3 + "ccc2utc3ut");
    }
    if (flags.useUnicodeBlock) {
      features.add(p.getString(UBlockAnnotation.class) + "-" + c.getString(UBlockAnnotation.class) + "-" + c2.getString(UBlockAnnotation.class) + "-uBlock");
    }

    if (flags.useShapeStrings) {
      if (flags.useShapeStrings1) {
        features.add(p.getString(ShapeAnnotation.class) + "ps");
        features.add(c.getString(ShapeAnnotation.class) + "cs");
        features.add(c2.getString(ShapeAnnotation.class) + "c2s");
      }
      if (flags.useShapeStrings3) {
        features.add(p.getString(ShapeAnnotation.class) + c.getString(ShapeAnnotation.class) + c2.getString(ShapeAnnotation.class) + "pscsc2s");
      }
      if (flags.useShapeStrings4) {
        features.add(p2.getString(ShapeAnnotation.class) + p.getString(ShapeAnnotation.class) + c.getString(ShapeAnnotation.class) + c2.getString(ShapeAnnotation.class) + "p2spscsc2s");
      }
      if (flags.useShapeStrings5) {
        features.add(p2.getString(ShapeAnnotation.class) + p.getString(ShapeAnnotation.class) + c.getString(ShapeAnnotation.class) + c2.getString(ShapeAnnotation.class) + c3.getString(ShapeAnnotation.class) + "p2spscsc2sc3s");
      }//以下的特征在窗口大小为1的 clique 中均不提取
      if (flags.useWordShapeConjunctions2) {
        features.add(p.getString(ShapeAnnotation.class) + charc + "pscc");
        features.add(charp + c.getString(ShapeAnnotation.class) + "pccs");
      }
      if (flags.useWordShapeConjunctions3) {
        features.add(p2.getString(ShapeAnnotation.class) + p.getString(ShapeAnnotation.class) + charc + "p2spscc");
        features.add(p.getString(ShapeAnnotation.class) + charc + c2.getString(ShapeAnnotation.class) + "psccc2s");
        features.add(charc + c2.getString(ShapeAnnotation.class) + c3.getString(ShapeAnnotation.class) + "ccc2sc3s");
      }
    }

    /*
      Radical N-gram features. N is upto 4.
      Smoothing method of N-gram, because there are too many characters in Chinese.
      (It works better than N-gram when they are used individually. less sparse)
    */

    char rcharc, rcharc2, rcharp, rcharp2;
    if (charc.length()==0) { rcharc='n'; } else { rcharc= RadicalMap.getRadical(charc.charAt(0));}
    if (charc2.length()==0) { rcharc2='n'; } else { rcharc2=RadicalMap.getRadical(charc2.charAt(0));}
    if (charp.length()==0)  { rcharp='n';  } else { rcharp=RadicalMap.getRadical(charp.charAt(0));  }
    if (charp2.length()==0) { rcharp2='n'; } else { rcharp2=RadicalMap.getRadical(charp2.charAt(0));}

    if (flags.useRad2) {
      features.add(rcharc+"rc");
      features.add(rcharc2+"rc2");
      features.add(rcharp+"rp");
      features.add(rcharp  +  rcharc+"rprc");
      features.add(rcharc +rcharc2 +"rcrc2");
      features.add(rcharp +  rcharc  +rcharc2 +"rprcrc2");
    }
    if (flags.useRad2b) {
      features.add(rcharc+"rc");
      features.add(rcharc2+"rc2");
      features.add(rcharp+"rp");
      features.add(rcharp  +  rcharc+"rprc");
      features.add(rcharc +rcharc2 +"rcrc2");
      features.add(rcharp2 +rcharp +"rp2rp");
    }

    /* Non-word dictionary: SEEN bi-gram marked as non-word.
     * This is frickin' useful.  I hadn't realized.  CDM Oct 2007.
     */
    if (flags.useDict2) {
      NonDict2 nd = new NonDict2(flags);
      features.add(nd.checkDic(charp+charc, flags)+"nondict");
    }

    if (flags.useOutDict2) {
      if (outDict == null) {
        createOutDict();
      }
      
      
      
      features.add(outDict.getW(charp+charc)+"pc_eoutdict");       // -1 0
      features.add(outDict.getW(charc+charc2)+"cc2_eoutdict");      // 0 1
      features.add(outDict.getW(charp2+charp)+"p2p_eoutdict");      // -2 -1
      features.add(outDict.getW(charp2+charp+charc)+"p2pc_eoutdict");      // -2 -1 0
      features.add(outDict.getW(charp3+charp2+charp)+"p3p2p_eoutdict");      // -3 -2 -1
      features.add(outDict.getW(charp+charc+charc2)+"pcc2_eoutdict");      // -1 0 1
      features.add(outDict.getW(charc+charc2+charc3)+"cc2c3_eoutdict");      // 0 1 2
      features.add(outDict.getW(charp+charc+charc2+charc3)+"pcc2c3_eoutdict");      // -1 0 1 2
      features.add(outDict.getW(charp2+charp+charc+charc2+charc3)+"p2pcc2c3_eoutdict");
      features.add(outDict.getW(charp3+charp2+charp+charc+charc2+charc3)+"p3p2pcc2c3_eoutdict");
      
      
      //if(outDict.contains(charp+charc)&&(!charp.equals(""))&&(!charc.equals("")))
      //{
      //features.add(outDict.getW(charp+charc)+"outdict");       // -1 0
    	//  features.add((charp+charc)+"pc_outdict");     
      //}
      
     // if(outDict.contains(charc+charc2)&&(!charc.equals(""))&&(!charc2.equals("")))
      //{
      //features.add(outDict.getW(charc+charc2)+"outdict");      // 0 1
       //  features.add((charc+charc2)+"cc2_outdict");   
     // }
      
      //if(outDict.contains(charp2+charp)&&(!charp2.equals(""))&&(!charp.equals("")))
      //{
     // features.add(outDict.getW(charp2+charp)+"outdict");      // -2 -1
    	//  features.add((charp2+charp)+"p2p_outdict");   
      //}
      
      //if(outDict.contains(charp2+charp+charc)&&(!charp2.equals(""))&&(!charp.equals(""))&&(!charc.equals("")))
      //{
     // features.add(outDict.getW(charp2+charp+charc)+"outdict");      // -2 -1 0
      //features.add((charp2+charp+charc)+"p2pc_outdict"); 
      //}
      
      //if(outDict.contains(charp3+charp2+charp)&&(!charp3.equals(""))&&(!charp2.equals(""))&&(!charp.equals("")))
      //{
     // features.add(outDict.getW(charp3+charp2+charp)+"outdict");      // -3 -2 -1
      //features.add((charp3+charp2+charp)+"p3p2p_outdict");
      //}
      
      //if(outDict.contains(charp+charc+charc2)&&(!charp.equals(""))&&(!charc.equals(""))&&(!charc2.equals("")))
      //{
     // features.add(outDict.getW(charp+charc+charc2)+"outdict");      // -1 0 1
      //features.add((charp+charc+charc2)+"pcc2_outdict"); 
      //}
      
      //if(outDict.contains(charc+charc2+charc3)&&(!charc.equals(""))&&(!charc2.equals(""))&&(!charc3.equals("")))
      //{
      //features.add(outDict.getW(charc+charc2+charc3)+"outdict");      // 0 1 2
      //features.add((charc+charc2+charc3)+"cc2c3_outdict");
      //}
      
      //if(outDict.contains(charp+charc+charc2+charc3)&&(!charp.equals(""))&&(!charc.equals(""))&&(!charc2.equals(""))&&(!charc3.equals("")))
      //{
      //features.add(outDict.getW(charp+charc+charc2+charc3)+"outdict");      // -1 0 1 2
      //features.add((charp+charc+charc2+charc3)+"pcc2c3_outdict"); 
     // }
      
     // if(outDict.contains(charp2+charp+charc+charc2+charc3)&&(!charp2.equals(""))&&(!charp.equals(""))&&(!charc.equals(""))&&(!charc2.equals(""))&&(!charc3.equals("")))
      //{
      //features.add(outDict.getW(charp2+charp+charc+charc2+charc3)+"outdict");      // -1 0 1 2
     // features.add((charp2+charp+charc+charc2+charc3)+"p2pcc2c3_outdict"); 
     // }
      //if(outDict.contains(charp3+charp2+charp+charc+charc2+charc3)&&(!charp3.equals(""))&&(!charp2.equals(""))&&(!charp.equals(""))&&(!charc.equals(""))&&(!charc2.equals(""))&&(!charc3.equals("")))
      //{
     // features.add(outDict.getW(charp3+charp2+charp+charc+charc2+charc3)+"outdict");      // -1 0 1 2
      //features.add((charp3+charp2+charp+charc+charc2+charc3)+"p3p2pcc2c3_outdict"); 
      //}
      
      
    }

    /*
      (CTB/ASBC/HK/PK/MSR) POS information of each characters.
      If a character falls into some function categories,
      it is very likely there is a boundary.
      A lot of Chinese function words belong to single characters.
      This feature is also good for numbers and punctuations.
      DE* are grouped into DE.
    */
    if (flags.useCTBChar2 || flags.useASBCChar2 || flags.useHKChar2
        || flags.usePKChar2 || flags.useMSRChar2) {
      String[] tagsets;
      // the "useChPos" now only works for CTB and PK
      if (flags.useChPos) {
        if(flags.useCTBChar2) {
          tagsets = new String[]{"AD", "AS", "BA", "CC", "CD", "CS", "DE", "DT", "ETC", "IJ", "JJ", "LB", "LC", "M",  "NN",  "NR", "NT", "OD", "P", "PN", "PU", "SB", "SP", "VA", "VC", "VE", "VV" };
        } else if (flags.usePKChar2) {
          //tagsets = new String[]{"r", "j", "t", "a", "nz", "l", "vn", "i", "m", "ns", "nr", "v", "n", "q", "Ng", "b", "d", "nt"};
          tagsets = new String[]{"2","3","4"};
        } else {
          throw new RuntimeException("only support settings for CTB and PK now.");
        }
      } else {
        //System.err.println("Using Derived features");
        tagsets = new String[]{"2","3","4"};
      }

      if (taDetector == null) {
        createTADetector();
      }
      for (String tag : tagsets) {//词性词典 从ctb6-train/dict/character_list构造，键值时tag+p 或 tag+i 元素是具有该标记的词典集合
	features.add(taDetector.checkDic(tag+"p", charp) + taDetector.checkDic(tag+"i", charp) + taDetector.checkDic(tag+"s", charc)+ taDetector.checkInDic(charp)+taDetector.checkInDic(charc)+ tag+ "prep-sufc" );
        //features.add("|ctbchar2");
      }
    }

    /*
      In error analysis, we found English words and numbers are often separated.
      Rule 1: isNumber feature: check if the current and previous char is a number.
      Rule 2: Disambiguation of time point and time duration.
      Rule 3: isEnglish feature: check if the current and previous character is an english letter.
      Rule 4: English name feature: check if the current char is a conjunct pu for English first and last name, since there is no space between two names.
      Most of PUs are a good indicator for word boundary, but - and .  is a strong indicator that there is no boundry within a previous , a follow char and it.
    */

    if (flags.useRule2) {
      /* Reduplication features */
      // previous character == current character
      if(charp.equals(charc)){ features.add("11-R2");}
      // previous character == next character
      if(charp.equals(charc2)){ features.add("22-R2");}

      // current character == next next character
      // fire only when usePk and useHk are both false.
      // Notice: this should be (almost) the same as the "22" feature, but we keep it for now.
      if( !flags.usePk && !flags.useHk) {
        if(charc.equals(charc2)){features.add("33-R2");}
      }

      char cur1 = ' ';
      char cur2 = ' ';
      char cur =  ' ';
      char pre =  ' ';
      // actually their length must be either 0 or 1
      if (charc2.length() > 0) { cur1 = charc2.charAt(0); }
      if (charc3.length() > 0) { cur2 = charc3.charAt(0); }
      if (charc.length() > 0) { cur = charc.charAt(0); }
      if (charp.length() > 0) { pre = charp.charAt(0); }

      String prer= String.valueOf(rcharp); // the radical of previous character

      Pattern E = Pattern.compile("[a-zA-Z]");
      Pattern N = Pattern.compile("[0-9]");
      Matcher m = E.matcher(charp);
      Matcher ce = E.matcher(charc);
      Matcher pe = E.matcher(charp2);
      Matcher cn = N.matcher(charc);
      Matcher pn = N.matcher(charp2);


      // if current and previous characters are numbers...
      if (cur >= '0' && cur <= '9'&& pre >= '0' && pre <= '9'){
        if (cur == '9' && pre == '1' && cur1 == '9'&& cur2 >= '0' && cur2 <= '9'){ //199x
          features.add("YR-R2");
        }else{
          features.add("2N-R2");
        }

        // if current and previous characters are not both numbers
        // but previous char is a number
        // i.e. patterns like "1N" , "2A", etc
      } else if (pre >= '0' && pre <= '9'){
        features.add("1N-R2");

        // if previous character is an English character
      } else if(m.matches()){
        features.add("E-R2");

        // if the previous character contains no radical (and it exist)
      } else if(prer.equals(".") && charp.length() == 1){
        if(ce.matches()){
          features.add("PU+E-R2");
        }
        if(pe.matches()){
          features.add("E+PU-R2");
        }
        if(cn.matches()){
          features.add("PU+N-R2");
        }
        if(pn.matches()){
          features.add("N+PU-R2");
        }
        features.add("PU-R2");
      }

      String engType = isEnglish(charp, charc);
      String engPU = isEngPU(charp);
      if ( ! engType.equals(""))
        features.add(engType);
      if ( ! engPU.equals("") && ! engType.equals("")) {
        StringBuilder sb = new StringBuilder();
        sb.append(engPU).append(engType).append("R2");
        features.add(sb.toString());
      }
    }//end of use rule


    // features using "Character.getType" information!
    String origS = c.getString(OriginalCharAnnotation.class);
    char origC = ' ';
    if (origS.length() > 0) { origC = origS.charAt(0); }
    int type = Character.getType(origC);
    switch (type) {
    case Character.UPPERCASE_LETTER: // A-Z and full-width A-Z
    case Character.LOWERCASE_LETTER: // a-z and full-width a-z
      features.add("CHARTYPE-LETTER");
      break;
    case Character.DECIMAL_DIGIT_NUMBER:
      features.add("CHARTYPE-DECIMAL_DIGIT_NUMBER");
      break;
    case Character.OTHER_LETTER: // mostly chinese chars
      features.add("CHARTYPE-OTHER_LETTER");
      break;
    default: // other types
      features.add("CHARTYPE-MISC");
    }

    features.add("cliqueCpC");

    return features;
  }


  protected Collection<String> featuresCnC(PaddedList<? extends CoreLabel> cInfo, int loc) {
    Collection<String> features = new ArrayList<String>();
    if (flags.useWordn) {
      CoreLabel c = cInfo.get(loc);
      CoreLabel c2 = cInfo.get(loc + 1);
      CoreLabel p = cInfo.get(loc - 1);
      CoreLabel p2 = cInfo.get(loc - 2);
      String charc = c.getString(CharAnnotation.class);
      String charc2 = c2.getString(CharAnnotation.class);
      String charp = p.getString(CharAnnotation.class);
      String charp2 = p2.getString(CharAnnotation.class);

      features.add(charc +"c");
      features.add(charc2+"c2");
      features.add(charp +"p");
      features.add(charp2 + "p2");
      features.add(charp2 + charp  +"p2p");
      features.add(charp + charc  +"pc");
      features.add(charc + charc2  +"cc2");
      features.add(charp + "-" + charc2 + "pc2");
      features.add("cliqueCnC");
    }
    return features;
  } //end of CnC

  protected Collection<String> featuresCpCp2Cp3C(PaddedList<? extends CoreLabel> cInfo, int loc) {
    Collection<String> features = new ArrayList<String>();
    if (flags.use4Clique && flags.maxLeft >= 3) {
      CoreLabel c = cInfo.get(loc);
      CoreLabel c2 = cInfo.get(loc + 1);
      CoreLabel p = cInfo.get(loc - 1);
      CoreLabel p2 = cInfo.get(loc - 2);
      CoreLabel p3 = cInfo.get(loc - 3);
      String charc = c.getString(CharAnnotation.class);
      String charp = p.getString(CharAnnotation.class);
      String charp2 = p2.getString(CharAnnotation.class);
      String charp3 = p3.getString(CharAnnotation.class);
      Integer cI = c.get(UTypeAnnotation.class);
      String uTypec = (cI != null ? cI.toString() : "");
      Integer c2I = c2.get(UTypeAnnotation.class);
      String uTypec2 = (c2I != null ? c2I.toString() : "");
      Integer pI = p.get(UTypeAnnotation.class);
      String uTypep = (pI != null ? pI.toString() : "");
      Integer p2I = p2.get(UTypeAnnotation.class);
      String uTypep2 = (p2I != null ? p2I.toString() : "");
      Integer p3I = p3.get(UTypeAnnotation.class);
      String uTypep3 = (p3I != null ? p3I.toString() : "");


      if (flags.useLongSequences) {
        features.add(charp3 + charp2 + charp + charc + "p3p2pc");
      }
      if (flags.useUnicodeType4gram || flags.useUnicodeType5gram) {
        features.add(uTypep3 + "-" + uTypep2 + "-" + uTypep + "-" + uTypec + "-uType4");
      }
      if (flags.useUnicodeType5gram) {
        features.add(uTypep3 + "-" + uTypep2 + "-" + uTypep + "-" + uTypec + "-" + uTypec2 + "-uType5");
      }
      features.add("cliqueCpCp2Cp3C");
    }
    return features;
  }

  private static final long serialVersionUID = 8197648719208850960L;

} // end class Gale2007ChineseSegmenterFeatureFactory

