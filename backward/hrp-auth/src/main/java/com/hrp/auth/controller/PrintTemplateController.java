package com.hrp.auth.controller;

import com.hrp.common.entity.PrintTemplate;
import com.hrp.common.entity.Result;
import com.hrp.common.dto.TableFieldInfo;
import com.hrp.auth.service.PrintTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.util.List;

/**
 * 打印模板管理控制器
 */
@RestController
@RequestMapping("/auth/print-template")
@CrossOrigin
public class PrintTemplateController {

    @Autowired
    private PrintTemplateService printTemplateService;

    /**
     * 根据ID查询打印模板
     */
    @GetMapping("/{id}")
    public Result<PrintTemplate> getById(@PathVariable Long id) {
        PrintTemplate template = printTemplateService.getById(id);
        if (template != null) {
            return Result.success(template);
        }
        return Result.error("打印模板不存在");
    }

    /**
     * 根据编码查询打印模板
     */
    @GetMapping("/code/{code}")
    public Result<PrintTemplate> getByCode(@PathVariable String code) {
        PrintTemplate template = printTemplateService.getByCode(code);
        if (template != null) {
            return Result.success(template);
        }
        return Result.error("打印模板不存在");
    }

    /**
     * 根据类型查询打印模板列表
     */
    @GetMapping("/type/{type}")
    public Result<List<PrintTemplate>> getByType(@PathVariable String type) {
        List<PrintTemplate> list = printTemplateService.getByType(type);
        return Result.success(list);
    }

    /**
     * 查询所有打印模板
     */
    @GetMapping("/list")
    public Result<List<PrintTemplate>> getAll() {
        List<PrintTemplate> list = printTemplateService.getAll();
        return Result.success(list);
    }

    /**
     * 获取默认模板
     */
    @GetMapping("/default/{type}")
    public Result<PrintTemplate> getDefaultByType(@PathVariable String type) {
        PrintTemplate template = printTemplateService.getDefaultByType(type);
        if (template != null) {
            return Result.success(template);
        }
        return Result.error("未找到默认模板");
    }

    /**
     * 新增打印模板
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody PrintTemplate template) {
        // 检查编码是否已存在
        PrintTemplate existTemplate = printTemplateService.getByCode(template.getTemplateCode());
        if (existTemplate != null) {
            return Result.error("模板编码已存在");
        }
        boolean success = printTemplateService.save(template);
        if (success) {
            return Result.success("新增成功");
        }
        return Result.error("新增失败");
    }

    /**
     * 更新打印模板
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody PrintTemplate template) {
        if (template.getTemplateId() == null) {
            return Result.error("模板ID不能为空");
        }
        boolean success = printTemplateService.update(template);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除打印模板
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = printTemplateService.delete(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 获取数据库字段列表（根据模板类型）
     */
    @GetMapping("/fields/{templateType}")
    public Result<List<TableFieldInfo>> getTableFields(@PathVariable String templateType) {
        try {
            List<TableFieldInfo> fields = printTemplateService.getTableFields(templateType);
            return Result.success(fields);
        } catch (Exception e) {
            return Result.error("获取字段列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取数据库中所有表名列表
     */
    @GetMapping("/tables")
    public Result<List<String>> getAllTableNames() {
        try {
            List<String> tableNames = printTemplateService.getAllTableNames();
            return Result.success(tableNames);
        } catch (Exception e) {
            return Result.error("获取表列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据表名获取数据库字段列表
     */
    @GetMapping("/table-fields/{tableName}")
    public Result<List<TableFieldInfo>> getTableFieldsByTableName(@PathVariable String tableName) {
        try {
            // URL解码表名
            String decodedTableName = java.net.URLDecoder.decode(tableName, "UTF-8");
            List<TableFieldInfo> fields = printTemplateService.getTableFieldsByTableName(decodedTableName);
            return Result.success(fields);
        } catch (Exception e) {
            return Result.error("获取字段列表失败：" + e.getMessage());
        }
    }

    /**
     * 生成打印内容（返回HTML）
     */
    @PostMapping("/generate")
    public Result<String> generatePrintContent(@RequestBody GeneratePrintRequest request) {
        try {
            String html = printTemplateService.generatePrintContent(
                request.getTemplateId(),
                request.getBusinessKey(),
                request.getTemplateType()
            );
            return Result.success(html);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "生成打印内容失败：" + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += "，原因：" + e.getCause().getMessage();
            }
            return Result.error(errorMessage);
        }
    }

    /**
     * 预览打印模板（返回HTML）
     */
    @PostMapping("/preview")
    public Result<String> previewPrintTemplate(@RequestBody GeneratePrintRequest request) {
        try {
            String html = printTemplateService.previewPrintTemplate(
                request.getTemplateId(),
                request.getBusinessKey(),
                request.getTemplateType()
            );
            return Result.success(html);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "预览失败：" + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += "，原因：" + e.getCause().getMessage();
            }
            return Result.error(errorMessage);
        }
    }

    /**
     * 启用/停用模板
     */
    @PutMapping("/toggle-active/{id}")
    public Result<String> toggleActive(@PathVariable Long id, @RequestParam Integer isActive) {
        PrintTemplate template = printTemplateService.getById(id);
        if (template == null) {
            return Result.error("模板不存在");
        }
        template.setIsActive(isActive);
        boolean success = printTemplateService.update(template);
        if (success) {
            return Result.success(isActive == 1 ? "启用成功" : "停用成功");
        }
        return Result.error("操作失败");
    }

    /**
     * 导出模板
     */
    @GetMapping("/export/{id}")
    public ResponseEntity<byte[]> exportTemplate(@PathVariable Long id) {
        try {
            PrintTemplate template = printTemplateService.getById(id);
            if (template == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 将模板转换为JSON格式用于导出
            String jsonContent = printTemplateService.exportTemplate(template);
            byte[] content = jsonContent.getBytes("UTF-8");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", 
                "template_" + template.getTemplateCode() + ".json");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(content);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 导入模板
     */
    @PostMapping("/import")
    public Result<String> importTemplate(@RequestBody ImportTemplateRequest request) {
        try {
            PrintTemplate template = printTemplateService.importTemplate(
                request.getJsonContent(),
                request.getTemplateCode(),
                request.getTemplateName(),
                request.getTemplateType()
            );
            if (template != null) {
                return Result.success("导入成功，模板ID: " + template.getTemplateId());
            }
            return Result.error("导入失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 生成报表请求对象
     */
    public static class GeneratePrintRequest {
        private Long templateId;
        private String businessKey;
        private String templateType;

        public Long getTemplateId() { return templateId; }
        public void setTemplateId(Long templateId) { this.templateId = templateId; }
        public String getBusinessKey() { return businessKey; }
        public void setBusinessKey(String businessKey) { this.businessKey = businessKey; }
        public String getTemplateType() { return templateType; }
        public void setTemplateType(String templateType) { this.templateType = templateType; }
    }

    /**
     * 导入模板请求对象
     */
    public static class ImportTemplateRequest {
        private String jsonContent;
        private String templateCode;
        private String templateName;
        private String templateType;

        public String getJsonContent() { return jsonContent; }
        public void setJsonContent(String jsonContent) { this.jsonContent = jsonContent; }
        public String getTemplateCode() { return templateCode; }
        public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        public String getTemplateType() { return templateType; }
        public void setTemplateType(String templateType) { this.templateType = templateType; }
    }

}

