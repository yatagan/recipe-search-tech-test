(ns recipe-search-tech-test.core
  (:gen-class)
  (:require [recipe-search-tech-test.storage :as storage]
            [recipe-search-tech-test.search :as search]))

(defn -main
  [& args]
  
  (let [docs (do
               (println "Loading recipes..")
               (->> (storage/recipes-from-resources)
                    (storage/read-recipes)
                    (search/make-documents)
                    (take 20)))
        index (do
                (println "Indexing recipes..")
                (time (search/make-index docs)))]
    (println "Ready, please enter a query:")
  (loop []
    (print "query> ")
    (flush)
    (let [query (read-line)]
      (when query
        (let [results (search/text-search query index)]
          (if (empty? results)
            (println "No results, try another query")
            (doseq [[recipe {:keys [score]}] (take 10 results)]
              (println (str score ": " recipe))))
          (recur)))))))
