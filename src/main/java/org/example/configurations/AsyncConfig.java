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

    @Bean(name = "taskExecutor")
    @Primary
    public Executor taskExecutor() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        int processors = Runtime.getRuntime().availableProcessors();
        int coreSize = Math.max(2, processors - 2);

        long freeRamBytes = osBean.getFreeMemorySize();
        double freeRamGb = freeRamBytes / (1024.0 * 1024.0 * 1024.0);

        double systemReserveGb = 1.0;
        double usableRamGb = Math.max(0.5, freeRamGb - systemReserveGb);

        int dynamicQueue = (int) Math.max(50, usableRamGb * 25);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(processors);
        executor.setQueueCapacity(dynamicQueue);
        executor.setThreadNamePrefix("DocProc-");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
