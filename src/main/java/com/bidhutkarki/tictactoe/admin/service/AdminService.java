package com.bidhutkarki.tictactoe.admin.service;

import com.bidhutkarki.tictactoe.admin.dto.PagedResponse;
import com.bidhutkarki.tictactoe.user.dto.ProfileResponse;
import com.bidhutkarki.tictactoe.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {

    private final ProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public PagedResponse<ProfileResponse> listUsers(Pageable pageable) {
        log.info("Listing users page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        PagedResponse<ProfileResponse> response =
                PagedResponse.from(profileRepository.findAll(pageable).map(ProfileResponse::from));
        log.info("Listed {} users (totalElements={})", response.content().size(), response.totalElements());
        return response;
    }
}
