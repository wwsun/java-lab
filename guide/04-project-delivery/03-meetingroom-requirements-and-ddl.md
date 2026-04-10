# 03 - 会议室预约系统：需求梳理、数据建模与 DDL 设计

你现在已经处于第 4 周阶段，这一节不再停留在“学概念”，而是直接进入交付视角：

> 目标不是把功能点列出来，而是产出一版可直接交给 Agent 生成业务骨架的需求与数据模型。

如果用 Node.js / TypeScript 心智来类比：

| Node.js / TS 世界 | Java / Spring Boot 世界 | 你应该怎么理解 |
| --- | --- | --- |
| `Prisma schema` / TypeORM Entity 设计 | MyBatis-Plus Entity + SQL DDL | 先定数据模型，再生成代码骨架 |
| `POST /bookings` 里的业务校验 | `Service` 层业务规则校验 | 冲突检测不该塞在 Controller |
| `class-validator` + service rule | `@Valid` + Service rule | 参数校验和业务校验是两层 |
| `users` 认证表复用 | 复用 starter 里的 `users` 表 | 不重复造认证模型 |

## 一、先把输入 / 输出 / 边界说清楚

基于路线图和当前脚手架，这里先采用一版明确假设。

### 输入

- 登录用户
- 会议室筛选条件
  - 容量
  - 设施标签
- 预约请求
  - 会议主题
  - 会议室 ID
  - 开始时间
  - 结束时间

### 输出

- 会议室列表
- 指定日期空闲时段
- 预约创建结果
- 我的预约分页列表

### 系统边界

本期只做核心闭环，不做以下高级特性：

- 审批流
- 邮件 / 短信通知
- 多租户
- 会议签到
- 复杂权限模型（先使用登录用户 + 基础角色）

## 二、最小可交付范围

这一版建议把 week 4 的 MVP 收敛成 4 个接口：

1. `GET /api/meeting-rooms`
   返回会议室列表，支持容量和设施筛选。
2. `GET /api/meeting-rooms/{id}/availability?date=2026-04-10`
   返回指定会议室在某天的已占用或可预约时段。
3. `POST /api/reservations`
   创建预约，执行双重冲突校验。
4. `GET /api/reservations/me`
   返回当前登录用户的预约列表，支持筛选和分页。

这 4 个接口已经足以覆盖完整闭环：

> 浏览列表 -> 查看时段 -> 提交预约 -> 查看我的预约

## 三、两大页面与两大模块

### 页面 1：会议室列表页

用户在这里完成：

- 浏览会议室基础信息
- 按容量筛选
- 按设施筛选
- 进入某个会议室的日历页

### 页面 2：预约日历页

用户在这里完成：

- 选择某个日期
- 查看该会议室的空闲 / 已预约时段
- 发起预约

### 模块 1：会议室查看模块

职责：

- 会议室列表查询
- 设施与容量筛选
- 单日可预约时段展示

### 模块 2：预约管理模块

职责：

- 创建预约
- 冲突检测
- 我的预约列表
- 取消预约（建议作为扩展接口预留）

## 四、核心业务流程

### 1. 浏览会议室

1. 用户进入会议室列表页
2. 按容量 / 设施筛选
3. 查看目标会议室详情

### 2. 查看空闲时段

1. 用户选择会议室和日期
2. 系统查询当天有效预约
3. 前端根据已占用时段展示空闲区间

### 3. 发起预约

1. 用户提交会议主题、开始时间、结束时间
2. 系统做基础业务校验
3. 系统执行会议室冲突校验
4. 系统执行用户时间冲突校验
5. 保存预约记录

### 4. 查看我的预约

1. 用户查看自己的预约列表
2. 可按会议室、日期范围筛选
3. 支持分页查询

## 五、业务规则确认

下面这些规则建议在本期作为强规则实现。

### 规则 1：同一会议室同一时段不允许重叠预约

冲突判断推荐统一使用这个条件：

```sql
existing.start_time < new_end_time
AND existing.end_time > new_start_time
```

这比枚举各种交叉情况更稳定。

### 规则 2：同一用户同一时段只能预约一间会议室

也就是说，除了会议室冲突，还要检查用户自己的时间是否冲突。

### 规则 3：单次预约时长上限 4 小时

这属于业务规则，建议放在 `Service` 层校验，不建议交给数据库表达式强行约束。

### 规则 4：不能预约过去时间

同样属于业务规则，建议在 `Service` 层基于当前时间校验。

### 规则 5：会议室需要预置初始化数据

至少预置 3 到 5 个会议室，便于前后端联调。

## 六、数据建模方案比较

这里给两个备选方案。

### 方案 A：`meeting_room.facility_tags` 用逗号字符串 / JSON 存储

优点：

- 表少
- 初期开发快

缺点：

- 设施筛选查询不优雅
- 后续扩展不方便
- 数据规范性较弱

### 方案 B：`meeting_room` + `meeting_room_feature` 关联表

优点：

- 查询和筛选更清晰
- 可扩展性更好
- 更符合关系型建模习惯

缺点：

- 比方案 A 多一张表

### 推荐方案

推荐 **方案 B**。

原因很简单：

