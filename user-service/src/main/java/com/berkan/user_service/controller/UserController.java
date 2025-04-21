package com.berkan.user_service.controller;

import com.berkan.user_service.dto.request.PasswordUpdateRequest;
import com.berkan.user_service.dto.request.UserCreateRequest;
import com.berkan.user_service.dto.request.UserUpdateRequest;
import com.berkan.user_service.dto.response.UserResponse;
import com.berkan.user_service.dto.response.UserUpdateResponse;
import com.berkan.user_service.model.User;
import com.berkan.user_service.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/users")
public class UserController {

    private final IUserService userService;

   public UserController(IUserService userService) {
       this.userService = userService;
   }

    @PostMapping("create")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request){
        UserResponse userResponse = userService.createUser(request);
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id){
        UserResponse userResponse = userService.getUserById(id);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @PutMapping("/update-password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request){
       userService.updatePassword(request);
       return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
