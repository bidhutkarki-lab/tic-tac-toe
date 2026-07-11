package com.bidhutkarki.tictactoe.admin.service;

import com.bidhutkarki.tictactoe.admin.dto.PagedResponse;
import com.bidhutkarki.tictactoe.user.dto.ProfileResponse;
import com.bidhutkarki.tictactoe.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public PagedResponse<ProfileResponse> listUsers(Pageable pageable) {
        return PagedResponse.from(profileRepository.findAll(pageable).map(ProfileResponse::from));
    }
}
