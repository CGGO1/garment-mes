package com.garment.mes.system.controller;

import com.garment.mes.common.R;
import com.garment.mes.system.dto.LoginRequest;
import com.garment.mes.system.dto.LoginResponse;
import com.garment.mes.system.entity.SysMenu;
import com.garment.mes.system.entity.SysUser;
import com.garment.mes.system.service.AuthService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public R<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @GetMapping("/info")
    public R<SysUser> info() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = authService.getById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return R.ok(user);
    }

    @GetMapping("/menus")
    public R<List<SysMenu>> menus() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return R.ok(authService.getMenusByUser(userId));
    }
}
