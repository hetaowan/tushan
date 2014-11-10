package org.jmlp.str.app;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;

import org.jmlp.file.utils.FileToArray;
import org.jmlp.str.basic.SSO;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

public class SegTestDouble {

	public String seg_server = "";
	public int seg_port = 0;
	
	public CRFClassifier<CoreLabel> classifier=null;
	
	public void load_local_config() throws Exception {

		InetAddress addr = InetAddress.getLocalHost();
		String ip = addr.getHostAddress().toString();// 获得本机IP
		String address = addr.getHostName().toString();// 获得本机名
		/*
		 * FileInputStream fis = new FileInputStream(config_file); Properties
		 * prop = new Properties(); prop.load(fis); seg_server =
		 * prop.getProperty("seg_server"); seg_port =
		 * Integer.parseInt(prop.getProperty("seg_port")); tag_server =
		 * prop.getProperty("tag_server"); tag_port =
		 * Integer.parseInt(prop.getProperty("tag_port"));
		 * 
		 * swa_dict_ip = prop.getProperty("swa_dict_ip"); swa_dict_port =
		 * Integer.parseInt(prop.getProperty("swa_dict_port")); swa_dict_db =
		 * Integer.parseInt(prop.getProperty("swa_dict_db"));
		 * 
		 * swa_dict_redis = new Jedis(swa_dict_ip, swa_dict_port, 100000);//
		 * redis服务器地址 swa_dict_redis.ping(); swa_dict_redis.select(swa_dict_db);
		 * fis.close();
		 */

		seg_port = 8092;

		address = address.trim();
		address="adt6";
		if (address.equals("adt2")) {
			seg_server = "222.85.64.100";
		} else if (address.equals("adt1")) {
			seg_server = "192.168.110.181";
		} else if (address.equals("adt6")) {
			seg_server = "192.168.110.186";
		} else if (address.equals("adt8")) {
			seg_server = "192.168.110.188";
		} else if (address.equals("hndx_fx_100")) {
			seg_server = "192.168.1.100";
		}

	}
	
	public String seg(String s) throws Exception {
        s=s.trim();
		s = s + "\n";
		String seg_s = "";
		String server = seg_server;
		int port = seg_port;
		try {
			Socket socket = new Socket(server, port);
			socket.setSoTimeout(10000);
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			out.write(s.getBytes());
			out.flush();

			byte[] receiveBuf = new byte[10032 * 8];
			in.read(receiveBuf);

			seg_s = new String(receiveBuf);
			socket.close();
		} catch (Exception e) {
			Thread.sleep(1000);
		}
		return seg_s;

	}
	
	
	public String double_seg(String s) throws Exception
	{
		String ds="";
		
		//String fs=seg(s);;
		//System.out.println("fs:"+fs);
		String[] seg_arr=s.split("\\s+");
		String nfs="";
		String word="";
		for(int i=0;i<seg_arr.length;i++)
		{
			word=seg_arr[i].trim();
			if((word==null)||(word.equals("")))
			{
				continue;
			}
			
			nfs=nfs+word+"/1"+" ";
		}
		
		nfs=nfs.trim();
		System.out.println("nfs:"+nfs);
		
		ds=classifier.classifyToString(nfs);
		
		//System.out.println("ds:"+ds);
		return ds;
		
	}
	
	
	public void loadSecondClassifier() throws Exception
	{
	    Properties props = new Properties();
        FileInputStream fis = new FileInputStream("config/train_beq2.prop");
        props.load(fis);
        // props.setProperty("serDictionary", "data/dict-chris6.ser.gz");
        props.setProperty("sighanCorporaDict", "data");          

		classifier = new CRFClassifier<CoreLabel>(props);
		classifier.loadClassifierNoExceptions("test.gz", props);
		// flags must be re-set after data is loaded
		classifier.flags.setProperties(props);
	}
	
