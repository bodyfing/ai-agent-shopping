package com.gooshare.service.impl;

import com.gooshare.common.Result;
import com.gooshare.mapper.ItemMapper;
import com.gooshare.service.CartService;
import com.gooshare.entity.User;
import com.gooshare.utils.UserHolder;
import com.gooshare.vo.CartVO;
import com.gooshare.entity.ItemInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gooshare.common.RedisConstants.ITEM_CART_PREFIX;

@Service
public class CartServiceImpl implements CartService {

    private static final int MAX_CART_ITEM_COUNT = 99;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ItemMapper itemMapper;

    public Result addCart(Integer itemId, Integer count){
        Result basicValidation = validateBasicInput(itemId, count);
        if (basicValidation != null) {
            return basicValidation;
        }

        User currentUser = UserHolder.getUser();
        if (currentUser == null || currentUser.getId() == null) {
            return Result.error("请先登录");
        }

        Long userId = currentUser.getId();
        String key = ITEM_CART_PREFIX + userId;
        String field = itemId.toString();
        Object isExist = stringRedisTemplate.opsForHash().get(key, field);
        int currentCount = isExist == null
                ? 0
                : Integer.parseInt(isExist.toString());

        long targetCount = (long) currentCount + count;
        Result quantityValidation = validateTargetQuantity(itemId, targetCount);
        if (quantityValidation != null) {
            return quantityValidation;
        }

        stringRedisTemplate.opsForHash().put(
                key,
                field,
                String.valueOf(targetCount)
        );

        System.out.println("商品 " + itemId + " 已加入购物车，当前数量: " + targetCount);
        return Result.success();
    }

    public Result showCart(){
        // 1. 获取当前用户ID
        Long userId = UserHolder.getUser().getId();
        String key = ITEM_CART_PREFIX + userId;

        // 2. 从 Redis 一次性拿到所有 Hash 数据
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        if (entries.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 3. 收集所有的 itemId 并转为 Integer
        List<Integer> itemIds = entries.keySet().stream()
                .map(id -> Integer.parseInt(id.toString()))
                .collect(Collectors.toList());

        // 4. 批量查询数据库
        List<ItemInfo> itemInfos = itemMapper.getItemByIds(itemIds);

        // 转为 Map 方便匹配：Map<ID, ItemInfo>
        Map<Integer, ItemInfo> itemMap = itemInfos.stream()
                .collect(Collectors.toMap(ItemInfo::getId, i -> i));

        // 5. 组装 VO
        List<CartVO> cartList = new ArrayList<>();
        entries.forEach((itemIdStr, countStr) -> {
            Integer itemId = Integer.parseInt(itemIdStr.toString());
            Integer count = Integer.parseInt(countStr.toString());

            ItemInfo item = itemMap.get(itemId);
            if (item != null) {
                cartList.add(new CartVO(
                        item.getId(),
                        item.getTitle(),
                        item.getImageURL(),
                        item.getPrice(),
                        count,
                        false
                ));
            }
        });

        return Result.success(cartList);
    }

    public Result updateCart(Integer itemId, Integer count){
        Result basicValidation = validateBasicInput(itemId, count);
        if (basicValidation != null) {
            return basicValidation;
        }

        Result quantityValidation = validateTargetQuantity(itemId, count);
        if (quantityValidation != null) {
            return quantityValidation;
        }

        User currentUser = UserHolder.getUser();
        if (currentUser == null || currentUser.getId() == null) {
            return Result.error("请先登录");
        }

        Long userId = currentUser.getId();
        String key = ITEM_CART_PREFIX + userId;
        String field = itemId.toString();
        stringRedisTemplate.opsForHash().put(key, field, String.valueOf(count));

        System.out.println("商品 " + itemId + " 已更新购物车数量，当前数量: " + count);
        return Result.success();
    }

    public Result deleteCart(Integer itemId){
        Long userId = UserHolder.getUser().getId();
        String key = ITEM_CART_PREFIX + userId;
        String field = itemId.toString();
        stringRedisTemplate.opsForHash().delete(key, field);
        return Result.success();
    }

    private Result validateBasicInput(Integer itemId, Integer count) {
        if (itemId == null || itemId <= 0) {
            return Result.error("商品ID不合法");
        }

        if (count == null || count <= 0) {
            return Result.error("加入购物车数量必须大于0");
        }

        return null;
    }

    private Result validateTargetQuantity(Integer itemId, long targetCount) {
        if (targetCount > MAX_CART_ITEM_COUNT) {
            return Result.error("购物车单件商品数量不得超过" + MAX_CART_ITEM_COUNT);
        }

        ItemInfo item = itemMapper.getItemById(itemId);
        if (item == null) {
            return Result.error("商品不存在或已下架");
        }

        Long stock = item.getStock();
        if (stock == null || stock <= 0) {
            return Result.error("商品库存不足，暂时无法加入购物车");
        }

        if (targetCount > stock) {
            return Result.error("加入购物车数量不能超过当前库存，当前库存为" + stock);
        }

        return null;
    }
}
