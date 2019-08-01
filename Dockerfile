FROM maven:3-jdk-8

WORKDIR /code
ADD . .
RUN mvn package
RUN mv target/libpdf*-dependencies.jar target/libpdf.jar

WORKDIR /ebook

ENTRYPOINT [ "java", "-jar", "/code/target/libpdf.jar"]

