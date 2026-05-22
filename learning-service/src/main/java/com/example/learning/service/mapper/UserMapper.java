package com.example.learning.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.learning.service.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
