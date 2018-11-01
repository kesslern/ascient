# Use an official Python runtime as a parent image
FROM anapsix/alpine-java:8_jdk

# Set the working directory to /app
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

EXPOSE 8080

CMD ["bash", "-c", "./gradlew run -Ddatabase.connection=${DB_CONNECTION}"]
