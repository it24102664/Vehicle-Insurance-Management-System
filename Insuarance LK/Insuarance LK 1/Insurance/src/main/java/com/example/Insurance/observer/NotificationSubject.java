package com.example.Insurance.observer;

import com.example.Insurance.entity.AdminNotification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NotificationSubject implements Subject {

    private List<Observer> observers = new ArrayList<>();
    private AdminNotification notification;

    @Override
    public void attach(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            System.out.println("✅ Observer attached: " + observer.getClass().getSimpleName());
        }
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
        System.out.println("❌ Observer detached: " + observer.getClass().getSimpleName());
    }

    @Override
    public void notifyObservers() {
        System.out.println("📢 Notifying " + observers.size() + " observers...");
        for (Observer observer : observers) {
            observer.update(notification);
        }
    }

    public void setNotification(AdminNotification notification) {
        this.notification = notification;
        notifyObservers();
    }

    public AdminNotification getNotification() {
        return notification;
    }
}
