package com.example.userservice.repository;

import com.example.userservice.entity.ExchangeDiary;
import com.example.userservice.entity.ExchangeDiaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExchangeDiaryEntryRepository extends JpaRepository<ExchangeDiaryEntry, Long> {
    // 특정 교환 일기(ExchangeDiary)에 연결된 모든 엔트리 조회
    List<ExchangeDiaryEntry> findByExchangeDiary(ExchangeDiary exchangeDiary);
}
