package com.hmdp;

import com.hmdp.utils.RedisWorker;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class HmDianPingApplicationTests {
    private RedisWorker redisWorker;
    private ExecutorService es= Executors.newFixedThreadPool(500);
    @Test
    public void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);

        Runnable task=(Runnable)()->{
            for(int i=0;i<100;i++){
                long id = redisWorker.nextId("order");
                System.out.println(id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();

        for(int i=0;i<300;i++){
            es.submit(task);
        }
        latch.await();

        long end = System.currentTimeMillis();
        System.out.printf("time="+(end-begin)+"ms");


    }

}
