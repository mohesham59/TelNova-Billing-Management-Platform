# ============================================================
#  TeleMeter  (Telemeter-one.war)
#  CDR Parser + Billing Job
#  Multi-stage: Maven build → Tomcat runtime
# ============================================================

# ---------- Stage 1: Build ----------
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Paths are relative to PROJECT ROOT (build context = .)
COPY apps/TeleMeter/pom.xml .
RUN mvn dependency:go-offline -q

COPY apps/TeleMeter/src ./src
RUN mvn package -DskipTests -q

# ---------- Stage 2: Runtime ----------
FROM tomcat:10.1-jre17-temurin

LABEL maintainer="TeleMeter Billing Platform"

# Remove default Tomcat webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy built WAR
COPY --from=builder /build/target/Telemeter-one.war /usr/local/tomcat/webapps/Telemeter-one.war

# context.xml lives in docker/telemeter/ relative to project root
COPY docker/telemeter/docker-context.xml /usr/local/tomcat/conf/context.xml

# CDR archive directory (mounted from host via Docker volume)
RUN mkdir -p /app/CDRArchive

EXPOSE 8080

CMD ["catalina.sh", "run"]
