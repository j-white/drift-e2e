FROM fedora:26
LABEL maintainer "Jesse White <jesse@opennms.ca>"
RUN yum -y --setopt=tsflags=nodocs update && \
    yum -y install net-snmp-libs net-snmp-agent-libs && \
    yum clean all
COPY ./udpgen /
ENTRYPOINT [ "/udpgen" ]
CMD [ "-h" ]
