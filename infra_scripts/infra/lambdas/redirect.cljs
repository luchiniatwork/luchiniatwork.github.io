(ns infra.lambdas.redirect
  (:require [clojure.string :as string])
  (:require-macros [infra.lambdas.baked :refer [defbaked]]))

(defbaked target-domain {:env-var "TARGET_DOMAIN"})

(defn ^:private create-request-handler
  [handler]
  (fn [event context callback]
    (let [event' (js->clj event :keywordize-keys true)]
      (callback nil (clj->js (handler event' context))))))

(defn ^:private build-query-string [m]
  (if m
    (->> m
         (map (fn [[k v]] (str (name k) "=" v)))
         (string/join "&")
         (str "?"))
    ""))

(defn ^:private redirect-handler
  [{:keys [path queryStringParameters] :as event} context]
  (println event)
  (println queryStringParameters)
  (let [query-string (build-query-string queryStringParameters)]
    {:statusCode 301
     :headers {:Location (str "https://" target-domain
                              path query-string)}}))

(def ^:export redirect-request-handler
  (create-request-handler redirect-handler))
