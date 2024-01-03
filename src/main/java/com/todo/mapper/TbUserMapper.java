package com.todo.mapper;

import com.todo.pojo.TbUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author dayuan
 * @since 2023-12-26
 */
@Mapper
public interface TbUserMapper extends BaseMapper<TbUser> {

}
