## Changes between Elastisch 1.0.0-alpha3 and 1.0.0-alpha4

### Fuzzy Like This Query Support

Elastisch now supports [fuzzy like this](http://www.elasticsearch.org/guide/reference/query-dsl/flt-query.html) and [fuzzy like this field](http://www.elasticsearch.org/guide/reference/query-dsl/flt-field-query.html) queries.


### Prefix, Field, Filtered Query Support

Elastisch now supports [prefix queries](http://www.elasticsearch.org/guide/reference/query-dsl/prefix-query.html), see `clojurewerkz.elastisch.query/prefix`,
`clojurewerkz.elastisch.query/field`, `clojurewerkz.elastisch.query/filtered`, and `clojurewerkz.elastisch.document/search` to learn more.


## Changes between Elastisch 1.0.0-alpha2 and 1.0.0-alpha3

### elastisch.document/more-like-this

`clojurewerkz.elastisch.document/more-like-this` provides access to the [Elastic Search More Like This API](http://www.elasticsearch.org/guide/reference/api/more-like-this.html) for
documents and returns documents similar to a given one:

``` clojure
(doc/more-like-this "people" "person" "1" :min_term_freq 1 :min_doc_freq 1)
```

Please note that `:min_doc_freq` and `:min_term_freq` parameters may be very important for small data sets.
If you observe responses with no results, try lowering them.



### elastisch.document/count

`clojurewerkz.elastisch.document/count` provides access to the [Elastic Search count API](http://www.elasticsearch.org/guide/reference/api/count.html)
and is almost always used with a query, for example:

``` clojure
(doc/count "people" "person" (q/term :username "clojurewerkz"))
```

### elastisch.document/delete-by-query

`clojurewerkz.elastisch.document/delete-by-query` provides access to the [Delete by query API](http://www.elasticsearch.org/guide/reference/api/delete-by-query.html) of Elastic Search, for example:

``` clojure
(doc/delete-by-query "people" "person" (q/term :tag "mongodb"))
```



## Changes between Elastisch 1.0.0-alpha1 and 1.0.0-alpha2

### elastisch.document/replace

`clojurewerkz.elastisch.document/replace` deletes a document by id and immediately adds a new one
with the same id.


### elastisch.response

`clojurewerkz.elastisch.response` was extracted from `clojurewerkz.elastisch.utils`


### Leiningen 2

Elastisch now uses [Leiningen 2](https://github.com/technomancy/leiningen/wiki/Upgrading).
