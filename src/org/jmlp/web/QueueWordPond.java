package org.jmlp.web;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jmlp.str.basic.DS2STR;
import org.jmlp.str.basic.SSO;

public class QueueWordPond extends WordPond {

	private static int count=0;
	
	private static int czero=0;
	
	private static Queue<String> queue = new ConcurrentLinkedQueue<String>();
	
	private static ArrayList<Thread> startedThreads=new ArrayList<Thread>();
	
	private static long sleepTime=1;
	
	private static int proxy=0;

	@Override
	public void add2Pond(String word) {

		if (isValidWord(word)) {
			return;
		}
		// System.out.println("record:"+record);
		queue.offer(word);

	}
	synchronized  private  void incrCount()
	{
		count++;
	}
	
	synchronized  private  void incrCzero()
	{
		czero++;
	}

	synchronized  private  void resetCzero()
	{
		czero=0;
	}
	
	
	
	@Override
	public String pollFromPond() {
		String nextElement = "";

		// System.out.println("queue.size:"+queue.size());
		nextElement = queue.poll();
		return nextElement;
	}

	@Override
	public void startConsume(int threadNum) {
		for (int i = 0; i < threadNum; i++) {
			WordResolve fr = new WordResolve();
			Thread consumeThread = new Thread(fr);
			startedThreads.add(consumeThread);
			consumeThread.setDaemon(true);
			consumeThread.start();
		}

	}

	public void restartConsume(int threadNum) {
		
		for(int i=0;i<startedThreads.size();i++)
		{
			startedThreads.get(i).stop();
		}
		
		startedThreads=new ArrayList<Thread>();
		startConsume(threadNum);

	}
	
	public boolean isValidWord(String word) {
		return false;
	}

	public static int getCount() {
		return count;
	}

	public static void setCount(int count) {
		QueueWordPond.count = count;
	}

	public static int getCzero() {
		return czero;
	}
	public static void setCzero(int czero) {
		QueueWordPond.czero = czero;
	}

	public static ArrayList<Thread> getStartedThreads() {
		return startedThreads;
	}
	public static void setStartedThreads(ArrayList<Thread> startedThreads) {
		QueueWordPond.startedThreads = startedThreads;
	}

	public static long getSleepTime() {
		return sleepTime;
	}
	public static void setSleepTime(long sleepTime) {
		QueueWordPond.sleepTime = sleepTime;
	}

	public static int getProxy() {
		return proxy;
	}
	public static void setProxy(int proxy) {
		QueueWordPond.proxy = proxy;
	}

	private class WordResolve implements Runnable {

		private ConfigureFactory confFactory;

		private WordAnalytic wa;

		private PrintWriter parsedWordWriter;

		public void init() {

			confFactory = ConfigureFactoryInstantiate.getConfigureFactory();

			String waOutDir = confFactory.getWordAnalyticOutputDir();
			File tempDir = new File(waOutDir);
			if (!(tempDir.exists())) {
				tempDir.mkdirs();
			}

			wa = confFactory.getWordAnalytic();
			wa.setProxy(proxy);
            wa.init();
            
			Thread current = Thread.currentThread();

			String presentThreadWordFile = waOutDir + "/wordAnalytic_"
					+ current.getName().replaceAll("Thread\\-", "") + ".log";

			File tempFile = new File(presentThreadWordFile);
			try {
				// 关闭上一个打开的parsedRecordWriter
				if (parsedWordWriter != null) {
					parsedWordWriter.close();
				}
				// 打开新一天的parsedRecordWriter
				parsedWordWriter = new PrintWriter(new FileWriter(tempFile,
						true));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			init();
			parseWord();

		}

		public void parseWord() {

			while (true) {
				
				try {
					//System.out.println("sleeptime is:"+getSleepTime());
					//Thread.sleep(getSleepTime());
					
					String word = pollFromPond();
					incrCount();
					//System.out.println("fetch word:"+word);
					if (SSO.tioe(word)) {
						Thread.sleep((long) (10 * Math.random()));
						continue;
					}

					WordResult wr = wa.parse(word);
					if (wr == null) {
						continue;
					}

					WordResultEN wren=(WordResultEN)wr;
					if(wren.getEn()==0)
					{
						incrCzero();
					}
					else
					{
						resetCzero();
					}
					parsedWordWriter.println(word+"\001"+wr.toString()+"\001"+DS2STR.arraylist2str(wa.getRevWords()));
					parsedWordWriter.flush();
					
				} catch (Exception e) {
					incrCzero();
					e.printStackTrace();
				}
			}
		}

		public WordAnalytic getWa() {
			return wa;
		}

		public void setWa(WordAnalytic wa) {
			this.wa = wa;
		}

	}

}
