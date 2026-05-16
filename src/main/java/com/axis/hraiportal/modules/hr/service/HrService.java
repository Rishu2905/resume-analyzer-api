package com.axis.hraiportal.modules.hr.service;

import com.axis.hraiportal.modules.hr.entity.HrUserModel;
import com.axis.hraiportal.modules.hr.repository.HrRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.axis.hraiportal.modules.hr.dtoresponse.hrResponse;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HrService {

    private final HrRepository hrRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Register new HR user ─────────────────────────────────
    public Mono<hrResponse> register(
            String name, String email, String password, String company) {
        return hrRepository.findByEmail(email)
                .flatMap(existingUsers -> {
                    if (!existingUsers.isEmpty()) {
                        return Mono.error(new RuntimeException(
                                "Account with email " + email +
                                        " already exists"));
                    }
                    String hashedpassword=passwordEncoder.encode(password);
                    HrUserModel newUser = HrUserModel.builder()
                            .hrId(UUID.randomUUID().toString())
                            .name(name)
                            .email(email)
                            .password(hashedpassword)   // hash this before saving in real prod
                            .company(company)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    return hrRepository.save(newUser)
                           .map(saved->toResponse(saved));
                });
    }

    // ── Login — find by email, verify password ───────────────
    public Mono<HrUserModel> login(
            String email, String password) {

        return hrRepository.findByEmail(email)
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return Mono.error(new RuntimeException(
                                "No account found with email: " + email));
                    }
                    HrUserModel user = list.get(0);
                    if (!user.getPassword().equals(password)) {
                        return Mono.error(new RuntimeException(
                                "Incorrect password"));
                    }
                    return Mono.just(user);
                });
    }

    // ── Get HR user by ID ────────────────────────────────────
    public Mono<HrUserModel> getById(String hrId) {
        return hrRepository.findByHrId(hrId)
                .flatMap(list -> list.isEmpty()
                        ? Mono.error(new RuntimeException(
                        "HR user not found: " + hrId))
                        : Mono.just(list.get(0)));
    }
    private hrResponse toResponse(HrUserModel user){
        return hrResponse.builder()
                .hrId(user.getHrId())
                .name(user.getName())
                .email(user.getEmail())
                .company(user.getCompany())
                .build();
    }
}
