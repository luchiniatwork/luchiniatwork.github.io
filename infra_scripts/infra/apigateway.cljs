(ns infra.apigateway
  (:require ["@pulumi/aws" :as aws]
            ["@pulumi/pulumi" :as pulumi]
            [clojure.string :as string]))

(defn ^:private create-uri [region lambda-arn]
  (pulumi/concat "arn:aws:apigateway:"
                 region
                 ":lambda:path/2015-03-31/functions/"
                 lambda-arn
                 "/invocations"))

(defn ^:private create-proxy-integration! [api id resource uri]
  (let [method (aws/apigateway.Method.
                (str "method-" id)
                #js {:authorization "NONE"
                     :httpMethod "ANY"
                     :resourceId (.-id resource)
                     :restApi (.-id api)})
        integration (aws/apigateway.Integration.
                     (str "integration-" id)
                     #js {:httpMethod (.-httpMethod method)
                          :integrationHttpMethod "POST"
                          :resourceId (.-id resource)
                          :restApi (.-id api)
                          :type "AWS_PROXY"
                          :uri uri})]))

(defn ^:private give-lambda-permission! [api lambda-name]
  (aws/lambda.Permission.
   "apigw-lambda"
   #js {:action "lambda:InvokeFunction"
        :function lambda-name
        :principal "apigateway.amazonaws.com"
        :sourceArn (pulumi/concat (.-executionArn api)
                                  "/*/*/*")}))

(defn run [{:keys [config/region
                   lambda/redirect-lambda-arn
                   lambda/redirect-lambda-name] :as opts}]
  (let [uri (create-uri region redirect-lambda-arn)
        redirect-api (aws/apigateway.RestApi.
                      "redirect-api"
                      #js {:description "API for redirecting requests to main URL"})
        proxy-resource (aws/apigateway.Resource.
                        "resource"
                        #js {:parentId (.-rootResourceId redirect-api)
                             :restApi (.-id redirect-api)
                             :pathPart "{proxy+}"})
        root-resource (pulumi/output
                       (aws/apigateway.getResource #js {:path "/"
                                                        :restApiId (.-id redirect-api)}))
        deployment (aws/apigateway.Deployment.
                    "default"
                    #js {:restApi (.-id redirect-api)
                         :stageName "default"})]
    (give-lambda-permission! redirect-api
                             redirect-lambda-name)
    (create-proxy-integration! redirect-api
                               "proxy"
                               proxy-resource
                               uri)
    (create-proxy-integration! redirect-api
                               "root"
                               root-resource
                               uri)
    (merge opts
           {:apigateway/invoke-url (.-invokeUrl deployment)
            :apigateway/deployment deployment})))
