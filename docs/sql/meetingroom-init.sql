-- ============================================
-- Database Initialization Script
-- meetingroom
-- 说明：
-- 1. 默认复用 starter 现有 users 表，不重复创建用户表
-- 2. 按 P3C 建议，本脚本不使用数据库外键约束
-- ============================================

CREATE DATABASE IF NOT EXISTS `meetingroom_db`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `meetingroom_db`;

-- -------------------------------------------
-- Table: meeting_room
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `meeting_room` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `room_code`    VARCHAR(32)  NOT NULL                COMMENT '会议室编码',
    `name`         VARCHAR(64)  NOT NULL                COMMENT '会议室名称',
    `location`     VARCHAR(128) NOT NULL                COMMENT '位置描述',
    `capacity`     INT          NOT NULL                COMMENT '容纳人数',
    `status`       TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：0-停用 1-可用',
    `description`  VARCHAR(255) NULL                    COMMENT '会议室描述',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_room_code` (`room_code`),
    INDEX `idx_capacity` (`capacity`),
    INDEX `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '会议室表';

-- -------------------------------------------
-- Table: meeting_room_feature
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `meeting_room_feature` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `room_id`      BIGINT      NOT NULL                COMMENT '会议室ID',
    `feature_code` VARCHAR(32) NOT NULL                COMMENT '设施编码',
    `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_room_feature` (`room_id`, `feature_code`),
    INDEX `idx_feature_code` (`feature_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '会议室设施关联表';

-- -------------------------------------------
-- Table: reservation
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `reservation` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `reservation_no`   VARCHAR(32)  NOT NULL                COMMENT '预约单号',
    `room_id`          BIGINT       NOT NULL                COMMENT '会议室ID',
    `user_id`          BIGINT       NOT NULL                COMMENT '预约人ID，对应 users.id',
    `subject`          VARCHAR(128) NOT NULL                COMMENT '会议主题',
    `start_time`       DATETIME     NOT NULL                COMMENT '开始时间',
    `end_time`         DATETIME     NOT NULL                COMMENT '结束时间',
    `status`           TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1-已预约 2-已取消 3-已结束',
    `remark`           VARCHAR(255) NULL                    COMMENT '备注',
    `cancelled_at`     DATETIME     NULL                    COMMENT '取消时间',
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_reservation_no` (`reservation_no`),
    INDEX `idx_room_time` (`room_id`, `start_time`, `end_time`, `status`),
    INDEX `idx_user_time` (`user_id`, `start_time`, `end_time`, `status`),
    INDEX `idx_user_created` (`user_id`, `created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '会议室预约表';

-- -------------------------------------------
-- Seed data: meeting rooms
-- -------------------------------------------
INSERT INTO `meeting_room` (`room_code`, `name`, `location`, `capacity`, `status`, `description`)
VALUES ('A101', '晨光会议室', 'A座 10层 101', 6, 1, '适合小型站会与面试'),
       ('A102', '远航会议室', 'A座 10层 102', 12, 1, '适合周会与需求评审'),
       ('B201', '星海会议室', 'B座 20层 201', 20, 1, '适合跨部门大型会议');

-- -------------------------------------------
-- Seed data: meeting room features
-- -------------------------------------------
INSERT INTO `meeting_room_feature` (`room_id`, `feature_code`)
SELECT mr.id, feature_code
FROM (
         SELECT 'A101' AS room_code, 'whiteboard' AS feature_code
         UNION ALL
         SELECT 'A101', 'tv'
         UNION ALL
         SELECT 'A102', 'projector'
         UNION ALL
         SELECT 'A102', 'whiteboard'
         UNION ALL
         SELECT 'A102', 'video_conf'
         UNION ALL
         SELECT 'B201', 'projector'
         UNION ALL
         SELECT 'B201', 'video_conf'
         UNION ALL
         SELECT 'B201', 'speakerphone'
     ) seed
         INNER JOIN meeting_room mr ON mr.room_code = seed.room_code
ON DUPLICATE KEY UPDATE feature_code = VALUES(feature_code);
