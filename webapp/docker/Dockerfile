FROM ubuntu:16.04

# Install Kurento Media Server (KMS) 
RUN echo "deb [arch=amd64] http://ubuntu.openvidu.io/6.13.0 xenial kms6" | tee /etc/apt/sources.list.d/kurento.list \
	&& apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 5AFA7A83 \
	&& apt-get update \
	&& apt-get -y install kurento-media-server \
	&& rm -rf /var/lib/apt/lists/*

RUN apt-get update \
	&& apt-get install -y curl \
	&& curl -sL https://deb.nodesource.com/setup_8.x | bash \
	&& apt-get install -y nodejs \
	&& npm install http-server -g

# Install Java, supervisor and netstat
RUN apt-get update && apt-get install -y \
  openjdk-8-jre \
  supervisor \
&& rm -rf /var/lib/apt/lists/*

# Configure supervisor
RUN mkdir -p /var/log/supervisor
COPY webapp/docker/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Copy OpenVidu Server
COPY webapp/docker/openvidu-server.jar openvidu-server.jar
COPY webapp/docker/openvidu.sh /openvidu.sh

# Copy KMS entrypoint
COPY webapp/docker/kms.sh /kms.sh

# Copy webapp repository
COPY webapp/ /webapp

EXPOSE 8888
EXPOSE 8080
EXPOSE 9091
EXPOSE 4443

# Exec supervisord
CMD ["/usr/bin/supervisord"]

# BUILD docker build -t maes95/openvidu-server-kms -f webapp/docker/Dockerfile .
# RUN docker run  --name $ET_SUT_CONTAINER_NAME -network $ET_NETWORK --rm -e openvidu.secret=MY_SECRET maes95/openvidu-server-kms