	public static void main(String[] args) throws Exception
	{
		SegTestDouble stdou=new SegTestDouble();
		stdou.load_local_config();
		stdou.loadSecondClassifier();
		/*
		FileWriter fw=new FileWriter(new File("testdouble.txt"));
		PrintWriter pw=new PrintWriter(fw);
		*/
		
		String[] strs={"一个良好的分词系统，应当由词典和统计两套系统组成。后者是为前者构造可持续更新的词典，识别新词，同时消歧的部分可引入词典匹配。而真正上线主要还应当基于词典，一个原因是性能，另一个原因是一致性。","工信处女干事每月经过下属科室都要亲口交代24口交换机等技术性器件的安装工作","国务院总理李克强调研上海外高桥时提出，支持上海积极探索新机制","希望网友也能够关注苹果园微博，加入微信群获得更多交流信息。","【情定马尔代夫，让浪漫不再奢侈】#2份#&lt;马尔代夫2000元旅游券&gt;途牛免费送！马上参与马上团，马上带她去马尔代夫尽享极致奢华、浪漫的甜蜜之旅！中奖号码根据沪深指数计算得出，无暗箱操作，还不快参加。_0元团团购团购_途牛旅游团购","2013韩版冬季保暖真皮女鞋高跟过膝靴子细跟侧拉链貂毛马丁长靴","去过长隆野生动物园多次啦。从刚开园到现在已开业十多年，看得出长隆品牌生长的历程及其不断前进的脚步。最近一次去是春节前，从侧门进入，人不算太多，但停车位有点紧张 ，不好找。表演节目比以前更多样化，看得小朋友都入了迷。吃的地方比以前多并舒适，我去的熊猫餐厅，气氛果然不错，就是价格较贵。纪念品品种多，比吸引小朋友的眼球。家长 就可怜啦，要破费。动物还是比较花心机去饲养，皮毛光亮。感觉动物品种没以前多，像鲟龙、海豹之类的，以前有现在没找到。听说有套票，比较划算。大家可以去看看。","东水山景区位于阳西西北山区，距县城约40公里，主峰鹅凰障高达1337米，是广东省第四高山，山顶可直望南海。东水山景区由于特殊的地理环境和气候，使世界濒临绝迹的许多珍贵植物在这里得以保存下来，已发现的珍稀树种有猪血木、红山茶、月杜鹃等以及各种野生动物，是天然的聚...宝库。 东水山有漫山遍墅的翠竹、形状各异的各种奇石、独具一格的流水瀑布，还有那古法造纸作坊。绿树掩映的村庄，是那浓浓的山乡风情。坐在古色古香的山庄中吃粗粮，嚼野菜，品山茶，你会感到滋味无穷。位于村后的盘古宫，是东水人来源久远的写照。他们崇拜先人，畏敬鬼神，在东水，有无数人居的岩洞。东水山景区内的望夫人为全国九大望夫山之一。","女儿很喜欢烂苹果乐园，就是她还小，胆子又小，大地震、青蛙跳等几个项目不敢玩。其他有的项目连我都觉得还有点意思，比如4Ｄ环境电影，烂苹果剧场，斜屋，魔镜屋等。由于我们是国庆长假去的，人很多，有的项目没排到队，以后再去的话再不能假期去了，要把没玩到的给补回来。","去过长隆野生动物园多次啦。从刚开园到现在已开业十多年，看得出长隆品牌生长的历程及其不断前进的脚步。最近一次去是春节前，从侧门进入，人不算太多，但停车位有点紧张 ，不好找。表演节目比以前更多样化，看得小朋友都入了迷。吃的地方比以前多并舒适，我去的熊猫餐厅，气氛果然不错，就是价格较贵。纪念品品种多，比吸引小朋友的眼球。家长 就可怜啦，要破费。动物还是比较花心机去饲养，皮毛光亮。感觉动物品种没以前多，像鲟龙、海豹之类的，以前有现在没找到。听说有套票，比较划算。大家可以去看看。","山东枣庄熊耳山国家地质公园位于山东省南部，枣庄市北偏东8公里，总面积98平方公里。国家地质公园以熊耳山岩溶地质地貌和抱犊崮地貌为特色。","情人节遇上元宵节，难得阳光明媚的好天气，天公作美，和家人登上雷峰塔看西湖全景，近处的三潭印月、苏堤，远处的断桥、保俶塔，尽收眼底。看西湖全景，非常不错。","伪满洲国国务院旧址就是吉林大学医学院基础教学楼，在这里风景谈不上，只是体会一下历史，看看建筑设计风格，整个建筑物呈川字形，塔式屋顶，屋顶葺以烟色琉璃瓦，另外还可参观一下末代皇帝溥仪阅兵台、伪满洲国国务总理大臣张景惠办公室、病理人体标本陈列馆、京华阁画廊。在京华阁画廊可购买名人字画、文房四宝、珠宝玉器、工艺美术、人参鹿茸、滋补品等旅游商品，总体说还是不错。","大明奇石馆座落在阳江市境内广湛公路边，占地2000多平方米，藏石数千件，重百余吨，是全国目前最具规模石种较多的私人奇石收藏展览场馆之一。","韩版冬季保暖真皮女鞋高跟过膝靴子细跟侧拉链貂毛马丁长靴","彭客网-徐州首席生活服务平台徐州社区,徐州论坛,徐州社区服务平台,徐州家庭服务平台,徐州生活服务平台彭客网是徐州报业传媒集团旗下综合性社区门户网站，《彭城晚报》战略合作伙伴，致力于为网民提供新鲜、真实、有用、好玩的生活资讯，打造徐州首席生活服务平台,彭客网","新浪网为全球用户24小时提供全面及时的中文资讯，内容覆盖国内外突发新闻事件、体坛赛事、娱乐时尚、产业资讯、实用信息等，设有新闻、体育、娱乐、财经、科技、房产、汽车等30多个内容频道，同时开设博客、视频、论坛等自由互动交流空间。","北京大学（Peking University）简称北大（PKU），创建于1898年，初名京师大学堂，是中国近代第一所国立大学，也是中国近代最早以“大学”身份和名称建立的学校，其成立标志着中国近代高等教育的开端。北大是中国近代唯一以最高学府身份创立的学校，最初也是国家最高教育行政机关，行使教育部职能，统管全国教育；并开创了中国高校中最早的文科、理科、政科、商科、农科、医科等学科的大学教育，是近代以来中国高等教育的奠基者。","《梁漱溟日记》是有“最后的儒家”之称的梁漱溟先生现存全部日记（近80万字）的汇编， 并附数十张首次公开的珍贵私家历史照片。梁先生早年起即有记日记的习惯，现存日记始于1932年，终于1981年，历经“文革”抄家等磨难才得留存。著者早年投身乡村建设，巡视抗战敌后，调停国共两党争端，上缙云山闭关修佛，解放后参观城乡新变与土地改革，“文革”抄家受辱，政协学习论辩，常年坚持著述修行等等，长达50年的行止经历及感受心境，在日记中都有朴实的记录。本书是梁漱溟日记首次完整单行，编者撰写了导读性质的前言和每一年大事提要，修订及增补注释600余条，并编制主要人名索引近2000条，是深入","俄裔美籍作家，全知全能，被全世界读者誉为“神一样的人”。美国政府授予他“国家的资源和大自然的奇迹”这个独一无二的称号，以表彰他在“拓展人类想象力”上做出的杰出贡献。阿西莫夫创作力丰沛，一生之中著作近500 本，涉及杜威图书分类法的每一个范畴，涵盖人类生活的每一个层面，上天下海、古往今来、从恐龙到亚原子到全宇宙无所不包，从通俗小说到罗马帝国史，从科普读物到远东千年历史，从圣经指南，到科学指南，到两性生活指南，每一部著作都朴实、严谨而又充满幽默风趣的格调。作为人类世界里最伟大的科幻小说家之一，阿西莫夫曾获得代表着科幻界最高荣誉的雨果奖和星云终身成就“大师奖”。他于1955年完成了一部极富远见的、关于时间旅行的长篇小说《永恒的终结》，厘清了关于时间旅行的终极奥秘和恢宏构想","这是只有阿西莫夫才能写出来的，浸淫着他一贯的对人类终极命运进行的思考，因为有了考察人类终极命运这个超然角度，这部小说可以说一网打尽了关于时间旅行这个长盛不衰话题的方方面面。 关于时间旅行，有大大小小的悖论，但常见的可以拿来做文章的可以大致分为以下几种： 祖父悖论（grandfather paradox），最著名代表作为海因莱因的短篇《你们这些回魂尸》。基本上就是说，别改变过去，对过去的任何调整都会导致现状","克里帝国与新星军团的战争绵延千年，家族世代为主战派的指控者罗南不顾两国政府签订的休战条约，擅作主张的对新星军团实施不同规模的恐[哔]袭击。由于其家族在克里帝国内部地位尊崇，因此当局对他的行为也是睁一只眼闭一只眼。而实际上罗南还在酝酿着更大的计划。 大帅哥李佩斯扮演的指控者罗南以一个蓝色大光头的造型，嘴里搅着屎一样的东西就一丝不挂的亮相了"};
		/*
		for(int i=0;i<strs.length;i++)
		{
		String first_seg=stdou.seg(strs[i]);
		String second_seg=stdou.double_seg(strs[i]);
		pw.println("first_seg:"+first_seg.trim());
		pw.println("second_seg:"+second_seg.trim());
		}
		fw.close();
		pw.close();
		*/
		
		/*
		String[] first_segs = FileToArray.fileToDimArr("firstdouble.txt");

		for (int i = 0; i < first_segs.length; i++) {
			if (SSO.tioe(first_segs[i])) {
				continue;
			}
			String second_seg = stdou.double_seg(first_segs[i]);
			pw.println("first_seg:" + first_segs[i]);
			pw.println("second_seg:" + second_seg.trim());
		}
		fw.close();
		pw.close();
	   */
	}
	
}

