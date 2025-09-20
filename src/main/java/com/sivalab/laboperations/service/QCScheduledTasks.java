package com.sivalab.laboperations.service;

import com.sivalab.laboperations.entity.QualityControl;
import com.sivalab.laboperations.repository.QualityControlRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class QCScheduledTasks {

    private final QualityControlRepository qualityControlRepository;
    private final NotificationService notificationService;

    public QCScheduledTasks(QualityControlRepository qualityControlRepository, NotificationService notificationService) {
        this.qualityControlRepository = qualityControlRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 9 * * *") // Run every day at 9 AM
    public void sendQcReminders() {
        List<QualityControl> qualityControls = qualityControlRepository.findAll();
        for (QualityControl qc : qualityControls) {
            if (qc.getNextDueDate() != null && qc.getNextDueDate().isBefore(LocalDateTime.now().plusDays(1))) {
                notificationService.sendQcReminder("QC for " + qc.getControlName() + " is due on " + qc.getNextDueDate());
            }
        }
    }
}
