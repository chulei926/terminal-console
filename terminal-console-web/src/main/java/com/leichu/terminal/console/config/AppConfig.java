package com.leichu.terminal.console.config;


import com.leichu.terminal.console.common.AppCtxWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@Bean
	public AppCtxWrapper appCtxWrapper() {
		return new AppCtxWrapper();
	}

}
