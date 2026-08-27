package com.gooshare.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI gooShareOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("GooShare校园二手交易平台API文档")
                        .description("包含用户管理、商品发布、订单处理等核心接口")
                        .version("v1.0.0"));
    }
}
