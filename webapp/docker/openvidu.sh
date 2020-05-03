#!/bin/bash
export OPENVIDU_URL=$(hostname -i);
java -jar -Dspring.profiles.active=docker -Dopenvidu.publicurl="https://${OPENVIDU_URL}:4443/" /openvidu-server.jar;
#java -jar -Dopenvidu.secret=MY_SECRET -Dopenvidu.publicurl="https://34.204.3.249:4443/" /openvidu-server.jar;