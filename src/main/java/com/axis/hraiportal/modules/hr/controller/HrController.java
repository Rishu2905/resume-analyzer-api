package com.axis.hraiportal.modules.hr.controller;

import com.axis.hraiportal.modules.hr.entity.HrUserModel;
import com.axis.hraiportal.modules.hr.service.HrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
@Slf4j
public class HrController {

    private final HrService hrService;

    // POST /api/hr/register
    @PostMapping("/register")
    public Mono<ResponseEntity<HrUserModel>> register(
            @RequestBody HrUserModel request) {
        return hrService
                .register(
                        request.getName(),
                        request.getEmail(),
                        request.getPassword(),
                        request.getCompany())
                .map(ResponseEntity::ok);
    }

    // POST /api/hr/login
    @PostMapping("/login")
    public Mono<ResponseEntity<HrUserModel>> login(
            @RequestBody HrUserModel request) {
        return hrService
                .login(
                        request.getEmail(),
                        request.getPassword())
                .map(ResponseEntity::ok);
    }

    // GET /api/hr/{hrId}
    @GetMapping("/{hrId}")
    public Mono<ResponseEntity<HrUserModel>> getById(
            @PathVariable String hrId) {
        return hrService.getById(hrId)
                .map(ResponseEntity::ok);
    }
}
