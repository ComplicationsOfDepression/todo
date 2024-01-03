package com.todo.controller;

import com.todo.anno.Log;
import com.todo.common.Result;
import com.todo.common.Status;
import com.todo.pojo.TbUser;
import com.todo.service.TbUserService;
import com.todo.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class LoginController {

    @Autowired
    private TbUserService tbUserService;

    @PostMapping("/login")
    public Result login(@RequestBody TbUser user){
        log.info("用户登录:{}",user);
        TbUser u=tbUserService.login(user);

        if (u!=null){
            Map<String,Object> claims=new HashMap<>();
            claims.put("id", u.getId());
            claims.put("name", u.getName());
            claims.put("username", u.getUsername());
            claims.put("phone", u.getPhone());

            String jwt = JwtUtils.generateJwt(claims);
            return Result.buildR(Status.OK,"token",jwt);
        }

        return Result.buildR(Status.SYSTEM_ERROR,"手机号或者密码错误");
    }


    @PostMapping("/register")
    public Result register(@RequestBody TbUser user){
        log.info("用户注册:{}", user);

        boolean registerResult = tbUserService.register(user.getPhone(), user.getPassword());

        if (registerResult){
            return Result.buildR(Status.OK,"注册成功",user);
        }
        return Result.buildR(Status.SYSTEM_ERROR,"手机号已经被使用");
    }

    @Log
    @GetMapping("test")
    public Result test(){
        log.info("test");
        return Result.buildR(Status.OK);
    }
}
