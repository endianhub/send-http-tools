package com.xh.web;

import java.io.IOException;
import java.util.Date;

/**
 * <b>Title: </b>
 * <p>Description: </p>
 * 
 * @author H.Yang
 * @email xhaimail@163.com
 * @date 2019年4月22日
 */
public class App {

	public static void main(String[] args) throws IOException {

		String url = "";

		HttpUtils http = new HttpUtils();
		http.addParameter("Name", "ABC");
		http.addParameter("Age", 12);
		http.addParameter("Sex", "男");
		http.addParameter("Date", new Date());

		System.out.println(HttpUtils.getParams());

		// HttpUtils.doPost(url, HttpUtils.TEXT_XML);
	}

}
