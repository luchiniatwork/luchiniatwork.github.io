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

(defn init-config-map [config-obj]
  {:config/path-to-website-contents (.require config-obj "path-to-website-contents")
   :config/target-domain (.require config-obj "target-domain")
   :config/certificate-arn (.require config-obj "certificate-arn")})

(let [infra-map (-> (pulumi/Config.)
                    init-config-map
                    s3/run
                    cloudfront/run)]
  (swap! exportable assoc :bucket-name (-> infra-map :s3/content-bucket :name))
  (swap! exportable assoc :website-url (-> infra-map :s3/content-bucket :website-endpoint)))

#_(let [{:keys [path-to-website-contents target-domain]} config
        {:keys [bucket-name website-url]} (s3/run
                                            path-to-website-contents
                                            target-domain)]
    (swap! exportable assoc :bucket-name bucket-name)
    (swap! exportable assoc :website-url website-url))

(goog/exportSymbol "bucket-name" (:bucket-name @exportable))
(goog/exportSymbol "website-url" (:website-url @exportable))
