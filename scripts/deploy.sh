#!/bin/bash

export TARGET_DOMAIN=$(pulumi config get target-domain)

echo "1. Building redirect lambda"
clojure -A:infra:build-lambda

echo "2. Packaging redirect lambda"
cd out/lambda
zip -q redirect.zip index.js
cd ../..

echo "3. Building infra scripts"
clojure -A:infra:build
