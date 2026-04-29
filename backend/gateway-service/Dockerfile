FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml ./
COPY common-orm/pom.xml common-orm/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY placement-service/pom.xml placement-service/pom.xml
COPY wallet-service/pom.xml wallet-service/pom.xml
COPY gateway-service/pom.xml gateway-service/pom.xml
COPY config-service/pom.xml config-service/pom.xml
COPY discovery-service/pom.xml discovery-service/pom.xml
COPY project-service/pom.xml project-service/pom.xml
COPY audit-service/pom.xml audit-service/pom.xml
COPY expense-service/pom.xml expense-service/pom.xml
COPY product-service/pom.xml product-service/pom.xml
COPY commerce-service/pom.xml commerce-service/pom.xml
COPY fulfillment-service/pom.xml fulfillment-service/pom.xml
COPY promotion-service/pom.xml promotion-service/pom.xml
RUN mvn -q -B -e -DskipTests -pl gateway-service -am dependency:go-offline

COPY gateway-service gateway-service
COPY common-orm common-orm

RUN mvn -q -B -DskipTests -pl gateway-service -am package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/gateway-service/target/*.jar /app/gateway-service.jar

EXPOSE 9443

ENTRYPOINT ["java","-jar","/app/gateway-service.jar"]
