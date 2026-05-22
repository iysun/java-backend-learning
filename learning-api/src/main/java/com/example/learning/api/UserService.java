package com.example.learning.api;

import com.example.learning.api.dto.CreateUserRequest;
import com.example.learning.api.dto.UpdateUserRequest;
import com.example.learning.api.dto.UserDTO;

import java.util.List;

/**
 * Dubbo 服务接口。
 * <p>
 * learning-service 模块提供实现（Provider），
 * learning-web 模块通过 @DubboReference 调用（Consumer）。
 */
public interface UserService {

    UserDTO createUser(CreateUserRequest request);

    UserDTO getUserById(Long id);

    List<UserDTO> listUsers();

    UserDTO updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);
}
