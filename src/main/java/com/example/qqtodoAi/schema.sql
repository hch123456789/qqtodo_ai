-- =============================================
-- QQ Todo 助手 - 数据库初始化脚本
-- 使用方法：mysql -u root -p < schema.sql
-- 本脚本不含任何个人信息，群和联系人请在网页或下方自行添加
-- =============================================

CREATE DATABASE IF NOT EXISTS qq_todo DEFAULT CHARACTER SET utf8mb4;
USE qq_todo;

-- 群信息表（白名单群）
CREATE TABLE IF NOT EXISTS group_info (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          group_id BIGINT NOT NULL UNIQUE COMMENT 'QQ群号',
                                          group_name VARCHAR(100) COMMENT '群名称',
    description VARCHAR(255) COMMENT '备注',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用(1=启用)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 联系人表（标记人）
CREATE TABLE IF NOT EXISTS contact_info (
                                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                            qq BIGINT NOT NULL COMMENT '联系人QQ号',
                                            name VARCHAR(100) COMMENT '姓名',
    role VARCHAR(50) COMMENT '角色:老师/辅导员/班委/同学',
    group_id BIGINT COMMENT '所属群号,NULL表示所有群生效',
    is_marked TINYINT DEFAULT 0 COMMENT '是否标记(1=是)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 消息表
CREATE TABLE IF NOT EXISTS message (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       message_id BIGINT COMMENT 'QQ消息ID',
                                       group_id BIGINT COMMENT '群号',
                                       sender_qq BIGINT COMMENT '发送者QQ',
                                       content LONGTEXT COMMENT '消息文本内容',
                                       msg_time DATETIME COMMENT '消息时间',
                                       raw_json LONGTEXT COMMENT '原始JSON',
                                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                       INDEX idx_message_group (group_id),
    INDEX idx_message_msg_time (msg_time)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 待办事项表
CREATE TABLE IF NOT EXISTS todo_item (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         task LONGTEXT COMMENT '任务内容',
                                         time DATETIME COMMENT '截止时间',
                                         location VARCHAR(255) COMMENT '地点',
    source VARCHAR(255) COMMENT '来源',
    priority VARCHAR(10) COMMENT '优先级:高/中/低',
    status TINYINT DEFAULT 0 COMMENT '0未完成 1已完成',
    message_id BIGINT COMMENT '关联消息ID',
    confirmed TINYINT DEFAULT 0 COMMENT '是否已确认',
    rejected TINYINT DEFAULT 0 COMMENT '是否被拒绝',
    reminded TINYINT DEFAULT 0 COMMENT '是否已提醒',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_todo_rejected_status (rejected, status, time, reminded)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文件信息表
CREATE TABLE IF NOT EXISTS file_info (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         message_id BIGINT COMMENT '关联消息ID',
                                         group_id BIGINT COMMENT '群号',
                                         sender_qq BIGINT COMMENT '发送者QQ',
                                         file_name VARCHAR(255) COMMENT '文件名',
    file_path VARCHAR(500) COMMENT '本地存储路径',
    file_type VARCHAR(20) COMMENT '文件类型',
    summary LONGTEXT COMMENT 'AI摘要',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_file_info_group (group_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 反馈表（AI 学习的数据来源）
CREATE TABLE IF NOT EXISTS feedback (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        message_id BIGINT COMMENT '关联消息',
                                        todo_item_id BIGINT COMMENT '关联todo',
                                        file_id BIGINT COMMENT '关联文件',
                                        ai_relevant TINYINT COMMENT 'AI当时判断',
                                        ai_result TEXT COMMENT 'AI当时的判断结果',
                                        user_feedback VARCHAR(20) COMMENT 'confirm/reject/correct/file',
    relevance_score INT COMMENT '不相关程度 0~100',
    user_note TEXT COMMENT '用户备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_feedback_user_feedback (user_feedback)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户画像表（身份 + 学习到的规则）
-- 基础身份在网页 /profile 填写，无需在此插入
CREATE TABLE IF NOT EXISTS user_profile (
                                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                            profile_key VARCHAR(50) UNIQUE COMMENT 'base_identity / learned_rules',
    profile_value TEXT COMMENT '内容',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 以下为示例数据，请按需修改后使用
-- 也可以在部署后通过网页（群管理/联系人）添加
-- =============================================

-- 示例：添加一个群到白名单（改成你的群号）
-- INSERT INTO group_info (group_id, group_name, description, enabled)
-- VALUES (123456789, '我的班级群', '', 1);

-- 示例：标记一个联系人（group_id 为 NULL 表示所有群生效）
-- INSERT INTO contact_info (qq, name, role, group_id, is_marked)
-- VALUES (987654321, '某某老师', '老师', NULL, 1);