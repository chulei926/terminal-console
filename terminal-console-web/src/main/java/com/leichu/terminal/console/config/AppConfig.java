package com.leichu.terminal.console.config;


import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.leichu.terminal.console.common.AppCtxWrapper;
import com.leichu.terminal.console.service.IObserver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.Executors;

@Configuration
public class AppConfig {

	@Bean
	public AppCtxWrapper appCtxWrapper() {
		return new AppCtxWrapper();
	}

	@Bean
	public EventBus eventBus(List<IObserver> observers) {
		EventBus eventBus = new AsyncEventBus(Executors.newFixedThreadPool(3));
		if (CollectionUtils.isEmpty(observers)) {
			return eventBus;
		}
		for (IObserver observer : observers) {
			eventBus.register(observer);
		}
		return eventBus;
	}
}
