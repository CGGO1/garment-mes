package com.garment.mes.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 配置（调用 Lili 知识库）
 */
@Data
@Component
@ConfigurationProperties(prefix = "mes.ai")
public class AiProperties {

    /** Lili 网关地址，如 http://192.168.11.81:10000 */
    private String liliBaseUrl = "http://192.168.11.81:10000";

    /** Lili 认证 token */
    private String liliToken = "";

    /** 超时（秒） */
    private int timeoutSeconds = 60;
}
