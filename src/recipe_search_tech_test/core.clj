(ns recipe-search-tech-test.core
  (:gen-class)
  (:require [recipe-search-tech-test.storage :as storage]
            [recipe-search-tech-test.search :as search]))

(defn -main
  [& args]
  (let [docs (do
               (println "Loading recipes..")
               (->> (storage/recipes-from-resources)
                    (storage/read-recipes)))
        index (do
                (println "Indexing recipes..")
                (time (search/make-index docs)))]
    (println "Ready, please enter a query:")
    (loop []
      (print "query> ")
      (flush)
      (let [query (read-line)]
        (when query
          (let [results (time (search/text-search query index))]
            (if (empty? results)
              (println "No results, please try another query")
              (doseq [[i [recipe _score]] (->> results
                                               (take 10)
                                               (map-indexed #(vector %1 %2)))]
                (println (str (inc i) ": " recipe))))
            (recur)))))))
