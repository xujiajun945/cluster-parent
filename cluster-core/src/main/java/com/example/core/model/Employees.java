package com.example.core.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author xujiajun
 * @since 2026/11/5
 */
@Setter
@Getter
public class Employees implements Serializable {

    private Integer empNo;

    private LocalDateTime birthDate;

    private String firstName;

    private String lastName;

    private String gender;

    private LocalDateTime hireDate;

}
