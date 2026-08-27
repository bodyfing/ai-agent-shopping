package com.gooshare.agent.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.gooshare.common.KnowledgeType;
import lombok.Data;

@Data
public class KnowledgeSearchToolRequest {

    @JsonPropertyDescription(
            "用户关于GooShare平台规则的完整问题"
    )
    private String query;

    @JsonPropertyDescription("""
            知识类型：
            SHIPPING 表示发货、物流、收货、退款和售后；
            CAMPUS_TRADE 表示校园面交、交易安全和防骗；
            PLATFORM_RULES 表示账号、平台功能、商品发布、审核和订单规则；
            无法判断或问题涉及多个类型时使用 ALL
            """)
    private KnowledgeType knowledgeType = KnowledgeType.ALL;
}
