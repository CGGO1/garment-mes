package com.garment.mes.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.garment.mes.common.BusinessException;
import com.garment.mes.common.JwtUtil;
import com.garment.mes.system.dto.LoginRequest;
import com.garment.mes.system.dto.LoginResponse;
import com.garment.mes.system.entity.SysMenu;
import com.garment.mes.system.entity.SysRole;
import com.garment.mes.system.entity.SysRoleMenu;
import com.garment.mes.system.entity.SysUser;
import com.garment.mes.system.mapper.SysMenuMapper;
import com.garment.mes.system.mapper.SysRoleMapper;
import com.garment.mes.system.mapper.SysRoleMenuMapper;
import com.garment.mes.system.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 认证服务：登录、当前用户信息、动态菜单
 */
@Service
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(SysUserMapper userMapper, SysRoleMapper roleMapper, SysMenuMapper menuMapper,
                       SysRoleMenuMapper roleMenuMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }
        String roleCode = null;
        if (user.getRoleId() != null) {
            SysRole role = roleMapper.selectById(user.getRoleId());
            if (role != null) {
                roleCode = role.getRoleCode();
            }
        }
        LoginResponse resp = new LoginResponse();
        resp.setToken(jwtUtil.generateToken(user.getUserId(), user.getUsername()));
        resp.setUserId(user.getUserId());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setRoleCode(roleCode);
        return resp;
    }

    public SysUser getByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    public SysUser getById(String userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 获取当前用户可见菜单（admin 角色返回全部）
     */
    public List<SysMenu> getMenusByUser(String userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getRoleId() == null) {
            return Collections.emptyList();
        }
        SysRole role = roleMapper.selectById(user.getRoleId());
        if (role != null && "admin".equals(role.getRoleCode())) {
            return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getVisible, "N").orderByAsc(SysMenu::getSort));
        }
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, user.getRoleId()));
        if (roleMenus.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).toList();
        return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getMenuId, menuIds)
                .eq(SysMenu::getVisible, "N")
                .orderByAsc(SysMenu::getSort));
    }
}
