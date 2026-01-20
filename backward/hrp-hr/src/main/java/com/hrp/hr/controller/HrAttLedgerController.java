package com.hrp.hr.controller;

import com.hrp.common.entity.HrAttLedger;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.Result;
import com.hrp.common.exception.BusinessException;
import com.hrp.common.util.ExcelUtil;
import com.hrp.hr.service.HrAttLedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hr/attendance/ledger")
@CrossOrigin
public class HrAttLedgerController {

    @Autowired
    private HrAttLedgerService hrAttLedgerService;

    /**
     * 分页查询考勤台账
     */
    @GetMapping("/page")
    public Result<PageResult<HrAttLedger>> getPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "empId", required = false) Long empId,
            @RequestParam(value = "attMonth", required = false) String attMonth,
            @RequestParam(value = "ledgerStatus", required = false) String ledgerStatus,
            @RequestParam(value = "empCode", required = false) String empCode,
            @RequestParam(value = "empName", required = false) String empName,
            @RequestParam(value = "deptId", required = false) Long deptId) {
        PageResult<HrAttLedger> pageResult = hrAttLedgerService.getPage(
                page, size, empId, attMonth, ledgerStatus, empCode, empName, deptId);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询考勤台账详情
     */
    @GetMapping("/{id}")
    public Result<HrAttLedger> getById(@PathVariable("id") Long id) {
        HrAttLedger ledger = hrAttLedgerService.getById(id);
        if (ledger == null) {
            return Result.error("考勤台账不存在");
        }
        return Result.success(ledger);
    }

    /**
     * 根据员工ID和月份查询考勤台账
     */
    @GetMapping("/emp-month")
    public Result<HrAttLedger> getByEmpIdAndMonth(
            @RequestParam(value = "empId") Long empId,
            @RequestParam(value = "attMonth") String attMonth) {
        HrAttLedger ledger = hrAttLedgerService.getByEmpIdAndMonth(empId, attMonth);
        if (ledger == null) {
            return Result.error("该员工在该月份的考勤台账不存在");
        }
        return Result.success(ledger);
    }

    /**
     * 统计考勤：按考勤月份 + 周期计算全员台账
     * body: { attMonth: "yyyy-MM", startDate: "yyyy-MM-dd", endDate: "yyyy-MM-dd" }
     */
    @PostMapping("/calculate")
    public Result<Integer> calculateAll(@RequestBody Map<String, String> body) {
        try {
            String attMonth = body != null ? body.get("attMonth") : null;
            String startDate = body != null ? body.get("startDate") : null;
            String endDate = body != null ? body.get("endDate") : null;
            Integer monthWorkDays = null;
            if (body != null && body.get("monthWorkDays") != null && !body.get("monthWorkDays").trim().isEmpty()) {
                monthWorkDays = Integer.parseInt(body.get("monthWorkDays").trim());
            }
            int affected = hrAttLedgerService.calculateAll(attMonth, startDate, endDate, monthWorkDays);
            return Result.success(affected);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("统计失败：" + e.getMessage());
        }
    }

    /**
     * 下载考勤台账导入模板
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        List<String> headers = new ArrayList<>();
        headers.add("员工编码");
        headers.add("考勤月份");
        headers.add("本月工作日");
        headers.add("正常出勤天数");
        headers.add("请假总天数");
        headers.add("加班总天数");
        headers.add("旷工天数");

        List<List<String>> dataList = new ArrayList<>();
        // 添加示例数据
        List<String> exampleRow = new ArrayList<>();
        exampleRow.add("EMP001");
        exampleRow.add("2026-01");
        exampleRow.add("22");
        exampleRow.add("20");
        exampleRow.add("2");
        exampleRow.add("0");
        exampleRow.add("0");
        dataList.add(exampleRow);

        ExcelUtil.exportExcel(response, "考勤台账导入模板", headers, dataList);
    }

    /**
     * 批量导入考勤台账
     */
    @PostMapping("/import")
    public Result<String> importLedgers(@RequestParam("file") MultipartFile file,
                                         @RequestParam(value = "createUser", required = false) String createUser,
                                         @RequestHeader(value = "Authorization", required = false) String token) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要导入的文件");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new BusinessException(400, "文件格式错误，请上传Excel文件");
        }

        try {
            List<List<String>> dataList = ExcelUtil.readExcel(file);
            if (dataList == null || dataList.isEmpty()) {
                throw new BusinessException(400, "文件内容为空");
            }

            // 如果没有传入createUser，从token中获取当前用户账号
            if (createUser == null || createUser.trim().isEmpty()) {
                createUser = getCurrentUserAccount(token);
                if (createUser == null || createUser.isEmpty()) {
                    createUser = "SYSTEM";
                }
            }

            return hrAttLedgerService.importLedgers(dataList, createUser);
        } catch (IOException e) {
            throw new BusinessException(500, "读取文件失败：" + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException(500, "导入失败：" + e.getMessage());
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
            String account = com.hrp.common.util.JwtUtil.getAccount(token);
            return account;
        } catch (Exception e) {
            return null;
        }
    }
}
