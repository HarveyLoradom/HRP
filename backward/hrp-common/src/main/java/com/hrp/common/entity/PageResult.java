package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 */
@Data
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<T> records;  // 数据列表
    private Long total;        // 总记录数
    private Long size;         // 每页大小
    private Long current;      // 当前页码
    private Long pages;        // 总页数

    public PageResult() {
    }

    public PageResult(List<T> records, Long total, Long size, Long current) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
        this.pages = (total + size - 1) / size; // 计算总页数
    }

    public static <T> PageResult<T> of(List<T> records, Long total, Long size, Long current) {
        return new PageResult<>(records, total, size, current);
    }
}



