package com.example.core.query;

import com.example.core.model.BaseQuery;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 *
 * @author xujiajun
 * @since 2026/11/5
 */
@Setter
@Getter
public class EmployeesQuery extends BaseQuery implements Serializable {

    private Integer keywordsType;

    private String keywords;
}
