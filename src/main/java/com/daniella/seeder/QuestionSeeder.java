package com.daniella.seeder;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.daniella.entity.Question;
import com.daniella.repository.QuestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class QuestionSeeder implements CommandLineRunner {

	private final QuestionRepository repository;

	public QuestionSeeder(QuestionRepository repository) {
		this.repository = repository;
	}

	@Override
	public void run(String... args) {

		System.out.println("Question Seeder started...");

		try {

			if (repository.count() > 0) {
				System.out.println("Questions already exist. Skipping seed.");
				return;
			}

			ObjectMapper objectMapper = new ObjectMapper();
			ClassPathResource resource = new ClassPathResource("questions.json");

			if (!resource.exists()) {
				System.out.println("questions.json NOT FOUND in resources folder!");
				return;
			}

			try (InputStream inputStream = resource.getInputStream()) {

				Question[] questionsArray = objectMapper.readValue(inputStream, Question[].class);

				List<Question> questions = Arrays.asList(questionsArray);

				repository.saveAll(questions);

				System.out.println("Inserted " + questions.size() + " questions successfully!");
			}

		} catch (Exception e) {
			System.out.println("Error while seeding questions: " + e.getMessage());
			e.printStackTrace();
		}
	}
}