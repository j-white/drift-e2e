FROM golang:alpine

MAINTAINER LinkedIn Burrow "https://github.com/linkedin/Burrow"

RUN apk add --no-cache curl bash git ca-certificates wget \
 && update-ca-certificates \
 && curl -sSO https://raw.githubusercontent.com/pote/gpm/v1.4.0/bin/gpm \
 && chmod +x gpm \
 && mv gpm /usr/local/bin

RUN go get github.com/linkedin/Burrow \
 && cd $GOPATH/src/github.com/linkedin/Burrow \
 && gpm install \
 && go install \
 && mv $GOPATH/bin/Burrow $GOPATH/bin/burrow \
 && apk del git curl wget

COPY ./docker-entrypoint.sh /

WORKDIR /var/tmp/burrow

ENTRYPOINT [ "/docker-entrypoint.sh" ]
CMD [ "-f" ]
