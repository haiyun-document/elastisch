(ns clojurewerkz.elastisch.document-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.document      :as doc]
            [clojurewerkz.elastisch.rest          :as rest]
            [clojurewerkz.elastisch.index         :as idx]
            [clojurewerkz.elastisch.utils         :as utils]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.response
        [clj-time.core :only [months ago now from-now]]))

(use-fixtures :each fx/reset-indexes)
(def ^{:const true} index-name "people")
(def ^{:const true} index-type "person")

;;
;; put
;;

(deftest test-put-with-autocreated-index
  (let [id         "1"
        document   fx/person-jack
        response   (doc/put index-name index-type id document)
        get-result (doc/get index-name index-type id)]
    (is (ok? response))
    (is (idx/exists? index-name))

    (are [expected actual] (= expected (actual get-result))
         document   :_source
         index-name :_index
         index-type :_type
         id         :_id
         true       :exists)))

(deftest test-put-with-precreated-index
  (let [id       "1"
        _        (idx/create index-name :mappings fx/people-mapping)
        document   fx/person-jack
        response   (doc/put index-name index-type id document)
        get-result (doc/get index-name index-type id)]
    (is (ok? response))
    (are [expected actual] (= expected (actual get-result))
         document   :_source
         index-name :_index
         index-type :_type
         id         :_id
         true       :exists)))

(deftest test-put-with-missing-document-versioning-type
  (let [id       "1"
        _        (doc/put index-name index-type id fx/person-jack)
        _        (doc/put index-name index-type id fx/person-mary)
        response (doc/put index-name index-type id fx/person-joe :version 1)]
    (is (conflict? response))))

(deftest test-put-with-conflicting-document-version
  (let [id       "1"
        _        (doc/put index-name index-type id fx/person-jack)
        _        (doc/put index-name index-type id fx/person-mary)
        response (doc/put index-name index-type id fx/person-joe :version 1 :version_type "external")]
    (is (conflict? response))
    (is (not (ok? response)))))

(deftest test-put-with-new-document-version
  (let [id       "1"
        _        (doc/put index-name index-type id fx/person-jack :version 1 :version_type "external")
        _        (doc/put index-name index-type id fx/person-mary :version 2 :version_type "external")
        response (doc/put index-name index-type id fx/person-joe  :version 3 :version_type "external")]
    (is (not (conflict? response)))
    (is (ok? response))))

(deftest create-when-already-created-test
  (let [id       "1"
        _        (doc/put index-name index-type id fx/person-jack)
        response (doc/put index-name index-type id fx/person-joe :op_type "create")]
    (is (conflict? response))))

(deftest test-put-with-a-timestamp
  (let [id       "1"
        _        (idx/create index-name :mappings fx/people-mapping)
        response (doc/put index-name index-type id fx/person-jack :timestamp (-> 2 months ago))]
    (is (ok? response))))

(deftest test-put-with-a-1-day-ttl
  (let [id       "1"
        _        (idx/create index-name :mappings fx/people-mapping)
        response (doc/put index-name index-type id fx/person-jack :ttl "1d")]
    (is (ok? response))))

(deftest test-put-with-a-10-seconds-ttl
  (let [id       "1"
        _        (idx/create index-name :mappings fx/people-mapping)
        response (doc/put index-name index-type id fx/person-jack :ttl 10000)]
    (is (ok? response))))

(deftest test-put-with-a-timeout
  (let [id       "1"
        _        (idx/create index-name :mappings fx/people-mapping)
        response (doc/put index-name index-type id fx/person-jack :timeout "1m")]
    (is (ok? response))))

(deftest test-put-with-refresh-set-to-true
  (let [id       "1"
        _        (idx/create index-name :mappings fx/people-mapping)
        response (doc/put index-name index-type id fx/person-jack :refresh true)]
    (is (ok? response))))


;;
;; create
;;

(deftest put-create-autogenerate-id-test
  (let [response (doc/create index-name index-type fx/person-jack)]
    (is (ok? response))
    (is (:_id response))
    (are [expected actual] (= expected (actual response))
         index-name :_index
         index-type  :_type
         1          :_version)))

;;
;; get, present?
;;

(deftest test-get-with-non-existing-document
  (is (nil? (doc/get index-name index-type "1"))))

(deftest test-present-on-non-existing-id
  (is (not (doc/present? index-name index-type "1"))))

(deftest test-present-on-existing-id
  (doc/put index-name index-type "1" fx/person-jack)
  (is (doc/present? index-name index-type "1")))


;;
;; count
;;

