FROM opennms/openjdk:8u151-jdk

LABEL maintainer "Jesse White <jesse@opennms.ca>"

COPY ./esstress.jar /
COPY ./docker-entrypoint.sh /

ENTRYPOINT [ "/docker-entrypoint.sh" ]
