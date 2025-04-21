package com.berkan.user_service.controller;

import com.berkan.user_service.dto.request.UserUpdateRequest;
import com.berkan.user_service.dto.response.UserUpdateResponse;
import com.berkan.user_service.model.User;
import com.berkan.user_service.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IUserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(){
        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping ("update/{id}")
    public ResponseEntity<UserUpdateResponse> updateUser(@Valid @RequestBody UserUpdateRequest request, @PathVariable Long id){
        UserUpdateResponse response= userService.updateUser(request, id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
