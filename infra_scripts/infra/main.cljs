(ns infra.main
  (:require ["@pulumi/aws" :as aws]
            ["@pulumi/pulumi" :as pulumi]
            [goog :as goog]
            [infra.s3 :as s3]))

(def config-obj (pulumi/Config.))

(def config
  {:path-to-website-contents (.require config-obj "path-to-website-contents")
   :target-domain (.require config-obj "target-domain")})

(def exportable (atom {}))

(let [{:keys [path-to-website-contents target-domain]} config
      {:keys [bucket-name website-url]} (s3/run
                                          path-to-website-contents
                                          target-domain)]
  (swap! exportable assoc :bucket-name bucket-name)
  (swap! exportable assoc :website-url website-url))

(goog/exportSymbol "bucket-name" (:bucket-name @exportable))
(goog/exportSymbol "website-url" (:website-url @exportable))
