package com.gooshare.service.impl;

import com.gooshare.dto.IntentResult;
import com.gooshare.common.Result;
import com.gooshare.dto.ItemSearchRequest;
import static com.gooshare.common.RedisConstants.*;
import com.gooshare.dto.CategoryDTO;
import com.gooshare.entity.ItemInfo;
import com.gooshare.mapper.SellerMapper;
import com.gooshare.mapper.UserMapper;
import com.gooshare.service.ItemService;
import com.gooshare.utils.UserHolder;
import com.gooshare.vo.*;
import com.gooshare.mapper.ItemMapper;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SellerMapper sellerMapper;

    @Override
    public List<ItemInfo> getItem() {
        return itemMapper.getItem();
    }

    @Override
    public List<CategoryVO> getCategory() {
        return itemMapper.getCategory();
    }

    @Override
    public List<ItemVO> findItemByCategory(CategoryDTO filterDTO) {
        String groupTitle = filterDTO.getGroupTitle();
        Long selectedItem = filterDTO.getSelectedItem();
        System.out.println("   ------------           ------" + groupTitle + selectedItem);
        return itemMapper.filterItem(groupTitle, selectedItem);
    }

    @Override
    public ItemInfo getItem(Integer id) {
        // 1. 先查出基本信息
        ItemInfo info = itemMapper.getItemById(id);
        if (info == null) {
            return null;
        }

        // 2. 安全地获取用户 ID
        Long userId = null;
        // 先判断 UserHolder 本身是不是空，再拿 User，再拿 ID
        if (UserHolder.getUser() != null) {
            userId = UserHolder.getUser().getId();
        }

        // 3. 判断逻辑
        if (userId == null) {
            // 如果没登录，直接设置点赞为 false
            info.setLiked(false);
            info.setCollected(false);
        } else {
            // 如果登录了，去数据库查一下
            Boolean liked = stringRedisTemplate.opsForSet().isMember(
                    ITEM_LIKED_PREFIX + id, userId.toString()
            );

            Boolean collected = stringRedisTemplate.opsForSet().isMember(
                    ITEM_COLLECTED_PREFIX + id, userId.toString()
            );

            Boolean isFollowed = stringRedisTemplate.opsForSet().isMember(
                    FANS_PREFIX + sellerMapper.selectUserId(Long.valueOf(info.getSellerId())), userId.toString());

            if (liked) {
                info.setLiked(true);
            } else {
                info.setLiked(false);
            }

            if (collected) {
                info.setCollected(true);
            } else {
                info.setCollected(false);
            }

            if (isFollowed) {
                info.setIsFollowed(true);
            } else {
                info.setIsFollowed(false);
            }
            return info;
        }

        return info;
    }

    @Override
    public Result like(Integer itemId) {
        Long userId = UserHolder.getUser().getId();
        LocalDateTime now = LocalDateTime.now();
        String key = ITEM_LIKED_PREFIX + itemId;

        Boolean isLiked = stringRedisTemplate.opsForSet().isMember(key, userId.toString());

        if (BooleanUtils.isTrue(isLiked)) {
            stringRedisTemplate.opsForSet().remove(ITEM_LIKED_PREFIX + itemId, userId.toString());
            itemMapper.subLikeCount(itemId);
            return Result.success("取消点赞");
        }

        itemMapper.addCountLike(itemId);
        stringRedisTemplate.opsForSet().add(key, userId.toString());
        return Result.success("点赞成功");
    }

    @Override
    public Result collect(Integer itemId) {
        Long userId = UserHolder.getUser().getId();
        LocalDateTime now = LocalDateTime.now();
        String key = ITEM_COLLECTED_PREFIX + itemId;

        Boolean isLiked = stringRedisTemplate.opsForSet().isMember(key, userId.toString());

        if (BooleanUtils.isTrue(isLiked)) {
            stringRedisTemplate.opsForSet().remove(ITEM_COLLECTED_PREFIX + itemId, userId.toString());
            itemMapper.subCountCollect(itemId);
            return Result.success("取消收藏");
        }

        itemMapper.addCountCollect(itemId);
        stringRedisTemplate.opsForSet().add(key, userId.toString());
        return Result.success("收藏成功");
    }

    @Override
    public Result browser(Integer itemId) {
        Long userId = UserHolder.getUser().getId();
        // 1. 定义冷却锁的 Key (商品ID + 用户ID)
        String lockKey = ITEM_BROWSER_LOCK_PREFIX + itemId + ":" + userId;

        // 2. 尝试设置锁，有效期 1 小时 (SETNX)
        // 如果 1 小时内该用户访问过，这里会返回 false
        Boolean canAdd = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", Duration.ofHours(1));

        if (BooleanUtils.isTrue(canAdd)) {
            // 3. 只有拿到锁，才增加总浏览量计数
            String countKey = ITEM_BROWSER_LOCK_PREFIX + itemId;
            stringRedisTemplate.opsForValue().increment(countKey);

            // 4. (可选) 同步给数据库，或者等定时任务统一同步
            itemMapper.addBrowserCount(itemId);
            return Result.success("浏览量+1");
        }
        return Result.success("还在冷却期，不增加计数");
    }

    @Override
    public Result getRanking() {
        List<ItemInfo> items = itemMapper.getItem();
        List<RankingVO> rankingList = new ArrayList<>();
        System.out.println(items + "???????????????????????");

        items.forEach(item -> {
            long like = item.getLikeCount() != null ? item.getLikeCount() : 0;
            long collect = item.getCollectCount() != null ? item.getCollectCount() : 0;
            long browser = item.getBrowserCount() != null ? item.getBrowserCount() : 0;
            double score = (double) (like * 10 + collect * 20 + browser * 1);
            System.out.println(score);
            stringRedisTemplate.opsForZSet().add(ITEM_SCORE_PREFIX, item.getId().toString(), score);
        });

        Set<String> rankingSet = stringRedisTemplate.opsForZSet().reverseRange(ITEM_SCORE_PREFIX, 0, 5);
        AtomicInteger index = new AtomicInteger();

        rankingSet.forEach(set -> {
            int i = index.getAndIncrement();
            ItemInfo item = itemMapper.getItemById(Integer.parseInt(set));
            rankingList.add(new RankingVO(item.getId(), item.getTitle(), i + 1));
        });

        return Result.success(rankingList);
    }

    @Override
    public Result search(String keyword) {
        List<ItemInfo> items = itemMapper.searchByTitle(keyword);
        return Result.success(items);
    }

    @Override
    public List<ItemInfo> search(ItemSearchRequest request){

        if(request == null){
            throw new IllegalArgumentException(
                    "商品搜索参数不能为空"
            );
        }

        return itemMapper.searchItems(
                request.getKeyword(),
                request.getBrandId(),
                request.getCategoryId(),
                request.getCategoryItemId(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.safeLimit(),
                request.safeSortType().name()
        );
    }
}
