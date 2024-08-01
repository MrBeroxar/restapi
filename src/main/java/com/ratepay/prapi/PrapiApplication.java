package com.ratepay.prapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ratepay.prapi.environment.RuntimeEnvironmentRestrictor;

@SpringBootApplication
public class PrapiApplication {

	public static void main(String[] args) throws IllegalStateException
	{
		if(RuntimeEnvironmentRestrictor.verifyEnvironment()) {
			SpringApplication.run(PrapiApplication.class, args);
		}else{
			throw new IllegalStateException("Application not allowed to run in this environment");
		}
	}
}