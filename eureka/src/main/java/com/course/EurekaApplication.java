package com.course;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.core.env.Environment;

@SpringBootApplication
@EnableEurekaServer
public class EurekaApplication {

	private static final Logger LOG = LoggerFactory.getLogger(EurekaApplication.class);


	/**
	 * eureka有时启动时会报这个错误：
	 * com.sun.jersey.api.client.ClientHandlerException: java.net.ConnectException: Connection refused: connect
	 * 解决办法：执行maven的clean命令，清除缓存，然后再重新启动项目
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(EurekaApplication.class);
		Environment env = app.run(args).getEnvironment();
		LOG.info("启动成功！！");
		LOG.info("Eureka地址: \thttp://127.0.0.1:{}", env.getProperty("server.port"));
	}
}
