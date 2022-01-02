# syntax = docker/dockerfile:experimental

# ------------------------------------------------------------------------------
# BUILD STAGE
# ------------------------------------------------------------------------------

FROM maven:3.8.4-openjdk-11 as build

ARG ARTIFACT_VERSION=0.1
ARG MAVEN_OPTS

WORKDIR /workspace/

COPY pom.xml .
COPY src src

RUN --mount=type=cache,target=/root/.m2/ \
    mvn -B -e -DnewVersion=${ARTIFACT_VERSION} versions:set package -DskipTests

# ------------------------------------------------------------------------------
# RUNTIME STAGE (deployment)
# ------------------------------------------------------------------------------

FROM openjdk:11.0.11-jre-slim

ARG ARTIFACT_VERSION=1.0
ENV app_name=portfolio
ENV app_user=appuser

RUN addgroup ${app_user} && adduser --ingroup ${app_user} ${app_user}

RUN mkdir -p /opt/logs \
    && chown ${app_user}:${app_user} /opt/logs -R \
    && mkdir -p /opt/software/${app_name} \
    && chown ${app_user}:${app_user} /opt/software/${app_name} -R

COPY --from=build /workspace/target/${app_name}-${ARTIFACT_VERSION}.jar /opt/software/${app_name}.jar

WORKDIR /opt/software/

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=$PROFILE -jar ${app_name}.jar"]
