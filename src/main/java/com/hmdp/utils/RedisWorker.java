package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class RedisWorker {
    private static final long BEGIN_TIMESTAMP=1735689600L;
    private StringRedisTemplate stringRedisTemplate;
    private final int COUNT_BITS=32;

    public RedisWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public long nextId(String keyPrefix){
        //时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        //生成序列号
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));

        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);




        return timestamp<<COUNT_BITS|count;
    }


}
