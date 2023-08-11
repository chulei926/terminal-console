package com.leichu.terminal.console.utils;

import com.google.common.collect.Lists;
import com.leichu.terminal.console.common.AppCtxWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.*;

/**
 * 线程池工具类.
 * <p>
 * 所有的自定义异步任务，都可以提交至此线程池。<br>
 * 线程池核心线程数可通过配置文件配置 custom.threadPool.core.count=10。
 * </p>
 *
 * @author chul 2022-08-19.
 */
public class ThreadPoolHelper {

	private static final Logger logger = LoggerFactory.getLogger(ThreadPoolHelper.class);

	private static final int DEFAULT_POOL_QUEUE_SIZE = 1000000;

	private static volatile ThreadPoolHelper instance = null;
	private static ExecutorService threadPool;

	static {
		Runtime runtime = Runtime.getRuntime();
		runtime.addShutdownHook(new Thread(() -> {
			if (null != threadPool) {
				threadPool.shutdown();
			}
			logger.info("Custom thread pool is closed！");
		}));
	}

	private ThreadPoolHelper() {
		final Environment env = AppCtxWrapper.getBean(Environment.class);
		int threadCount = env.getProperty("custom.threadPool.core.count", Integer.class, 50);
		threadPool = Executors.newFixedThreadPool(threadCount);
		logger.info("Custom thread pool initialization succeeded, size:{}", threadCount);
	}

	public static ThreadPoolHelper getInstance() {
		if (null == instance) {
			synchronized (ThreadPoolHelper.class) {
				if (null == instance) {
					instance = new ThreadPoolHelper();
				}
			}
		}
		return instance;
	}

	public void execute(Runnable runnable) {
		if (null == runnable) {
			return;
		}
		threadPool.execute(runnable);
	}

	public void execute(List<Runnable> runnableList) {
		if (CollectionUtils.isEmpty(runnableList)) {
			return;
		}
		for (Runnable runnable : runnableList) {
			threadPool.execute(runnable);
		}
	}

	public <T> List<T> submit(List<Callable<T>> callableList, int seconds) throws InterruptedException, ExecutionException, TimeoutException {
		List<T> results = Lists.newArrayList();
		List<Future<T>> futures = Lists.newArrayList();
		for (Callable<T> call : callableList) {
			Future<T> future = threadPool.submit(call);
			futures.add(future);
		}
		for (Future<T> future : futures) {
			results.add(future.get(seconds, TimeUnit.SECONDS));
		}
		return results;

	}

	public static ThreadPoolTaskExecutor createSimplePool(int coreSize, int maxSize, String threadPrefix) {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(coreSize);
		threadPoolTaskExecutor.setMaxPoolSize(maxSize);
		threadPoolTaskExecutor.setQueueCapacity(DEFAULT_POOL_QUEUE_SIZE);
		threadPoolTaskExecutor.setThreadNamePrefix(threadPrefix);
		threadPoolTaskExecutor.initialize();
		return threadPoolTaskExecutor;
	}


}
