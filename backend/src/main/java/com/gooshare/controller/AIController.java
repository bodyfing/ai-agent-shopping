package com.gooshare.controller;

import ai.djl.repository.Artifact;
import com.gooshare.dto.IntentResult;
import com.gooshare.common.IntentType;
import com.gooshare.dto.ItemSearchRequest;
import com.gooshare.service.IntentRecognitionService;
import com.gooshare.service.ItemService;
import com.gooshare.entity.ItemInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AIController {

    private final IntentRecognitionService intentRecognitionService;
    private final ItemService itemService;
    private final ChatClient chatClient;

    @GetMapping("/ai/chat")
    public String chat(@RequestParam String message) {
        IntentResult intent = intentRecognitionService.recoginze(message);
        System.out.println(intent);
        List<ItemInfo> itemList;
        if(intent.getIntent() == IntentType.SEARCH_ITEM){

            ItemSearchRequest searchRequest =
                    ItemSearchRequest.from(intent);
            itemList = itemService.search(searchRequest);

            if(itemList.isEmpty()){
                return "没有找到符合条件的商品";
            }


            return chatClient
                    .prompt()
                    .system("""
                    你是校园二手交易助手。
                    根据商品列表回答用户。
                    不要编造商品。
                    """)
                    .user("""
                    用户需求：
                    %s
                    
                    商品列表：
                    %s
                    """.formatted(
                            message,
                            itemList
                    ))
                    .call()
                    .content();
        }
        return "暂时无法理解你的需求";
    }
}