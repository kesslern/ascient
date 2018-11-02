# Use an official Python runtime as a parent image
FROM anapsix/alpine-java:8_jdk as builder

# Set the working directory to /app
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

RUN ./gradlew jar

FROM anapsix/alpine-java:8_jdk
RUN mkdir /app
COPY --from=builder /app/build/libs/ascient.jar /app

EXPOSE 8080

CMD ["bash", "-c", "java -Ddatabase.connection=${DB_CONNECTION} -jar /app/ascient.jar"]
