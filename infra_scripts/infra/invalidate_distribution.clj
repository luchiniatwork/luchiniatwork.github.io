(ns infra.invalidate-distribution
  (:require [cognitect.aws.client.api :as aws]))

(def cf (aws/client {:api :cloudfront}))

(defn -main [& args]
  (if (not= 1 (count args))
    (println "Syntax: $ clojure -A:infra -m infra.invalidate-distribution <distribution-id>")
    (let [distribution-id (first args)
          request {:DistributionId distribution-id
                   :InvalidationBatch
                   {:Paths {:Quantity 1
                            :Items ["/*"]}
                    :CallerReference (str (System/currentTimeMillis))}}
          {:keys [Location]} (aws/invoke cf {:op :CreateInvalidation
                                             :request request})]
      (println Location))))
