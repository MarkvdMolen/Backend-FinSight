package com.finsight;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinsightApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.filename(".env")
				.directory(".")
				.ignoreIfMissing()
				.load();

		String host = dotenv.get("BACKEND_DB_HOST");
		String db   = dotenv.get("BACKEND_DB_NAME");
		String user = dotenv.get("BACKEND_USER");
		String pass = dotenv.get("BACKEND_PASSWORD");

		if (host == null || db == null || user == null || pass == null) {
			System.err.println("Missing Environment Variables");
			System.exit(1);
		}

		String url = "jdbc:postgresql://" + host + ":5432/" + db;
		System.setProperty("DB_URL", url);
		System.setProperty("DB_USERNAME", user);
		System.setProperty("DB_PASSWORD", pass);

		SpringApplication.run(FinsightApplication.class, args);
	}
}