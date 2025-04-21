package com.berkan.user_service.service.impl;

import com.berkan.user_service.dto.request.PasswordUpdateRequest;
import com.berkan.user_service.dto.request.UserCreateRequest;
import com.berkan.user_service.dto.request.UserUpdateRequest;
import com.berkan.user_service.dto.response.UserResponse;
import com.berkan.user_service.dto.response.UserUpdateResponse;
import com.berkan.user_service.exception.UserNotFoundException;
import com.berkan.user_service.model.User;
import com.berkan.user_service.repository.UserRepository;
import com.berkan.user_service.service.IUserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        User user=modelMapper.map(request, User.class);
        User savedUser=userRepository.save(user);
        UserResponse userResponse=modelMapper.map(savedUser, UserResponse.class);
        return userResponse;
    }

    @Override
    public UserUpdateResponse updateUser(UserUpdateRequest request, Long id) {
        User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User not found"));
        modelMapper.map(user, UserUpdateRequest.class);
        User savedUser=userRepository.save(user);
        UserUpdateResponse userResponse=modelMapper.map(savedUser, UserUpdateResponse.class);
        return userResponse;
    }

    @Override
    public UserResponse getUserById(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(long id) {
        User user=userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Override
    public void updatePassword(PasswordUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username=authentication.getName();
        User user=userRepository.findByUsername(username).orElseThrow(()-> new UserNotFoundException("User not found"));
        if(!passwordEncoder.matches(request.getOldPassword(), user.getPassword())){
            throw  new BadCredentialsException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
