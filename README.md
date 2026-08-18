# Garment MES · 服装智造

**杭州 · 4 年正式开发经验 · 后端 / 全栈程序员**，在职，正在寻求更好的工作机会（跳槽）。

📮 邮箱：[1300023059@qq.com](mailto:1300023059@qq.com)

服装制造业进出口贸易 MES（制造执行）系统，覆盖**订单 → 生产 → 库存 → 质检**全链路，并内置 **AI 即席报表** 与 **AI 助手**（对接本地 Lili 知识库）。

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue 3 + Vite + Element Plus + Pinia + Vue Router + TypeScript + ECharts |
| 后端 | Java 21 + Spring Boot 3.5 + MyBatis-Plus 3.5.15 + Spring Security + JWT |
| 数据库 | PostgreSQL 16 |
| 文档 | knife4j + springdoc |
| AI | 通过 HTTP 调用 Lili 知识库（本地目录，通过环境变量配置） |

## 功能模块

- **系统管理**：用户、角色（50+ 演示角色）、菜单（含按钮级权限）、字典（25+ 类型 / 50+ 数据）
- **主数据**：客户、供应商、产品、物料（面料/辅料）、产品 BOM（物料清单）、工序
- **进出口贸易**：订单（出口/进口）、明细、贸易术语、状态流转、单证管理、出货计划
- **生产管理**：生产工单、工序明细、裁床记录、报工（扫码/条码）、扫码登记
- **库存管理**：仓库、入库、出库（含明细与流水）、现存量、低库存预警、库存流水
- **质量管理**：质检标准（AQL）、验货单（IQC/IPQC/FQC/OQC）、检验项目、疵点记录
- **AI 能力**：AI 即席报表（PDF + Excel 下载）、AI 助手（工艺问答/合规检索）

## V2 体验优化（2026-08）

- **统一设计语言**：全站统一页头（eyebrow + 标题 + 摘要）、卡片式布局、状态标签、柔和阴影与圆角；
- **时间/金额格式化**：后端全局输出 `yyyy-MM-dd HH:mm:ss`，前端提供金额千分位、相对时间等格式化工具；
- **详情抽屉**：订单/工单/出入库/验货/BOM 等页面提供详情抽屉，展示全量明细；
- **生产进度可视化**：工单列表与详情展示完成进度条，订单详情展示状态流转步骤条；
- **工作台真实数据**：产量趋势、订单状态分布、低库存预警、质量快照全部基于真实接口；
- **演示数据**：每个业务页面 ≥50 条真实感数据，订单/工单/出入库/验货/BOM 均含 2-6 条明细；
- **修复**：动态路由硬刷新 404、时间戳裸显示、工序/供应商/BOM 无页面等历史问题。

## 目录结构

```
MES/
├── mes-backend/        # Spring Boot 后端（模块化单体，按业务域分包）
│   └── src/main/java/com/garment/mes/
│       ├── common/  config/
│       ├── system/ master/ trade/ production/ inventory/ quality/
│       ├── report/ ai/   # 报表 + AI 客户端
├── mes-frontend/       # Vue 3 前端
├── docker/             # PostgreSQL 16 编排
├── docs/               # 系统规划 / 数据库设计 / 部署文档
└── mvnw.sh             # Maven wrapper（Git Bash 路径转换修复）
```

## 快速开始

### 1. 启动数据库

先启动 Docker Desktop，然后：

```bash
cd docker
docker compose up -d
```

PostgreSQL 16 映射到宿主机 **5433** 端口（库 `mes` / 用户 `mes` / 密码 `mes123456`）。

### 2. 启动后端

```bash
cd mes-backend
../mvnw.sh spring-boot:run
```

> 后端启动时会自动执行 `schema.sql` 建表，并初始化管理员账号与菜单。
> 接口文档：http://localhost:8080/doc.html

### 3. 启动前端

```bash
cd mes-frontend
npm install
npm run dev
```

访问 http://localhost:5175

### 默认账号

- 用户名：`admin`
- 密码：`admin123`

## AI 能力配置

AI 报表与 AI 助手通过 HTTP 调用 Lili 知识库网关，在 `mes-backend/src/main/resources/application.yml` 配置：

```yaml
mes:
  ai:
    lili-base-url: http://192.168.11.81:10000   # Lili 网关地址
    lili-token: ${MES_AI_LILI_TOKEN:}            # Lili 认证 token（环境变量注入）
```

> 未配置或 Lili 不可用时，AI 报表自动**降级为本地统计报表**，PDF/Excel 下载仍可用。

## 详细文档

- [系统规划文档](docs/系统规划文档.md)
- [数据库设计](docs/数据库设计.md)
- [部署文档](docs/部署文档.md)
