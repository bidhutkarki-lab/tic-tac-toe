package com.bidhutkarki.tictactoe.user.service;

import com.bidhutkarki.tictactoe.user.client.AuthClient;
import com.bidhutkarki.tictactoe.user.dto.AuthRegisterRequest;
import com.bidhutkarki.tictactoe.user.dto.ProfileResponse;
import com.bidhutkarki.tictactoe.user.dto.RegisterRequest;
import com.bidhutkarki.tictactoe.user.dto.UserResponse;
import com.bidhutkarki.tictactoe.user.entity.Profile;
import com.bidhutkarki.tictactoe.user.exception.ProfileNotFoundException;
import com.bidhutkarki.tictactoe.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final AuthClient authClient;
    private final ProfileRepository profileRepository;

    @Transactional
    public ProfileResponse register(RegisterRequest request) {
        log.info("Registering user with username={}", request.username());
        UserResponse user = authClient.register(AuthRegisterRequest.from(request));
        Profile profile = profileRepository.save(new Profile(
                user.id(),
                request.username().trim(),
                request.firstName().trim(),
                request.lastName().trim()));
        log.info("Registered user profileId={} authId={}", profile.getId(), profile.getAuthId());
        return ProfileResponse.from(profile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getByAuthId(String authId) {
        Profile profile = profileRepository.findByAuthId(authId)
                .orElseThrow(() -> new ProfileNotFoundException("no profile for authId=" + authId));
        return ProfileResponse.from(profile);
    }
}
