package com.axis.hraiportal.modules.users.controller;

import com.axis.hraiportal.modules.users.dtoresponse.LoginResponse;
import com.axis.hraiportal.modules.users.dtoresponse.UserResponse;
import com.axis.hraiportal.modules.users.entity.UserModel;
import com.axis.hraiportal.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api") // change it for global users and not for hr only
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // POST /api/hr/register
    @PostMapping("/register")
    public Mono<ResponseEntity<UserResponse>> register(
            @RequestBody UserModel request) {
        return userService
                .register(
                        request.getName(),
                        request.getEmail(),
                        request.getPassword(),
                        request.getCompany(),
                        request.getRole())
                .map(ResponseEntity::ok);
    }

    // POST /api/hr/login
    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(
            @RequestBody UserModel request) {
        return userService
                .login(request.getEmail(), request.getPassword())
                .map(ResponseEntity::ok);
    }


    //     GET /api/hr/{hrId}
    // ─────────────────────────────────────────────
// GET AUTHENTICATED HR PROFILE
// GET /api/hr/me
// ─────────────────────────────────────────────
    @GetMapping("/me")
    public Mono<ResponseEntity<UserResponse>> getById() {

        return ReactiveSecurityContextHolder
                .getContext()

                .map(context ->
                        context.getAuthentication()
                                .getPrincipal()
                                .toString())

                .flatMap(userService::getById)

                .map(ResponseEntity::ok);
    }
}
