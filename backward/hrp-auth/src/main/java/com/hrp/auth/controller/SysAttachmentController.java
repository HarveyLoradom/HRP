package com.hrp.auth.controller;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.SysAttachment;
import com.hrp.common.exception.BusinessException;
import com.hrp.auth.service.SysAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 附件管理控制器
 */
@RestController
@RequestMapping("/auth/attachment")
@CrossOrigin
public class SysAttachmentController {

    @Autowired
    private SysAttachmentService sysAttachmentService;

    @Value("${file.upload.base-path:${user.dir}/uploads}")
    private String basePath;

    @Value("${file.upload.attachment-path:attachments}")
    private String attachmentPath;

    @Value("${file.upload.photo-path:employee-photos}")
    private String photoPath;

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
        if (!success) {
            throw new BusinessException("保存失败");
        }
        return Result.success(attachment.getAttachmentId());
    }

    @PostMapping("/batch")
    public Result<Void> saveBatch(@RequestBody List<SysAttachment> attachments) {
        boolean success = sysAttachmentService.saveBatch(attachments);
        if (!success) {
            throw new BusinessException("批量保存失败");
        }
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysAttachment attachment) {
        boolean success = sysAttachmentService.update(attachment);
        if (!success) {
            throw new BusinessException("更新失败");
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 先获取附件信息，以便删除文件
        SysAttachment attachment = sysAttachmentService.getById(id);
        if (attachment == null) {
            throw new BusinessException("附件不存在");
        }
        
        // 删除数据库记录
        boolean success = sysAttachmentService.delete(id);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        
        // 删除文件
        try {
            String filePath = attachment.getFilePath();
            if (filePath != null && !filePath.trim().isEmpty()) {
                File file = new File(filePath);
                if (file.exists() && file.isFile()) {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        // 文件删除失败，但不影响数据库删除，只记录日志
                        System.err.println("警告：删除附件文件失败：" + filePath);
                    }
                }
            }
        } catch (Exception e) {
            // 文件删除失败，但不影响数据库删除，只记录日志
            System.err.println("警告：删除附件文件时出错：" + e.getMessage());
        }
        
        return Result.success();
    }

    @DeleteMapping("/business")
    public Result<Void> deleteByBusiness(@RequestParam String businessType, @RequestParam Long businessId) {
        boolean success = sysAttachmentService.deleteByBusiness(businessType, businessId);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success();
    }

    /**
     * 批量更新附件的 businessId（用于新增业务记录保存后更新附件）
     */
    @PutMapping("/update-business-id")
    public Result<Void> updateBusinessId(@RequestParam String businessType,
                                         @RequestParam Long businessId,
                                         @RequestParam(required = false) String attachmentIdsStr) {
        List<Long> attachmentIds = null;
        if (attachmentIdsStr != null && !attachmentIdsStr.trim().isEmpty()) {
            try {
                attachmentIds = Arrays.stream(attachmentIdsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                throw new BusinessException(400, "附件ID格式错误");
            }
        }
        boolean success = sysAttachmentService.updateBusinessIdBatch(businessType, businessId, attachmentIds);
        if (!success) {
            throw new BusinessException("更新附件业务ID失败");
        }
        return Result.success();
    }

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result<Long> upload(@RequestParam("file") MultipartFile file,
                              @RequestParam("businessType") String businessType,
                              @RequestParam(value = "businessId", required = false) String businessIdStr) {
        Long businessId = null;
        if (businessIdStr != null && !businessIdStr.trim().isEmpty() 
                && !"null".equalsIgnoreCase(businessIdStr) 
                && !"undefined".equalsIgnoreCase(businessIdStr)
                && !"temp".equalsIgnoreCase(businessIdStr)) {
            try {
                businessId = Long.parseLong(businessIdStr.trim());
            } catch (NumberFormatException e) {
                // 如果转换失败，businessId 保持为 null，这是正常的（新增时可能没有businessId）
            }
        }
        if (file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        try {
            // 使用配置的路径生成文件存储路径
            String uploadDir = Paths.get(normalizePath(basePath), attachmentPath, businessType).toString();
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

            // 保存附件记录（允许businessId为null，用于临时附件）
            SysAttachment attachment = new SysAttachment();
            attachment.setBusinessType(businessType);
            attachment.setBusinessId(businessId); // 可以为null
            attachment.setFileName(originalFilename);
            attachment.setFilePath(filePath);
            attachment.setFileSize(file.getSize());
            attachment.setFileType(fileExtension);
            attachment.setUploadTime(LocalDateTime.now());
            // TODO: 从当前登录用户获取
            attachment.setUploadUser("SYSTEM");

            boolean success = sysAttachmentService.save(attachment);
            if (!success) {
                throw new BusinessException("保存附件记录失败");
            }
            return Result.success(attachment.getAttachmentId());
        } catch (IOException e) {
            throw new BusinessException("文件上传失败：" + e.getMessage(), e);
        }
    }

    /**
     * 员工照片上传（返回文件路径，用于保存到sys_emp表）
     */
    @PostMapping("/upload-photo")
    public Result<String> uploadPhoto(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!fileExtension.matches("\\.(jpg|jpeg|png|gif|bmp)")) {
                throw new BusinessException(400, "只支持图片格式：jpg, jpeg, png, gif, bmp");
            }
        }

        try {
            // 使用配置的路径生成文件存储路径
            String uploadDir = Paths.get(normalizePath(basePath), photoPath).toString();
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;
            String filePath = Paths.get(uploadDir, fileName).toString();

            // 保存文件
            file.transferTo(new File(filePath));

            // 返回相对路径（便于前端访问），格式：photo-path/fileName
            String relativePath = photoPath + "/" + fileName;
            return Result.success(relativePath);
        } catch (IOException e) {
            throw new BusinessException("照片上传失败：" + e.getMessage(), e);
        }
    }

    /**
     * 删除照片文件
     * @param photoPath 照片相对路径，格式：employee-photos/xxx.jpg
     */
    @DeleteMapping("/delete-photo")
    public Result<Void> deletePhoto(@RequestParam String photoPath) {
        if (photoPath == null || photoPath.trim().isEmpty()) {
            throw new BusinessException(400, "照片路径不能为空");
        }

        try {
            // 构建完整文件路径
            String filePath = Paths.get(normalizePath(basePath), photoPath).toString();
            File file = new File(filePath);
            
            if (file.exists() && file.isFile()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    throw new BusinessException("删除照片文件失败");
                }
            } else {
                // 文件不存在，可能是已经删除，不报错
                // 日志记录但不抛出异常
            }
            
            return Result.success();
        } catch (Exception e) {
            throw new BusinessException("删除照片失败：" + e.getMessage(), e);
        }
    }

    /**
     * 规范化路径，处理 ${user.dir} 等占位符
     */
    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return System.getProperty("user.dir") + File.separator + "uploads";
        }
        // 替换 ${user.dir} 占位符
        String normalized = path.replace("${user.dir}", System.getProperty("user.dir"));
        // 统一路径分隔符
        return normalized.replace("/", File.separator).replace("\\", File.separator);
    }
}

