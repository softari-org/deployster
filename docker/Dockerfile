FROM gradle:jdk8-jammy as build

WORKDIR /build
COPY gradle.properties ./
COPY gradle/ gradle/
COPY *.gradle.kts ./
COPY src/ src/

RUN \
    apt-get update && \
    # Install native dependencies
    apt-get install -y --no-install-recommends libgcc-12-dev libstdc++-12-dev libssl-dev libcurl4-openssl-dev && \
    # The Kotlin native compiler refuses to look for these object files in the right location
    cp /usr/lib/x86_64-linux-gnu/crt* /usr/lib/ && \
    gradle build --info --exclude-task nativeTest

FROM ubuntu:jammy as release

WORKDIR /opt/opstopus

COPY --from=build /build/build/bin/native/releaseExecutable/deploptopus.kexe /usr/bin/deploptopus
RUN \
   apt-get update && \
   apt-get install -y --no-install-recommends libssl3 libcurl4 && \
   # Remove apt lists
   rm -rf /var/lib/apt/lists/*

COPY default.config.json /etc/deploptopus/config.json

ENTRYPOINT ["deploptopus"]
