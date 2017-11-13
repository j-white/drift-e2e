FROM opennms/openjdk:8u151-jdk

LABEL maintainer "Ronny Trommer <ronny@opennms.org>"

ARG MINION_VERSION=branches-features-drift

ENV MINION_HOME /opt/minion
ENV MINION_LOCATION MINION
ENV MINION_CONFIG /opt/minion/etc/org.opennms.minion.controller.cfg
ENV MINION_ID 00000000-0000-0000-0000-deadbeef0001
ENV OPENNMS_BROKER_URL tcp://127.0.0.1:61616
ENV OPENNMS_HTTP_URL http://127.0.0.1:8980/opennms

RUN yum -y --setopt=tsflags=nodocs update && \
    rpm -Uvh http://yum.opennms.org/repofiles/opennms-repo-${MINION_VERSION}-rhel7.noarch.rpm && \
    rpm --import http://yum.opennms.org/OPENNMS-GPG-KEY && \
    yum -y install opennms-minion && \
    yum clean all && \
    rm -rf /var/cache/yum

COPY ./docker-entrypoint.sh /

VOLUME [ "/opt/minion/etc", "/opt/minion/data" ]

HEALTHCHECK --interval=10s --timeout=3s CMD /opt/minion/bin/client ping | grep -Pzo "(?s).*OK.*.OK.*" || exit 1

LABEL license="AGPLv3" \
      org.opennms.horizon.version="${MINION_VERSION}" \
      vendor="OpenNMS Community" \
      name="Minion"

# Workaround for Felix bug on GCP: invalid version "4.4.64.": invalid format
#java.lang.IllegalArgumentException: invalid version "4.4.64.": invalid format
#        at org.osgi.framework.Version.<init>(Version.java:147) ~[org.osgi.core-6.0.0.jar:?]
#        at org.apache.felix.framework.util.manifestparser.NativeLibraryClause.normalizeOSVersion(NativeLibraryClause.java:770) ~[?:?]
#        at org.apache.felix.framework.Felix.initializeFrameworkProperties(Felix.java:4617) ~[?:?]
#        at org.apache.felix.framework.Felix.<init>(Felix.java:384) ~[?:?]
#        at org.apache.felix.framework.FrameworkFactory.newFramework(FrameworkFactory.java:28) ~[?:?]
#        at org.apache.karaf.main.Main.launch(Main.java:256) ~[org.apache.karaf.main-4.1.2.jar:4.1.2]
ENV JAVA_OPTS -Dos.version=4.13.11-200.fc26.x86_64

ENTRYPOINT [ "/docker-entrypoint.sh" ]

CMD [ "-f" ]

##------------------------------------------------------------------------------
## EXPOSED PORTS
##------------------------------------------------------------------------------
## -- OpenNMS KARAF SSH   8201/TCP
## -- OpenNMS JMX        18980/TCP
## -- SNMP Trapd           162/UDP
## -- Syslog               514/UDP
## -- Netflow 5           8877/UDP
EXPOSE 8201 18980 162/udp 514/udp 8877/udp
