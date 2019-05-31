(ns infra.cloudfront
  (:require ["@pulumi/aws" :as aws]
            ["@pulumi/pulumi" :as pulumi]))

(defn ^:private distribution-args
  [{:keys [target-domain
           aliases
           origin-arn
           origin-domain-name
           origin-path
           origin-protocol-policy
           certificate-arn
           logs-bucket-domain-name
           default-ttl-minutes
           max-ttl-minutes
           forward-query-string?]}]
  (clj->js {:enabled true
            :compress true
            :aliases aliases
            :origins [{:originId origin-arn
                       :domainName origin-domain-name
                       :originPath (or origin-path "")
                       :customOriginConfig
                       {:originProtocolPolicy origin-protocol-policy
                        :httpPort 80
                        :httpsPort 443
                        :originSslProtocols ["TLSv1.2"]}}]
            :defaultRootObject "index"
            :defaultCacheBehavior {:targetOriginId origin-arn
                                   :viewerProtocolPolicy "redirect-to-https"
                                   :allowedMethods ["GET" "HEAD" "OPTIONS"],
                                   :cachedMethods ["GET" "HEAD" "OPTIONS"]
                                   :forwardedValues {:cookies {:forward "none"}
                                                     :queryString (or forward-query-string?
                                                                      false)}
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

(defn ^:private domain-from-url [url]
  (-> url
      (.apply #(second (re-matches #"^https://([^/]+)/(.+)" %)))))

(defn ^:private path-from-url [url]
  (-> url
      (.apply #(str "/" (last (re-matches #"^https://([^/]+)/(.+)" %))))))

(defn run [{:keys [config/target-domain
                   config/extra-aliases
                   config/certificate-arn
                   config/cache-s-max-age
                   s3/content-bucket
                   s3/logs-bucket
                   apigateway/invoke-url
                   apigateway/deployment] :as opts}]
  (let [cdn-static (aws/cloudfront.Distribution.
                    "cdn-static"
                    (distribution-args
                     {:target-domain target-domain
                      :aliases [target-domain]
                      :origin-arn (:arn content-bucket)
                      :origin-domain-name (:website-endpoint content-bucket)
                      :origin-protocol-policy "http-only"
                      :certificate-arn certificate-arn
                      :logs-bucket-domain-name (:domain-name logs-bucket)
                      :default-ttl-minutes cache-s-max-age
                      :max-ttl-minutes cache-s-max-age
                      :forward-query-string? false}))
        cdn-redirect (aws/cloudfront.Distribution.
                      "cdn-redirect"
                      (distribution-args
                       {:target-domain target-domain
                        :aliases extra-aliases
                        :origin-arn (.-executionArn deployment)
                        :origin-domain-name (domain-from-url invoke-url)
                        :origin-path (path-from-url invoke-url)
                        :origin-protocol-policy "https-only"
                        :certificate-arn certificate-arn
                        :logs-bucket-domain-name (:domain-name logs-bucket)
                        :default-ttl-minutes cache-s-max-age
                        :max-ttl-minutes cache-s-max-age
                        :forward-query-string? true}))]
    (merge opts {:cloudfront/domain-name (.-domainName cdn-static)
                 :cloudfront/hosted-zone-id (.-hostedZoneId cdn-static)})))
