package com.garment.mes.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.mes.common.R;
import com.garment.mes.system.entity.SysRole;
import com.garment.mes.system.entity.SysRoleMenu;
import com.garment.mes.system.mapper.SysRoleMapper;
import com.garment.mes.system.mapper.SysRoleMenuMapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理
 */
@RestController
@RequestMapping("/api/system/role")
public class SysRoleController {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public SysRoleController(SysRoleMapper roleMapper, SysRoleMenuMapper roleMenuMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @GetMapping("/page")
    public R<Page<SysRole>> page(@RequestParam(defaultValue = "1") long pageNum,
                                 @RequestParam(defaultValue = "10") long pageSize,
                                 @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysRole> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.like(SysRole::getRoleName, keyword).or().like(SysRole::getRoleCode, keyword);
        }
        qw.orderByDesc(SysRole::getCreateTime);
        return R.ok(roleMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @GetMapping("/list")
    public R<List<SysRole>> list() {
        return R.ok(roleMapper.selectList(null));
    }

    @GetMapping("/{id}")
    public R<SysRole> get(@PathVariable String id) {
        return R.ok(roleMapper.selectById(id));
    }

    @PostMapping
    public R<Void> create(@RequestBody SysRole role) {
        roleMapper.insert(role);
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@RequestBody SysRole role) {
        roleMapper.updateById(role);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        roleMapper.deleteById(id);
        return R.ok();
    }

    @GetMapping("/{roleId}/menus")
    public R<List<String>> menus(@PathVariable String roleId) {
        List<SysRoleMenu> list = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        return R.ok(list.stream().map(SysRoleMenu::getMenuId).toList());
    }

    @PutMapping("/{roleId}/menus")
    public R<Void> saveMenus(@PathVariable String roleId, @RequestBody List<String> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        for (String menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        }
        return R.ok();
    }
}
