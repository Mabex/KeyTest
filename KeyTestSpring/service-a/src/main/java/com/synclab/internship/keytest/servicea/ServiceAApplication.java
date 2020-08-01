package com.synclab.internship.keytest.servicea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

@EnableRetry
@SpringBootApplication
public class ServiceAApplication  {

	public static void main(String[] args) {
		SpringApplication.run(ServiceAApplication.class, args);
	}

	@LoadBalanced
	@Bean
	public RestTemplate loadbalencedRestTemplate() {
		return new RestTemplate();
	}

	@Bean
	public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		return keyPairGenerator.generateKeyPair();
	}
}
