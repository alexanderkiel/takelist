FROM java:openjdk-8-alpine

ADD target/takelist-*-standalone.jar /takelist.jar

CMD ["/usr/bin/java", "-jar", "/takelist.jar"]
