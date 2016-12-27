package com.chanapp.chanjet.customer.test;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.chanjet.csp.platform.test.HttpResponse;

public class GZQ extends RestletBaseTest{
	static String PAYLOAD = FileReader.read("importData/ImportData.json");
	@Test
	public void testGZQImport() throws Exception{	
		HttpResponse response =  this.doPost("appManager","/restlet/gzq/ImportData", PAYLOAD);
		assertEquals(200, response.getStatusCode());
	}
}
