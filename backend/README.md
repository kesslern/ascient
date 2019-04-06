# ascient-backend
The backend for ascient, providing a REST API for CRUD operations on arbitrary data from a database.

## Getting Started

### Prerequisites
Ascient-backend requires JDK 8+ to be on the path and a running PostgreSQL instance. To run the tests, a docker daemon must be running and accessible to the current user.

### Running
Use the gradle wrapper to start the backend:
```shell
./gradlew run
```
The server will listen at `http://localhost:8080`. It connects to a PostgreSQL database at `localhost:5432/postgres` with user `user` and password `pass`.

### Tests
Run integration tests with the `test` task. The tests use a mock ktor server and do not require an open port. A PostgreSQL test container is started to provide a fresh database each run.
```shell
./gradlew test
```
The tests can also run against a running backend. This is useful for testing deployments.
```shell
./gradlew test -Dascient.backend=http://localhost:8080
```

## Deployment
A production bundle can be built with the `build` task.
```shell
./gradlew build
```
The build is stored in `build/distributions/ascient.zip`. To run it, unzip and run the executable with the necessary environment variables.

```shell
unzip build/distributions/ascient.zip
JAVA_OPTS='-Ddatabase.connection="jdbc:postgresql://[HOST]:[PORT]/[DATABASE]" -Ddatabase.username="[USERNAME]" -Ddatabase.password=[PASSWORD]' bin/ascient"
```

### Docker
The app can also run in a Docker container.
```shell
docker build -t ascient .
docker run \
  --name ascient \
  -p 8081:8080 \
  -eDB_CONNECTION="jdbc:postgresql://[HOST]:[PORT]/[DATABASE]" \
  -eDB_USERNAME="[USERNAME]" \
  -eDB_PASSWORD="[PASSWORD]" \
   ascient
```

## Built With
* [Ktor](https://ktor.io/) (Kotlin web framework)
* [PostgreSQL](https://www.postgresql.org/)
* [Exposed](https://github.com/JetBrains/Exposed) (Kotlin SQL framework)* 
* [jbcrypt](https://www.mindrot.org/projects/jBCrypt/)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging)
