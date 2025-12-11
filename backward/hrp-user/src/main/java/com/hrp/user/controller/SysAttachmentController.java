package com.hrp.user.controller;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.SysAttachment;
import com.hrp.user.service.SysAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 附件管理控制器
 */
@RestController
@RequestMapping("/attachment")
@CrossOrigin
public class SysAttachmentController {

    @Autowired
    private SysAttachmentService sysAttachmentService;

    @GetMapping("/{id}")
    public Result<SysAttachment> getById(@PathVariable Long id) {
        SysAttachment attachment = sysAttachmentService.getById(id);
        return Result.success(attachment);
    }

    @GetMapping("/business")
    public Result<List<SysAttachment>> getByBusiness(@RequestParam String businessType, @RequestParam Long businessId) {
        List<SysAttachment> attachments = sysAttachmentService.getByBusiness(businessType, businessId);
        return Result.success(attachments);
    }

    @PostMapping
    public Result<Long> save(@RequestBody SysAttachment attachment) {
        boolean success = sysAttachmentService.save(attachment);
        if (success) {
            return Result.success(attachment.getAttachmentId());
        }
        return Result.error("保存失败");
    }

    @PostMapping("/batch")
    public Result<Void> saveBatch(@RequestBody List<SysAttachment> attachments) {
        boolean success = sysAttachmentService.saveBatch(attachments);
        return success ? Result.success() : Result.error("批量保存失败");
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysAttachment attachment) {
        boolean success = sysAttachmentService.update(attachment);
        return success ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = sysAttachmentService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    @DeleteMapping("/business")
    public Result<Void> deleteByBusiness(@RequestParam String businessType, @RequestParam Long businessId) {
        boolean success = sysAttachmentService.deleteByBusiness(businessType, businessId);
        return success ? Result.success() : Result.error("删除失败");
    }

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result<Long> upload(@RequestParam("file") MultipartFile file,
                              @RequestParam("businessType") String businessType,
                              @RequestParam(value = "businessId", required = false) Long businessId) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            // 生成文件存储路径
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + businessType;
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;
            String filePath = uploadDir + File.separator + fileName;

            // 保存文件
            file.transferTo(new File(filePath));

            // 保存附件记录
            SysAttachment attachment = new SysAttachment();
            attachment.setBusinessType(businessType);
            attachment.setBusinessId(businessId);
            attachment.setFileName(originalFilename);
            attachment.setFilePath(filePath);
            attachment.setFileSize(file.getSize());
            attachment.setFileType(fileExtension);
            attachment.setUploadTime(LocalDateTime.now());
            // TODO: 从当前登录用户获取
            attachment.setUploadUser("SYSTEM");

            boolean success = sysAttachmentService.save(attachment);
            if (success) {
                return Result.success(attachment.getAttachmentId());
            }
            return Result.error("保存附件记录失败");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
}

