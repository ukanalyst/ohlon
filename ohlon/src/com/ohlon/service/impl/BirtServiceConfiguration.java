package com.ohlon.service.impl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ohlon.service.BirtService;

@Configuration
public class BirtServiceConfiguration {

	@Bean
	public BirtService birtService() {
		return new BirtServiceImpl();

	}
}