(ns recipe-search-tech-test.storage
  (:require [clojure.java.io :as io]))

;; (defn- get-resource-folder-files [folder]
;;   "To make it work from uberjar"
;;   (let [loader (-> (Thread/currentThread)
;;                    (.getContextClassLoader))
;;         url (.getResource loader folder)
;;         path (.getPath url)]
;;     (-> path
;;         (io/file)
;;         (file-seq))))
;;         ;; (.listFiles)
;;         ;; (seq))))

(defn recipes-from-resources []
  (->> "recipes/"
       (io/resource)
       (io/file)
       (file-seq)
       (filter #(.isFile %))))

(defn read-recipes [files]
  (for [file files]
    [(.getName file)
     (slurp file)]))

(comment
  (->> (recipes-from-resources)
       (read-recipes)
       (take 10)))


