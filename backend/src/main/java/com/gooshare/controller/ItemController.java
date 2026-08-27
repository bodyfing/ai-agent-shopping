package com.gooshare.controller;

import com.gooshare.common.Result;
import com.gooshare.dto.CategoryDTO;
import com.gooshare.service.ItemService;
import com.gooshare.vo.CategoryVO;
import com.gooshare.entity.ItemInfo;
import com.gooshare.vo.ItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品管理", description = "提供搜索、分类及交互功能")
@RequestMapping("/item")
@RestController
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Operation(summary = "关键词快搜")
    @GetMapping("/search")
    public Result searchSimple(@RequestParam("keyword") String keyword) {
        return itemService.search(keyword);
    }

    @Operation(summary = "获取商品列表")
    @PostMapping("/list")
    public Result getItemList() {
        List<ItemInfo> list = itemService.getItem();
        return Result.success(list);
    }

    @Operation(summary = "获取全部分类树")
    @GetMapping("/category")
    public Result getCategory() {
        List<CategoryVO> list = itemService.getCategory();
        return Result.success(list);
    }

    @Operation(summary = "按分类筛选商品")
    @PostMapping("/filter")
    public Result getItemByCategory(@RequestBody CategoryDTO filterDTO) {
        List<ItemVO> list = itemService.findItemByCategory(filterDTO);
        return Result.success(list);
    }

    @Operation(summary = "查询商品详情")
    @GetMapping("/{id}")
    public Result getItemDetail(@PathVariable Integer id) {
        ItemInfo info = itemService.getItem(id);
        return Result.success(info);
    }

    @Operation(summary = "点赞商品")
    @PostMapping("/like/{itemId}")
    public Result like(@PathVariable Integer itemId) {
        return itemService.like(itemId);
    }

    @Operation(summary = "收藏商品")
    @PostMapping("/collect/{itemId}")
    public Result collect(@PathVariable Integer itemId) {
        return itemService.collect(itemId);
    }

    @Operation(summary = "记录浏览量")
    @PostMapping("/browser/{itemId}")
    public Result browser(@PathVariable Integer itemId) {
        return itemService.browser(itemId);
    }

    @Operation(summary = "获取热度排行榜")
    @GetMapping("/ranking")
    public Result getRanking() {
        return itemService.getRanking();
    }
}