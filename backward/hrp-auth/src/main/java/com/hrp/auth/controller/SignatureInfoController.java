package com.hrp.auth.controller;

import com.hrp.auth.service.SignatureInfoService;
import com.hrp.auth.service.impl.SignatureInfoServiceImpl;
import com.hrp.auth.service.UserEmployeeService;
import com.hrp.common.entity.Result;
import com.hrp.common.entity.SignatureInfo;
import com.hrp.common.entity.Employee;
import com.hrp.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 手写签名信息控制器
 */
@RestController
@RequestMapping("/auth/signature")
@CrossOrigin
public class SignatureInfoController {

    @Autowired
    private SignatureInfoService signatureInfoService;

    @Autowired
    private SignatureInfoServiceImpl signatureInfoServiceImpl;

    @Autowired
    private UserEmployeeService userEmployeeService;

    /**
     * 根据ID获取签名信息
     */
    @GetMapping("/{id}")
    public Result<SignatureInfo> getById(@PathVariable("id") Long id) {
        SignatureInfo signatureInfo = signatureInfoService.getById(id);
        return Result.success(signatureInfo);
    }

    /**
     * 根据员工ID获取签名信息（获取最新的）
     */
    @GetMapping("/emp/{empId}")
    public Result<SignatureInfo> getByEmpId(@PathVariable("empId") Long empId) {
        SignatureInfo signatureInfo = signatureInfoService.getByEmpId(empId);
        return Result.success(signatureInfo);
    }

    /**
     * 获取当前用户的签名信息
     */
    @GetMapping("/current")
    public Result<SignatureInfo> getCurrentUserSignature(HttpServletRequest request) {
        Employee employee = getCurrentEmployee(request);
        if (employee == null || employee.getEmpId() == null) {
            throw new BusinessException(400, "未获取到用户信息，请重新登录");
        }
        
        SignatureInfo signatureInfo = signatureInfoService.getByEmpId(employee.getEmpId());
        return Result.success(signatureInfo);
    }

    /**
     * 获取当前用户的签名图片（Base64格式）
     */
    @GetMapping("/current/image")
    public Result<Map<String, String>> getCurrentUserSignatureImage(HttpServletRequest request) {
        Employee employee = getCurrentEmployee(request);
        if (employee == null || employee.getEmpId() == null) {
            throw new BusinessException(400, "未获取到用户信息，请重新登录");
        }
        
        SignatureInfo signatureInfo = signatureInfoService.getByEmpId(employee.getEmpId());
        if (signatureInfo == null || signatureInfo.getSignatureBlob() == null) {
            return Result.success(null);
        }
        
        // 将二进制数据转换为Base64字符串
        String base64Image = Base64.getEncoder().encodeToString(signatureInfo.getSignatureBlob());
        String dataUrl = "data:image/png;base64," + base64Image;
        
        Map<String, String> result = new HashMap<>();
        result.put("imageUrl", dataUrl);
        return Result.success(result);
    }

    /**
     * 保存或更新签名信息（接收 Base64 字符串）
     */
    @PostMapping("/save")
    public Result<Long> saveSignature(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        // 获取 Base64 图片字符串
        String base64Image = (String) params.get("base64Image");
        if (base64Image == null || base64Image.isEmpty()) {
            throw new BusinessException(400, "签名图片数据不能为空");
        }

        // 从 token 获取当前员工信息
        Employee employee = getCurrentEmployee(request);
        if (employee == null || employee.getEmpId() == null) {
            throw new BusinessException(400, "未获取到用户信息，请重新登录");
        }

        Long empId = employee.getEmpId();
        String empCode = employee.getEmpCode();
        String empName = employee.getEmpName();
        
        try {
            // 解码 Base64 图片
            byte[] imageBytes = signatureInfoServiceImpl.decodeBase64Image(base64Image);
            if (imageBytes == null || imageBytes.length == 0) {
                throw new BusinessException(400, "签名图片数据无效");
            }
            
            // 检查是否已存在签名，如果存在则更新，否则新增
            SignatureInfo existingSignature = signatureInfoService.getByEmpId(empId);
            SignatureInfo signatureInfo;
            
            if (existingSignature != null) {
                // 更新
                signatureInfo = existingSignature;
                signatureInfo.setSignatureName(empCode + "_signature.png");
                signatureInfo.setSignatureBlob(imageBytes);
                signatureInfo.setFileSize(imageBytes.length);
                signatureInfo.setFileType("png");
                
                boolean success = signatureInfoService.update(signatureInfo);
                if (!success) {
                    throw new BusinessException("更新签名失败");
                }
            } else {
                // 新增
                signatureInfo = new SignatureInfo();
                signatureInfo.setEmpId(empId);
                signatureInfo.setEmpCode(empCode);
                signatureInfo.setEmpName(empName);
                signatureInfo.setSignatureName(empCode + "_signature.png");
                signatureInfo.setSignatureBlob(imageBytes);
                signatureInfo.setFileSize(imageBytes.length);
                signatureInfo.setFileType("png");
                
                boolean success = signatureInfoService.save(signatureInfo);
                if (!success) {
                    throw new BusinessException("保存签名失败");
                }
            }
            
            return Result.success(signatureInfo.getId());
        } catch (RuntimeException e) {
            throw new BusinessException(400, e.getMessage());
        }
    }

    /**
     * 删除签名信息
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        boolean success = signatureInfoService.delete(id);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success();
    }

    /**
     * 删除当前用户的签名信息
     */
    @DeleteMapping("/current")
    public Result<Void> deleteCurrentUserSignature(HttpServletRequest request) {
        Employee employee = getCurrentEmployee(request);
        if (employee == null || employee.getEmpId() == null) {
            throw new BusinessException(400, "未获取到用户信息，请重新登录");
        }
        
        boolean success = signatureInfoService.deleteByEmpId(employee.getEmpId());
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success();
    }

    /**
     * 从 token 中获取当前员工信息
     */
    private Employee getCurrentEmployee(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // 解析token获取账号
            String account = com.hrp.common.util.JwtUtil.getAccount(token);
            if (account == null || account.isEmpty()) {
                return null;
            }
            // 根据账号查询员工信息
            return userEmployeeService.getEmployeeByCode(account);
        } catch (Exception e) {
            return null;
        }
    }
}

