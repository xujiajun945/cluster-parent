package com.example.core.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xujiajun
 * @since 2026/11/5
 */
@Setter
@Getter
public class EmployeesDTO implements Serializable {

    private Integer empNo;

    private Date birthDate;

    private String firstName;

    private String lastName;

    private String gender;

    private Date hireDate;
}
