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
    public Result<SysAttachment> getById(@PathVariable("id") Long id) {
        SysAttachment attachment = sysAttachmentService.getById(id);
        return Result.success(attachment);
    }

    @GetMapping("/business")
    public Result<List<SysAttachment>> getByBusiness(@RequestParam(value = "businessType") String businessType, @RequestParam(value = "businessId") String businessId) {
        List<SysAttachment> attachments = sysAttachmentService.getByBusiness(businessType, businessId);
        return Result.success(attachments);
    }

    @GetMapping("/business-id")
    public Result<List<SysAttachment>> getByBusinessId(@RequestParam(value = "businessId") String businessId) {
        List<SysAttachment> attachments = sysAttachmentService.getByBusinessId(businessId);
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
    public Result<Void> delete(@PathVariable("id") Long id) {
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
    public Result<Void> deleteByBusiness(@RequestParam(value = "businessType") String businessType, @RequestParam(value = "businessId") String businessId) {
        boolean success = sysAttachmentService.deleteByBusiness(businessType, businessId);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success();
    }

    @DeleteMapping("/business-id")
    public Result<Void> deleteByBusinessId(@RequestParam(value = "businessId") String businessId) {
        boolean success = sysAttachmentService.deleteByBusinessId(businessId);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success();
    }

    /**
     * 批量更新附件的 businessId（用于新增业务记录保存后更新附件）
     */
    @PutMapping("/update-business-id")
    public Result<Void> updateBusinessId(@RequestParam(value = "businessType") String businessType,
                                         @RequestParam(value = "businessId") String businessId,
                                         @RequestParam(value = "attachmentIdsStr", required = false) String attachmentIdsStr) {
        System.out.println("=== 更新附件businessId ===");
        System.out.println("businessType: " + businessType);
        System.out.println("businessId: " + businessId);
        System.out.println("attachmentIdsStr: " + attachmentIdsStr);
        
        List<Long> attachmentIds = null;
        if (attachmentIdsStr != null && !attachmentIdsStr.trim().isEmpty()) {
            try {
                attachmentIds = Arrays.stream(attachmentIdsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
                System.out.println("解析后的attachmentIds: " + attachmentIds);
            } catch (NumberFormatException e) {
                System.err.println("附件ID格式错误: " + attachmentIdsStr);
                throw new BusinessException(400, "附件ID格式错误");
            }
        }
        
        boolean success = sysAttachmentService.updateBusinessIdBatch(businessType, businessId, attachmentIds);
        System.out.println("更新结果: " + success);
        if (!success) {
            System.err.println("更新附件业务ID失败");
            throw new BusinessException("更新附件业务ID失败");
        }
        System.out.println("=== 更新附件businessId完成 ===");
        return Result.success();
    }

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result<Long> upload(@RequestParam("file") MultipartFile file,
                              @RequestParam("businessType") String businessType,
                              @RequestParam(value = "businessId", required = false) String businessIdStr) {
        String businessId = null;
        if (businessIdStr != null && !businessIdStr.trim().isEmpty() 
                && !"null".equalsIgnoreCase(businessIdStr) 
                && !"undefined".equalsIgnoreCase(businessIdStr)
                && !"temp".equalsIgnoreCase(businessIdStr)) {
            // businessId现在支持字符串类型（如申请单号）
            businessId = businessIdStr.trim();
        }
        if (file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        try {
            // 使用配置的路径生成文件存储路径
            String normalizedBasePath = normalizePath(basePath);
            // 如果businessId是时间戳（纯数字），创建以时间戳命名的子文件夹
            String uploadDir;
            if (businessId != null && !businessId.trim().isEmpty() && businessId.matches("^\\d+$")) {
                // businessId是纯数字（时间戳），使用它作为子文件夹名
                uploadDir = Paths.get(normalizedBasePath, attachmentPath, businessType, businessId).toString();
            } else {
                // 否则使用原来的路径（businessType下）
                uploadDir = Paths.get(normalizedBasePath, attachmentPath, businessType).toString();
            }
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    System.err.println("警告：创建上传目录失败：" + uploadDir);
                } else {
                    System.out.println("创建上传目录成功：" + uploadDir);
                }
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;
            // 统一使用 Paths.get() 来构建路径，确保跨平台兼容性
            String filePath = Paths.get(uploadDir, fileName).toString();
            
            System.out.println("准备上传文件：" + originalFilename);
            System.out.println("上传目录：" + uploadDir);
            System.out.println("完整文件路径：" + filePath);
            System.out.println("文件大小：" + file.getSize() + " 字节");

            // 保存文件
            File targetFile = new File(filePath);
            file.transferTo(targetFile);
            
            // 验证文件是否真的被保存了
            if (!targetFile.exists()) {
                throw new BusinessException("文件保存失败：文件不存在");
            }
            if (targetFile.length() != file.getSize()) {
                System.err.println("警告：保存的文件大小不匹配，期望：" + file.getSize() + "，实际：" + targetFile.length());
            }
            System.out.println("文件上传成功，文件路径：" + filePath + "，文件大小：" + targetFile.length() + " 字节");

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

            System.out.println("=== 准备保存附件记录 ===");
            System.out.println("businessType: " + businessType);
            System.out.println("businessId: " + businessId);
            System.out.println("fileName: " + originalFilename);
            System.out.println("filePath: " + filePath);
            System.out.println("fileSize: " + file.getSize());

            boolean success = false;
            try {
                success = sysAttachmentService.save(attachment);
            } catch (Exception e) {
                System.err.println("保存附件记录时发生异常：" + e.getMessage());
                e.printStackTrace();
                // 如果数据库保存失败，删除已上传的文件
                if (targetFile.exists()) {
                    targetFile.delete();
                }
                throw new BusinessException("保存附件记录失败：" + e.getMessage(), e);
            }
            
            if (!success) {
                // 如果数据库保存失败，删除已上传的文件
                if (targetFile.exists()) {
                    targetFile.delete();
                }
                System.err.println("保存附件记录失败：sysAttachmentService.save 返回 false");
                throw new BusinessException("保存附件记录失败");
            }
            
            System.out.println("附件记录保存成功，attachmentId：" + attachment.getAttachmentId() + "，businessId：" + attachment.getBusinessId());
            
            // 验证附件是否真的保存到数据库
            if (attachment.getAttachmentId() != null) {
                try {
                    SysAttachment saved = sysAttachmentService.getById(attachment.getAttachmentId());
                    if (saved == null) {
                        System.err.println("警告：附件记录保存后查询不到，attachmentId: " + attachment.getAttachmentId());
                    } else {
                        System.out.println("验证成功：附件记录已保存到数据库，attachmentId: " + saved.getAttachmentId() + ", businessId: " + saved.getBusinessId());
                    }
                } catch (Exception e) {
                    System.err.println("验证附件记录时发生异常：" + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            return Result.success(attachment.getAttachmentId());
        } catch (BusinessException e) {
            // 重新抛出业务异常
            throw e;
        } catch (IOException e) {
            System.err.println("文件上传失败：" + e.getMessage());
            e.printStackTrace();
            throw new BusinessException("文件上传失败：" + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("文件上传过程中发生未知异常：" + e.getMessage());
            e.printStackTrace();
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
    public Result<Void> deletePhoto(@RequestParam(value = "photoPath") String photoPath) {
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
            String defaultPath = Paths.get(System.getProperty("user.dir"), "uploads").toString();
            System.out.println("使用默认上传路径：" + defaultPath);
            return defaultPath;
        }
        // 替换 ${user.dir} 占位符
        String normalized = path.replace("${user.dir}", System.getProperty("user.dir"));
        // 使用 Paths.get() 规范化路径，自动处理路径分隔符
        try {
            // 如果路径是绝对路径，直接规范化
            if (normalized.startsWith("/") || (normalized.length() > 2 && normalized.charAt(1) == ':')) {
                return Paths.get(normalized).normalize().toString();
            } else {
                // 相对路径，相对于当前工作目录
                return Paths.get(System.getProperty("user.dir"), normalized).normalize().toString();
            }
        } catch (Exception e) {
            // 如果 Paths.get() 失败，使用原来的方法
            System.err.println("路径规范化失败，使用原始路径：" + normalized);
            return normalized.replace("/", File.separator).replace("\\", File.separator);
        }
    }
}

