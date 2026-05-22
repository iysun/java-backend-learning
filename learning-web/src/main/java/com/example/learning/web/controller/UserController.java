package com.example.learning.web.controller;

import com.example.learning.api.UserService;
import com.example.learning.api.dto.CreateUserRequest;
import com.example.learning.api.dto.UpdateUserRequest;
import com.example.learning.api.dto.UserDTO;
import com.example.learning.web.common.Result;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Validated
@RequiredArgsConstructor
public class UserController {

    @DubboReference(version = "1.0.0", check = false, timeout = 5000)
    private UserService userService;

    @PostMapping
    public Result<UserDTO> createUser(@Valid @RequestBody CreateUserBody body) {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(body.getUsername());
        request.setEmail(body.getEmail());
        request.setAge(body.getAge());
        return Result.success(userService.createUser(request));
    }

    @GetMapping("/{id}")
    public Result<UserDTO> getUser(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @GetMapping
    public Result<List<UserDTO>> listUsers() {
        return Result.success(userService.listUsers());
    }

    @PutMapping("/{id}")
    public Result<UserDTO> updateUser(@PathVariable Long id, @RequestBody UpdateUserBody body) {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername(body.getUsername());
        request.setEmail(body.getEmail());
        request.setAge(body.getAge());
        return Result.success(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success(null);
    }

    public static class CreateUserBody {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;

        @NotNull(message = "年龄不能为空")
        @Min(value = 0, message = "年龄必须大于等于 0")
        private Integer age;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }

    public static class UpdateUserBody {
        private String username;
        private String email;
        private Integer age;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }
}
