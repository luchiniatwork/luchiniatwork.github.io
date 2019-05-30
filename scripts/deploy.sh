#!/bin/bash

export TARGET_DOMAIN=$(pulumi config get target-domain)

echo "+ Building redirect lamda."
echo "  - redirect to: ${TARGET_DOMAIN}"
clojure -A:infra:build-lambda
