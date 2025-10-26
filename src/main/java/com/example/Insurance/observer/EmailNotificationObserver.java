package com.example.Insurance.observer;

import com.example.Insurance.entity.AdminNotification;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationObserver implements Observer {

    @Override
    public void update(AdminNotification notification) {
        System.out.println("📧 EmailObserver: Sending email notifications...");
        System.out.println("   Title: " + notification.getTitle());
        System.out.println("   Target: " + notification.getTarget());
        System.out.println("   Priority: " + notification.getPriority());

        // TODO: Add actual email sending logic here
        // Example: emailService.sendEmail(...)

        System.out.println("✅ EmailObserver: Emails sent successfully");
    }
}