- 你已经进入 week 4，目标是交付工程，而不是只求最少代码
- “按设施筛选”是核心功能，不该建立在字符串模糊匹配上
- 这更贴近真实 Java Web 项目的数据设计方式

## 七、推荐数据模型

### 1. `users`

直接复用 starter 现有认证表。

也就是说，`meetingroom` 不建议重新新建一个 `user` 表，而是沿用脚手架已有的：

- `users.id`
- `users.username`
- `users.password`
- `users.status`

这和 Node.js 里“不要重复创建 auth_user 和 biz_user 两套基础账号表”是一个道理。

### 2. `meeting_room`

存会议室主数据：

- 房间编码
- 名称
- 位置
- 容量
- 启用状态
- 描述

### 3. `meeting_room_feature`

存会议室和设施标签的关联关系：

- room_id
- feature_code

示例设施值：

- `projector`
- `whiteboard`
- `video_conf`
- `tv`
- `speakerphone`

### 4. `reservation`

存预约记录：

- 预约单号
- 会议室 ID
- 用户 ID
- 会议主题
- 开始时间
- 结束时间
- 状态
- 备注

## 八、为什么这里不建议使用数据库外键

按照 Alibaba P3C 常见实践，这里建议：

- 逻辑上有关联
- 物理上不加数据库外键约束

原因：

- 便于后续演进和迁移
- 避免强耦合的级联副作用
- 更符合很多 Java 企业项目的落地习惯

所以这版 DDL 采用：

- 通过索引保证查询效率
- 通过应用层保证数据一致性

## 九、推荐索引设计

### `meeting_room`

- `uk_room_code`
- `idx_capacity`
- `idx_status`

### `meeting_room_feature`

- `uk_room_feature(room_id, feature_code)`
- `idx_feature_code(feature_code)`

### `reservation`

- `uk_reservation_no`
- `idx_room_time(room_id, start_time, end_time, status)`
- `idx_user_time(user_id, start_time, end_time, status)`
- `idx_user_created(user_id, created_at)`

这些索引主要是为了服务：

- 会议室冲突检测
- 用户时间冲突检测
- 我的预约分页查询

## 十、冲突检测 SQL 草案

### 会议室冲突检测

```sql
SELECT COUNT(1)
FROM reservation
WHERE deleted = 0
  AND status = 1
  AND room_id = ?
  AND start_time < ?
  AND end_time > ?;
```

参数顺序建议：

1. `room_id`
2. `new_end_time`
3. `new_start_time`

### 用户时间冲突检测

```sql
SELECT COUNT(1)
FROM reservation
WHERE deleted = 0
  AND status = 1
  AND user_id = ?
  AND start_time < ?
  AND end_time > ?;
```

## 十一、接口草案

### 1. 查询会议室列表

```http
GET /api/meeting-rooms?capacity=10&feature=projector
```

返回字段建议：

- `id`
- `roomCode`
- `name`
- `location`
- `capacity`
- `features`
- `status`

### 2. 查询指定日期可预约信息

```http
GET /api/meeting-rooms/{id}/availability?date=2026-04-10
```

返回建议：

- `roomId`
- `date`
- `occupiedSlots`
- `availableSlots`

### 3. 创建预约

```http
POST /api/reservations
Content-Type: application/json
```

```json
{
  "roomId": 1,
  "subject": "周会",
  "startTime": "2026-04-10T14:00:00",
  "endTime": "2026-04-10T15:00:00",
  "remark": "产品需求评审"
}
```

### 4. 我的预约列表

```http
GET /api/reservations/me?pageNum=1&pageSize=10&roomId=1&startDate=2026-04-01&endDate=2026-04-30
```

## 十二、可直接落到脚手架的 DDL 文件

对应 SQL 草案已写入：

- [`../docs/sql/meetingroom-init.sql`](../docs/sql/meetingroom-init.sql)

如果你后面是从 `java-web-starter` fork 出 `meetingroom`，建议把它迁移到：

- `doc/sql/init.sql`

## 十三、下一步推荐执行顺序

现在不要急着直接生成所有代码。更合理的顺序是：

1. 先确认这版数据模型和业务规则
2. 基于 DDL 生成 Entity / Mapper / Service / Controller 骨架
3. 先实现“会议室列表 + 空闲时段”
4. 再实现“创建预约 + 双重冲突校验”
5. 最后补“我的预约列表”和测试

## 十四、AI 辅助开发实战建议

你可以直接这样给 Agent 下指令：

> 请基于 `docs/sql/meetingroom-init.sql` 生成 `meetingroom` 项目的 MyBatis-Plus 业务骨架。
> 要求：
> 1. 复用脚手架现有 `users` 认证模型；
> 2. 生成 `Entity`、`Mapper`、`Service`、`Controller`；
> 3. `Entity` 必须使用 Lombok；
> 4. 关键逻辑使用简体中文注释；
> 5. 同时生成 JUnit 5 测试骨架（AAA 模式）。

## 十五、扩展阅读

1. [`54-spring-profiles-multi-env.md`](./54-spring-profiles-multi-env.md)
2. [`55-nginx-reverse-proxy-guide.md`](./55-nginx-reverse-proxy-guide.md)
3. [`56-ssh-remote-deployment-basics.md`](./56-ssh-remote-deployment-basics.md)
