package com.yixi.pkg.app;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import redis.clients.jedis.Jedis;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.sun.net.httpserver.HttpServer;

public class AppMain {

	/**
	 * @param args
	 */
	static final String databaseDriver="com.mysql.jdbc.Driver";
	static final String databaseUrl="jdbc:mysql://localhost:10100/?useSSL=false&useUnicode=true&characterEncoding=UTF-8";
	static final String databaseUser="root";
	static final String databasePwd="888888";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Jedis jedis=null;
		jedis=new Jedis("127.0.0.1");
		ComboPooledDataSource cpds=new ComboPooledDataSource();
		try {
			cpds.setDriverClass(databaseDriver);
		} catch (PropertyVetoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		cpds.setJdbcUrl(databaseUrl);
		cpds.setUser(databaseUser);
		cpds.setPassword(databasePwd);
		cpds.setInitialPoolSize(3);
		cpds.setMaxPoolSize(10);
		cpds.setMaxIdleTime(1000);
		cpds.setPreferredTestQuery("SELECT 1");
		InetSocketAddress address=new InetSocketAddress("0.0.0.0",10008);
		HttpServer server=null;
		try {
			server=HttpServer.create(address, 10);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(server==null){
			System.out.println("server is error!");
			cpds.close();
		}
		server.createContext("/", new AppHandler(jedis,cpds));
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
		System.out.println("AppServer is listening on 10008");
	}

}
