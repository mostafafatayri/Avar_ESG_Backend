package com.fatayriTech.avarESG.repository;

import com.fatayriTech.avarESG.enums.EmailQueueStatus;
import com.fatayriTech.avarESG.model.EmailQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;

public interface EmailQueueRepository
        extends JpaRepository<EmailQueue, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT email
            FROM EmailQueue email
            WHERE email.status IN :statuses
              AND email.nextAttemptAt <= :now
            ORDER BY email.creationDate ASC
            """)
    List<EmailQueue> findReadyEmails(
            @Param("statuses")
            List<EmailQueueStatus> statuses,

            @Param("now")
            LocalDateTime now
    );
}