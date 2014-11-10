package org.jmlp.web;

public class ConfigureFactoryInstantiate {

	public static ConfigureFactory getConfigureFactory() {
		
		return new EasyConfigureFactory();
	}

}
