package edu.jmaycon.spring.transactions.jpa.config;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Maycon Cesar
 * 
 */
@SpringBootApplication
@ComponentScan(basePackages = "edu.jmaycon.spring.transactions.jpa")
@EntityScan(basePackages = "edu.jmaycon.spring.transactions.jpa.entity")
@PropertySource("classpath:datasource-${project.select.database}.properties")
@EnableTransactionManagement
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@PostConstruct
	public void postConstruct() throws Exception {
	}

	@PreDestroy
	public void predDestroy() throws Exception {
	}

}
