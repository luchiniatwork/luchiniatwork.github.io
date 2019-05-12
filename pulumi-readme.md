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

pulumi login

pulumi stack ls
pulumi stack select dev
pulumi stack output

clojure -A:infra

pulumi preview
pulumi update


# minifying a CSS file (e.g. style.css -> style.min.css)
curl -X POST -s --data-urlencode 'input@style.css' https://cssminifier.com/raw > style.min.css

aws cloudfront create-invalidation --distribution-id <dist-id> --paths /index.html
