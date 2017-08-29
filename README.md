# Takelist

[![Build Status](https://travis-ci.org/alexanderkiel/takelist.svg?branch=master)](https://travis-ci.org/alexanderkiel/takelist)

An application which manages things like coffee or drinks which are available in a shared space at work. Takelist is like an honesty box as a service.

## Usage

### Run as docker container

In order to run the takelist app as docker container you need to compile the
project, build an uberjar and package this in a docker image providing a jvm.
For this purpose a Makefile is provided including the necessary steps. To be
be able to execute the Makefile you need the programs `make`, `grep`, `lein`
and `docker` preinstalled on your machine. The build process is started by
executing

`make docker-image`

After the image has been successful built run the takelist app using docker and
providing the necessary parameters as environment variables, which are:

- `DATABASE_URI` - the file path to the H2 database (`/h2/db`). This folder
 (here `/h2`) should be mounted from outside the docker container to survive
 restarts.
- `BASE_URI` - the protocol, hostname and port number the app will be accessed
 (e.q. `http://localhost:8081`)
- `CLIENT_ID` - the google OAuth client id
- `CLIENT_SECRET` - the google OAuth client secret

The resulting docker command is

`docker run -d --name takelist -p 8081:8080 -v /home/user/.takelist/:/h2 -e DATABASE_URI=/h2/db -e BASE_URI=http://localhost:8081 -e CLIENT_ID 123456-foobar.apps.googleusercontent.com -e CLIENT_SECRET ABCDEF1234 takelist:<VERSION>`

`<VERSION>` is the docker image tag which is printed right after the docker
image build or by running `docker images`.

## Authentication

We use [Google Sign-In for server-side apps][1].

## License

Copyright © 2016 Alexander Kiel, Ying-Chi Lin, Thomas Peschel, Matthias Reusche, Mathias Rühle, Alexander Twrdik, Jonas Wagner

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[1]: <https://developers.google.com/identity/sign-in/web/server-side-flow>
