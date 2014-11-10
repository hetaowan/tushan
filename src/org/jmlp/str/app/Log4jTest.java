package org.jmlp.str.app;

import org.apache.log4j.Logger;

public class Log4jTest {

	private static Logger logger = Logger.getLogger(Log4jTest.class);
	 
	public static void main(String[] args)
	{
		
		logger.error("this is a error!");
		logger.warn("this is a warn.");
		logger.info("this is a info");
		logger.debug("this is a debug");
	    logger.fatal("this is a fatal");
		
		
	}
}
