package com.hrp.auth.controller;

import com.hrp.auth.service.ProcessDefinitionService;
import com.hrp.common.entity.ProcessDefinition;
import com.hrp.common.entity.Result;
import com.hrp.common.util.JwtUtil;
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
    public Result<ProcessDefinition> getById(@PathVariable("id") Long id) {
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
    public Result<ProcessDefinition> getByKey(@PathVariable("key") String key) {
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
    public Result<List<ProcessDefinition>> getByType(@PathVariable("type") String type, @RequestParam(value = "isActive", required = false) Long isActive) {
        List<ProcessDefinition> list = processDefinitionService.getByType(type, isActive);
        return Result.success(list);
    }

    /**
     * 查询所有流程定义
     */
    @GetMapping("/list")
    public Result<List<ProcessDefinition>> getAll(@RequestParam(value = "isActive", required = false) Long isActive) {
        List<ProcessDefinition> list = processDefinitionService.getAll(isActive);
        return Result.success(list);
    }

    /**
     * 分页查询所有流程定义
     */
    @GetMapping("/page")
    public Result<com.hrp.common.entity.PageResult<ProcessDefinition>> getAllPage(
            @RequestParam(value = "isActive", required = false) Long isActive,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<ProcessDefinition> pageResult = processDefinitionService.getAllPage(isActive, page, size);
        return Result.success(pageResult);
    }

    /**
     * 根据类型分页查询流程定义
     */
    @GetMapping("/type/{type}/page")
    public Result<com.hrp.common.entity.PageResult<ProcessDefinition>> getByTypePage(
            @PathVariable("type") String type,
            @RequestParam(value = "isActive", required = false) Long isActive,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<ProcessDefinition> pageResult = processDefinitionService.getByTypePage(type, isActive, page, size);
        return Result.success(pageResult);
    }

    /**
     * 新增流程定义
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody ProcessDefinition definition,
                                @RequestHeader(value = "Authorization", required = false) String token) {
        // 从token中获取当前用户账号作为createUser
        String createUser = getCurrentUserAccount(token);
        if (createUser != null && !createUser.isEmpty()) {
            definition.setCreateUser(createUser);
        }
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
    public Result<String> delete(@PathVariable("id") Long id) {
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
    public Result<String> toggleActive(@PathVariable("id") Long id, @RequestParam(value = "isActive") Integer isActive) {
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
    public ResponseEntity<byte[]> exportDefinition(@PathVariable("id") Long id) {
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
            // 文件扩展名使用.bpmn，但内容是JSON格式
            headers.setContentDispositionFormData("attachment", 
                "process_" + definition.getDefinitionKey() + ".bpmn");
            
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
    public Result<String> importDefinition(@RequestBody ImportDefinitionRequest request,
                                            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 从token中获取当前用户账号作为createUser
            String createUser = getCurrentUserAccount(token);
            
            ProcessDefinition definition = processDefinitionService.importDefinition(
                request.getJsonContent(),
                request.getDefinitionKey(),
                request.getDefinitionName(),
                request.getDefinitionType(),
                createUser
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

    /**
     * 获取流程定义节点信息
     */
    @GetMapping("/{id}/nodes")
    public Result<List<com.hrp.common.entity.ProcessNodeInfo>> getProcessNodes(@PathVariable("id") Long id) {
        System.out.println("=== 获取流程节点信息 ===");
        System.out.println("流程定义ID: " + id);
        try {
            List<com.hrp.common.entity.ProcessNodeInfo> nodes = processDefinitionService.getProcessNodes(id);
            System.out.println("解析到的节点数量: " + (nodes != null ? nodes.size() : 0));
            if (nodes != null && !nodes.isEmpty()) {
                System.out.println("节点列表:");
                for (com.hrp.common.entity.ProcessNodeInfo node : nodes) {
                    System.out.println("  - ID: " + node.getId() + ", Name: " + node.getName() + ", Type: " + node.getType());
                }
            } else {
                System.out.println("警告：未找到任何流程节点");
            }
            System.out.println("=== 获取流程节点信息完成 ===");
            return Result.success(nodes != null ? nodes : new java.util.ArrayList<>());
        } catch (Exception e) {
            System.err.println("获取流程节点失败: " + e.getMessage());
            e.printStackTrace();
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = e.getClass().getSimpleName();
            }
            return Result.error("获取流程节点失败：" + errorMsg);
        }
    }
    
    /**
     * 获取流程定义节点信息（根据业务数据动态获取审批人）
     */
    @GetMapping("/{id}/nodes/business")
    public Result<List<com.hrp.common.entity.ProcessNodeInfo>> getProcessNodesWithBusiness(
            @PathVariable("id") Long id,
            @RequestParam(value = "applyNo") String applyNo) {
        try {
            List<com.hrp.common.entity.ProcessNodeInfo> nodes = processDefinitionService.getProcessNodesWithBusiness(id, applyNo);
            return Result.success(nodes != null ? nodes : new java.util.ArrayList<>());
        } catch (Exception e) {
            System.err.println("获取流程节点失败: " + e.getMessage());
            e.printStackTrace();
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = "未知错误";
            }
            return Result.error("获取流程节点失败：" + errorMsg);
        }
    }

    /**
     * 从token中获取当前用户账号
     */
    private String getCurrentUserAccount(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // 解析token获取账号
            String account = JwtUtil.getAccount(token);
            return account;
        } catch (Exception e) {
            return null;
        }
    }
}



