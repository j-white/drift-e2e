FROM fedora:26
LABEL maintainer "Jesse White <jesse@opennms.ca>"
COPY ./flowgen /
ENTRYPOINT [ "/flowgen" ]
CMD [ "-i","30","-f","30","-p","8877","127.0.0.1" ]
