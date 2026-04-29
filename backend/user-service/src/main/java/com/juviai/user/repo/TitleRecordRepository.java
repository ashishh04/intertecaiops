package com.juviai.user.repo;

import com.juviai.user.domain.TitleRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TitleRecordRepository extends JpaRepository<TitleRecord, UUID> {
    List<TitleRecord> findByUserId(UUID userId);
}
