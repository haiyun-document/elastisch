(ns clojurewerkz.elastisch.fixtures
  (:require [clojurewerkz.elastisch.index :as index]))

(defn reset-indexes*
  []
  (doseq [i ["people" "articles"]]
    (index/delete i)))

(defn reset-indexes
  [f]
  (reset-indexes*)
  (f)
  (reset-indexes*))

;;
;; People
;;

(def person-jack
  {:username   "esjack"
   :first-name "Jack"
   :last-name  "Black"
   :title      "Sales Manager"
   :biography  "Tries to avoid eating fat, being good to other people and does sports every now and then"
   :planet     "Earth"
   :age 22})

(def person-joe
  {:username   "esjoe"
   :first-name "Joe"
   :last-name  "Mahjo"
   :title      "Trader"
   :biography  "Quite a nice guy"
   :planet     "Earth"
   :age 37})

(def person-mary
  {:username   "esmary"
   :first-name "Mary"
   :last-name  "Lindey"
   :title      "Copywriter"
   :biography  "Writes copy and copies writes"
   :planet     "Earth"
   :age 28})

(def people-mapping
  { :person { :properties {:username   { :type "string" :store "yes" }                        
                           :first-name { :type "string" :store "yes" }
                           :last-name  { :type "string" }
                           :title      { :type "string" :analyzer "snowball" }
                           :planet     { :type "string" }
                           :biography  { :type "string" :analyzer "snowball"}}}})



;;
;; Articles
;;

(def article-on-elasticsearch
  {:url "http://en.wikipedia.org/wiki/ElasticSearch"
   :title "ElasticSearch"
   :language "English"
   :tags "technology, opensource, search, full-text search, distributed, software, lucene"
   :summary "ElasticSearch is a distributed, RESTful, free/open source search server based on Apache Lucene.
             It is developed by Shay Banon and is released under the Apache Software License. ElasticSearch can be used to search all kind of documents.
             It provides a scalable search solution, has near real-time search and support for multitenancy."
   :latest-edit {:date "06:07, 26 March 2012"
                 :author "Unknown"}})

(def article-on-lucene
  {:url "http://en.wikipedia.org/wiki/Apache_Lucene"
   :title "Apache Lucene"
   :language "English"
   :tags "technology, opensource, search, full-text search, distributed, software, lucene"
   :summary "Apache Lucene is a free/open source information retrieval software library, originally created in Java by Doug Cutting. It is
             supported by the Apache Software Foundation and is released under the Apache Software License."
   :latest-edit {:date "02:19, 11 March 2012"
                 :author "Thorwald"}})

(def article-on-nueva-york
  {:url "http://es.wikipedia.org/wiki/Nueva_York"
   :title "Nueva York"
   :language "Spanish"
   :tags "geografía, EEUU, historia, ciudades, Norteamérica"
   :summary "Nueva York (en inglés y oficialmente, New York City) es la ciudad más poblada del Estado de Nueva York, de los
             Estados Unidos de América y la segunda aglomeración urbana del continente. Es el centro del área metropolitana de Nueva York,
             la cual está entre las aglomeraciones urbanas más grandes del mundo."})

(def article-on-austin
  {:url "http://es.wikipedia.org/wiki/Austin"
   :title "Austin"
   :language "Spanish"
   :tags "geografía, EEUU, historia, ciudades, Norteamérica"
   :summary "Austin es una ciudad y capital estatal, ubicada en los condados de Travis, Williamson y Hays,
             en el estado estadounidense de Texas."})

(def articles-mapping
  {:article {:properties {:title    {:type "string" :analyzer "snowball"}
                          :summary  {:type "string" :analyzer "snowball"}
                          :url      {:type "string"}
                          :language {:type "string"}
                          :tags     {:type "string" :analyzer "standard"}}}})
