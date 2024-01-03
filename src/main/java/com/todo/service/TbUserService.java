package com.todo.service;

import com.todo.pojo.TbUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author dayuan
 * @since 2023-12-26
 */
public interface TbUserService extends IService<TbUser> {

    TbUser login(TbUser user);

    boolean checkIfPhoneRegistered(String phone);
    boolean register(String phone,String password);
}
