(ns infra.s3
  (:require ["@pulumi/aws" :as aws]
            ["@pulumi/pulumi" :as pulumi]
            ["fs" :as fs]
            ["mime" :as mime]
            ["path" :as path]
            [clojure.string :as string]))

#_(defn ^:private public-read-policy-for-bucket [bucket-name]
    (clj->js {"Version" "2012-10-17"
              "Statement" [{"Effect" "Allow"
                            "Principal" "*"
                            "Action" ["s3:GetObject"]
                            "Resource" [(str "arn:aws:s3:::" bucket-name "/*")]}]}))

(defn ^:private copy-to-bucket
  ([source bucket]
   (copy-to-bucket source bucket ""))
  ([source bucket base-path]
   (doseq [file (fs/readdirSync source)]
     (let [file-path (path/join source file)
           file? (-> file-path fs/statSync .isFile)
           content-type (mime/getType file-path)
           file' (cond-> (path/join base-path file)
                   (= "text/html" content-type)
                   (string/replace #"\.html" ""))]
       (if file?
         (aws/s3.BucketObject. file' #js {:bucket bucket
                                          :key file'
                                          :source (pulumi/asset.FileAsset. file-path)
                                          :contentType content-type})
         (copy-to-bucket file-path bucket (path/join base-path file)))))))

(defn run [source target-domain]
  (let [site-bucket (aws/s3.Bucket. (str target-domain "-content")
                                    (clj->js {:acl "public-read"
                                              :website {:indexDocument "index"
                                                        :errorDocument "404"}}))
        logs-bucket (aws/s3.Bucket. (str target-domain "-logs")
                                    #js {:acl "private"})
        #_bucket-policy
        #_(aws/s3.BucketPolicy.
           "bucketPolicy"
           #js {:bucket (.-bucket site-bucket)
                :policy (.apply (.-bucket site-bucket) public-read-policy-for-bucket)})]
    (copy-to-bucket source site-bucket)
    {:bucket-name (.-bucket site-bucket)
     :website-url (.-websiteEndpoint site-bucket)}))
