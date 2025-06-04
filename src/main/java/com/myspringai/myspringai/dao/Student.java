package com.myspringai.myspringai.dao;

import jakarta.persistence.*;

@Entity
@Table(name = "student")
public class Student {
    @Id // 标识主键
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自增
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "age")
    private int age;
}
