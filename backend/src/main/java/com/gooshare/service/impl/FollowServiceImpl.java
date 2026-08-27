package com.gooshare.service.impl;

import com.gooshare.common.Result;
import static com.gooshare.common.RedisConstants.*;

import com.gooshare.mapper.SellerMapper;
import com.gooshare.mapper.UserMapper;
import com.gooshare.service.FollowService;
import com.gooshare.utils.UserHolder;
import com.gooshare.entity.Seller;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FollowServiceImpl implements FollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SellerMapper  sellerMapper;
    @Autowired
    private UserMapper userMapper;

    public Result follow(Long followId, Boolean isFollowed, Long type){

        //1、获取登录的用户id
        Long currentUserId = UserHolder.getUser().getId();
        Long finalTargetUserId;

        // 判断传过来的是什么 ID
        if (type == 1) { // 1 代表传的是 sellerId (详情页顶部)
            Long userId = sellerMapper.selectUserId(followId);
            finalTargetUserId = userId;
        } else { // 2 代表传的是 userId (留言区)
            finalTargetUserId = followId;
        }

        if(currentUserId.equals(finalTargetUserId)){
            return Result.error("不能关注自己！");
        }


        //2、创建redis的set集合
        if(isFollowed){
            stringRedisTemplate.opsForSet().remove(FOLLOW_PREFIX+currentUserId,String.valueOf(finalTargetUserId));
            stringRedisTemplate.opsForSet().remove(FANS_PREFIX+finalTargetUserId, String.valueOf(currentUserId));
            return Result.success("取消关注成功");
        }

        stringRedisTemplate.opsForSet().add(FOLLOW_PREFIX+currentUserId, String.valueOf(finalTargetUserId));
        stringRedisTemplate.opsForSet().add(FANS_PREFIX+finalTargetUserId, String.valueOf(currentUserId));

        return Result.success();
    }

    @Override
    public Result getCommonFollowers(Long sellerId){
        Long currentUserId = UserHolder.getUser().getId();
        Long sellerUserId = sellerMapper.selectUserId(sellerId);
        Set<String> common = stringRedisTemplate.opsForSet().intersect(FANS_PREFIX + sellerUserId, FOLLOW_PREFIX + currentUserId);
        List<String> commonFollowers = new ArrayList<>();
        if(common.isEmpty()){
            return Result.success("");
        }
        common.forEach((item) -> {
            commonFollowers.add(userMapper.getUserById(Integer.parseInt(item)));
        });
        System.out.println("sellerId:"+sellerId);
        return Result.success(commonFollowers);
    }
}
