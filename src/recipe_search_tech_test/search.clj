(ns recipe-search-tech-test.search
  (:require [clojure.string :as string]
            [memsearch.core :as ms]))

(defn- cleanup-text [text]
  (as-> text $
    (string/split $ #"[^a-zA-Z]")
    (filter #(not (string/blank? %)) $)
    (string/join " " $)))

(defn- filename-to-name [filename]
  (as-> filename $
    (string/split $ #"\.")
    (first $)
    (string/split $ #"-")
    (string/join " " $)))

(defn make-documents [recipes-pairs-name-content]
  (loop [docs []
         recipes recipes-pairs-name-content]
    (if (empty? recipes)
      docs
      (let [[filename content] (first recipes)
            name (filename-to-name filename)
            name-content (string/join " " [name content])
            cleaned-content (cleanup-text name-content)]
        (recur (conj docs {:id name :content cleaned-content})
               (rest recipes))))))

(defn make-index [documents]
  (ms/text-index documents))

(defn text-search [query index]
  (ms/text-search query index {:sorted? true}))

(comment
  (require '[recipe-search-tech-test.storage :refer [recipes-from-resources read-recipes]])

  (def index
    (do
      (println "Indexing...")
      (time
       (->> (recipes-from-resources)
            (read-recipes)
            ;; (take 1)
            (make-documents)
            (make-index)))))

  (text-search "brokkoli" index)
  (text-search "broccoli stilton soup" index)
  (text-search "broccoli with chicken" index)
  (text-search "sup" index)
  (time (text-search "turki" index)))