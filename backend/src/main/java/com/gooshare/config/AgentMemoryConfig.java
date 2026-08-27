package com.gooshare.config;

import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import jodd.util.StringUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class AgentMemoryConfig {
    /**
     * 创建 RedisSaver需要的Redisson客户端
     * 复用application.yml中已有的Redis配置
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(
            RedisProperties redisProperties
    ){
        Config config = new Config();

        String address = "redis://"
                + redisProperties.getHost()
                + ":"
                + redisProperties.getPort();
        SingleServerConfig serverConfig =
                config.useSingleServer()
                        .setAddress(address)
                        .setDatabase(redisProperties.getDatabase());

        if(StringUtils.hasText(redisProperties.getUsername())){
            serverConfig.setUsername(redisProperties.getUsername());
        };

        if(StringUtils.hasText(redisProperties.getPassword())){
            serverConfig.setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }

    /**
     * 使用Redis保存Agent对话检查点
     */
    @Bean
    public BaseCheckpointSaver agentCheckpointSaver(
            RedissonClient redissonClient
    ){
        return RedisSaver.builder()
                .redisson(redissonClient)
                .build();
    }
}
