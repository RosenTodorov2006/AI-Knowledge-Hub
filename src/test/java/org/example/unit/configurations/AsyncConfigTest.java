package org.example.unit.configurations;

import org.example.configurations.AsyncConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import static org.junit.jupiter.api.Assertions.*;
public class AsyncConfigTest {
    private AsyncConfig asyncConfig;
    @BeforeEach
    void setUp() {
        asyncConfig = new AsyncConfig();
    }
    @Test
    void testTaskExecutorBeanCreation() {
        Executor executor = asyncConfig.taskExecutor();

        assertTrue(executor instanceof ThreadPoolTaskExecutor);
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;

        assertEquals(AsyncConfig.THREAD_NAME_PREFIX, taskExecutor.getThreadNamePrefix());
        assertEquals(
                ThreadPoolExecutor.CallerRunsPolicy.class,
                taskExecutor.getThreadPoolExecutor().getRejectedExecutionHandler().getClass()
        );

        assertNotNull(taskExecutor.getThreadPoolExecutor());
    }

    @Test
    void testCalculateCorePoolSizeLogic() {
        int resultLow = ReflectionTestUtils.invokeMethod(asyncConfig, "calculateCorePoolSize", 2);
        assertEquals(AsyncConfig.MIN_CORE_POOL_SIZE, resultLow);

        int resultHigh = ReflectionTestUtils.invokeMethod(asyncConfig, "calculateCorePoolSize", 8);
        assertEquals(6, resultHigh);
    }

    @Test
    void testCalculateUsableRamLogic() {
        double resultLow = ReflectionTestUtils.invokeMethod(asyncConfig, "calculateUsableRam", 0L);
        assertEquals(AsyncConfig.MIN_USABLE_RAM_GB, resultLow);

        long fourGbInBytes = (long) (4 * AsyncConfig.BYTES_IN_GB);
        double resultHigh = ReflectionTestUtils.invokeMethod(asyncConfig, "calculateUsableRam", fourGbInBytes);
        assertEquals(3.0, resultHigh, 0.001);
    }
    @Test
    void testCalculateQueueCapacityLogic() {
        int resultLow = ReflectionTestUtils.invokeMethod(asyncConfig, "calculateQueueCapacity", 0.1);
        assertEquals(AsyncConfig.MIN_QUEUE_CAPACITY, resultLow);

        int resultHigh = ReflectionTestUtils.invokeMethod(asyncConfig, "calculateQueueCapacity", 10.0);
        assertEquals(250, resultHigh);
    }
}
