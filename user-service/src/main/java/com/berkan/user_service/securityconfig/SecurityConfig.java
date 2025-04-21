package com.berkan.user_service.securityconfig;

import com.berkan.user_service.jwt.JwtAuthenticationFilter;
import com.berkan.user_service.jwt.JwtService;
import com.berkan.user_service.jwt.TokenBlackListService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter authorizationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final TokenBlackListService tokenBlacklistService;
    private final JwtService jwtService;

    public static final String REGISTER = "/api/auth/register";
    public static final String VERIFY = "/api/auth/verify";
    public static final String LOGIN = "/api/auth/login";
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers(REGISTER,VERIFY,LOGIN).permitAll()
                        .requestMatchers("/api/admin/**").hasAnyRole(ADMIN)
                        .requestMatchers("/api/users/**").hasAnyRole( USER,ADMIN)
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutRequestMatcher(new AntPathRequestMatcher("/api/auth/logout", "POST"))
                        .logoutSuccessHandler((request, response, authentication) -> {
                            String token = request.getHeader("Authorization").replace("Bearer ", "");
                            if (jwtService.isTokenExpired(token) || !jwtService.isTokenValid(token)) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.getWriter().write("{\"message\": \"Invalid token.\"}");
                            } else {
                                tokenBlacklistService.addTokenToBlacklist(token);
                                response.setStatus(HttpServletResponse.SC_OK);
                                response.getWriter().write("{\"message\": \"User logged out successfully\"}");
                            }
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}
