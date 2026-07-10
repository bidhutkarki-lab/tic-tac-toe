package com.bidhutkarki.tictactoe.user.repository;

import com.bidhutkarki.tictactoe.user.entity.Profile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByAuthId(Long authId);

    boolean existsByAuthId(Long authId);
}
