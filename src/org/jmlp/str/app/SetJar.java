package org.jmlp.str.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Properties;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;

public class SetJar {
	public String seg_server = "";
	public int seg_port = 0;

	public CRFClassifier<CoreLabel> first_classifier = null;

	public CRFClassifier<CoreLabel> second_classifier = null;

	public void init_first() {
		try {

			Properties props = new Properties();
			// FileInputStream fis = new FileInputStream("train.prop");
			InputStream fis = IOUtils
					.getInputStreamFromURLOrClasspathOrFileSystem("train.prop");
			props.load(fis);
			props.setProperty("serDictionary", "data/dict-chris6.ser.gz");
			// props.setProperty("sighanCorporaDict", "data");
			first_classifier = new CRFClassifier<CoreLabel>(props);
			first_classifier.loadClassifierNoExceptions(
					"models/seg_external_dict.gz", props);
			// first_classifier.loadClassifierNoExceptions("data/ctb.gz",
			// props);
			first_classifier.flags.setProperties(props);
			/*
			 * Properties props = new Properties();
			 * props.setProperty("sighanCorporaDict", "data");
			 * props.setProperty("NormalizationTable", "data/norm.simp.utf8");
			 * props.setProperty("normTableEncoding", "UTF-8"); // below is
			 * needed because CTBSegDocumentIteratorFactory accesses it //
			 * props.setProperty("serDictionary","data/dict-chris6.ser.gz");
			 * props.setProperty("serDictionary","data/dict-chris6.ser.gz"); //
			 * props.setProperty("testFile", args[0]);
			 * props.setProperty("inputEncoding", "UTF-8");
			 * props.setProperty("sighanPostProcessing", "true");
			 * 
			 * first_classifier = new CRFClassifier<CoreLabel>(props);
			 * first_classifier.loadClassifierNoExceptions("data/ctb.gz",
			 * props); // flags must be re-set after data is loaded
			 * first_classifier.flags.setProperties(props);
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void init_second() {
		try {
			Properties props = new Properties();
			// FileInputStream fis = new
			// FileInputStream("config/train_beq2.prop");
			InputStream fis = IOUtils
					.getInputStreamFromURLOrClasspathOrFileSystem("config/train_beq2.prop");
			props.load(fis);
			// props.setProperty("serDictionary", "data/dict-chris6.ser.gz");
			// props.setProperty("sighanCorporaDict", "data");

			second_classifier = new CRFClassifier<CoreLabel>(props);
			second_classifier.loadClassifierNoExceptions("models/test.gz",
					props);
			// flags must be re-set after data is loaded
			second_classifier.flags.setProperties(props);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String seg(String s) throws Exception {
		s = s.trim();
		String res = first_classifier.classifyToString(s);
		return res;

	}

	public String double_seg(String s, PrintWriter pw) throws Exception {
		String ds = "";
		s = s.trim();
		String fs = seg(s);
		fs = fs.trim();
		//pw.println("fs:" + fs);

		String[] seg_arr = fs.split("\\s+");
		String nfs = "";
		String word = "";
		for (int i = 0; i < seg_arr.length; i++) {
			word = seg_arr[i].trim();
			if ((word == null) || (word.equals(""))) {
				continue;
			}

			nfs = nfs + word + "/1" + " ";
		}

		nfs = nfs.trim();
		// System.out.println("nfs:" + nfs);

		ds = second_classifier.classifyToString(nfs);
		ds = ds.trim();
		// System.out.println("ds:"+ds);
		return ds;

	}

	public static void main(String[] args) throws Exception {

		if (args.length != 3) {
			System.err
					.println("Usage: <field_num> <seg_field_index> <separator>");
			System.err.println("    field_num : 输入的字段个数");
			System.err.println("    seg_field_index: 要分词的字段编号，从0开始，即0表示第一个字段");
			System.err
					.println("    separator:字段间的分隔符，001 表示 字符001，blank 表示\\s+ 即连续空格,tab标识\t");
			System.exit(1);
		}

		// 输入的字段个数用
		int fieldNum = 0;

		// 待分词的字段编号
		int segFieldIndex = 0;

		// 字段间的分隔符:001 表示 \001
		// :blank 表示\\s+ 即连续空格
		String separator = "";
		String outputSeparator = "";

		fieldNum = Integer.parseInt(args[0]);
		segFieldIndex = Integer.parseInt(args[1]);
		if (args[2].equals("001")) {
			separator = "\001";
			outputSeparator = "\001";
		} else if (args[2].equals("blank")) {
			separator = "\\s+";
			outputSeparator = "\t";
		} else if (args[2].equals("tab")) {
			separator = "\t";
			outputSeparator = "\t";
		} else {
			separator = args[2].trim();
			outputSeparator = separator.trim();
		}

		SetJar sd = new SetJar();
		sd.init_first();
		sd.init_second();

		// String text="再看新白娘子传奇，才明白小青和张公子的爱情故事那么悲情。";
		// System.out.println("seg_text:"+sd.double_seg(text));

		/*
		 * String[] strs = { "再看新白娘子传奇，才明白小青和张公子的爱情故事那么悲情。",
		 * "一个良好的分词系统，应当由词典和统计两套系统组成。后者是为前者构造可持续更新的词典，识别新词，同时消歧的部分可引入词典匹配。而真正上线主要还应当基于词典，一个原因是性能，另一个原因是一致性。"
		 * , "工信处女干事每月经过下属科室都要亲口交代24口交换机等技术性器件的安装工作",
		 * "国务院总理李克强调研上海外高桥时提出，支持上海积极探索新机制", "希望网友也能够关注苹果园微博，加入微信群获得更多交流信息。",
		 * "【情定马尔代夫，让浪漫不再奢侈】#2份#&lt;马尔代夫2000元旅游券&gt;途牛免费送！马上参与马上团，马上带她去马尔代夫尽享极致奢华、浪漫的甜蜜之旅！中奖号码根据沪深指数计算得出，无暗箱操作，还不快参加。_0元团团购团购_途牛旅游团购"
		 * , "2013韩版冬季保暖真皮女鞋高跟过膝靴子细跟侧拉链貂毛马丁长靴",
		 * "去过长隆野生动物园多次啦。从刚开园到现在已开业十多年，看得出长隆品牌生长的历程及其不断前进的脚步。最近一次去是春节前，从侧门进入，人不算太多，但停车位有点紧张 ，不好找。表演节目比以前更多样化，看得小朋友都入了迷。吃的地方比以前多并舒适，我去的熊猫餐厅，气氛果然不错，就是价格较贵。纪念品品种多，比吸引小朋友的眼球。家长 就可怜啦，要破费。动物还是比较花心机去饲养，皮毛光亮。感觉动物品种没以前多，像鲟龙、海豹之类的，以前有现在没找到。听说有套票，比较划算。大家可以去看看。"
		 * ,
		 * "东水山景区位于阳西西北山区，距县城约40公里，主峰鹅凰障高达1337米，是广东省第四高山，山顶可直望南海。东水山景区由于特殊的地理环境和气候，使世界濒临绝迹的许多珍贵植物在这里得以保存下来，已发现的珍稀树种有猪血木、红山茶、月杜鹃等以及各种野生动物，是天然的聚...宝库。 东水山有漫山遍墅的翠竹、形状各异的各种奇石、独具一格的流水瀑布，还有那古法造纸作坊。绿树掩映的村庄，是那浓浓的山乡风情。坐在古色古香的山庄中吃粗粮，嚼野菜，品山茶，你会感到滋味无穷。位于村后的盘古宫，是东水人来源久远的写照。他们崇拜先人，畏敬鬼神，在东水，有无数人居的岩洞。东水山景区内的望夫人为全国九大望夫山之一。"
		 * ,
		 * "女儿很喜欢烂苹果乐园，就是她还小，胆子又小，大地震、青蛙跳等几个项目不敢玩。其他有的项目连我都觉得还有点意思，比如4Ｄ环境电影，烂苹果剧场，斜屋，魔镜屋等。由于我们是国庆长假去的，人很多，有的项目没排到队，以后再去的话再不能假期去了，要把没玩到的给补回来。"
		 * ,
		 * "去过长隆野生动物园多次啦。从刚开园到现在已开业十多年，看得出长隆品牌生长的历程及其不断前进的脚步。最近一次去是春节前，从侧门进入，人不算太多，但停车位有点紧张 ，不好找。表演节目比以前更多样化，看得小朋友都入了迷。吃的地方比以前多并舒适，我去的熊猫餐厅，气氛果然不错，就是价格较贵。纪念品品种多，比吸引小朋友的眼球。家长 就可怜啦，要破费。动物还是比较花心机去饲养，皮毛光亮。感觉动物品种没以前多，像鲟龙、海豹之类的，以前有现在没找到。听说有套票，比较划算。大家可以去看看。"
		 * ,
		 * "山东枣庄熊耳山国家地质公园位于山东省南部，枣庄市北偏东8公里，总面积98平方公里。国家地质公园以熊耳山岩溶地质地貌和抱犊崮地貌为特色。"
		 * ,
		 * "情人节遇上元宵节，难得阳光明媚的好天气，天公作美，和家人登上雷峰塔看西湖全景，近处的三潭印月、苏堤，远处的断桥、保俶塔，尽收眼底。看西湖全景，非常不错。"
		 * ,
		 * "伪满洲国国务院旧址就是吉林大学医学院基础教学楼，在这里风景谈不上，只是体会一下历史，看看建筑设计风格，整个建筑物呈川字形，塔式屋顶，屋顶葺以烟色琉璃瓦，另外还可参观一下末代皇帝溥仪阅兵台、伪满洲国国务总理大臣张景惠办公室、病理人体标本陈列馆、京华阁画廊。在京华阁画廊可购买名人字画、文房四宝、珠宝玉器、工艺美术、人参鹿茸、滋补品等旅游商品，总体说还是不错。"
		 * ,
		 * "大明奇石馆座落在阳江市境内广湛公路边，占地2000多平方米，藏石数千件，重百余吨，是全国目前最具规模石种较多的私人奇石收藏展览场馆之一。"
		 * , "韩版冬季保暖真皮女鞋高跟过膝靴子细跟侧拉链貂毛马丁长靴",
		 * "彭客网-徐州首席生活服务平台徐州社区,徐州论坛,徐州社区服务平台,徐州家庭服务平台,徐州生活服务平台彭客网是徐州报业传媒集团旗下综合性社区门户网站，《彭城晚报》战略合作伙伴，致力于为网民提供新鲜、真实、有用、好玩的生活资讯，打造徐州首席生活服务平台,彭客网"
		 * ,
		 * "新浪网为全球用户24小时提供全面及时的中文资讯，内容覆盖国内外突发新闻事件、体坛赛事、娱乐时尚、产业资讯、实用信息等，设有新闻、体育、娱乐、财经、科技、房产、汽车等30多个内容频道，同时开设博客、视频、论坛等自由互动交流空间。"
		 * ,
		 * "北京大学（Peking University）简称北大（PKU），创建于1898年，初名京师大学堂，是中国近代第一所国立大学，也是中国近代最早以“大学”身份和名称建立的学校，其成立标志着中国近代高等教育的开端。北大是中国近代唯一以最高学府身份创立的学校，最初也是国家最高教育行政机关，行使教育部职能，统管全国教育；并开创了中国高校中最早的文科、理科、政科、商科、农科、医科等学科的大学教育，是近代以来中国高等教育的奠基者。"
		 * ,
		 * "《梁漱溟日记》是有“最后的儒家”之称的梁漱溟先生现存全部日记（近80万字）的汇编， 并附数十张首次公开的珍贵私家历史照片。梁先生早年起即有记日记的习惯，现存日记始于1932年，终于1981年，历经“文革”抄家等磨难才得留存。著者早年投身乡村建设，巡视抗战敌后，调停国共两党争端，上缙云山闭关修佛，解放后参观城乡新变与土地改革，“文革”抄家受辱，政协学习论辩，常年坚持著述修行等等，长达50年的行止经历及感受心境，在日记中都有朴实的记录。本书是梁漱溟日记首次完整单行，编者撰写了导读性质的前言和每一年大事提要，修订及增补注释600余条，并编制主要人名索引近2000条，是深入"
		 * ,
		 * "俄裔美籍作家，全知全能，被全世界读者誉为“神一样的人”。美国政府授予他“国家的资源和大自然的奇迹”这个独一无二的称号，以表彰他在“拓展人类想象力”上做出的杰出贡献。阿西莫夫创作力丰沛，一生之中著作近500 本，涉及杜威图书分类法的每一个范畴，涵盖人类生活的每一个层面，上天下海、古往今来、从恐龙到亚原子到全宇宙无所不包，从通俗小说到罗马帝国史，从科普读物到远东千年历史，从圣经指南，到科学指南，到两性生活指南，每一部著作都朴实、严谨而又充满幽默风趣的格调。作为人类世界里最伟大的科幻小说家之一，阿西莫夫曾获得代表着科幻界最高荣誉的雨果奖和星云终身成就“大师奖”。他于1955年完成了一部极富远见的、关于时间旅行的长篇小说《永恒的终结》，厘清了关于时间旅行的终极奥秘和恢宏构想"
		 * ,
		 * "这是只有阿西莫夫才能写出来的，浸淫着他一贯的对人类终极命运进行的思考，因为有了考察人类终极命运这个超然角度，这部小说可以说一网打尽了关于时间旅行这个长盛不衰话题的方方面面。 关于时间旅行，有大大小小的悖论，但常见的可以拿来做文章的可以大致分为以下几种： 祖父悖论（grandfather paradox），最著名代表作为海因莱因的短篇《你们这些回魂尸》。基本上就是说，别改变过去，对过去的任何调整都会导致现状"
		 * ,
		 * "克里帝国与新星军团的战争绵延千年，家族世代为主战派的指控者罗南不顾两国政府签订的休战条约，擅作主张的对新星军团实施不同规模的恐[哔]袭击。由于其家族在克里帝国内部地位尊崇，因此当局对他的行为也是睁一只眼闭一只眼。而实际上罗南还在酝酿着更大的计划。 大帅哥李佩斯扮演的指控者罗南以一个蓝色大光头的造型，嘴里搅着屎一样的东西就一丝不挂的亮相了"
		 * };
		 */

		/*
		 * String[] strs = {
		 * "国共两党决战之际，以蒋经国为首的国民党少壮派，突然对涉嫌通共的国民党空军王牌飞行员方孟敖委以重任，将其飞行大队改编为国防部经济稽查大队，前往北平调查民食调配物资的贪腐案，藉此打击以方孟敖的父亲、国民党中央银行北平分行行长方步亭为核心的孔宋家族贪腐势力，真正目的其实是要执行国民党“黄金运台”的惊天计划。"
		 * , "国务院总理李克强调研上海外高桥时提出，支持上海积极探索新机制。",
		 * "中国的IT行业的就业形势一直呈上升趋势，中国软件市场保持稳定增长，2009年已达62.3亿美元，市场增长潜力巨大。在技术领域，高级软件工程师也一直是各个公司极为需要的。"
		 * , "都是月亮惹的祸, 都是月亮惹的祸在线试听, 都是月亮惹的祸歌词下载, 都是月亮惹的祸在线试听,MP3免费下载。",
		 * "《桃花井》是蒋晓云复归文坛完成的第一部作品，也是她的第一本长篇小说。",
		 * "看完匆匆那年，我竟然泪流满面。谁和谁的那年，不匆匆。不是我们走的太慢，是时间走的太快。",
		 * "在小米供职期间，黎万强任小米副总裁，负责小米的市场营销、电商和服务，曾著有《参与感——小米口碑营销内部手册》并在本书中提出，小米营销的三个基石：做爆品，做粉丝，做自媒体"
		 * };
		 */
		/*
		 * String[] strs={"2013春秋装新款女装韩版甜美修身长袖雪纺连衣裙 翻领雪纺女裙子",
		 * "2013秋冬装新款ShuLier8818可拆卸 貉子毛领裙摆式毛呢大衣外套",
		 * "&lt;足尚鞋吧&gt;专柜正品2013秋冬新款蓝鸟女鞋棉靴21-9",
		 * "2013秋冬装新款ShuLier8818可拆卸 貉子毛领裙摆式毛呢大衣外套",
		 * "秋冬季黑色韩版绒面单靴 平底女靴子高筒弹力靴过膝长靴女鞋二棉", "2013真皮粗跟头层牛皮单鞋女鞋圆头中跟水钻蝴蝶结浅口黑色低帮鞋",
		 * "赚人气3支包邮自制可以吃的护唇膏超保湿防干裂/任何人都可以用",
		 * "美国Crayola绘儿乐 16色可水洗短杆水笔-造型笔尖 58-8709",
		 * "特价秋冬新款女T恤长袖修身加厚保暖打底衫海狸绒气质淑女休闲", "制谱，打谱，简谱、五线谱制作、声乐钢琴伴奏谱移调2",
		 * "2013冬季新款女棉鞋 真皮羊毛中跟短靴大码妈妈鞋女棉靴子35-43码-tmall.com天猫",
		 * "包邮专柜正品旁氏无瑕透白精致凝白水润霜女士面霜保湿补水美白", "2013秋冬款包臀性感淑女A字裙OL通勤连衣裙夏赫本风连衣裙夏",
		 * "结婚喜糖盒 创意喜糖盒子 婚庆用品 大号马口铁盒  可批发定制", "换季清仓 Polo 斯文细条纹柔软舒适薄棉男士翻领T恤（2色）",
		 * "真能超轻水植物精油眼部活力紧致精华 淡纹 去黑眼圈眼袋 保湿", "深圳红玫瑰鲜花速递深圳沙井福永松岗南山宝安龙岗罗湖福田鲜花店",
		 * "ochirly fiveplus服装　Ｔ恤　马夹　特价商品绝对正品专柜验货",
		 * "2013新款女装秋装连衣裙 秋冬长袖修身韩版潮 新品中长款打底裙",
		 * "2013冬allo lugh原单正品小狐狸白毛毛马甲+长袖t恤2件套",
		 * "amyy/定制款藏青色羊毛呢双排扣大衣 限时折扣 预定一周", "特价 欧美包臀V领性感夜店豹纹修身长袖裹胸连衣裙2013秋新款女",
		 * "复古vintage撞色翻领彼得潘娃娃领兔毛修身套头毛衣女", "带光带灯发光兔耳朵发箍套装兔子头箍发卡发饰品兔女郎 跳舞道具",
		 * "秋冬裸靴中筒靴女靴子欧美复古高跟鞋冬中靴粗跟马丁靴防水台女鞋", "10件包邮韩版家居清新可爱糖果色五角星收纳袋购物袋大容量手提袋",
		 * "资生堂UNO吾诺男士洗面奶控油/保湿/磨砂洁面乳130g 台湾版", "2013秋季韩国新款学院风五角星铆钉长袖前短后长卫衣套头衫",
		 * "正品美国辉瑞卫佳五联疫苗5联4联卫佳四联（宠物医院）狗狗疫苗", "送男友老公首选 包邮双拼男士风格全棉床上用品 床单式纯棉四件套",
		 * "正品太阳眼镜男士女士太阳眼镜驾驶眼镜蛤蟆镜墨镜男太阳镜 代购",
		 * "安程腰靠 腰枕靠背 布纹 高档尼龙布沙发腰靠枕 靠垫 防污防腿色"};
		 */

		PrintWriter pw = new PrintWriter(new FileWriter("testdouble.txt"));
		String[] strs = { "看完匆匆那年，我竟然泪流满面。谁和谁的那年，不匆匆。不是我们走的太慢，是时间走的太快。","电影四大名捕怎么样" };
		// String[] strs=FileToArray.fileToDimArr("temp/title.txt");
		String seg = "";
		for (int i = 0; i < strs.length; i++) {

			try {
				seg = sd.double_seg(strs[i], pw);
				pw.println(seg.replaceAll("\\s+", "_"));
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);

		String line = "";
		String[] fields = null;
		while ((line = br.readLine()) != null) {
			try {
				fields = line.split(separator);
				if (fields.length != fieldNum) {
					continue;
				}
				for (int j = 0; j < segFieldIndex; j++) {
					pw.print(fields[j] + outputSeparator);
				}
				if (segFieldIndex < (fieldNum - 1)) {
					pw.print(sd.double_seg(fields[segFieldIndex], pw).trim()
							+ outputSeparator);
				} else {
					pw.print(sd.double_seg(fields[segFieldIndex], pw).trim());
				}

				for (int j = segFieldIndex + 1; j < fieldNum - 1; j++) {
					pw.println(fields[j] + outputSeparator);
				}

				if (segFieldIndex < (fieldNum - 1)) {
					// pw.print(seg.segAnsi(fields[fieldNum-1]));
					pw.print(fields[fieldNum - 1]);
				}
				pw.println();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		

		isr.close();

		br.close();
		pw.close();

		/*
		 * SetDouble sd = new SetDouble(); sd.init_second();
		 * 
		 * String[] first_segs = FileToArray.fileToDimArr("firstdouble.txt");
		 * 
		 * for (int i = 0; i < first_segs.length; i++) { if
		 * (SSO.tioe(first_segs[i])) { continue; } String second_seg =
		 * sd.double_seg(first_segs[i]); pw.println("first_seg:" +
		 * first_segs[i]); pw.println("second_seg:" + second_seg.trim()); }
		 * fw.close(); pw.close();
		 */

	}
}
