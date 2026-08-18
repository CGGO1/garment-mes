package com.garment.mes.wechat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 微信 JS-SDK wx.config 所需参数
 */
@Data
@AllArgsConstructor
public class JsSdkConfig {

    /** 是否已配置微信扫码（false 时前端自动降级为摄像头扫码） */
    private boolean enabled;

    private String appId;

    private Long timestamp;

    private String nonceStr;

    private String signature;

    public static JsSdkConfig disabled() {
        return new JsSdkConfig(false, null, null, null, null);
    }
}
