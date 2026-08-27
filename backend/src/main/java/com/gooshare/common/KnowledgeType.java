package com.gooshare.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KnowledgeType {

    /**
     * 平台功能、账号、商品发布、审核、订单规则。
     */
    PLATFORM_RULES("platform_rules"),

    /**
     * 校园面交、交易安全、防骗建议。
     */
    CAMPUS_TRADE("campus_trade"),

    /**
     * 发货、物流、收货、退款和售后。
     */
    SHIPPING("shipping"),

    /**
     * 不限制知识类型。
     */
    ALL(null);

    private final String metadataValue;
}
