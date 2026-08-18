package com.garment.mes.system.dto;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginResponse {

    private String token;

    private String userId;

    private String username;

    private String nickname;

    private String roleCode;
}
