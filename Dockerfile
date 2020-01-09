FROM maven:3.6.3-jdk-8-slim

MAINTAINER Jan Forberg <forberg@infai.org>

COPY ./client /client

WORKDIR /client

RUN mvn package

ENTRYPOINT /bin/bash download.sh
