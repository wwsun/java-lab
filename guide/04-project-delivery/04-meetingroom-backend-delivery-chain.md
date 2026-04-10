# 04 - Meetingroom 后端交付链路：从 DDL 到四层骨架

这一节不是继续补概念，而是把你接下来在 `java-web-starter` 里真正要落地的后端工作拆成一条可执行链路。

配合前一节 [03-meetingroom-requirements-and-ddl.md](./03-meetingroom-requirements-and-ddl.md) 与初始化脚本 [meetingroom-init.sql](../../docs/sql/meetingroom-init.sql)，目标是产出一版可直接交给 Agent 生成代码的实施方案。

如果用 Node.js / TypeScript 的交付心智类比：

| Node.js / TS 交付动作 | Java / Spring Boot 对应动作 | 本阶段你要关注什么 |
| --- | --- | --- |
| 复制后台模板项目 | fork `java-web-starter` | 不要从零搭基础设施 |
| 根据 Prisma schema 生成模块 | 根据 DDL 生成 Entity / Mapper / Service / Controller | 先定模型，再落四层 |
| `zod` + service rule + repository | `@Valid` + Service 业务规则 + Mapper 查询 | 参数校验和业务校验分层 |
| `express router + service + dao` | Controller + Service + Mapper | Controller 不写业务细节 |

## 一、先明确这次定制要复用什么

基于 [../../../java-web-starter/README.md](../../../java-web-starter/README.md)，你不需要重复建设这些能力：

- 统一返回结构 `Result`
- 全局异常处理
- Spring Security + JWT 认证
- MyBatis-Plus
- Knife4j
- Redis
- Docker Compose 基础设施

所以 `meetingroom` 项目的核心目标只有一个：

> 在已有脚手架上补齐会议室域模型和预约域模型。

## 二、建议的模块边界

不要把所有代码都塞进一个 `meetingroom` 大包里。按当前业务规模，拆成两个模块最合适：

### 1. `meetingroom` 模块

负责会议室主数据和可用性查询：

- 会议室列表
- 容量筛选
- 设施筛选
- 某天已占用时段 / 空闲时段查询

### 2. `reservation` 模块

负责预约业务闭环：

- 创建预约
- 我的预约分页列表
- 取消预约
- 双重冲突校验

### 3. 复用 `user` 模块

用户体系直接复用 starter 已有的 `user` / `auth` / `security`，不要再建一套 `meetingroom_user`。

这和 Node.js 项目里“不要重复造 auth 表”是同一个原则。

## 三、建议的包结构

脚手架当前后端包根路径是：

```text
backend/src/main/java/com/music163/starter/
```

建议新增如下目录：

```text
backend/src/main/java/com/music163/starter/module/
  meetingroom/
    controller/
    dto/
    entity/
    mapper/
    service/
    vo/
  reservation/
    controller/
    dto/
    entity/
    mapper/
    service/
    vo/
```

### 为什么不建议把 `feature` 单独拆第三个模块

因为 `meeting_room_feature` 当前只是会议室的附属信息，不是独立业务域。

所以更推荐：

- `MeetingRoomFeature` 实体放在 `meetingroom/entity`
- 相关查询放在 `meetingroom/mapper`

## 四、先从数据库表映射到 Java 类

当前 SQL 里核心表有三张：

1. `meeting_room`
2. `meeting_room_feature`
3. `reservation`

建议对应生成 3 个 Entity。

### 1. `MeetingRoom`

对应字段：

- `id`
- `roomCode`
- `name`
- `location`
- `capacity`
- `status`
- `description`
- `createdAt`
- `updatedAt`
- `deleted`

### 2. `MeetingRoomFeature`

对应字段：

- `id`
- `roomId`
- `featureCode`
- `createdAt`

### 3. `Reservation`

对应字段：

- `id`
- `reservationNo`
- `roomId`
- `userId`
- `subject`
- `startTime`
- `endTime`
- `status`
- `remark`
- `cancelledAt`
- `createdAt`
- `updatedAt`
- `deleted`

## 五、Entity 应该长什么样

参考 starter 现有 `User` 实体风格，建议 meetingroom 业务实体也遵循：

- Lombok 注解
- MyBatis-Plus 注解
- `java.time.LocalDateTime`
- `@TableLogic`

示例：

```java
package com.music163.starter.module.reservation.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会议室预约实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("reservation")
public class Reservation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String reservationNo;

    private Long roomId;

    private Long userId;

    private String subject;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 状态：1-已预约 2-已取消 3-已结束
     */
    private Integer status;

    private String remark;

    private LocalDateTime cancelledAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @Builder.Default
    private Integer deleted = 0;
}
```

## 六、DTO、VO 和 Entity 怎么分

这一块最容易因为赶进度而糊成一团。

### 1. `dto`

只放请求对象，例如：

- `MeetingRoomQueryRequest`
- `MeetingRoomAvailabilityQuery`
- `ReservationCreateRequest`
- `MyReservationQueryRequest`

### 2. `vo`

只放响应对象，例如：

- `MeetingRoomListItemVO`
- `MeetingRoomAvailabilityVO`
- `ReservationVO`

### 3. `entity`

只放数据库映射对象，不直接返回给前端。

你可以把它理解成：

- DTO = 前端传进来的 contract
- VO = 后端返回出去的 contract
- Entity = 数据库存储模型

## 七、Controller 层只做四件事

Controller 不要做冲突检测，不要写跨表查询细节。

它只做：

1. 接参
2. 参数校验
3. 调 Service
4. 包装 `Result`

