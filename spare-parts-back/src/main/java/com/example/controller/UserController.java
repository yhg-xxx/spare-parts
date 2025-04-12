package com.example.controller;

import com.example.dao.UserRepository;
import com.example.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @PostMapping("/login")
    public User login(@RequestBody User user) {
        System.out.println(123123);
        System.out.println(user.getName());
        System.out.println(user.getPassword());
        return userRepository.findByNameAndPassword(user.getName(), user.getPassword());
    }
    // 新增用户
    @PostMapping("/login/user")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    // 查询所有用户
    @GetMapping("/login/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
//



}
