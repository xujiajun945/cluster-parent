package com.example.core.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author xujiajun
 * @since 2026/11/5
 */
@Setter
@Getter
public class Pager<T> implements Serializable {

    private Integer pageNo;

    private Integer rows;

    private Integer total;

    private List<T> list;

    public Pager() {}

    public Pager(Integer pageNo, Integer rows) {
        this.pageNo = pageNo;
        this.rows = rows;
    }

    public Pager(Integer pageNo, Integer rows, Integer total) {
        this.pageNo = pageNo;
        this.rows = rows;
        this.total = total;
    }

    public Integer position() {
        int start = (pageNo - 1) * rows;
        return start;
    }
}
