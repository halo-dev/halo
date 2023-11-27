Please add the corresponding e2e (aka end-to-end) test cases if you add or update APIs.

## How to work
* Start and watch the [docker-compose](https://docs.docker.com/compose/) via [the script](start.sh)
  * It has three containers: database, Halo, and testing
* Run the e2e testing via [api-testing](https://github.com/LinuxSuRen/api-testing)
  * It will run the test cases from top to bottom
  * You can add the necessary asserts to it

## Run locally
Please follow these steps if you want to run the e2e testing locally.

> Please make sure you have installed docker-compose v2

* Build project via `./gradlew clean build -x check` in root directory of this repository
* Build image via `docker build . -t ghcr.io/halo-dev/halo-dev:main`
* Change the directory to `e2e`, then execute `./start.sh`

## Run Halo only
Please run the following command if you only want to run Halo.

```shell
docker-compose up halo
```