(deftest test-count-with-a-term-query
  (idx/create index-name :mappings fx/people-mapping)
  (doc/create index-name index-type fx/person-jack)
  (doc/create index-name index-type fx/person-joe)
  (idx/refresh index-name)
  (are [c r] (is (= c (count-from r)))
       2 (doc/count index-name index-type)))

(deftest test-count-with-a-term-query
  (idx/create index-name :mappings fx/people-mapping)
  (doc/create index-name index-type fx/person-jack)
  (doc/create index-name index-type fx/person-joe)
  (idx/refresh index-name)
  (are [c r] (is (= c (count-from r)))
       1 (doc/count index-name index-type (q/term :username "esjack"))
       1 (doc/count index-name index-type (q/term :username "esjoe"))
       0 (doc/count index-name index-type (q/term :username "esmary"))))


;;
;; delete
;;

(deftest test-delete-when-a-document-exists
  (let [id "1"]
    (doc/put index-name index-type id fx/person-jack)
    (is (doc/present? index-name index-type id))
    (is (ok? (doc/delete index-name index-type id)))
    (is (not (doc/present? index-name index-type id)))))


;;
;; delete by query
;;

(deftest test-delete-by-query-with-a-term-query
  (idx/create index-name :mappings fx/people-mapping)
  (doc/create index-name index-type fx/person-jack)
  (doc/create index-name index-type fx/person-joe)
  (idx/refresh index-name)
  (doc/delete-by-query index-name index-type (q/term :username "esjoe"))
  (idx/refresh index-name)
  (are [c r] (is (= c (count-from r)))
       1 (doc/count index-name index-type (q/term :username "esjack"))
       0 (doc/count index-name index-type (q/term :username "esjoe"))
       0 (doc/count index-name index-type (q/term :username "esmary"))))



;;
;; mget
;;

(deftest multi-get-test
  (doc/put index-name index-type "1" fx/person-jack)
  (doc/put index-name index-type "2" fx/person-mary)
  (doc/put index-name index-type "3" fx/person-joe)
  (let [mget-result (doc/multi-get
                     [ { :_index index-name :_type index-type :_id "1"  }
                       { :_index index-name :_type index-type :_id "2" } ])]
    (is (= fx/person-jack (:_source (first mget-result))))
    (is (= fx/person-mary (:_source (second mget-result)))))
  (let [mget-result (doc/multi-get index-name
                                   [ { :_type index-type :_id "1"  }
                                     { :_type index-type :_id "2" } ])]
    (is (= fx/person-jack (:_source (first mget-result))))
    (is (= fx/person-mary (:_source (second mget-result)))))
  (let [mget-result (doc/multi-get index-name index-type
                                   [ { :_id "1"  } { :_id "2" } ])]
    (is (= fx/person-jack (:_source (first mget-result))))
    (is (= fx/person-mary (:_source (second mget-result))))))


;;
;; replace
;;

(deftest test-replacing-documents
  (let [id      "1"
        new-bio "Such a brilliant person"]
    (idx/create index-name :mappings fx/people-mapping)

    (doc/put index-name index-type "1" fx/person-jack)
    (doc/put index-name index-type "2" fx/person-mary)
    (doc/put index-name index-type "3" fx/person-joe)

    (idx/refresh index-name)
    (is (any-hits? (doc/search index-name index-type :query (q/term :biography "nice"))))
    (is (no-hits? (doc/search index-name index-type :query (q/term :biography "brilliant"))))
    (doc/replace index-name index-type id (assoc fx/person-joe :biography new-bio))
    (idx/refresh index-name)
    (is (any-hits? (doc/search index-name index-type :query (q/term :biography "brilliant"))))
    ;; TODO: investigate this. MK.
    #_ (is (no-hits? (doc/search index-name index-type :query (q/term :biography "nice"))))))


;;
;; more-like-this
;;

(deftest test-more-like-this
  (let [index "articles"
        type  "article"]
    (idx/create index :mappings fx/articles-mapping)
    (doc/put index type "1" fx/article-on-elasticsearch)
    (doc/put index type "2" fx/article-on-lucene)
    (doc/put index type "3" fx/article-on-nueva-york)
    (doc/put index type "4" fx/article-on-austin)
    (idx/refresh index)
    (let [response (doc/more-like-this index type "2" :mlt_fields ["tags"] :min_term_freq 1 :min_doc_freq 1)]
      (is (= 1 (total-hits response)))
      (is (= fx/article-on-elasticsearch (-> response :hits :hits first :_source))))))


;; deftest optional-type
;; deftest fields
;; deftest routing
;; deftest preference
;; deftest refresh
