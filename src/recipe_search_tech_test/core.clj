(ns recipe-search-tech-test.core
  (:gen-class)
  (:require [recipe-search-tech-test.storage :as storage]
            [recipe-search-tech-test.search :as search]
            [clj-fuzzy.phonetics :as ph]))

(defn -main
  [& args]
  (let [docs (do
               (println "Loading recipes..")
               (->> (storage/recipes-from-resources)
                    (storage/load-recipes)))
        hash-fn ph/soundex
        index (do
                (println "Indexing recipes..")
                (time (search/make-index docs :hash-fn hash-fn)))]
    (println "Ready, please enter a query:")
    (loop []
      (print "query> ")
      (flush)
      (let [query (read-line)]
        (when query
          (let [results (time (search/text-search query index :hash-fn hash-fn))]
            (if (empty? results)
              (println "No results, please try another query")
              (do 
                (println "score \trecipe")
                (println "--------------")
                (doseq [[recipe score] (take 10 results)]
                    (println (str score "\t" recipe)))))
            (recur)))))))
