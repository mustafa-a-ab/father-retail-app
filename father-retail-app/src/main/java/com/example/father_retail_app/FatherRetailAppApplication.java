package com.example.father_retail_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FatherRetailAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(FatherRetailAppApplication.class, args);
		System.out.println("\n=======================================================");
		System.out.println(" App is running on port: 8080");
		System.out.println(" Open the order form: http://localhost:8080/orders/form");
		System.out.println("=======================================================\n");

	}

}
