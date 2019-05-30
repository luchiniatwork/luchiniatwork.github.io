(ns infra.apigateway
  (:require ["@pulumi/aws" :as aws]
            ["@pulumi/pulumi" :as pulumi]
            [clojure.string :as string]))

#_(defn run [opts]
    (let [redirect-api (aws/apigateway.RestApi. "redirect-api" #js {})
          proxy-resource (aws/apigateway.Resource. "resource"
                                                   #js {:parentId (.-rootResourceId redirect-api)
                                                        :restApi (.-id redirect-api)
                                                        :pathPart "/{proxy+}"})
          proxy-method (aws/apigateway.Method. "method"
                                               #js {:authorization "NONE"
                                                    :httpMethod "ANY"
                                                    :resourceId (.-id proxy-resource)
                                                    :restApi (.-id redirect-api)})
          proxy-integration (aws/apigateway.Integration. "integration"
                                                         #js {:httpMethod (.-httpMethod proxy-method)
                                                              :integrationHttpMethod "POST"
                                                              :resourceId (.-id proxy-resource)
                                                              :restApi (.-id redirect-api)
                                                              :type "AWS_PROXY"
                                                              :uri "???"})])
    opts)
