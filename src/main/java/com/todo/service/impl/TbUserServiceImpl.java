package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.todo.pojo.TbUser;
import com.todo.mapper.TbUserMapper;
import com.todo.service.TbUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author dayuan
 * @since 2023-12-26
 */
@Service
public class TbUserServiceImpl extends ServiceImpl<TbUserMapper, TbUser> implements TbUserService {

    @Autowired
    private TbUserMapper tbUserMapper;
    @Override
    public TbUser login(TbUser user) {
        // 根据用户名和密码查询用户信息
        QueryWrapper<TbUser> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("phone", user.getPhone());
        queryWrapper.eq("password", user.getPassword());

        // 如果查询到用户，则登录成功，返回用户信息；否则返回null表示登录失败
        TbUser loginUser = tbUserMapper.selectOne(queryWrapper);

        return loginUser;
    }

    @Override
    public boolean checkIfPhoneRegistered(String phone) {
        QueryWrapper<TbUser> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        return tbUserMapper.selectCount(queryWrapper)>0;
    }



    @Override
    public boolean register(String phone, String password) {
        if (checkIfPhoneRegistered(phone)){
            return false;
        }
        TbUser user=new TbUser();
       /* id=UUID.randomUUID().toString();
        user.setId(id);*/
        user.setPhone(phone);
        user.setPassword(password);
        int result  = tbUserMapper.insert(user);
        return result>0 ;
    }
}
