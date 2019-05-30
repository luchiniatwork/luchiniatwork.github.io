(ns infra.lambdas.baked)

(defmacro defbaked
  [name {:keys [env-var default] :as opts}]
  (let [value# (or (System/getenv env-var) default)]
    `(def ~name ~value#)))
