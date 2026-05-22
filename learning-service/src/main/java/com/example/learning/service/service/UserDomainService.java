package com.example.learning.service.service;

import com.example.learning.api.dto.CreateUserRequest;
import com.example.learning.api.dto.UpdateUserRequest;
import com.example.learning.api.dto.UserDTO;
import com.example.learning.service.entity.User;
import com.example.learning.service.exception.BusinessException;
import com.example.learning.service.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDomainService {

    private static final String USER_CACHE_KEY_PREFIX = "user:id:";

    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper redisObjectMapper;

    public UserDTO createUser(CreateUserRequest request) {
        validateCreateRequest(request);

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setAge(request.getAge());

        userMapper.insert(user);
        UserDTO dto = toDto(user);
        cacheUser(dto);
        return dto;
    }

    public UserDTO getUserById(Long id) {
        String cacheKey = buildCacheKey(id);
        String cachedJson = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cachedJson)) {
            try {
                return redisObjectMapper.readValue(cachedJson, UserDTO.class);
            } catch (Exception ignored) {
                stringRedisTemplate.delete(cacheKey);
            }
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在，id=" + id);
        }

        UserDTO dto = toDto(user);
        cacheUser(dto);
        return dto;
    }

    public List<UserDTO> listUsers() {
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                        .orderByDesc(User::getId))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在，id=" + id);
        }

        if (StringUtils.hasText(request.getUsername())) {
            user.setUsername(request.getUsername().trim());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail().trim());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }

        userMapper.updateById(user);
        evictUserCache(id);

        UserDTO dto = toDto(userMapper.selectById(id));
        cacheUser(dto);
        return dto;
    }

    public void deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在，id=" + id);
        }
        userMapper.deleteById(id);
        evictUserCache(id);
    }

    private void validateCreateRequest(CreateUserRequest request) {
        if (request == null) {
            throw new BusinessException(400, "请求体不能为空");
        }
        if (!StringUtils.hasText(request.getUsername())) {
            throw new BusinessException(400, "用户名不能为空");
        }
        if (!StringUtils.hasText(request.getEmail())) {
            throw new BusinessException(400, "邮箱不能为空");
        }
        if (request.getAge() == null || request.getAge() < 0) {
            throw new BusinessException(400, "年龄必须大于等于 0");
        }
    }

    private UserDTO toDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAge(user.getAge());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    private void cacheUser(UserDTO dto) {
        try {
            String json = redisObjectMapper.writeValueAsString(dto);
            stringRedisTemplate.opsForValue().set(buildCacheKey(dto.getId()), json, 30, TimeUnit.MINUTES);
        } catch (Exception ignored) {
            // 缓存失败不影响主流程
        }
    }

    private void evictUserCache(Long id) {
        stringRedisTemplate.delete(buildCacheKey(id));
    }

    private String buildCacheKey(Long id) {
        return USER_CACHE_KEY_PREFIX + id;
    }
}
