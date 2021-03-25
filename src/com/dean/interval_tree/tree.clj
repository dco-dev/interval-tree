(ns com.dean.interval-tree.tree
  (:require [clojure.core.reducers                    :as r]
            [com.dean.interval-tree.tree.interval     :as interval]
            [com.dean.interval-tree.tree.interval-map :refer [->IntervalMap]]
            [com.dean.interval-tree.tree.interval-set :refer [->IntervalSet]]
            [com.dean.interval-tree.tree.node         :as node]
            [com.dean.interval-tree.tree.order        :as order]
            [com.dean.interval-tree.tree.protocol     :as proto]
            [com.dean.interval-tree.tree.ordered-map  :refer [->OrderedMap]]
            [com.dean.interval-tree.tree.ordered-set  :refer [->OrderedSet]]
            [com.dean.interval-tree.tree.tree         :as tree]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Set Algebra
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def intersection proto/intersection)
(def union        proto/union)
(def difference   proto/difference)
(def subset       proto/subset)
(def superset     proto/superset)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Ordered Set
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: allow high speed construction AND custom compare-fn
;; TODO: refactor

;; NOTE: subject to change!
;; experimentally determined to be in the ballpark, given the current
;; performance characteristics upstream

(def ^:private +chunk-size+ 2048)

(defn- ordered-set* [compare-fn coll]
  (binding [order/*compare* compare-fn]
    (->OrderedSet
      (r/fold +chunk-size+
              (fn
                ([]      (node/leaf))
                ([n0 n1] (tree/node-set-union n0 n1))) tree/node-add coll)
      compare-fn nil nil {})))

(defn ordered-set
  ([]
   (ordered-set* order/normal-compare nil))
  ([coll]
   (ordered-set* order/normal-compare coll)))

(defn ordered-set-by [pred coll]
  (-> pred order/compare-by (ordered-set* (seq coll))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Ordered Map
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ordered-map
  ([]
   (ordered-map order/normal-compare nil))
  ([coll]
   (ordered-map order/normal-compare coll))
  ([compare-fn coll]
   (binding [order/*compare* compare-fn]
     (->OrderedMap (reduce (fn [n [k v]] (tree/node-add n k v)) (node/leaf) coll)
                   compare-fn nil nil {}))))

(defn ordered-map-by [pred coll]
  (-> pred order/compare-by (ordered-map coll)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Interval Map
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn interval-map
  ([]
   (interval-map nil))
  ([coll]
   (binding [tree/*t-join*  tree/node-create-weight-balanced-interval
             order/*compare* order/normal-compare]
     (->IntervalMap (reduce (fn [n [k v]] (tree/node-add n k v)) (node/leaf) coll)
                    order/*compare* tree/*t-join* nil {}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Interval Set
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn interval-set
  ([]
   (interval-set nil))
  ([coll]
   (binding [tree/*t-join*   tree/node-create-weight-balanced-interval
             order/*compare* order/normal-compare]
     (->IntervalSet (reduce #(tree/node-add %1 (interval/ordered-pair %2)) (node/leaf) coll)
                     order/*compare* tree/*t-join* nil {}))))
