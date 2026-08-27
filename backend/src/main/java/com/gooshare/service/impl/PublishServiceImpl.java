package com.gooshare.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gooshare.common.Result;
import static com.gooshare.common.RedisConstants.*;

import com.gooshare.dto.UserDTO;
import com.gooshare.mapper.ItemMapper;
import com.gooshare.mapper.PublishMapper;
import com.gooshare.service.PublishService;
import com.gooshare.utils.UserHolder;
import com.gooshare.entity.ItemInfo;
import com.gooshare.entity.Seller;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PublishServiceImpl extends ServiceImpl<PublishMapper, Seller> implements PublishService {

    @Autowired
    private PublishMapper publishMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void publish(ItemInfo itemInfo){

        Long userId = UserHolder.getUser().getId();

        Seller seller = query().eq("user_id", userId).one();

        itemInfo.setSellerId(Long.valueOf(seller.getId()));

        publishMapper.publishItem(itemInfo);

        //获取粉丝信息
        Set<String> members = stringRedisTemplate.opsForSet().members(FANS_PREFIX + userId);

        //推模式 推送到每个粉丝的收件箱
        members.forEach(member -> {
            stringRedisTemplate.opsForZSet().add(INBOX_PREFIX+member.toString(),itemInfo.getId().toString(),System.currentTimeMillis());
        });

    }
}
