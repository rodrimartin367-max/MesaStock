# ---------- Etapa 1: compilar el WAR con Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Primero solo el pom, para cachear las dependencias
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Después el código fuente y el empaquetado
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---------- Etapa 2: ejecutar en Tomcat ----------
FROM tomcat:10.1-jdk21-temurin

# Quitamos las aplicaciones de ejemplo de Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copiamos solo el WAR desde la etapa de compilación
COPY --from=build /build/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]