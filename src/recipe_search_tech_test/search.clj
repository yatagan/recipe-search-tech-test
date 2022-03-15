(ns recipe-search-tech-test.search
  (:require [clojure.string :as string]
            [clj-fuzzy.phonetics :as ph]))

(defn- split-text [text]
  (let [words (as-> text $
                (string/split $ #"[^a-zA-Z]")
                (filter #(not (string/blank? %)) $))]
    words))

(defn- filename-to-name [filename]
  (as-> filename $
    (string/split $ #"\.")
    (first $)
    (string/split $ #"-")
    (string/join " " $)))

;; Index

(defn- index-recipe [name words]
  (reduce
   (fn [acc word]
     (update-in acc [(ph/soundex word) name] (fnil inc 0)))
   {}
   words))

(defn make-index [recipes-pairs-name-content]
  (let [recipes-words-by-name
        (loop [docs []
               recipes recipes-pairs-name-content]
          (if (empty? recipes)
            docs
            (let [[filename content] (first recipes)
                  name (filename-to-name filename)
                  words (split-text content)]
              (recur (conj docs [name words])
                     (rest recipes)))))]
  (->> recipes-words-by-name
       (seq)
       (pmap (fn [[name words]]
              (index-recipe name words)))
       (apply (partial merge-with #(merge-with + %1 %2))))))

;; Search

(defn- sort-results [results]
  (into (sorted-map-by (fn [key1 key2]
                         (compare [(get results key2) key2]
                                  [(get results key1) key1])))
        results))

(defn text-search [query index]
  (let [key-word (->> query
                     (split-text)
                     (map ph/soundex))]
    (->> (select-keys index key-word)
         (vals)
         (reduce #(merge-with * %1 %2) {})
         (sort-results))))

(comment
  (require '[recipe-search-tech-test.storage :refer [recipes-from-resources read-recipes]])

  (def index
    (do
      (println "Indexing...")
      (time
       (->> (recipes-from-resources)
            (read-recipes)
            (make-index)))))

  (text-search "brokkoli" index)
  (time (text-search "broccoli stilton soup" index))
  (text-search "broccoli chicken" index)
  (text-search "pork" index)

  (time (text-search "turki" index)))
