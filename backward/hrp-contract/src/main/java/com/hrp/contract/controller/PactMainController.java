package com.hrp.contract.controller;

import com.hrp.common.entity.PactMain;
import com.hrp.common.entity.Result;
import com.hrp.contract.service.PactMainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contract")
@CrossOrigin
public class PactMainController {

    @Autowired
    private PactMainService pactMainService;

    @GetMapping("/list")
    public Result<List<PactMain>> getAll() {
        List<PactMain> contracts = pactMainService.getAll();
        return Result.success(contracts);
    }

    @GetMapping("/status/{status}")
    public Result<List<PactMain>> getByStatus(@PathVariable String status) {
        List<PactMain> contracts = pactMainService.getByStatus(status);
        return Result.success(contracts);
    }

    @GetMapping("/{id}")
    public Result<PactMain> getById(@PathVariable Long id) {
        PactMain contract = pactMainService.getById(id);
        return Result.success(contract);
    }

    @PostMapping
    public Result<Void> save(@RequestBody PactMain pactMain) {
        boolean success = pactMainService.save(pactMain);
        return success ? Result.success() : Result.error("保存失败");
    }

    @PutMapping
    public Result<Void> update(@RequestBody PactMain pactMain) {
        boolean success = pactMainService.update(pactMain);
        return success ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = pactMainService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}

