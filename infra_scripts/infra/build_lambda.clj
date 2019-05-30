(ns infra.build-lambda
  (:require [cljs.build.api :as b]))

(defn -main [& args]
  (b/build "infra_scripts"
           {:output-dir "out/lambda"
            :output-to "out/lambda/index.js"
            :target :nodejs
            :optimizations :simple
            :main 'infra.lambdas.redirect}))
