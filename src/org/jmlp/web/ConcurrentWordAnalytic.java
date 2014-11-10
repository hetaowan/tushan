package org.jmlp.web;

import java.util.ArrayList;

import org.jmlp.file.utils.FileToArray;

/**
 * 向queuePond里写数据，并启动线程处理
 * 
 * @author lq
 * 
 */
public class ConcurrentWordAnalytic {

	private QueueWordPond queuePond = new QueueWordPond();

	private int inputNum = 0;
	
	private int sleepNum=1;

	public void startPond(int threadNum) {
		queuePond.startConsume(threadNum);
	}

	public void startAnalyticFromFile(String inputFile, int threadNum,int proxy) {

		try {
			String[] inputs = FileToArray.fileToDimArr(inputFile);
			inputNum = inputs.length;
			System.err.println(inputs.length + " input words");
			for (int i = 0; i < inputs.length; i++) {
				queuePond.add2Pond(inputs[i]);
			}
			queuePond.setProxy(proxy);
			startPond(threadNum);
			while (queuePond.getCount() < inputNum) {
				
				//if(sleepNum%1200==0)
				//{
					//queuePond.restartConsume(threadNum);
			      //  queuePond.setSleepTime(300000);
			      //  Thread.sleep(250000);
			      //  queuePond.setSleepTime(1);
					//ArrayList<Thread> startedThreads=queuePond.getStartedThreads();
					//for(int j=0;j<startedThreads.size();j++)
					//{
					//	startedThreads.get(j).sleep(300000);
					//}
					
					//Thread.sleep(300000);
				//}
				
				
				System.out.println("-----count:"+queuePond.getCount()+"---czero:"+queuePond.getCzero()+"----sleepNum"+sleepNum+"-----");
				if(queuePond.getCzero()>50)
				{
					System.exit(1);
				}
				sleepNum++;
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		
		if(args.length!=3)
		{
			System.err.println("Usage:<input> <thread_num> <proxy>");
			System.exit(1);
		}
		
		
		ConcurrentWordAnalytic cwa = new ConcurrentWordAnalytic();

		cwa.startAnalyticFromFile(args[0], Integer.parseInt(args[1]),Integer.parseInt(args[2]));

	}

}
