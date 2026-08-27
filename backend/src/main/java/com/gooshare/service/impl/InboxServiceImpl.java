package com.gooshare.service.impl;

import static com.gooshare.common.RedisConstants.*;

import cn.hutool.core.util.StrUtil;
import com.gooshare.common.Result;
import com.gooshare.mapper.InboxMapper;
import com.gooshare.mapper.ItemMapper;
import com.gooshare.service.InboxService;
import com.gooshare.utils.UserHolder;
import com.gooshare.entity.ItemInfo;
import com.gooshare.vo.ScrollResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class InboxServiceImpl implements InboxService {

    @Autowired
    private InboxMapper inboxMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ItemMapper itemMapper;

    @Override
    public Result getInbox(Long max, Integer offset){
        Long userId = UserHolder.getUser().getId();

        String key = INBOX_PREFIX + userId;

        Set<ZSetOperations.TypedTuple<String>> msg = stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, 0, max, offset, 5);

        //判断消息是否为空
        if(msg == null || msg.isEmpty()){
            return Result.success();
        }

        List<Long> ids = new ArrayList<>();

        long minTime = 0;
        int os = 1;
        for(ZSetOperations.TypedTuple<String> t: msg){
            //1、获取id
            ids.add(Long.valueOf(t.getValue()));
            //2、获取分数(用作计算偏移量）
            long time = t.getScore().longValue();
            if(time == minTime) {
                os++;
            }else{
                minTime = time;
                os = 1;
            }

        }
        //滚动分页完成之后，根据id获取所需要的items
        List<ItemInfo> items = itemMapper.getItemsByIds(ids);

        ScrollResult scrollResult = new ScrollResult();
        scrollResult.setList(items);
        scrollResult.setMinTime(minTime);
        scrollResult.setOffset(os);


        return Result.success(scrollResult);
    }
}
