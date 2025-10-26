package com.example.Insurance.observer;

import com.example.Insurance.entity.AdminNotification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LogNotificationObserver implements Observer {

    @Override
    public void update(AdminNotification notification) {
        System.out.println("📋 LogObserver: Logging notification event...");
        System.out.println("   [" + LocalDateTime.now() + "] Notification ID: " + notification.getId());
        System.out.println("   Type: " + notification.getType());
        System.out.println("   Status: " + notification.getStatus());
        System.out.println("✅ LogObserver: Event logged successfully");
    }
}
