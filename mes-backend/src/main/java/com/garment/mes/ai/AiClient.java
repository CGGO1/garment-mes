package com.garment.mes.ai;

import com.garment.mes.ai.config.AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 客户端：调用 Lili 知识库的 LLM 能力（问答/助手）
 * 调用失败时返回 null，由调用方降级处理。
 */
@Slf4j
@Component
public class AiClient {

    private final AiProperties props;
    private final RestClient restClient;

    public AiClient(AiProperties props) {
        this.props = props;
        this.restClient = RestClient.builder().build();
    }

    /**
     * 调用 Lili 助手对话接口，返回 LLM 文本；失败返回 null
     */
    public String chat(String message) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("message", message);
            body.put("mode", "CHAT");

            String url = props.getLiliBaseUrl() + "/api/assistant/chat";
            log.info("调用 Lili AI 接口: {}", url);
            String resp = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + props.getLiliToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return resp;
        } catch (Exception e) {
            log.warn("调用 Lili AI 接口失败，将降级为本地报表生成: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 是否已配置 AI 服务
     */
    public boolean enabled() {
        return props.getLiliBaseUrl() != null && !props.getLiliBaseUrl().isBlank();
    }
}
