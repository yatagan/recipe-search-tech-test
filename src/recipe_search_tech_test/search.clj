(ns recipe-search-tech-test.search
  (:require [clojure.string :as string]
            [clojure.set :as set]))

(defonce default-hash-fn string/lower-case)

(declare split-cleanup-text)
(declare index-document)
(declare merge-indices)
(declare sort-by-score)

(defn make-index
  "Builds index map from a sequence of documents. A document is a pair of a document `id` 
   and content e.g. `[\"doc-id\" \"full text\"]`.
   Uses pmap for indexing documents in parallel.
   User can pass opt map with key `:hash-fn` which is a functions for hashing words while indexing."
  [documents & {hash-fn :hash-fn :or {hash-fn default-hash-fn}}]
  (->> documents
       (map (fn [[doc-id text]]
              [doc-id (split-cleanup-text text)]))
       (pmap #(apply (partial index-document hash-fn) %))
       (merge-indices)))

(defn search
  "Searches a query in the index, returns list of pairs with document `id` and a `score`,
   results are sorted by score, e.g.
   ```
   ([\" broccoli soup with stilton \" 36]
    [\" broccoli bulghur stilton grapes \" 30]
    [\" broccoli soup with gorgonzola \" 18])
   ```
   User should pass the same `:hash-fn` she used for indexing."
  [query index & {hash-fn :hash-fn :or {hash-fn default-hash-fn}}]
  (->> query
       (split-cleanup-text)
       (map hash-fn)
       (select-keys index)
       (vals)
       (reduce #(merge-with * %1 %2) {})
       (sort-by-score)))

(defn- index-document [hash-fn doc-id words]
  (reduce
   (fn [acc word]
     (update-in acc [(hash-fn word) doc-id] (fnil inc 0)))
   {}
   words))

(defn- split-cleanup-text [text]
  (let [words (as-> text $
                (string/split $ #"[^a-zA-Z]")
                (filter #(not (string/blank? %)) $))]
    words))

(defn- merge-indices [indices]
  (apply (partial merge-with #(merge-with + %1 %2)) indices))

(defn- sort-by-score [results]
  (->> results
       (seq)
       (sort (fn [[_ s1] [_ s2]]
               (> s1 s2)))))

(comment
  (require '[recipe-search-tech-test.storage :refer [files-from-resources load-recipes]]
           '[clj-fuzzy.phonetics :as ph])

  (def index-strict
    (do
      (println "Indexing...")
      (time
       (-> (files-from-resources "recipes")
           (load-recipes)
           (make-index)))))
  (time (search "broccoli stilton soup" index-strict))
  ;; => (["broccoli soup with stilton" 36]
  ;;     ["broccoli bulghur stilton grapes" 30]
  ;;     ["cauliflower stilton soup" 18]
  ;;     ["purple sprouting broccoli bean and pasta" 10]
  ;;     ["lettuce soup" 8]
  ;;     ["broccoli cooked in red wine" 7]
  ;;     ["penne with purple sprouting broccoli" 6]
  ;;     ["purple sprouting broccoli pasta with gre" 5]
  ;;     ["purple sprouting broccoli bacon poached" 4]
  ;;     ["leek potato soup with cheese herb scones" 3]
  ;;     ["squash soup" 2]
  ;;     ["romanesco easy ideas" 1])

  (time (search "kiev" index-strict))
  ;; => (["wild garlic chicken kiev" 2] 
  ;;     ["pork olives option 2" 1] 
  ;;     ["chicken kiev" 1] 
  ;;     ["pork leg roast with red cabbage" 1])


  (def index-fuzzy
    (do
      (println "Fuzzy indexing...")
      (time
       (-> (files-from-resources "recipes")
           (load-recipes)
           (make-index :hash-fn ph/soundex)))))
  (time (search "brokkolli stelton sup" index-fuzzy :hash-fn ph/soundex))
  ;; => (["broccoli-soup-with-stilton.txt" 36]
  ;;     ["broccoli-bulghur-stilton-grapes.txt" 30]
  ;;     ["cauliflower-stilton-soup.txt" 18]
  ;;     ["curried-broccoli-quinoa.txt" 16]
  ;;     ["broccoli-mustard-dill-tart.txt" 12]
  ;;     ["broccoli-bean-pasta-soup.txt" 10]
  ;;     ["sausage-celeriac-barley-sprouts.txt" 9]
  ;;     ["purple-sprouting-broccoli-with-pilchard.txt" 8]
  ;;     ["asian-style-psb-with-ginger-tamarind.txt" 7]
  ;;     ["wild-garlic-psb-ragout.txt" 6]
  ;;     ["barley-spring-veg-sheeps-cheese.txt" 5]
  ;;     ["gazpacho.txt" 4]
  ;;     ["mash-91n-92-greens.txt" 3]
  ;;     ["brussels-sprouts-red-onion-blue-cheese.txt" 2]
  ;;     ["marinated-cucumber-salad.txt" 1])

  (time (search "kiev" index-fuzzy :hash-fn ph/soundex))
  ;; => (["cooking-turkey.txt" 5]
  ;;     ["lemon-curd-chocolate-tart.txt" 4]
  ;;     ["saag-aloo-with-homemade-paneer.txt" 4]
  ;;     ["eggs-benedict-with-psb-or-spring-greens.txt" 4]
  ;;     ["gnocchi-greens-brown-butter.txt" 4]
  ;;     ["asparagus-portobello-noodles.txt" 4]
  ;;     ["bonfire-chilli-bowl.txt" 4]
  ;;     ["baked-avo-with-eggs-and-quinoa.txt" 3]
  ;;     ["teriyaki-pork-spring-greens-rice.txt" 3]
  ;;     ["yakitori-chicken-noodles.txt" 3]
  ;;     ["spinach-linguine-with-tomatoes.txt" 3]
  ;;     ["turkey-black-bean-quesadillas.txt" 3]
  ;;     ["roast-cauliflower-gnocchi.txt" 3]
  ;;     ["beef-and-squash-quesadillas.txt" 3]
  ;;     ["macaroni-cauliflower-cheese.txt" 3]
  ;;     ["broccoli-tomato-wild-garlic-wheatberries.txt" 3]
  ;;     ["chorizo-cauliflower-cider-penne.txt" 3]
  ;;     ["roasted-peppers-white-beans.txt" 3]
  ;;     ["aloo-gobi-coconut-cardamom-rice.txt" 3]
  ;;     ["lebanese-spinach-and-chickpeas.txt" 3]
  ;;     ["torn-lasagne-chicken.txt" 3]
  ;;     ["basic-pancake-mix.txt" 3]
  ;;     ["smoked-chicken-carbonara.txt" 3]
  ;;     ["wild-garlic-psb-ragout.txt" 3]
  ;;     ["barley-spring-veg-sheeps-cheese.txt" 3]
  ;;     ["romanesco-cauliflower-pasta.txt" 2]
  ;;     ["roast-chicken-joints-with-thai-dressing.txt" 2]
  ;;     ["beef-asparagus-bean-stir-fry.txt" 2]
  ;;     ["lemon-and-thyme-roasted-chicken.txt" 2]
  ;;     ["caldo-verde-spelt-bread-rolls.txt" 2]
  ;;     ["chicken-kiev.txt" 2]
  ;;     ["teriyaki-pork-braised-radishes.txt" 2]
  ;;     ["green-bean-herb-cheese-fritters.txt" 2]
  ;;     ["home-dried-tomatoes.txt" 2]
  ;;     ["chocolate-ginger-sauce.txt" 2]
  ;;     ["honey-sesame-chicken-with-noodles.txt" 2]
  ;;     ["squash-and-aubergine-risotto.txt" 2]
  ;;     ["honeycomb-melon-and-raspberries.txt" 2]
  ;;     ["portobello-toad-in-the-hole.txt" 2]
  ;;     ["chicken-pepper-maftoul-bowl.txt" 2]
  ;;     ["potato-radish-bean-salad.txt" 2]
  ;;     ["chicken-pesto-dressing.txt" 2]
  ;;     ["bacon-radicchio-omelette.txt" 2]
  ;;     ["leek-pain-de-sucre-salad.txt" 2]
  ;;     ["smoked-cheddar-pork.txt" 2]
  ;;     ["mushroom-leek-tart-apple.txt" 2]
  ;;     ["beetroot-relish.txt" 2]
  ;;     ["kumquat-baked-ricotta-pancakes.txt" 2]
  ;;     ["hainanese-chicken-and-rice-penang-acar.txt" 2]
  ;;     ["celeriac-kale-mushroom-pie.txt" 2]
  ;;     ...)
  )
