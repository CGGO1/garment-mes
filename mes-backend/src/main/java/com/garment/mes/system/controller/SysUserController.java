package com.garment.mes.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.mes.common.BusinessException;
import com.garment.mes.common.R;
import com.garment.mes.system.entity.SysUser;
import com.garment.mes.system.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理
 */
@RestController
@RequestMapping("/api/system/user")
public class SysUserController {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public SysUserController(SysUserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/page")
    public R<Page<SysUser>> page(@RequestParam(defaultValue = "1") long pageNum,
                                 @RequestParam(defaultValue = "10") long pageSize,
                                 @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(SysUser::getUsername, keyword).or().like(SysUser::getNickname, keyword));
        }
        qw.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = userMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        result.getRecords().forEach(u -> u.setPassword(null));
        return R.ok(result);
    }

    @GetMapping("/{id}")
    public R<SysUser> get(@PathVariable String id) {
        SysUser user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return R.ok(user);
    }

    @PostMapping
    public R<Void> create(@RequestBody SysUser user) {
        if (!StringUtils.hasText(user.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, user.getUsername()));
        if (exists > 0) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(StringUtils.hasText(user.getStatus()) ? user.getStatus() : "NORMAL");
        userMapper.insert(user);
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@RequestBody SysUser user) {
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null); // 不修改密码
        }
        userMapper.updateById(user);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        userMapper.deleteById(id);
        return R.ok();
    }
}
