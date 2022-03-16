(ns recipe-search-tech-test.storage
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(defn files-from-resources [path]
  (->> path
       (io/resource)
       (io/file)
       (file-seq)
       (filter #(.isFile %))))

(defn- filename-to-recipename [filename]
  (as-> filename $
    (string/split $ #"\.")
    (first $)
    (string/split $ #"-")
    (string/join " " $)))

(defn load-recipes [files]
  (for [file files]
    [(-> file
         (.getName)
         (filename-to-recipename))
     (slurp file)]))
