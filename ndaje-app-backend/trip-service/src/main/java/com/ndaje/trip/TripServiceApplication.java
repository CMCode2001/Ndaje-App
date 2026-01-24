package com.ndaje.trip;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.cloud.client.discovery.EnableDiscoveryClient
@org.springframework.cloud.openfeign.EnableFeignClients
public class TripServiceApplication {

	public static void main(String[] args) {
		// Check for .env in current directory
		java.io.File envInCurrent = new java.io.File(".env");
		java.io.File envInParent = new java.io.File("../.env");

		Dotenv dotenv;
		if (envInCurrent.exists()) {
			dotenv = Dotenv.configure().load();
		} else if (envInParent.exists()) {
			dotenv = Dotenv.configure().directory("../").load();
		} else {
			dotenv = Dotenv.configure().ignoreIfMissing().load();
		}

		dotenv.entries().forEach(entry -> {
			String key = entry.getKey();
			String value = entry.getValue();
			System.setProperty(key, value);

			// Explicitly override Spring datasource properties to bypass placeholder
			// resolution issues
			if ("TRIP_DB_URL".equals(key)) {
				System.setProperty("spring.datasource.url", value);
			} else if ("DB_USERNAME".equals(key)) {
				System.setProperty("spring.datasource.username", value);
			} else if ("DB_PASSWORD".equals(key)) {
				System.setProperty("spring.datasource.password", value);
			}
		});
		SpringApplication.run(TripServiceApplication.class, args);
	}

}
