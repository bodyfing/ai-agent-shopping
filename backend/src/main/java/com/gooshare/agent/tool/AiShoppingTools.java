package com.gooshare.agent.tool;


import com.gooshare.agent.dto.ItemSearchToolRequest;
import com.gooshare.dto.ItemSearchRequest;
import com.gooshare.mapper.BrandMapper;
import com.gooshare.mapper.CategoryItemMapper;
import com.gooshare.mapper.CategoryMapper;
import com.gooshare.service.ItemService;
import com.gooshare.vo.CategoryVO;
import com.gooshare.entity.ItemInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiShoppingTools {

    private final ItemService itemService;

    private final BrandMapper brandMapper;

    private final CategoryMapper categoryMapper;

    private final CategoryItemMapper categoryItemMapper;

    /**
     * 商品搜索工具。
     *
     * 大模型负责提取名称和价格，
     * Java负责吧名称转换成数据库ID并执行真实查询
     */
    @Tool(description = """
            根据关键词、品牌、商品分类和价格范围搜索校园二手商品。
            
            当用户想查找、购买、筛选或比较商品时调用。
            
            示例：
            - 帮我找2000元以内的苹果手机
            - 有没有500~1000元的自行车
            - 找一些二手考研教材
            
            商品、价格和库存必须来自本工具的返回结果，不得编造。
            """)
    public List<ItemInfo> searchItems(
            @ToolParam(description = "商品搜索条件")
            ItemSearchToolRequest input
    ){
        validateSearchInput(input);

        //定义
        String keyword = normalize(input.getKeyword());
        String brand = normalize(input.getBrand());
        String category = normalizeCategory(input.getCategory());
        String categoryItem = normalize(input.getCategoryItem());
        log.info(
                "Agent搜索入参：keyword={}, brand={}, category={}, " +
                        "categoryItem={}, minPrice={}, maxPrice={}, sortType={}, limit={}",
                keyword,
                brand,
                category,
                categoryItem,
                input.getMinPrice(),
                input.getMaxPrice(),
                input.getSortType(),
                input.getLimit()
        );

        Long brandId = null;
        Long categoryId = null;
        Long categoryItemId = null;

        /*
         * 品牌名称转换为数据库 ID。
         */
        if (brand != null) {
            brandId = brandMapper.selectIdByName(brand);

            /*
             * 用户明确指定品牌，但数据库中不存在时，
             * 直接返回空列表，不能忽略品牌条件扩大搜索。
             */
            if(brandId == null){
                return List.of();
            }
        }

        /*
         * 一级分类名称转换为数据库 ID
         */
        if (category != null) {
            categoryId = categoryMapper.selectIdByName(category);

            if(categoryId == null){
                return List.of();
            }
        }

        /*
         * 二级分类名称转换为数据库 ID。
         */
        if (categoryItem != null) {
            categoryItemId = categoryItemMapper.selectIdByName(categoryItem);
            if(categoryItemId == null){
                return List.of();
            }
        }

        /*
         * 添加关键词分类兜底
         * 如果关键词恰好是平台已有的二级分类名称
         * 自动补充对应的 categoryItemId
         */

        if(categoryItemId == null && keyword != null){

            Long keywordCategoryItemId =
                    categoryItemMapper.selectIdByName(keyword);

            if(keywordCategoryItemId != null){
                categoryItemId = keywordCategoryItemId;

                log.info(
                        "关键词命中二级分类: keyword={}, categoryItemId={}",
                        keyword,
                        categoryItemId
                );
            }
        }

        ItemSearchRequest request =
                new ItemSearchRequest();

        request.setKeyword(keyword);
        request.setBrandId(brandId);
        request.setCategoryId(categoryId);
        request.setCategoryItemId(categoryItemId);
        request.setMinPrice(input.getMinPrice());
        request.setMaxPrice(input.getMaxPrice());
        request.setLimit(input.getLimit());
        request.setSortType(input.getSortType());

        List<ItemInfo> items = itemService.search(request);

        log.info(
                "Agent搜索结果: brandId={}, categoryId={}, categoryItemId={}, " +
                        "sortType={}, resultCount={}",
                brandId,
                categoryId,
                categoryItemId,
                request.safeSortType(),
                items.size()
        );

        return items;

    }

    /**
     * 查询商品分类
     */
    @Tool(description = """
            查询 GooShare 平台支持的一级分类和二级分类
            
            当用户不知道商品属于哪个分类，
            或者只描述用途而没有明确商品名称时调用。
            """)
    public List<CategoryVO> getCategories() {
        return itemService.getCategory();
    }

    /**
     * 查询具体商品详情
     */
    @Tool(description = """
            根据商品 ID 查询当前商品详情。
            
            当用户询问某个具体商品，
            或继续追问搜索结果中的某件商品时调用。
            """)
    public ItemInfo getItemDetail(
            @ToolParam(description = "需要查询的商品 ID")
            Integer itemId
    ){

        if(itemId == null || itemId <= 0){
            throw new IllegalArgumentException(
                    "商品 ID 必须是大于 0 的整数"
            );
        }

        ItemInfo item = itemService.getItem(itemId);

        if(item == null){
            throw new IllegalArgumentException(
                    "商品不存在或已下架"
            );
        }

        return item;

    }

    /**
     * 校验搜索参数
     * @param input
     */
    private void validateSearchInput(ItemSearchToolRequest input) {

        if(input == null){
            throw new IllegalArgumentException(
                    "商品搜索条件不能为空"
            );
        }

        boolean noCondition = !hasText(input.getKeyword())
                && !hasText(input.getCategory())
                && !hasText(input.getBrand())
                && !hasText(input.getCategoryItem())
                && input.getMinPrice() == null
                && input.getMaxPrice() == null;

        if(noCondition){
            throw new IllegalArgumentException(
                    "至少需要提供一个商品搜索条件"
            );
        }

        if(input.getMinPrice() != null && input.getMinPrice().signum() < 0){
            throw new IllegalArgumentException(
                    "最低价格不能小于0"
            );
        }

        if(input.getMaxPrice() != null && input.getMaxPrice().signum() < 0){
            throw new IllegalArgumentException(
                    "最高价格不能小于0"
            );
        }

        if(input.getMinPrice() != null && input.getMaxPrice() != null
            && input.getMinPrice().compareTo(input.getMaxPrice()) >0 ){
            throw new IllegalArgumentException(
                    "最低价格不能大于最高价格"
            );
        }
    }

    /**
     * 去掉无意义空格
     */
    private String normalize(String value){

        if(value == null){
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    /** 将模型可能使用的分类别名统一为数据库中的标准名称。 */
    private String normalizeCategory(String value){
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        return switch (normalized) {
            case "数码产品", "电子产品", "数码" -> "数码电子";
            case "家居", "家居用品" -> "家居生活";
            case "学习", "办公" -> "学习办公";
            default -> normalized;
        };
    }

    /**
     * 验证有无文本
     */
    private boolean hasText(String value){
        return value != null && !value.trim().isEmpty();
    }
}
