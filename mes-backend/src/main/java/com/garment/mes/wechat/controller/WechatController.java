package com.garment.mes.wechat.controller;

import com.garment.mes.common.R;
import com.garment.mes.wechat.dto.JsSdkConfig;
import com.garment.mes.wechat.service.WechatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信 JS-SDK：供前端 wx.config 获取签名
 */
@Slf4j
@RestController
@RequestMapping("/api/wechat")
public class WechatController {

    private final WechatService wechatService;

    public WechatController(WechatService wechatService) {
        this.wechatService = wechatService;
    }

    /**
     * 获取 JS-SDK 签名。未配置或调用失败时返回 enabled=false，前端自动降级为摄像头扫码。
     *
     * @param url 当前页面完整 URL（不含 #hash），需与公众号「JS 接口安全域名」一致
     */
    @GetMapping("/jssdk")
    public R<JsSdkConfig> jsSdk(@RequestParam("url") String url) {
        if (!wechatService.enabled()) {
            return R.ok(JsSdkConfig.disabled());
        }
        try {
            return R.ok(wechatService.buildJsSdkConfig(url));
        } catch (Exception e) {
            log.warn("生成微信 JSSDK 签名失败，降级为摄像头扫码: {}", e.getMessage());
            return R.ok(JsSdkConfig.disabled());
        }
    }
}
