package com.chanapp.chanjet.customer.test;

import java.io.File;

public class Test {

	public static void main(String[] args) {
		String cspappPath="E:\\chanjet3.3\\chanjet_customer\\customer-service\\config\\cspapp.properties";
		 File f = new File(cspappPath);
		    System.out.println("CCS Property File: " + cspappPath + ", and this file is readable?"
		        + (f.exists() && f.canRead() ? "true" : "false"));


	}

}
