# This could also be another Ubuntu or Debian based distribution
FROM ubuntu:22.04

# Install maven
RUN apt update && apt upgrade -y
RUN apt install openjdk-17-jdk -y && apt install maven -y
RUN apt install curl -y
RUN apt install sudo
RUN curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash -
RUN apt-get install nodejs -y

# # Clone the repository
RUN apt install git -y

RUN git clone --branch youneskamli https://github.com/JJponciano/SpaLod.git /home/spalod


# Add a step to checkout the desired branch
WORKDIR /home/spalod

RUN mvn install:install-file \
-Dfile=/home/spalod/libs/pisemantic-1.0-SNAPSHOT.jar \
-DpomFile=/home/spalod/pom.xml \
-DgroupId=info.ponciano.lab \
-DartifactId=pisemantic \
-Dversion=1.0 \
-Dpackaging=jar

RUN mvn install:install-file \
-Dfile=/home/spalod/libs/pitools-1.0-SNAPSHOT.jar \
-DpomFile=/home/spalod/pom.xml \
-DgroupId=info.ponciano.lab \
-DartifactId=pitools \
-Dversion=1.0-SNAPSHOT\
-Dpackaging=jar


# command not cached
ARG CACHEBUST=1
RUN git -C  /home/spalod/ pull
RUN mvn -f /home/spalod/pom.xml package -DskipTests
RUN chmod 755 /home/spalod/spalod.sh
