package com.gooshare.service;

import com.gooshare.common.Result;
import com.gooshare.mapper.ItemMapper;
import com.gooshare.service.impl.CartServiceImpl;
import com.gooshare.entity.User;
import com.gooshare.utils.UserHolder;
import com.gooshare.entity.ItemInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(100L);
        UserHolder.saveUser(user);
        lenient().when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @AfterEach
    void tearDown() {
        UserHolder.removeUser();
    }

    @Test
    void shouldRejectZeroCountWhenAddingForFirstTime() {
        Result result = cartService.addCart(1, 0);

        assertEquals(0, result.getCode());
        assertEquals("加入购物车数量必须大于0", result.getMsg());
        verify(hashOperations, never()).put(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void shouldRejectItemWithZeroStock() {
        when(hashOperations.get("item:cart:100", "1")).thenReturn(null);
        when(itemMapper.getItemById(1)).thenReturn(itemWithStock(0L));

        Result result = cartService.addCart(1, 1);

        assertEquals(0, result.getCode());
        assertEquals("商品库存不足，暂时无法加入购物车", result.getMsg());
        verify(hashOperations, never()).put(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void shouldRejectAccumulatedCountGreaterThanStock() {
        when(hashOperations.get("item:cart:100", "1")).thenReturn("2");
        when(itemMapper.getItemById(1)).thenReturn(itemWithStock(3L));

        Result result = cartService.addCart(1, 2);

        assertEquals(0, result.getCode());
        assertEquals("加入购物车数量不能超过当前库存，当前库存为3", result.getMsg());
        verify(hashOperations, never()).put(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void shouldAddPositiveCountWithinStock() {
        when(hashOperations.get("item:cart:100", "1")).thenReturn("2");
        when(itemMapper.getItemById(1)).thenReturn(itemWithStock(5L));

        Result result = cartService.addCart(1, 2);

        assertEquals(200, result.getCode());
        verify(hashOperations).put("item:cart:100", "1", "4");
    }

    @Test
    void shouldRejectZeroCountWhenUpdatingCart() {
        Result result = cartService.updateCart(1, 0);

        assertEquals(0, result.getCode());
        assertEquals("加入购物车数量必须大于0", result.getMsg());
        verify(hashOperations, never()).put(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    private ItemInfo itemWithStock(Long stock) {
        ItemInfo item = new ItemInfo();
        item.setId(1);
        item.setStock(stock);
        return item;
    }
}
