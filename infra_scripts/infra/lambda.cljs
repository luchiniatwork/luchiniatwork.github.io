(ns infra.lambda
  (:require ["@pulumi/aws" :as aws]
            ["@pulumi/pulumi" :as pulumi]
            [clojure.string :as string]))

(def assume-role-policy
  (clj->js {:Version "2012-10-17"
            :Statement [{:Action "sts:AssumeRole"
                         :Principal {:Service "lambda.amazonaws.com"}
                         :Effect "Allow"
                         :Sid ""}]}))

(defn run [opts]
  (let [iam (aws/iam.Role. "iam-for-redirect-lambda"
                           #js {:assumeRolePolicy assume-role-policy})
        redirect-lambda (aws/lambda.Function.
                         "redirect-lambda"
                         #js {:code (pulumi/asset.FileArchive. "out/lambda/redirect.zip")
                              :handler "index.infra.lambdas.redirect.redirect_request_handler"
                              :role (.-arn iam)
                              :runtime "nodejs10.x"})]
    (merge opts
           {:lambda/redirect-lambda-arn (.-arn redirect-lambda)
            :lambda/redirect-lambda-name (.-name redirect-lambda)})))
