# This could also be another Ubuntu or Debian based distribution
FROM ubuntu:22.04

# Install maven
RUN apt update && apt upgrade -y
RUN apt install openjdk-17-jdk -y && apt install maven -y
RUN apt install curl -y
RUN apt install sudo
RUN curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash -
RUN apt-get install nodejs -y
RUN apt-get install unzip

# # Clone the repository
RUN apt install git -y

RUN git clone --branch youneskamli https://github.com/JJponciano/SpaLod.git /home/spalod

WORKDIR /home/graphdb

RUN curl -k -o graphdb.zip "https://download.ontotext.com/owlim/b07b1aba-e359-11ed-b5b7-42843b1b6b38/graphdb-10.2.1-dist.zip?__hstc=95638467.cc456c1a5e66b7c07f080e8acbd24576.1681727451955.1686139172296.1686141753795.6&__hssc=95638467.10.1686141753795&hsCtaTracking=ed4ee978-afe2-438d-b2a5-139cf9c665bb%7C3d3cab68-ee83-4dfc-a040-aa707e9a751a"
RUN unzip graphdb.zip

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

CMD /home/graphdb/graphdb-10.2.1/bin/graphdb

#docker build -t spalod .
#docker run -p 8080:8080 -p 8081:8081 -p 7200:7200 -d -i --name spalod-container spalod
#docker exec spalod-container /home/spalod/spalod.sh