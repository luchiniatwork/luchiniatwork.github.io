#!/usr/bin/env bash
set -e

heading () {
  printf "\n\e[31m$1...\e[0m\n\n"
}

export CSS=resources/public/css/style.css
export TARGET_DOMAIN=$(pulumi config get target-domain)

heading "0. Clean up"
rm -rf out/
rm -rf resources/public/

# ------------------------------
heading "1. Generating static assets"
lein run

# ------------------------------
heading "2. Minifying CSS"
curl -X POST -s --data-urlencode 'input@$CSS' https://cssminifier.com/raw > $CSS

# ------------------------------
heading "3. Building redirect lambda"
clojure -A:infra:build-lambda

# ------------------------------
heading "4. Packaging redirect lambda"
cd out/lambda
zip redirect.zip index.js
cd ../..

# ------------------------------
heading "5. Building infra scripts"
clojure -A:infra:build

# ------------------------------
heading "6. Deploying infra"
pulumi up

# ------------------------------
heading "7. Invalidating CDN cache"
export STATIC_DISTRO_ID=$(pulumi stack output static-cloudfront-distribution-id)
export REDIRECT_DISTRO_ID=$(pulumi stack output redirect-cloudfront-distribution-id)

clojure -A:infra -m infra.invalidate-distribution $STATIC_DISTRO_ID
clojure -A:infra -m infra.invalidate-distribution $REDIRECT_DISTRO_ID

heading "Done"
