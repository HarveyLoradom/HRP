package com.hrp.auth.controller;

import com.hrp.common.exception.BusinessException;
import com.hrp.common.util.ExcelUtil;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 通用Excel导出控制器
 * 支持前端传递表头和数据，适用于多个页面的导出需求
 */
@RestController
@RequestMapping("/auth/common/excel")
@CrossOrigin
public class ExcelExportController {

    /**
     * 通用Excel导出接口
     * 接收前端传递的表头和数据，生成Excel文件并下载
     * 
     * @param exportData 导出数据对象，包含：
     *                   - fileName: 文件名（不含扩展名）
     *                   - headers: 表头列表
     *                   - dataList: 数据列表（二维数组）
     * @param response HTTP响应对象
     * @throws IOException IO异常
     */
    @PostMapping("/export")
    public void exportExcel(@RequestBody Map<String, Object> exportData, HttpServletResponse response) throws IOException {
        // 验证参数
        if (exportData == null) {
            throw new BusinessException(400, "导出数据不能为空");
        }

        String fileName = (String) exportData.get("fileName");
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "导出数据";
        }

        @SuppressWarnings("unchecked")
        List<String> headers = (List<String>) exportData.get("headers");
        if (headers == null || headers.isEmpty()) {
            throw new BusinessException(400, "表头不能为空");
        }

        @SuppressWarnings("unchecked")
        List<List<String>> dataList = (List<List<String>>) exportData.get("dataList");
        if (dataList == null) {
            dataList = new java.util.ArrayList<>();
        }

        // 调用ExcelUtil工具类导出
        ExcelUtil.exportExcel(response, fileName, headers, dataList);
    }
}

