package com.berkan.user_service.service.impl;

import com.berkan.user_service.dto.auth.*;
import com.berkan.user_service.dto.request.VerifyUserRequest;
import com.berkan.user_service.enums.Role;
import com.berkan.user_service.exception.UserAlreadyExistsException;
import com.berkan.user_service.exception.UserNotFoundException;
import com.berkan.user_service.jwt.JwtService;
import com.berkan.user_service.model.User;
import com.berkan.user_service.repository.UserRepository;
import com.berkan.user_service.service.IAuthService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationProvider authenticationProvider;
    private final EmailService emailService;

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        if(userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("The mail address already exists");
        }
        Set<Role> userRoles=registerRequest.getRoles();
        if(userRoles==null || userRoles.size()==0) {
            userRoles=Set.of(Role.USER);
        }
        User user=modelMapper.map(registerRequest, User.class);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setRoles(userRoles);
        user.setEnabled(false);
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        sendVerificationEmail(user);
        return new RegisterResponse("User registered successfully", null);

    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        try{
            UsernamePasswordAuthenticationToken auth=
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
            authenticationProvider.authenticate(auth);
            User user=userRepository.findByEmail(loginRequest.getEmail());
            if(user==null) {
                throw new UserNotFoundException("User not found");
            }
            if(!user.isEnabled()){
               throw  new RuntimeException("Account not verified. Please verify your account");
            }
            String token=jwtService.generateToken(user);
            long expiresIn = jwtService.getExpirationTime();
            return new LoginResponse(token, expiresIn);
        }catch(Exception e){
            e.printStackTrace();
            return new LoginResponse(null, 0);
        }

    }

    @Override
    public void verifyUser(VerifyUserRequest input) {

        User user = userRepository.findByEmail(input.getEmail());

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code has expired");
        }

        if (user.getVerificationCode().equals(input.getVerificationCode())) {
            user.setEnabled(true);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiresAt(null);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Invalid verification code");
        }
    }

    @Override
    public void resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (user.isEnabled()) {
            throw new RuntimeException("Account is already verified");
        }

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));

        sendVerificationEmail(user);

        userRepository.save(user);
    }

    private void sendVerificationEmail(User user) {
        String subject = "Account Verification";
        String verificationCode = "VERIFICATION CODE " + user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<head>"
                + "<style>"
                + "body { font-family: 'Arial', sans-serif; background-color: #f4f6f9; margin: 0; padding: 0; }"
                + ".container { max-width: 600px; margin: 20px auto; padding: 20px; background-color: #ffffff; border-radius: 12px; box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1); }"
                + ".header { background-color: #5e3f8b; color: #ffffff; padding: 40px 30px; border-radius: 12px 12px 0 0; text-align: center; }"
                + ".header h2 { font-size: 26px; margin: 0; }"
                + ".content { padding: 20px; font-size: 16px; color: #333333; text-align: center; }"
                + ".verification-code-box { background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); margin: 30px auto; text-align: center; border: 2px solid #e0e0e0; width: 100%; max-width: 400px; }"
                + ".verification-label { font-size: 18px; font-weight: 500; color: #333333; margin-bottom: 10px; }"
                + ".verification-code { font-size: 36px; font-weight: bold; color: #5e3f8b; letter-spacing: 4px; background-color: #f0f0f0; padding: 12px 24px; border-radius: 8px; display: inline-block; }"
                + ".cta-button { background-color: #5e3f8b; color: #ffffff; padding: 12px 30px; text-decoration: none; font-size: 16px; border-radius: 5px; margin-top: 30px; display: inline-block; }"
                + ".cta-button:hover { background-color: #4b337a; }"
                + ".footer { background-color: #f9f9f9; padding: 20px; text-align: center; font-size: 14px; color: #777777; border-radius: 0 0 12px 12px; }"
                + ".footer p { margin: 5px 0; font-size: 14px; }"
                + ".footer a { color: #5e3f8b; text-decoration: none; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class=\"container\">"
                + "<div class=\"header\">"
                + "<h2>Welcome to TwitchClone!</h2>"
                + "</div>"
                + "<div class=\"content\">"
                + "<p>Thank you for registering with TwitchClone!</p>"
                + "<p>Please use the verification code below to activate your account:</p>"
                + "<div class=\"verification-code-box\">"
                + "<div class=\"verification-label\">Verification Code:</div>"
                + "<div class=\"verification-code\">" + verificationCode + "</div>"
                + "</div>"
                + "<p>Once verified, you'll be able to enjoy all features of the platform.</p>"
                + "<a href=\"#\" class=\"cta-button\">Verify Account</a>"
                + "</div>"
                + "<div class=\"footer\">"
                + "<p>&copy; 2025 TwitchClone, Inc. All rights reserved.</p>"
                + "<p>If you did not sign up, please ignore this email or contact <a href=\"mailto:support@twitchclone.com\">support@twitchclone.com</a>.</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
