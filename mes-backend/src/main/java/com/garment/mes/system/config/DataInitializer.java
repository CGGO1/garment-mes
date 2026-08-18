package com.garment.mes.system.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.garment.mes.system.entity.SysDictData;
import com.garment.mes.system.entity.SysDictType;
import com.garment.mes.system.entity.SysMenu;
import com.garment.mes.system.entity.SysRole;
import com.garment.mes.system.entity.SysUser;
import com.garment.mes.system.mapper.SysDictDataMapper;
import com.garment.mes.system.mapper.SysDictTypeMapper;
import com.garment.mes.system.mapper.SysMenuMapper;
import com.garment.mes.system.mapper.SysRoleMapper;
import com.garment.mes.system.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化：内置管理员、角色、菜单、字典
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(SysUserMapper userMapper, SysRoleMapper roleMapper, SysMenuMapper menuMapper,
                           SysDictTypeMapper dictTypeMapper, SysDictDataMapper dictDataMapper,
                           PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.dictTypeMapper = dictTypeMapper;
        this.dictDataMapper = dictDataMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initRole();
        initMenus();
        initAdminUser();
        initDict();
        log.info("MES 数据初始化完成");
    }

    private void initRole() {
        if (roleMapper.selectCount(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, "admin")) == 0) {
            SysRole role = new SysRole();
            role.setRoleId("role-admin");
            role.setRoleName("超级管理员");
            role.setRoleCode("admin");
            role.setRemark("系统内置超级管理员");
            roleMapper.insert(role);
        }
    }

    private void initMenus() {
        // 工作台
        addMenu("m-dashboard", "0", "工作台", "MENU", "/dashboard", "dashboard/index", "Odometer", 1, null);
        // 主数据
        addMenu("m-master", "0", "主数据", "DIR", "/master", null, "Box", 10, null);
        addMenu("m-master-customer", "m-master", "客户管理", "MENU", "/master/customer", "master/customer/index", "User", 1, null);
        addMenu("m-master-supplier", "m-master", "供应商管理", "MENU", "/master/supplier", "master/supplier/index", "Van", 2, null);
        addMenu("m-master-product", "m-master", "产品管理", "MENU", "/master/product", "master/product/index", "Goods", 3, null);
        addMenu("m-master-material", "m-master", "物料管理", "MENU", "/master/material", "master/material/index", "Box", 4, null);
        addMenu("m-master-bom", "m-master", "产品BOM", "MENU", "/master/bom", "master/bom/index", "List", 5, null);
        addMenu("m-master-process", "m-master", "工序管理", "MENU", "/master/process", "master/process/index", "Operation", 6, null);
        // 进出口贸易
        addMenu("m-trade", "0", "进出口贸易", "DIR", "/trade", null, "Ship", 20, null);
        addMenu("m-trade-order", "m-trade", "订单管理", "MENU", "/trade/order", "trade/order/index", "Document", 1, null);
        // 生产管理
        addMenu("m-production", "0", "生产管理", "DIR", "/production", null, "Cpu", 30, null);
        addMenu("m-production-work-order", "m-production", "生产工单", "MENU", "/production/work-order", "production/work-order/index", "Tickets", 1, null);
        addMenu("m-production-scan", "m-production", "扫码登记", "MENU", "/production/scan", "production/scan/index", "Camera", 2, null);
        // 库存管理
        addMenu("m-inventory", "0", "库存管理", "DIR", "/inventory", null, "Files", 40, null);
        addMenu("m-inventory-stock", "m-inventory", "库存管理", "MENU", "/inventory/stock", "inventory/stock/index", "Box", 1, null);
        // 质量管理
        addMenu("m-quality", "0", "质量管理", "DIR", "/quality", null, "CircleCheck", 50, null);
        addMenu("m-quality-inspection", "m-quality", "质量管理", "MENU", "/quality/inspection", "quality/inspection/index", "Checked", 1, null);
        // 设备管理（M4）
        addMenu("m-equipment", "0", "设备管理", "DIR", "/equipment", null, "Cpu", 35, null);
        addMenu("m-equipment-list", "m-equipment", "设备台账", "MENU", "/equipment", "equipment/index", "Box", 1, null);
        // AI 助手
        addMenu("m-ai", "0", "AI 助手", "MENU", "/ai/assistant", "ai/assistant/index", "ChatDotRound", 60, null);
        // 系统管理
        addMenu("m-system", "0", "系统管理", "DIR", "/system", null, "Setting", 90, null);
        addMenu("m-system-user", "m-system", "用户管理", "MENU", "/system/user", "system/user/index", "User", 1, null);
        addMenu("m-system-role", "m-system", "角色管理", "MENU", "/system/role", "system/role/index", "UserFilled", 2, null);
        addMenu("m-system-menu", "m-system", "菜单管理", "MENU", "/system/menu", "system/menu/index", "Menu", 3, null);
        addMenu("m-system-dict", "m-system", "字典管理", "MENU", "/system/dict", "system/dict/index", "Collection", 4, null);

        // 按钮级权限（菜单管理页展示，控制操作权限）
        addButtonPerms();
    }

    private void addMenu(String id, String parentId, String name, String type, String path,
                         String component, String icon, int sort, String perms) {
        if (menuMapper.selectById(id) != null) {
            return;
        }
        SysMenu menu = new SysMenu();
        menu.setMenuId(id);
        menu.setParentId(parentId);
        menu.setMenuName(name);
        menu.setMenuType(type);
        menu.setPath(path);
        menu.setComponent(component);
        menu.setIcon(icon);
        menu.setSort(sort);
        menu.setPerms(perms);
        menu.setVisible("N");
        menuMapper.insert(menu);
    }

    /** 按钮级权限（RuoYi 风格：菜单管理树中展示，不影响侧栏与路由） */
    private void addButtonPerms() {
        String[][] buttons = {
                // [菜单ID, 权限编码, 名称]
                {"m-master-customer", "master:customer:add", "客户新增"},
                {"m-master-customer", "master:customer:delete", "客户删除"},
                {"m-master-supplier", "master:supplier:add", "供应商新增"},
                {"m-master-supplier", "master:supplier:delete", "供应商删除"},
                {"m-master-product", "master:product:add", "产品新增"},
                {"m-master-product", "master:product:delete", "产品删除"},
                {"m-master-material", "master:material:add", "物料新增"},
                {"m-master-material", "master:material:delete", "物料删除"},
                {"m-master-bom", "master:bom:add", "BOM新增"},
                {"m-master-bom", "master:bom:delete", "BOM删除"},
                {"m-master-process", "master:process:add", "工序新增"},
                {"m-master-process", "master:process:delete", "工序删除"},
                {"m-trade-order", "trade:order:add", "订单新增"},
                {"m-trade-order", "trade:order:status", "订单流转"},
                {"m-trade-order", "trade:order:delete", "订单删除"},
                {"m-production-work-order", "production:work-order:add", "工单新增"},
                {"m-production-work-order", "production:work-order:report", "工单报工"},
                {"m-production-work-order", "production:work-order:delete", "工单删除"},
                {"m-production-scan", "production:scan:add", "扫码登记"},
                {"m-equipment-list", "equipment:add", "设备新增"},
                {"m-equipment-list", "equipment:scan", "设备扫码"},
                {"m-equipment-list", "equipment:delete", "设备删除"},
                {"m-inventory-stock", "inventory:inbound:add", "入库新增"},
                {"m-inventory-stock", "inventory:outbound:add", "出库新增"},
                {"m-quality-inspection", "quality:inspection:add", "验货新增"},
                {"m-quality-inspection", "quality:inspection:delete", "验货删除"},
                {"m-system-user", "system:user:add", "用户新增"},
                {"m-system-user", "system:user:delete", "用户删除"},
                {"m-system-role", "system:role:add", "角色新增"},
                {"m-system-role", "system:role:delete", "角色删除"},
                {"m-system-menu", "system:menu:add", "菜单新增"},
                {"m-system-dict", "system:dict:add", "字典新增"},
        };
        int idx = 0;
        for (String[] b : buttons) {
            idx++;
            String menuId = "m-btn-" + String.format("%03d", idx);
            if (menuMapper.selectById(menuId) != null) {
                continue;
            }
            SysMenu menu = new SysMenu();
            menu.setMenuId(menuId);
            menu.setParentId(b[0]);
            menu.setMenuName(b[2]);
            menu.setMenuType("BUTTON");
            menu.setPerms(b[1]);
            menu.setVisible("N");
            menuMapper.insert(menu);
        }
    }

    private void initAdminUser() {
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "admin")) == 0) {
            SysUser admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("系统管理员");
            admin.setRoleId("role-admin");
            admin.setStatus("NORMAL");
            userMapper.insert(admin);
            log.info("已创建内置管理员账号：admin / admin123");
        }
    }

    private void initDict() {
        // 贸易术语 Incoterms 2020
        if (dictTypeMapper.selectCount(new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getDictType, "incoterms")) == 0) {
            createDictType("贸易术语", "incoterms");
            String[][] incoterms = {
                    {"EXW", "工厂交货"},
                    {"FCA", "货交承运人"},
                    {"FOB", "船上交货（离岸价）"},
                    {"CFR", "成本加运费"},
                    {"CIF", "成本加保险费运费（到岸价）"},
                    {"DAP", "目的地交货"},
                    {"DDP", "完税后交货"},
            };
            for (String[] item : incoterms) {
                createDictData("incoterms", item[0], item[1]);
            }
        }
        // 质检 AQL 验货等级
        if (dictTypeMapper.selectCount(new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getDictType, "aql_level")) == 0) {
            createDictType("AQL 验货等级", "aql_level");
            String[][] aql = {
                    {"1.0", "AQL 1.0"},
                    {"1.5", "AQL 1.5"},
                    {"2.5", "AQL 2.5"},
                    {"4.0", "AQL 4.0"},
            };
            for (String[] item : aql) {
                createDictData("aql_level", item[0], item[1]);
            }
        }
    }

    private void createDictType(String name, String type) {
        SysDictType t = new SysDictType();
        t.setDictName(name);
        t.setDictType(type);
        t.setStatus("NORMAL");
        dictTypeMapper.insert(t);
    }

    private void createDictData(String type, String value, String label) {
        SysDictData d = new SysDictData();
        d.setDictType(type);
        d.setDictValue(value);
        d.setDictLabel(label);
        d.setStatus("NORMAL");
        dictDataMapper.insert(d);
    }
}
