package com.berkan.user_service.service;

import com.berkan.user_service.dto.request.PasswordUpdateRequest;
import com.berkan.user_service.dto.request.UserCreateRequest;
import com.berkan.user_service.dto.request.UserUpdateRequest;
import com.berkan.user_service.dto.response.UserResponse;
import com.berkan.user_service.dto.response.UserUpdateResponse;
import com.berkan.user_service.model.User;

import java.util.List;

public interface IUserService {
    UserResponse createUser(UserCreateRequest request);
    UserUpdateResponse updateUser(UserUpdateRequest request, Long id);
    UserResponse getUserById(long id);
    List<User> getAllUsers();
    void deleteUser(long id);
    void updatePassword(PasswordUpdateRequest request);

}
