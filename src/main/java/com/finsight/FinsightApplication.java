package com.finsight;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinsightApplication {

	public static void main(String[] args) {
		// Laad .env, maar crash niet als het bestand ontbreekt (bijv. binnen Docker)
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

		// Fallback naar systeemvariabelen (Docker) als .env (lokaal) de waarden niet heeft
		String host = dotenv.get("BACKEND_DB_HOST") != null ? dotenv.get("BACKEND_DB_HOST") : System.getenv("DB_HOST");
		String name = dotenv.get("BACKEND_DB_NAME") != null ? dotenv.get("BACKEND_DB_NAME") : System.getenv("DB_NAME");
		String user = dotenv.get("BACKEND_USER") != null ? dotenv.get("BACKEND_USER") : System.getenv("DB_USER");
		String password = dotenv.get("BACKEND_PASSWORD") != null ? dotenv.get("BACKEND_PASSWORD") : System.getenv("DB_PASSWORD");

		// Als je lokaal via CMD opstart, draait je database op 'localhost', niet op 'db'
		if ("db".equals(host) && System.getenv("DB_HOST") == null) {
			host = "localhost";
		}

		// Zorg voor een veilige fallback om NullPointerExceptions te voorkomen
		if (host != null && name != null) {
			String dbUrl = "jdbc:postgresql://" + host + ":5432/" + name;
			System.setProperty("DB_URL", dbUrl);
		}

		if (user != null) System.setProperty("DB_USERNAME", user);
		if (password != null) System.setProperty("DB_PASSWORD", password);

		SpringApplication.run(FinsightApplication.class, args);
	}
}