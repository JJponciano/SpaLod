# This could also be another Ubuntu or Debian based distribution
FROM ubuntu:impish

# Install maven
RUN apt update && apt upgrade -y
RUN apt install openjdk-17-jdk -y && apt install maven -y

# Clone the repository
RUN apt install git -y

RUN git clone https://github.com/JJponciano/SpaLod.git /home/spalod

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


