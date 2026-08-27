package com.gooshare;

import com.livehelps.dify.DifyApiFactory;
import com.livehelps.dify.api.DifyChatApi;
import com.livehelps.dify.api.DifyChatFlowApi;
import com.livehelps.dify.data.enums.ResponseMode;
import com.livehelps.dify.data.request.ChatMessageRequest; // 假设存在这个类
import com.livehelps.dify.data.response.ChatMessageResponse; // 假设存在这个类
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class DifyTest {

    @Test
    public void test() {
        // 1. 初始化 Chatflow 客户端（注意方法名可能不同）
        DifyChatApi chatFlowApi = DifyApiFactory
                .newInstance("https://api.dify.ai/v1", "app-cN7Yo2Ha1oJ0EycTnpjWEL74")
                .newDifyChatApi(); // 注意这里要改成 newDifyChatFlowApi()

        // 2. 构建请求参数（使用 ChatMessageRequest）
        ChatMessageRequest request = new ChatMessageRequest();
        request.setUser("要买二手商品的买家同学");
        request.setResponseMode(ResponseMode.BLOCKING);
        request.setQuery("防骗提醒告诉我一下"); // 关键：Chatflow 用 query 字段

        // 如果还需要额外输入变量，放入 inputs
        Map<String, Object> inputs = new HashMap<>();
        // inputs.put("other_key", "other_value"); // 按需添加
        request.setInputs(inputs);

        // 3. 发送消息并获取结果
        ChatMessageResponse response = chatFlowApi.sendChatMessage(request); // 假设方法名
        System.out.println(response);
    }
}