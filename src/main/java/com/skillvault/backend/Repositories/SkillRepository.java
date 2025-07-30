package com.skillvault.backend.Repositories;

import com.skillvault.backend.Domain.Certificate;
import com.skillvault.backend.Domain.Skill;
import com.skillvault.backend.Domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID> {
    boolean existsByUserAndNameIgnoreCase(User user, String name);
    Page<Skill> findByUserId(UUID userId, Pageable pageable);
    Optional<Skill> findByUserAndNameIgnoreCase(User user, String name);
    void deleteByUserAndNameIgnoreCase(User user, String name);
}
