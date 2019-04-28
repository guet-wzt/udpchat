package com.network.udpchat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.network.udpchat.common.dao")
public class UdpchatApplication{

	public static void main(String[] args) {
		SpringApplication.run(UdpchatApplication.class, args);
	}

}
