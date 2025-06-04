package com.myspringai.myspringai.controller;

import com.myspringai.myspringai.dao.Student;
import com.myspringai.myspringai.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/student/{id}")
    public Student getStudent(@PathVariable long id) {
        return studentService.getStudentById(id);
    }

}
