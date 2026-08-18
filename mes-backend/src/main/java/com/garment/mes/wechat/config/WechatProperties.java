package com.garment.mes.wechat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 微信公众号配置（用于 JS-SDK 扫码 wx.scanQRCode）
 */
@Data
@Component
@ConfigurationProperties(prefix = "mes.wechat")
public class WechatProperties {

    /** 公众号 appId（服务号），留空则页面内扫码自动降级为摄像头/手动输入 */
    private String appId = "";

    /** 公众号 appSecret */
    private String appSecret = "";

    public boolean enabled() {
        return StringUtils.hasText(appId) && StringUtils.hasText(appSecret);
    }
}
