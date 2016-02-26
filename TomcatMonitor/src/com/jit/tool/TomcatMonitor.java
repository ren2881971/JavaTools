package com.jit.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/*
 *	@author junming_ren	
 *	@version 2016-2-26 上午9:42:54
 */
public class TomcatMonitor {
	private static String tomcatPath;
	private static String ip;
	private static int delayTime;
	private static int periodTime;
	private static final String FILE_PATH = "tomcatPath.properties";
	private static final String TOMCAT_PATH="tomcatPath";
	private static final String ADDRESS="address";
	private static final String DELAYTIME="delayTime";
	private static final String PERIODTIME="periodTime";
	public static void main(String[] args) {
		TomcatMonitor mo = new TomcatMonitor();
		mo.startUp();
	}

	public static Properties getFileInfo() {
		Properties p = new Properties();
		InputStream in = null;
		in = TomcatMonitor.class.getResourceAsStream(FILE_PATH);
		try {
			p.load(in);
			in.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return p;
	}

	public TomcatMonitor() {
		Properties p = TomcatMonitor.getFileInfo();
		this.tomcatPath = p.getProperty(TOMCAT_PATH);
		this.ip = p.getProperty(ADDRESS);
		this.delayTime = Integer.valueOf( p.getProperty(DELAYTIME));
		this.periodTime = Integer.valueOf(p.getProperty(PERIODTIME));
	}

	public synchronized URL isAvailableURL(String address) {
		if (address == null || address.length() <= 0) {
			return null;
		}
		URL url = null;
		for (int i = 1; i < 4; i++) {
			try {
				url = new URL(address);
				HttpURLConnection con = (HttpURLConnection) url
						.openConnection();
				int statusCode = con.getResponseCode();
				System.out.println("第" + i + "次连接 statusCode=" + statusCode+" "+new Date());
				if (statusCode == 200) {
					System.out.println("第" + i + "次连接可用 "+new Date());
					break;
				}
			} catch (Exception e) {
				url = null;
				System.out.println("第" + i + "次连接 url连接不可用 "+new Date());
				continue;
			}
		}
		return url;
	}

	public boolean webIsAvailable(String address) throws IOException {
		boolean flag = false;
		URL url = isAvailableURL(address);
		if (null != url) {
			InputStream stream = url.openStream();
			BufferedReader buffer = new BufferedReader(new InputStreamReader(
					stream));
			String temp;
			while ((temp = buffer.readLine()) != null) {
				if (temp.length() > 0) {
					flag = true;
					break;
				}
			}
			stream.close();
		}
		return flag;
	}

	public void startUp() {
		Timer timer = new Timer();
		timer.schedule(new MyTimerTask(), this.delayTime, this.periodTime);
	}

	class MyTimerTask extends TimerTask {

		@Override
		public void run() {
			TomcatMonitor mo = new TomcatMonitor();
			try {
				if (!mo.webIsAvailable(ip)) {
					System.out.println("tomcat 没有启动");
					Process p = Runtime.getRuntime()
							.exec("cmd /c start " + tomcatPath
									+ "\\bin\\startup.bat", null,
									new File(tomcatPath));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
