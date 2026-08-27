package com.gooshare.service.impl;

import com.gooshare.dto.IntentResult;
import com.gooshare.service.CategoryService;
import com.gooshare.service.IntentRecognitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IntentRecognitionServiceImpl implements IntentRecognitionService {

    private final ChatClient chatClient;

    private final CategoryService categoryService;

    public IntentResult recoginze(String message){

        IntentResult result =  chatClient
                .prompt()
                .system("""
                        你是校园二手交易平台的意图识别器。
                        
                                       你的任务：
                                       1. 分析用户输入
                                       2. 判断用户意图
                                       3. 提取商品搜索条件
                        
                        
                                       意图包括：
                        
                                       SEARCH_ITEM：
                                       用户希望搜索商品，例如：
                                       "帮我找苹果手机"
                                       "搜索2000以内的电脑"
                        
                        
                                       RECOMMEND_ITEM：
                                       用户希望根据需求推荐商品，例如：
                                       "推荐一款适合学生的笔记本"
                        
                        
                                       ITEM_DETAIL：
                                       用户询问某个具体商品的信息，例如：
                                       "iPhone 13多少钱"
                        
                        
                                       PUBLISH_ITEM：
                                       用户希望发布商品
                        
                        
                                       QUERY_ORDER：
                                       用户查询订单
                        
                        
                                       UNKNOWN：
                                       无法判断用户需求
                        
                        
                        
                                       需要提取以下参数：
                        
                                       keyword：
                                       商品核心关键词。
                        
                                       例如：
                                       手机
                                       电脑
                                       自行车
                                       耳机
                        
                                       不要包含品牌和价格。
                        
                        
                                       brand：
                                       商品品牌。
                        
                                       例如：
                                       苹果
                                       华为
                                       小米
                                       索尼
                                       戴尔
                        
                                       如果用户没有提到品牌，返回null。
                        
                        
                                       category：
                                       商品一级分类，只能从以下选择：
                        
                                       数码电子
                                       家居生活
                                       服饰穿搭
                                       运动户外
                                       学习办公
                                       美妆个护
                                       娱乐收藏
                                       交通出行
                        
                        
                                       categoryItem：
                                       商品二级分类，只能从以下选择：
                        
                        
                                       数码电子：
                                       手机
                                       平板电脑
                                       笔记本电脑
                                       显示器
                                       耳机
                                       键盘鼠标
                                       相机摄影
                                       智能穿戴
                                       游戏设备
                        
                        
                                       家居生活：
                                       照明用品
                                       厨房电器
                                       生活电器
                                       收纳用品
                                       家具
                        
                        
                                       服饰穿搭：
                                       男装
                                       女装
                                       鞋靴
                                       箱包
                                       运动服饰
                                       配饰
                        
                        
                                       运动户外：
                                       球类运动
                                       健身器材
                                       户外装备
                        
                        
                                       学习办公：
                                       教材
                                       考研资料
                                       语言学习
                                       办公用品
                                       学习设备
                        
                        
                                       美妆个护：
                                       护肤品
                                       化妆品
                                       个人护理
                        
                        
                                       娱乐收藏：
                                       游戏机
                                       乐器
                                       绘画用品
                                       收藏品
                        
                        
                                       交通出行：
                                       自行车
                                       电动车
                                       滑板
                                       旅行用品
                        
                        
                                       minPrice：
                                       用户要求的最低价格。
                        
                                       例如：
                                       "1000以上"
                                       返回1000。
                        
                        
                                       maxPrice：
                                       用户要求的最高价格。
                        
                                       例如：
                                       "2000以内"
                                       返回2000。
                        
                        
                                       规则：
                        
                                       1. 不要猜测用户没有提供的信息。
                                       2. 无法判断的字段返回null。
                                       3. keyword只保留商品核心名称。
                                       4. brand、category、categoryItem必须来自给定列表。
                                       5. 返回JSON格式。
                        """)
                .user(message)
                .call()
                .entity(IntentResult.class);

        categoryService.convert(result);

        return result;

    }
}
