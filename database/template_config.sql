-- 模板设置表
CREATE TABLE IF NOT EXISTS `sys_template_config` (
  `config_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
  `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型（从sys_code获取，如：PAYOUT_TYPE、APPLY_TYPE等）',
  `business_type_value` VARCHAR(50) NOT NULL COMMENT '业务类型值（如：专项资金、差旅费等）',
  `business_type_name` VARCHAR(100) NOT NULL COMMENT '业务类型名称',
  `print_template_id` BIGINT COMMENT '打印模板ID',
  `print_template_name` VARCHAR(100) COMMENT '打印模板名称',
  `process_definition_id` BIGINT COMMENT '流程定义ID',
  `process_definition_name` VARCHAR(100) COMMENT '流程定义名称',
  `is_active` INT DEFAULT 1 COMMENT '是否启用：0-否，1-是',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_business_type_value` (`business_type`, `business_type_value`),
  KEY `idx_business_type` (`business_type`),
  KEY `idx_print_template_id` (`print_template_id`),
  KEY `idx_process_definition_id` (`process_definition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板设置表';

