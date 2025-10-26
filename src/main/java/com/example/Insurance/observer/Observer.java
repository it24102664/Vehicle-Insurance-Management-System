package com.example.Insurance.observer;

import com.example.Insurance.entity.AdminNotification;

public interface Observer {
    void update(AdminNotification notification);
}
