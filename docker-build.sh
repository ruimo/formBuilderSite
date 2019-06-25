#!/bin/sh
sbt clean universal:packageZipTarball
docker build --no-cache -t fcap/devsite:${TAG_NAME:-latest} .