建议接口划分如下。

### 1. `MeetingRoomController`

建议最少提供：

- `GET /meeting-rooms`
- `GET /meeting-rooms/{id}/availability`

### 2. `ReservationController`

建议最少提供：

- `POST /reservations`
- `GET /reservations/me`
- `PUT /reservations/{id}/cancel`（可选，但建议预留）

## 八、Service 层才是真正的业务核心

你接下来最重要的代码基本都在这里。

### `MeetingRoomService` 负责什么

- 会议室列表查询
- 设施聚合
- 某会议室某日已占用时段查询

### `ReservationService` 负责什么

- 创建预约
- 生成预约单号
- 校验预约时间是否合法
- 校验预约时长不超过 4 小时
- 校验不能预约过去时间
- 校验会议室时间冲突
- 校验用户时间冲突
- 保存预约记录

### 关键原则

所有“规则判断”都应该沉到 Service 层，而不是 Controller 层。

这和你在 Node.js 里把业务逻辑放到 service，而不是 Express route handler 里，是完全一样的。

## 九、Mapper 层需要哪些查询

MyBatis-Plus 的 `BaseMapper` 只能解决基础 CRUD，业务查询还需要你补方法。

### `MeetingRoomMapper`

至少需要考虑：

- 按容量分页 / 列表查询
- 按设施筛选会议室
- 查询会议室基础信息

### `ReservationMapper`

至少需要考虑：

- 查询某会议室某时间段是否冲突
- 查询某用户某时间段是否冲突
- 查询当前用户预约分页列表
- 查询某会议室某天全部有效预约

### 冲突检测 SQL 核心条件

推荐统一使用这一组条件：

```sql
start_time < #{endTime}
AND end_time > #{startTime}
AND status = 1
AND deleted = 0
```

然后分别按：

- `room_id = #{roomId}`
- `user_id = #{userId}`

做两次查询。

## 十、创建预约的标准执行顺序

这个顺序建议固定下来，后面无论自己写还是让 Agent 写，都按这个来。

1. 从 `SecurityContext` 获取当前用户
2. 校验 DTO 基本字段
3. 校验开始时间 < 结束时间
4. 校验开始时间不能早于当前时间
5. 校验预约时长 <= 4 小时
6. 校验会议室存在且状态可用
7. 校验会议室时间冲突
8. 校验用户时间冲突
9. 生成 `reservationNo`
10. 保存预约记录
11. 返回 `ReservationVO`

其中第 7 步和第 8 步都必须放在同一个 Service 方法里，后续再考虑是否加事务和并发保护。

## 十一、建议优先生成哪些文件

不要一上来让 Agent 生成全部业务代码。更稳妥的顺序是：

### 第一批：数据骨架

- `MeetingRoom.java`
- `MeetingRoomFeature.java`
- `Reservation.java`
- `MeetingRoomMapper.java`
- `ReservationMapper.java`

### 第二批：请求 / 响应模型

- `ReservationCreateRequest.java`
- `MeetingRoomQueryRequest.java`
- `MeetingRoomListItemVO.java`
- `MeetingRoomAvailabilityVO.java`
- `ReservationVO.java`

### 第三批：Service 接口

- `MeetingRoomService.java`
- `ReservationService.java`

### 第四批：Controller

- `MeetingRoomController.java`
- `ReservationController.java`

### 第五批：测试骨架

- `ReservationServiceTest.java`
- `MeetingRoomControllerTest.java`

这个顺序的好处是：

- 先把数据边界定住
- 再补接口契约
- 最后才落业务编排

## 十二、测试最少要覆盖什么

week 4 阶段不要只写“能跑通 happy path”的测试。

至少覆盖这些场景：

1. 创建预约成功
2. 会议室时间冲突，创建失败
3. 用户时间冲突，创建失败
4. 预约时长超过 4 小时，创建失败
5. 预约过去时间，创建失败

JUnit 5 测试骨架建议保持 AAA 模式：

```java
@Test
void shouldThrowWhenRoomTimeConflicts() {
    // Arrange: 构造请求和已有预约数据

    // Act: 调用 createReservation

    // Assert: 断言抛出业务异常
}
```

## 十三、给 Agent 的高质量指令模板

当你准备开始真正生成代码时，建议像这样下指令：

> 基于 `meetingroom-init.sql`，在 `java-web-starter/backend` 中生成会议室预约模块骨架代码，要求：
> 1. 包路径遵循 `com.music163.starter.module.meetingroom` 和 `com.music163.starter.module.reservation`；
> 2. Entity 使用 Lombok + MyBatis-Plus 注解；
> 3. DTO / VO / Entity 分离，不要直接返回 Entity；
> 4. Controller 只负责接参和返回 `Result`；
> 5. 冲突检测、时长校验、过去时间校验放在 Service 层；
> 6. 同时生成 JUnit 5 测试骨架，遵循 AAA 模式；
> 7. 注释使用简体中文，遵循 P3C。

## 十四、你接下来真正要做的事

按交付顺序，下一步不是继续写文档，而是开始动 `java-web-starter`：

1. fork / clone 脚手架为 `meetingroom`
2. 将初始化 SQL 落到新项目的 `doc/sql/init.sql`
3. 先生成 3 个 Entity + 2 个 Mapper
4. 再生成请求 / 响应对象
5. 最后生成 Service / Controller / 测试骨架

如果你愿意继续，我下一步可以直接帮你在 `java-web-starter` 里生成第一批会议室模块骨架代码。
