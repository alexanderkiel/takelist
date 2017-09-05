VERSION = $(shell /usr/bin/grep defproject project.clj | /usr/bin/grep -Poh "\d+(\.\d+)+(-SNAPSHOT)?")

target/takelist-%-standalone.jar:
	lein compile
	lein uberjar

docker-image: target/takelist-%-standalone.jar
	docker build -t takelist:$(VERSION) .
