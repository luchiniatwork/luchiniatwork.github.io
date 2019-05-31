(ns infra.route53
  (:require ["@pulumi/aws" :as aws]
            ["@pulumi/pulumi" :as pulumi]
            [clojure.string :as string]))

(defn ^:private parse-target-domain [domain]
  (let [parts (string/split domain #"\.")]
    (cond
      (< (count parts) 2)
      (throw (ex-info "No TLD provided" {:domain domain}))
      (= (count parts) 2)
      {:sub-domain ""
       :parent-domain (str domain ".")}
      :otherwise
      {:sub-domain (first parts)
       :parent-domain (str (string/join "." (rest parts)) ".")})))

(defn ^:private create-alias-record!
  [target-domain cdn-domain-name cdn-hosted-zone-id]
  (let [{:keys [sub-domain parent-domain]} (parse-target-domain target-domain)
        hosted-zone (-> aws/route53
                        (.getZone #js {:name parent-domain})
                        pulumi/output)]
    (aws/route53.Record. target-domain
                         (clj->js {:name sub-domain
                                   :zoneId (.-zoneId hosted-zone)
                                   :type "A"
                                   :aliases [{:name cdn-domain-name
                                              :zoneId cdn-hosted-zone-id
                                              :evaluateTargetHealth false}]}))))

(defn run [{:keys [config/target-domain
                   config/extra-aliases
                   cloudfront/static-domain-name
                   cloudfront/static-hosted-zone-id
                   cloudfront/redirect-domain-name
                   cloudfront/redirect-hosted-zone-id] :as opts}]
  (create-alias-record! target-domain
                        static-domain-name
                        static-hosted-zone-id)
  (doseq [extra-alias extra-aliases]
    (create-alias-record! extra-alias
                          redirect-domain-name
                          redirect-hosted-zone-id))
  opts)
