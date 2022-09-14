package com.example.config;
import com.example.bean.Person;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyConfig{
    @Bean("per")
    public Person person01(){
        return new Person("hahaha");
    }
}