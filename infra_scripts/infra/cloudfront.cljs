(ns infra.cloudfront
  (:require ["@pulumi/aws" :as aws]
            ["@pulumi/pulumi" :as pulumi]))

(defn distribution-args [{:keys [target-domain
                                 content-bucket-arn
                                 content-bucket-website-endpoint
                                 certificate-arn
                                 logs-bucket-domain-name
                                 default-ttl-minutes
                                 max-ttl-minutes]}]
  (clj->js {:enabled true
            :compress true
            :aliases [target-domain]
            :origins [{:originId content-bucket-arn
                       :domainName content-bucket-website-endpoint
                       :customOriginConfig
                       {:originProtocolPolicy "http-only"
                        :httpPort 80
                        :httpsPort 443
                        :originSslProtocols ["TLSv1.2"]}}]
            :defaultRootObject "index"
            :defaultCacheBehavior {:targetOriginId content-bucket-arn
                                   :viewerProtocolPolicy "redirect-to-https"
                                   :allowedMethods ["GET" "HEAD" "OPTIONS"],
                                   :cachedMethods ["GET" "HEAD" "OPTIONS"]
                                   :forwardedValues {:cookies {:forward "none"}
                                                     :queryString false}
                                   :minTtl 0
                                   :defaultTtl default-ttl-minutes
                                   :maxTtl max-ttl-minutes}
            :priceClass "PriceClass_100"
            :customErrorResponses [{:errorCode 404
                                    :responseCode 404
                                    :responsePagePath "/404"}]
            :restrictions {:geoRestriction {:restrictionType "none"}}
            :viewerCertificate {:acmCertificateArn certificate-arn
                                :sslSupportMethod "sni-only"}
            :loggingConfig {:bucket logs-bucket-domain-name
                            :includeCookies false
                            :prefix (str target-domain "/")}}))

(defn run [{:keys [config/target-domain
                   config/certificate-arn
                   config/cache-s-max-age
                   s3/content-bucket
                   s3/logs-bucket] :as opts}]
  (let [cdn (aws/cloudfront.Distribution.
             "cdn"
             (distribution-args
              {:target-domain target-domain
               :content-bucket-arn (:arn content-bucket)
               :content-bucket-website-endpoint (:website-endpoint content-bucket)
               :certificate-arn certificate-arn
               :logs-bucket-domain-name (:domain-name logs-bucket)
               :default-ttl-minutes cache-s-max-age
               :max-ttl-minutes cache-s-max-age}))]
    (merge opts {:cloudfront/domain-name (.-domainName cdn)
                 :cloudfront/hosted-zone-id (.-hostedZoneId cdn)})))
