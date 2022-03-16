(ns recipe-search-tech-test.search
  (:require [clojure.string :as string]
            [clojure.set :as set]))

(defonce default-hash-fn string/lower-case)

;; Index

(defn- split-clean-text [text]
  (let [words (as-> text $
                (string/split $ #"[^a-zA-Z]")
                (filter #(not (string/blank? %)) $))]
    words))

(defn- index-document [hash-fn name words]
  (reduce
   (fn [acc word]
     (update-in acc [(hash-fn word) name] (fnil inc 0)))
   {}
   words))

(defn- merge-indices [indices]
  (apply (partial merge-with #(merge-with + %1 %2)) indices))

(defn make-index [documents & {hash-fn :hash-fn :or {hash-fn default-hash-fn}}]
  "document is like {\"doc-id\" \"full text\"}"
  (->> documents
       (map (fn [[doc-id text]]
              [doc-id (split-clean-text text)]))
       (pmap #(apply (partial index-document hash-fn) %))
       (merge-indices)))

;; Search

(defn- sort-results [score-by-id]
  (let [id-by-score (set/map-invert score-by-id)
        sorted-scores (->> id-by-score
                           (keys)
                           (sort >))]
    (for [score sorted-scores]
      [(get id-by-score score) score ])))

(defn text-search [query index & {hash-fn :hash-fn :or {hash-fn default-hash-fn}}]
  (let [key-word (->> query
                     (split-clean-text)
                     (map hash-fn))]
    (->> (select-keys index key-word)
         (vals)
         (reduce #(merge-with * %1 %2) {})
         (sort-results))))

(comment
  (require '[recipe-search-tech-test.storage :refer [recipes-from-resources load-recipes]])

  (def index
    (do
      (println "Indexing...")
      (time
       (-> (recipes-from-resources)
           (load-recipes)
           (make-index)))))

  (time (text-search "broccoli stilton soup" index))
  (time (text-search "brokkolli stelton sup" index))
  
  )
