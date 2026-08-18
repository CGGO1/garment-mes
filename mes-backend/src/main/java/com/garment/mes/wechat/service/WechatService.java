package com.garment.mes.wechat.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.garment.mes.wechat.config.WechatProperties;
import com.garment.mes.wechat.dto.JsSdkConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * 微信 JS-SDK 签名服务：access_token / jsapi_ticket 缓存 + 前端签名
 */
@Slf4j
@Service
public class WechatService {

    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket";
    /** token/ticket 提前 5 分钟过期，避免临界失效 */
    private static final long EXPIRE_LEAD = 300;

    private final WechatProperties props;
    private final RestClient restClient;

    private volatile String accessToken;
    private volatile Instant tokenExpireAt = Instant.EPOCH;
    private volatile String jsapiTicket;
    private volatile Instant ticketExpireAt = Instant.EPOCH;

    public WechatService(WechatProperties props) {
        this.props = props;
        this.restClient = RestClient.builder().build();
    }

    public boolean enabled() {
        return props.enabled();
    }

    /**
     * 生成当前页面 URL 的 JS-SDK 签名配置
     */
    public synchronized JsSdkConfig buildJsSdkConfig(String url) {
        if (!props.enabled()) {
            return JsSdkConfig.disabled();
        }
        String ticket = getJsapiTicket();
        String nonceStr = randomNonce();
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String raw = "jsapi_ticket=" + ticket
                + "&noncestr=" + nonceStr
                + "&timestamp=" + timestamp
                + "&url=" + url;
        return new JsSdkConfig(true, props.getAppId(), Long.parseLong(timestamp), nonceStr, sha1(raw));
    }

    private synchronized String getAccessToken() {
        if (StringUtils.hasText(accessToken) && Instant.now().isBefore(tokenExpireAt)) {
            return accessToken;
        }
        String url = TOKEN_URL + "?grant_type=client_credential"
                + "&appid=" + props.getAppId()
                + "&secret=" + props.getAppSecret();
        TokenResp resp = restClient.get().uri(url).retrieve().body(TokenResp.class);
        if (resp == null || resp.errcode() != 0 || !StringUtils.hasText(resp.accessToken())) {
            String msg = resp == null ? "空响应" : resp.errmsg();
            log.warn("获取微信 access_token 失败: {}", msg);
            throw new IllegalStateException("获取微信 access_token 失败: " + msg);
        }
        accessToken = resp.accessToken();
        tokenExpireAt = Instant.now().plusSeconds(Math.max(resp.expiresIn() - EXPIRE_LEAD, 60));
        return accessToken;
    }

    private synchronized String getJsapiTicket() {
        if (StringUtils.hasText(jsapiTicket) && Instant.now().isBefore(ticketExpireAt)) {
            return jsapiTicket;
        }
        String url = TICKET_URL + "?access_token=" + getAccessToken() + "&type=jsapi";
        TicketResp resp = restClient.get().uri(url).retrieve().body(TicketResp.class);
        if (resp == null || resp.errcode() != 0 || !StringUtils.hasText(resp.ticket())) {
            String msg = resp == null ? "空响应" : resp.errmsg();
            log.warn("获取微信 jsapi_ticket 失败: {}", msg);
            throw new IllegalStateException("获取微信 jsapi_ticket 失败: " + msg);
        }
        jsapiTicket = resp.ticket();
        ticketExpireAt = Instant.now().plusSeconds(Math.max(resp.expiresIn() - EXPIRE_LEAD, 60));
        return jsapiTicket;
    }

    private static String sha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 算法不可用", e);
        }
    }

    private static String randomNonce() {
        byte[] bytes = new byte[8];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record TokenResp(@JsonProperty("access_token") String accessToken,
                            @JsonProperty("expires_in") long expiresIn,
                            @JsonProperty("errcode") int errcode,
                            @JsonProperty("errmsg") String errmsg) {
    }

    public record TicketResp(@JsonProperty("ticket") String ticket,
                             @JsonProperty("expires_in") long expiresIn,
                             @JsonProperty("errcode") int errcode,
                             @JsonProperty("errmsg") String errmsg) {
    }
}
