package com.bidhutkarki.tictactoe.user.service;

import com.bidhutkarki.tictactoe.user.client.AuthClient;
import com.bidhutkarki.tictactoe.user.dto.AuthRegisterRequest;
import com.bidhutkarki.tictactoe.user.dto.ProfileResponse;
import com.bidhutkarki.tictactoe.user.dto.RegisterRequest;
import com.bidhutkarki.tictactoe.user.dto.UserResponse;
import com.bidhutkarki.tictactoe.user.entity.Profile;
import com.bidhutkarki.tictactoe.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthClient authClient;
    private final ProfileRepository profileRepository;

    @Transactional
    public ProfileResponse register(RegisterRequest request) {
        UserResponse user = authClient.register(AuthRegisterRequest.from(request));
        Profile profile = profileRepository.save(new Profile(
                user.id(),
                user.username(),
                request.firstName().trim(),
                request.lastName().trim()));
        return ProfileResponse.from(profile);
    }
}
