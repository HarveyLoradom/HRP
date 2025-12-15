package com.hrp.auth.controller;

import com.hrp.auth.service.ProcessDefinitionService;
import com.hrp.common.entity.ProcessDefinition;
import com.hrp.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程定义管理控制器
 */
@RestController
@RequestMapping("/auth/process-definition")
@CrossOrigin
public class ProcessDefinitionController {

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    /**
     * 根据ID查询流程定义
     */
    @GetMapping("/{id}")
    public Result<ProcessDefinition> getById(@PathVariable Long id) {
        ProcessDefinition definition = processDefinitionService.getById(id);
        if (definition != null) {
            return Result.success(definition);
        }
        return Result.error("流程定义不存在");
    }

    /**
     * 根据KEY查询流程定义
     */
    @GetMapping("/key/{key}")
    public Result<ProcessDefinition> getByKey(@PathVariable String key) {
        ProcessDefinition definition = processDefinitionService.getByKey(key);
        if (definition != null) {
            return Result.success(definition);
        }
        return Result.error("流程定义不存在");
    }

    /**
     * 根据类型查询流程定义列表
     */
    @GetMapping("/type/{type}")
    public Result<List<ProcessDefinition>> getByType(@PathVariable String type) {
        List<ProcessDefinition> list = processDefinitionService.getByType(type);
        return Result.success(list);
    }

    /**
     * 查询所有流程定义
     */
    @GetMapping("/list")
    public Result<List<ProcessDefinition>> getAll() {
        List<ProcessDefinition> list = processDefinitionService.getAll();
        return Result.success(list);
    }

    /**
     * 分页查询所有流程定义
     */
    @GetMapping("/page")
    public Result<com.hrp.common.entity.PageResult<ProcessDefinition>> getAllPage(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<ProcessDefinition> pageResult = processDefinitionService.getAllPage(page, size);
        return Result.success(pageResult);
    }

    /**
     * 根据类型分页查询流程定义
     */
    @GetMapping("/type/{type}/page")
    public Result<com.hrp.common.entity.PageResult<ProcessDefinition>> getByTypePage(
            @PathVariable String type,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<ProcessDefinition> pageResult = processDefinitionService.getByTypePage(type, page, size);
        return Result.success(pageResult);
    }

    /**
     * 新增流程定义
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody ProcessDefinition definition) {
        boolean success = processDefinitionService.save(definition);
        if (success) {
            return Result.success("新增成功");
        }
        return Result.error("新增失败");
    }

    /**
     * 更新流程定义
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody ProcessDefinition definition) {
        if (definition.getDefinitionId() == null) {
            return Result.error("流程定义ID不能为空");
        }
        boolean success = processDefinitionService.update(definition);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除流程定义
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = processDefinitionService.delete(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 启用/停用流程定义
     */
    @PutMapping("/toggle-active/{id}")
    public Result<String> toggleActive(@PathVariable Long id, @RequestParam Integer isActive) {
        ProcessDefinition definition = processDefinitionService.getById(id);
        if (definition == null) {
            return Result.error("流程定义不存在");
        }
        definition.setIsActive(isActive.longValue());
        boolean success = processDefinitionService.update(definition);
        if (success) {
            return Result.success(isActive == 1 ? "启用成功" : "停用成功");
        }
        return Result.error("操作失败");
    }

    /**
     * 导出流程定义
     */
    @GetMapping("/export/{id}")
    public ResponseEntity<byte[]> exportDefinition(@PathVariable Long id) {
        try {
            ProcessDefinition definition = processDefinitionService.getById(id);
            if (definition == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 将流程定义转换为JSON格式用于导出
            String jsonContent = processDefinitionService.exportDefinition(definition);
            byte[] content = jsonContent.getBytes("UTF-8");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", 
                "process_" + definition.getDefinitionKey() + ".json");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(content);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 导入流程定义
     */
    @PostMapping("/import")
    public Result<String> importDefinition(@RequestBody ImportDefinitionRequest request) {
        try {
            ProcessDefinition definition = processDefinitionService.importDefinition(
                request.getJsonContent(),
                request.getDefinitionKey(),
                request.getDefinitionName(),
                request.getDefinitionType()
            );
            if (definition != null) {
                return Result.success("导入成功，流程定义ID: " + definition.getDefinitionId());
            }
            return Result.error("导入失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 导入流程定义请求对象
     */
    public static class ImportDefinitionRequest {
        private String jsonContent;
        private String definitionKey;
        private String definitionName;
        private String definitionType;

        public String getJsonContent() { return jsonContent; }
        public void setJsonContent(String jsonContent) { this.jsonContent = jsonContent; }
        public String getDefinitionKey() { return definitionKey; }
        public void setDefinitionKey(String definitionKey) { this.definitionKey = definitionKey; }
        public String getDefinitionName() { return definitionName; }
        public void setDefinitionName(String definitionName) { this.definitionName = definitionName; }
        public String getDefinitionType() { return definitionType; }
        public void setDefinitionType(String definitionType) { this.definitionType = definitionType; }
    }
}



