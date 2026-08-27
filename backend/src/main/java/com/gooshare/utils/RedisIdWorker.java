package com.gooshare.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {

    // 起始时间戳（可以随便定一个过去的时间）
    private static final long BEGIN_TIMESTAMP = 1704067200L; // 2024-01-01 00:00:00

    // 序列号位数
    private static final int COUNT_BITS = 32;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public long nextId(String keyPrefix) {
        // 1. 当前时间（秒）
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);

        // 2. 时间差
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        // 3. 生成序列号（每天递增）
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));

        Long count = stringRedisTemplate.opsForValue().increment(
                "icr:" + keyPrefix + ":" + date
        );

        // 4. 拼接（时间戳 << 32 | 序列号）
        return (timestamp << COUNT_BITS) | count;
    }
}