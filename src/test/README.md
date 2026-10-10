# Backendtests

De tests volgen de huidige wealth-structuur:

- wealth/service/WealthBusinessServiceTest: berekeningen met echte entities; geen Spring of database.
- wealth/service/WealthApplicationServiceTest: repositoryselecties, sortering, maandgrenzen en overdracht naar de business-service met Mockito.
- wealth/controller/WealthControllerTest: HTTP-parameters, DTO-JSON en foutcodes via standalone MockMvc; geen Spring Boot-context of database.

Voer vanuit de backendmap uit:

~~~powershell
.\mvnw.cmd clean test
~~~

Dit voert 26 tests uit. Docker, PostgreSQL en een .env zijn hiervoor niet nodig. Een eerste uitvoering kan Maven-dependencies downloaden. Gebruik na packagewijzigingen clean om oude classbestanden te verwijderen.

## Spring-integratietest

De bestaande contexttest is behouden als FinsightApplicationIT. De naam IT voorkomt dat deze databaseafhankelijke test meedraait met de unit tests. Start de ontwikkel-PostgreSQL en configureer DB_URL, DB_USERNAME en DB_PASSWORD als procesvariabelen voordat je uitvoert:

~~~powershell
.\mvnw.cmd '-Dtest=FinsightApplicationIT' test
~~~

De Spring-testcontext roept de main-methode niet aan en laadt daarom backend/.env niet via de Dotenv-code in FinsightApplication. Deze integratietest gebruikt de bestaande JPA-configuratie, inclusief ddl-auto=update; gebruik een ontwikkel-/testdatabase. Hij wordt niet automatisch door mvn verify gestart omdat er geen Failsafe-plugin geconfigureerd is.
