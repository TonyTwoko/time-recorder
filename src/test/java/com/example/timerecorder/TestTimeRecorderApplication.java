package com.example.timerecorder;

import org.springframework.boot.SpringApplication;

public class TestTimeRecorderApplication {

	public static void main(String[] args) {
		SpringApplication.from(TimeRecorderApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
