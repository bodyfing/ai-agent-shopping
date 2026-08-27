package com.gooshare;

import com.gooshare.service.SeckillService;
import static com.gooshare.common.RedisConstants.*;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class GooShareApplicationTests {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void warmUpSeckillStock(){
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_PREFIX+"1", "1000");
        stringRedisTemplate.opsForValue().set(COUPON_STOCK_PREFIX+"2", "100");
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_PREFIX+"3", "50");
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_PREFIX+"4", "500");
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_PREFIX+"5", "1000");
    }

}
