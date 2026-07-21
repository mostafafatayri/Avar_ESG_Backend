package com.fatayriTech.avarESG.service.EmailService;

import com.fatayriTech.avarESG.enums.EmailQueueStatus;
import com.fatayriTech.avarESG.model.EmailQueue;
import com.fatayriTech.avarESG.repository.EmailQueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailQueueClaimService {

    private final EmailQueueRepository emailQueueRepository;

    @Transactional
    public List<Long> claimReadyEmailIds() {
        return emailQueueRepository
                .findReadyEmails(
                        List.of(
                                EmailQueueStatus.PENDING,
                                EmailQueueStatus.RETRY
                        ),
                        LocalDateTime.now()
                )
                .stream()
                .limit(20)
                .map(email -> {
                    email.setStatus(
                            EmailQueueStatus.PROCESSING
                    );

                    return emailQueueRepository
                            .save(email)
                            .getId();
                })
                .toList();
    }
}