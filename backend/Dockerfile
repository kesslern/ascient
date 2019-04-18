# Use an official Python runtime as a parent image
FROM anapsix/alpine-java:8_jdk as builder

# Set the working directory to /app
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

RUN ./gradlew distZip
RUN mkdir /dist
RUN unzip build/distributions/ascient.zip -d /dist

FROM anapsix/alpine-java:8_jdk
COPY --from=builder /dist /app

EXPOSE 8080

CMD ["bash", "-c", "JAVA_OPTS='\
 -Ddatabase.username=${DB_USERNAME}\
 -Ddatabase.password=${DB_PASSWORD}\
 -Ddatabase.hostname=${DB_HOSTNAME}\
 -Ddatabase.port=${DB_PORT}\
 -Ddatabase.dbname=${DB_NAME}\
' /app/ascient/bin/ascient"]
