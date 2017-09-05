VERSION = $(shell /usr/bin/grep defproject project.clj | /usr/bin/grep -Eoh "\d+(\.\d+)+(-SNAPSHOT)?")

target/takelist-%-standalone.jar:
	lein uberjar

docker-image: target/takelist-%-standalone.jar
	docker build -t takelist:$(VERSION) .

clean:
	lein clean

.PHONY: clean
