# ============================================================
#  telecom-billing portal  (telnova.war)
#  Multi-stage: Maven build → Tomcat runtime
# ============================================================

# ---------- Stage 1: Build ----------
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Paths are relative to PROJECT ROOT (build context = .)
COPY apps/telecom-billing/pom.xml .
# Download dependencies first (layer cache friendly)
RUN mvn dependency:go-offline -q

COPY apps/telecom-billing/src ./src
RUN mvn package -DskipTests -q

# ---------- Stage 2: Runtime ----------
FROM tomcat:10.1-jre17-temurin

LABEL maintainer="TeleMeter Billing Platform"

# Remove default Tomcat webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy built WAR
COPY --from=builder /build/target/telnova.war /usr/local/tomcat/webapps/telnova.war

# context.xml lives in docker/telecom-billing/ relative to project root
COPY docker/telecom-billing/docker-context.xml /usr/local/tomcat/conf/context.xml

# Expose Tomcat port
EXPOSE 8080

CMD ["catalina.sh", "run"]
