package com.axis.hraiportal.modules.users.service;

import com.axis.hraiportal.common.util.JwtUtil;
import com.axis.hraiportal.modules.document.entity.DocumentRecord;
import com.axis.hraiportal.modules.document.repository.DocumentRepository;
import com.axis.hraiportal.modules.users.dtoresponse.CandidateResponse;
import com.axis.hraiportal.modules.users.entity.UserModel;
import com.axis.hraiportal.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.axis.hraiportal.modules.users.dtoresponse.UserResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import com.axis.hraiportal.modules.users.dtoresponse.LoginResponse;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ── Register new HR user ─────────────────────────────────
    public Mono<UserResponse> register(
            String name, String email, String password, String company,String role) {
        return userRepository.findByEmail(email)
                .flatMap(existingUsers -> {
                    if (!existingUsers.isEmpty()) {
                        return Mono.error(new RuntimeException(
                                "Account with email " + email +
                                        " already exists"));
                    }
                    String hashedpassword=passwordEncoder.encode(password);
                    UserModel newUser = UserModel.builder()
                            .userId(UUID.randomUUID().toString())
                            .name(name)
                            .email(email)
                            .password(hashedpassword)
                            .company(company)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .role(role)
                            .build();

                    return userRepository.save(newUser)
                           .map(saved->toResponse(saved));
                });
    }

    // ── Login — find by email, verify password ───────────────
    public Mono<LoginResponse> login(
            String email, String password,String role) {

        return userRepository.findByEmail(email)
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return Mono.error(new RuntimeException(
                                "Invalid Credentials"));
                    }

                    UserModel user = list.getFirst();

                    if (!passwordEncoder.matches(
                            password, user.getPassword())) {
                        return Mono.error(new RuntimeException(
                                "Invalid Credentials"));
                    }

                    if(!Objects.equals(user.getRole(), role)){return Mono.error(new RuntimeException("Invalid Credentials"));}

                    // generate JWT token
                    String token = jwtUtil.generateToken(
                            user.getUserId(), user.getEmail(), user.getRole());

//                    log.info("Login successful: {}", email);

                    return Mono.just(LoginResponse.builder()
                            .userId(user.getUserId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .company(user.getCompany())
                            .token(token)
                            .tokenType("Bearer")
                            .build());
                });
    }

    // GET candidate profile
    public Mono<CandidateResponse> getCandidateById(String userId){
        Mono<List<UserModel>> userMono =
                userRepository.findByHrId(userId);

        Mono<List<DocumentRecord>> docsMono =
                documentRepository.findByUserId(userId);

        return Mono.zip(userMono, docsMono)
                .map(tuple -> {

                    List<UserModel> users = tuple.getT1();
                    List<DocumentRecord> docs = tuple.getT2();

                    if (users.isEmpty()) {
                        throw new RuntimeException(
                                "User not found");
                    }

                    UserModel user = users.get(0);


                    List<String> documentId = docs.stream()
                            .map(DocumentRecord::getDocumentId)
                            .toList();

                    return CandidateResponse.builder()
                            .userId(user.getUserId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .document_id(documentId)
                            .build();
                });

    }

    // ─────────────────────────────────────────────
// GET AUTHENTICATED HR PROFILE
// ─────────────────────────────────────────────
    public Mono<UserResponse> getById(
            String userId) {


        return userRepository

                .findByHrId(userId)

                .flatMap(list -> {

                    if (list.isEmpty()) {

                        return Mono.error(
                                new RuntimeException(
                                        "HR user not found"));
                    }


                    return Mono.just(
                            toResponse(
                                    list.getFirst()));
                });
    }

    private UserResponse toResponse(UserModel user){
        return UserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .company(user.getCompany())
                .build();
    }
}
