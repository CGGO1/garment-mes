package com.garment.mes.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.garment.mes.common.R;
import com.garment.mes.system.entity.SysMenu;
import com.garment.mes.system.mapper.SysMenuMapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单管理
 */
@RestController
@RequestMapping("/api/system/menu")
public class SysMenuController {

    private final SysMenuMapper menuMapper;

    public SysMenuController(SysMenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    @GetMapping("/list")
    public R<List<SysMenu>> list() {
        return R.ok(menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort)));
    }

    @GetMapping("/tree")
    public R<List<SysMenu>> tree() {
        List<SysMenu> all = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort));
        return R.ok(buildTree(all, "0"));
    }

    @GetMapping("/{id}")
    public R<SysMenu> get(@PathVariable String id) {
        return R.ok(menuMapper.selectById(id));
    }

    @PostMapping
    public R<Void> create(@RequestBody SysMenu menu) {
        menuMapper.insert(menu);
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@RequestBody SysMenu menu) {
        menuMapper.updateById(menu);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        menuMapper.deleteById(id);
        return R.ok();
    }

    private List<SysMenu> buildTree(List<SysMenu> all, String parentId) {
        return all.stream()
                .filter(m -> parentId.equals(m.getParentId()))
                .map(m -> {
                    m.setChildren(buildTree(all, m.getMenuId()));
                    return m;
                })
                .toList();
    }
}
