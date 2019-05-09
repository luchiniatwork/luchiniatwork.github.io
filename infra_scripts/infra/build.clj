(ns infra.build
  (:require [cljs.build.api :as b]))

(defn -main [& args]
  (b/build "infra_scripts"
           {:output-dir "out"
            :output-to "index.js"
            :target :nodejs
            :optimizations :simple
            :main 'infra.main
            :install-deps true
            :npm-deps {"@pulumi/pulumi" "latest"
                       "@pulumi/aws"    "latest"
                       "mime"           "latest"}}))
