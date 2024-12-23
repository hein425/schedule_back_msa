package com.example.friendservice.repository;

import com.example.friendservice.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SearchRepository extends JpaRepository<Schedule, Long> {

    // 일정 검색
    @Query(value = "SELECT s.s_idx AS idx, s.s_title AS title, s.s_content AS content, s.s_start AS startDate, null AS date, 'SCHEDULE' AS type, null AS category " +
            "FROM schedule s WHERE (s.s_title LIKE %:query% OR s.s_content LIKE %:query%) AND s.c_idx = :calendarIdx",
            nativeQuery = true)
    List<Object[]> searchSchedule(@Param("query") String query, @Param("calendarIdx") Long calendarIdx);

    // 일기 검색
    @Query(value = "SELECT d.d_idx AS idx, d.d_title AS title, d.d_content AS content, null AS startDate, d.d_date AS date, 'DIARY' AS type, d.d_category AS category " +
            "FROM diary d WHERE (d.d_title LIKE %:query% OR d.d_content LIKE %:query%) AND d.c_idx = :calendarIdx",
            nativeQuery = true)
    List<Object[]> searchDiary(@Param("query") String query, @Param("calendarIdx") Long calendarIdx);

    // 전체 검색
    @Query(value = "SELECT s.s_idx AS idx, s.s_title AS title, s.s_content AS content, s.s_start AS startDate, null AS date, 'SCHEDULE' AS type, null AS category " +
            "FROM schedule s WHERE (s.s_title LIKE %:query% OR s.s_content LIKE %:query%) AND s.c_idx = :calendarIdx " +
            "UNION " +
            "SELECT d.d_idx AS idx, d.d_title AS title, d.d_content AS content, null AS startDate, d.d_date AS date, 'DIARY' AS type, d.d_category AS category " +
            "FROM diary d WHERE (d.d_title LIKE %:query% OR d.d_content LIKE %:query%) AND d.c_idx = :calendarIdx",
            nativeQuery = true)
    List<Object[]> searchAll(@Param("query") String query, @Param("calendarIdx") Long calendarIdx);
}
