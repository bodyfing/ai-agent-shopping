package com.gooshare.consumer;

import com.gooshare.mapper.SeckillMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class SeckillOrderConsumer implements Runnable {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SeckillMapper seckillMapper;

    @PostConstruct
    public void init(){
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                // 1 读取所有消息
                List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                        Consumer.from("g1", "c1"),
                        StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                        StreamOffset.create("stream:orders", ReadOffset.lastConsumed())
                );
                if (list == null || list.isEmpty()) {
                    continue;
                }
                //2 获取第一条消息
                MapRecord<String, Object, Object> record = list.get(0);

                Map<Object, Object> value = record.getValue();

                String userId = value.get("userId").toString();
                String orderId = value.get("orderId").toString();
                String seckillId = value.get("seckillId").toString();

                //3 数据库存消息
                handlerOrder(userId,seckillId,orderId);

                //4 返回ACK
                stringRedisTemplate.opsForStream().acknowledge(
                        "stream:orders",
                        "g1",
                        record.getId()
                );
            } catch (Exception e) {
                handlePendingList();
                try{
                    Thread.sleep(100);
                }catch (InterruptedException ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    private void handlerOrder(String userId, String seckillId, String orderId) {
        // 1. 判断是否重复下单（数据库兜底）
        int count = seckillMapper.countOrder(userId, seckillId);
        if (count > 0) {
            System.out.println("重复下单");
            return;
        }

        // 2. 扣减库存（乐观锁）
        int success = seckillMapper.updateStock(seckillId);
        if (success == 0) {
            System.out.println("库存不足");
            return;
        }

        // 3. 创建订单
        seckillMapper.addSeckillOrder(userId, orderId, seckillId);
    }

    private void handlePendingList() {
        while (true) {
            try {
                List<MapRecord<String, Object, Object>> list =
                        stringRedisTemplate.opsForStream().read(
                                Consumer.from("g1", "c1"),
                                StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                                StreamOffset.create("stream:orders", ReadOffset.from("0"))
                        );

                if (list == null || list.isEmpty()) {
                    break;
                }

                MapRecord<String, Object, Object> record = list.get(0);
                Map<Object, Object> value = record.getValue();

                String userId = value.get("userId").toString();
                String orderId = value.get("orderId").toString();
                String seckillId = value.get("seckillId").toString();

                seckillMapper.addSeckillOrder(userId, orderId, seckillId);

                stringRedisTemplate.opsForStream().acknowledge(
                        "stream:orders",
                        "g1",
                        record.getId()
                );

            } catch (Exception e) {
                break;
            }
        }
    }
}
