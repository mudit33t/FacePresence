package com.org.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.model.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    boolean existsByUserIdAndDate(int userId, LocalDate date);
}
