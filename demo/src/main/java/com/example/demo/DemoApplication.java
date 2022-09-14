package com.example.demo;

import com.example.bean.Person;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {

		ConfigurableApplicationContext run=SpringApplication.run(DemoApplication.class, args);
		Person my=run.getBean("per", Person.class);
	}

}
