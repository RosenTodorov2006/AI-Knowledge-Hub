package org.example.configurations;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
    public static final String EXECUTOR_BEAN_NAME = "taskExecutor";
    public static final String THREAD_NAME_PREFIX = "DocProc-";
    public static final int MIN_CORE_POOL_SIZE = 2;
    public static final int CPU_RESERVE_CORES = 2;
    public static final double BYTES_IN_GB = 1024.0 * 1024.0 * 1024.0;
    public static final double SYSTEM_RESERVE_RAM_GB = 1.0;
    public static final double MIN_USABLE_RAM_GB = 0.5;
    public static final int MIN_QUEUE_CAPACITY = 50;
    public static final int QUEUE_PER_GB_MULTIPLIER = 25;

    @Bean(name = EXECUTOR_BEAN_NAME)
    @Primary
    public Executor taskExecutor() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        int processors = Runtime.getRuntime().availableProcessors();

        int coreSize = calculateCorePoolSize(processors);
        double usableRam = calculateUsableRam(osBean.getFreeMemorySize());
        int queueCapacity = calculateQueueCapacity(usableRam);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(processors);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }

    private int calculateCorePoolSize(int processors) {
        return Math.max(MIN_CORE_POOL_SIZE, processors - CPU_RESERVE_CORES);
    }

    private double calculateUsableRam(long freeRamBytes) {
        double freeRamGb = freeRamBytes / BYTES_IN_GB;
        return Math.max(MIN_USABLE_RAM_GB, freeRamGb - SYSTEM_RESERVE_RAM_GB);
    }

    private int calculateQueueCapacity(double usableRamGb) {
        return (int) Math.max(MIN_QUEUE_CAPACITY, usableRamGb * QUEUE_PER_GB_MULTIPLIER);
    }
}
