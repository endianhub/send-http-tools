package com.xh.web;

import java.io.IOException;
import java.net.Proxy;
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

		HttpTools http = new HttpTools();
		http.addParameter("Name", "ABC");
		http.addParameter("Age", 12);
		http.addParameter("Sex", "男");
		http.addParameter("Date", new Date());
		http.addParameter("A", "");
		http.addParameter("B", "");
		http.addParameter("C", "");
		System.out.println(HttpTools.getParams());

		HttpTools.doPost(url, HttpTools.getParams(), HttpTools.TEXT_HTML, new IProxy() {
			@Override
			public Proxy settingProxy() {

				return null;
			}
		});

	}

}
