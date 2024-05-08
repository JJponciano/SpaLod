FROM maven:3.8.6-openjdk-18 as build

WORKDIR /app

# Install custom dependencies first
COPY libs/pisemantic-1.0-SNAPSHOT.jar /app/libs/pisemantic-1.0-SNAPSHOT.jar
COPY libs/pitools-1.0-SNAPSHOT.jar /app/libs/pitools-1.0-SNAPSHOT.jar
COPY pom.xml /app/pom.xml

# Install pisemantic
RUN mvn install:install-file \
    -Dfile=/app/libs/pisemantic-1.0-SNAPSHOT.jar \
    -DgroupId=info.ponciano.lab \
    -DartifactId=pisemantic \
    -Dversion=1.0 \
    -Dpackaging=jar \
    -DpomFile=/app/pom.xml

# Install pitools
RUN mvn install:install-file \
    -Dfile=/app/libs/pitools-1.0-SNAPSHOT.jar \
    -DgroupId=info.ponciano.lab \
    -DartifactId=pitools \
    -Dversion=1.0-SNAPSHOT \
    -Dpackaging=jar \
    -DpomFile=/app/pom.xml

# Copy the rest of the application's source code
COPY src /app/src
COPY application.yml /app/src/main/resources/

# Build and skip tests to speed up the process
RUN mvn package -DskipTests

# Final stage to prepare the runtime image
FROM openjdk:18-jdk-slim
WORKDIR /app
COPY --from=build /app/target/spalod-0.0.1-SNAPSHOT.jar /app/

# Set the default command to run the jar
CMD ["java", "-jar", "spalod-0.0.1-SNAPSHOT.jar"]