package com.myspringai.myspringai.service;

import com.myspringai.myspringai.dao.Student;
import com.myspringai.myspringai.dao.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService{

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public Student getStudentById(long id) {
        return studentRepository.findById(id).orElseThrow(RuntimeException::new); // 不存在则抛出异常
    }
}
