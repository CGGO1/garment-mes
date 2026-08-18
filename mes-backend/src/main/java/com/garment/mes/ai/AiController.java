package com.garment.mes.ai;

import com.garment.mes.common.R;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AI 助手接口：后端代理到 Lili 知识库
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiClient aiClient;

    public AiController(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    @PostMapping("/chat")
    public R<String> chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        if (message == null || message.isBlank()) {
            return R.fail("请输入问题");
        }
        String resp = aiClient.chat(message);
        if (resp == null) {
            return R.fail("AI 服务未连接，请检查 Lili 知识库配置（mes.ai.lili-base-url）");
        }
        return R.ok(resp);
    }
}
