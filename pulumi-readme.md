# Reqs

clojure
node/npm
pulumi
./aws/credentials

``` text
[default]
aws_access_key_id = your_access_key_id
aws_secret_access_key = your_secret_access_key
```

# Reqs on AWS

(optional) domain
need route53 hosted zone first
acm certificate

# Starting with pulumi

pulumi login
pulumi config

pulumi stack ls
pulumi stack select dev

# Using pulumi

clojure -A:infra:build

pulumi preview
pulumi update
pulumi stack output

# Lambda

clojure -A:infra:build-lambda

# Others to add

##  minifying a CSS file (e.g. style.css -> style.min.css)
curl -X POST -s --data-urlencode 'input@style.css' https://cssminifier.com/raw > style.min.css

## Invalidate cache
aws cloudfront create-invalidation --distribution-id <dist-id> --paths /index.html



------

# Need to add to pulumi

lamda
- compile lambda
- create lambda
api gateway
- create api
- create stage
- deploy api
cloudfront
- distro to api
