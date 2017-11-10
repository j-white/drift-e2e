#!/bin/bash -e
# Error codes
E_ILLEGAL_ARGS=126

# Help function used in error messages and -h option
usage() {
    echo ""
    echo "Docker entry script for Burrow container"
    echo ""
    echo "-f: Initialize and start Burrow in foreground."
    echo "-h: Show this help."
    echo ""
}

initConfig() {
    mkdir -p /etc/burrow
    cat <<EOT >> /etc/burrow/burrow.cfg
[general]
logconfig=/etc/burrow/logging.cfg
group-blacklist=^(console-consumer-|python-kafka-consumer-).*$

[zookeeper]
hostname=zk-cs
port=2181
timeout=6
lock-path=/burrow/notifier

[kafka "local"]
broker=kafka-hs
broker-port=9092
offsets-topic=__consumer_offsets
zookeeper=zk-cs
zookeeper-path=/
zookeeper-offsets=true
offsets-topic=__consumer_offsets

[tickers]
broker-offsets=60

[lagcheck]
intervals=10
expire-group=604800
zookeeper-interval=15
zk-group-refresh=60

[httpserver]
server=on
port=8000
EOT

    cat <<EOT >> /etc/burrow/logging.cfg
<seelog minlevel="info">
  <outputs formatid="main">
    <console />
  </outputs>
  <formats>
    <format id="main" format="%Date(2006-01-02 15:04:05) [%LEVEL] %Msg%n"/>
  </formats>
</seelog>
EOT
}

start() {
    cat /etc/burrow/burrow.cfg
    /go/bin/burrow --config /etc/burrow/burrow.cfg
}

# Evaluate arguments for build script.
if [[ "${#}" == 0 ]]; then
    usage
    exit ${E_ILLEGAL_ARGS}
fi

# Evaluate arguments for build script.
while getopts fhis flag; do
    case ${flag} in
        f)
            initConfig
            start
            ;;
        h)
            usage
            exit
            ;;
        *)
            usage
            exit ${E_ILLEGAL_ARGS}
            ;;
    esac
done

# Strip of all remaining arguments
shift $((OPTIND - 1));

# Check if there are remaining arguments
if [[ "${#}" > 0 ]]; then
    echo "Error: To many arguments: ${*}."
    usage
    exit ${E_ILLEGAL_ARGS}
fi
