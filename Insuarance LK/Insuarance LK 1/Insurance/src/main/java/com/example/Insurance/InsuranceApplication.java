package com.example.Insurance;

import com.example.Insurance.observer.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InsuranceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InsuranceApplication.class, args);
	}

	@Bean
	CommandLineRunner initObservers(NotificationSubject subject,
									DatabaseNotificationObserver databaseObserver,
									EmailNotificationObserver emailObserver,
									LogNotificationObserver logObserver) {
		return args -> {
			System.out.println("🔧 Registering Observers...");
			subject.attach(databaseObserver);
			subject.attach(emailObserver);
			subject.attach(logObserver);
			System.out.println("✅ All observers registered successfully!");
		};
	}
}
