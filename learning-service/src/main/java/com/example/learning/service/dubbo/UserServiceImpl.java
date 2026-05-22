package com.example.learning.service.dubbo;

import com.example.learning.api.UserService;
import com.example.learning.api.dto.CreateUserRequest;
import com.example.learning.api.dto.UpdateUserRequest;
import com.example.learning.api.dto.UserDTO;
import com.example.learning.service.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService(version = "1.0.0", timeout = 5000)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDomainService userDomainService;

    @Override
    public UserDTO createUser(CreateUserRequest request) {
        return userDomainService.createUser(request);
    }

    @Override
    public UserDTO getUserById(Long id) {
        return userDomainService.getUserById(id);
    }

    @Override
    public List<UserDTO> listUsers() {
        return userDomainService.listUsers();
    }

    @Override
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        return userDomainService.updateUser(id, request);
    }

    @Override
    public void deleteUser(Long id) {
        userDomainService.deleteUser(id);
    }
}
