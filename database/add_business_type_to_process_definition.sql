-- 为流程定义表添加业务类型字段
ALTER TABLE `wf_process_definition` 
ADD COLUMN `business_type` VARCHAR(50) NULL COMMENT '业务类型值（如：MEAL-餐费，TRAVEL-差旅费等）' 
AFTER `definition_type`;

