package org.example.baozi.book.response;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> data;     // 当前页数据
    private long total;       // 总记录数
    private long pageSize;    // 每页条数
    private long currentPage; // 当前页码

    public PageResult(List<T> data, long total, long pageSize, long currentPage) {
        this.data = data;
        this.total = total;
        this.pageSize = pageSize;
        this.currentPage = currentPage;
    }
}
