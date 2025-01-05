# Profile Based Routing (POC)

This is a proof-of-concept to demonstrate how the Artemis services can be auto-discovered and traffic being routed based on the active Spring Profiles.

## Getting Started

1. Build the API-Gateway and the Artemis-Service:
```bash
cd api-gateway/ && ./gradlew build && cd ../artemis && ./gradlew build
```
2. Run the following command:
```bash
docker compose up -d
```
3. Access the JHipster registry at [http://localhost:8761](http://localhost:8761/registry/applications) and verify that it looks like this:
![JHipster Registry](./docs/jhipster_registry_discovered_services.png)

See that the two Artemis services have different Spring Profiles enabled.

4. Wait for a few seconds.
5. Access the functionality of the QuizController by calling [/api/quiz](http://localhost:8080/api/quiz) in your browser.
6. Verify that there is a response and by spamming the requests, the response changes (different instance name).
7. Repeat the same for [/api/quiz](http://localhost:8080/api/text).

Note: If you see a 503 response, you might just need to wait a little bit more until the API gateway has fetched the available service instances from Jhipster Registry.