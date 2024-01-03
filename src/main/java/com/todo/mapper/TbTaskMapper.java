package com.todo.mapper;

import com.todo.pojo.TbTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 任务表 Mapper 接口
 * </p>
 *
 * @author dayuan
 * @since 2023-12-26
 */
@Mapper
public interface TbTaskMapper extends BaseMapper<TbTask> {

}
