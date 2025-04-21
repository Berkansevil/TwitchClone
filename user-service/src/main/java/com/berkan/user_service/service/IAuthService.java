package com.berkan.user_service.service;

import com.berkan.user_service.dto.auth.*;
import com.berkan.user_service.dto.request.VerifyUserRequest;

public interface IAuthService {

    RegisterResponse register(RegisterRequest registerRequest);
    LoginResponse login(LoginRequest loginRequest);
    void verifyUser(VerifyUserRequest input);
     void resendVerificationCode(String email);

}
