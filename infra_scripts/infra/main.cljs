(ns infra.main
  (:require ["@pulumi/aws" :as aws]
            ["@pulumi/pulumi" :as pulumi]
            [goog :as goog]
            [infra.s3 :as s3]))

(def exportable (atom {}))

(let [{:keys [bucket-name website-url]} (s3/run)]
  (swap! exportable assoc :bucket-name bucket-name)
  (swap! exportable assoc :website-url website-url))

(goog/exportSymbol "bucket-name" (:bucket-name @exportable))
(goog/exportSymbol "website-url" (:website-url @exportable))
