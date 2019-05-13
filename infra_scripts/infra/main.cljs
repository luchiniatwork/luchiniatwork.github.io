(ns infra.main
  (:require ["@pulumi/aws" :as aws]
            ["@pulumi/pulumi" :as pulumi]
            [goog :as goog]
            [infra.cloudfront :as cloudfront]
            [infra.s3 :as s3]))

#_(def config-obj (pulumi/Config.))

#_(def config
    {:path-to-website-contents (.require config-obj "path-to-website-contents")
     :target-domain (.require config-obj "target-domain")
     :certificate-arn (.require config-obj "certificate-arn")})

(def exportable (atom {}))

(def two-hours (* 2 60 60))

(def an-year (* 365 24 60 60))

(defn init-config-map [config-obj]
  {:config/path-to-website-contents (.require config-obj "path-to-website-contents")
   :config/target-domain (.require config-obj "target-domain")
   :config/certificate-arn (.require config-obj "certificate-arn")
   :config/cache-max-age (or (.get config-obj "cache-max-age") two-hours)
   :config/cache-s-max-age (or (.get config-obj "cache-s-max-age") an-year)})

(let [{:keys [config/target-domain
              s3/content-bucket
              s3/log-bucket
              cloudfront/domain-name] :as infra-map}
      (-> (pulumi/Config.)
          init-config-map
          s3/run
          cloudfront/run)]
  (swap! exportable assoc

         :content-bucket-name
         (:name content-bucket)

         :content-bucket-website-endpoint
         (:website-endpoint content-bucket)

         :log-bucket-name
         (:name log-bucket)

         :cloudfront-domain
         domain-name

         :target-url
         (str "https://" target-domain)))

#_(let [{:keys [path-to-website-contents target-domain]} config
        {:keys [bucket-name website-url]} (s3/run
                                            path-to-website-contents
                                            target-domain)]
    (swap! exportable assoc :bucket-name bucket-name)
    (swap! exportable assoc :website-url website-url))

(goog/exportSymbol "content-bucket-name" (:content-bucket-name @exportable))
(goog/exportSymbol "content-bucket-website-endpoint" (:content-bucket-website-endpoint @exportable))
(goog/exportSymbol "log-bucket-name" (:log-bucket-name @exportable))
(goog/exportSymbol "cloudfront-domain" (:cloudfront-domain @exportable))
(goog/exportSymbol "target-url" (:target-url @exportable))
