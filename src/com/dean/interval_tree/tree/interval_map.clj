(ns com.dean.interval-tree.tree.interval-map
  (:require [clojure.core.reducers       :as r :refer [coll-fold]]
            [com.dean.interval-tree.tree.interval :as interval]
            [com.dean.interval-tree.tree.node     :as node]
            [com.dean.interval-tree.tree.root]
            [com.dean.interval-tree.tree.order    :as order]
            [com.dean.interval-tree.tree.tree     :as tree])
  (:import  [clojure.lang                RT]
            [com.dean.interval_tree.tree.root     INodeCollection
                                         IBalancedCollection
                                         IOrderedCollection
                                         IIntervalCollection]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Dynamic Environment
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro with-interval-map [x & body]
  `(binding [order/*compare* (.getCmp ~(with-meta x {:tag 'com.dean.interval_tree.tree.root.IOrderedCollection}))
             tree/*t-join*   (.getAllocator ~(with-meta x {:tag 'com.dean.interval_tree.tree.root.INodeCollection}))]
     ~@body))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Interval Map
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftype IntervalMap [root cmp alloc stitch _meta]

  INodeCollection
  (getAllocator [_]
    alloc)
  (getRoot [_]
    root)

  IOrderedCollection
  (getCmp [_]
    cmp)
  (isCompatible [_ o]
    (and (instance? IntervalMap o) (= cmp (.getCmp o)) (= stitch (.getStitch o))))
  (isSimilar [_ o]
    (map? o))

  IBalancedCollection
  (getStitch [_]
    stitch)

  IIntervalCollection

  clojure.lang.IMeta
  (meta [_]
    _meta)

  clojure.lang.IObj
  (withMeta [_ m]
    (IntervalMap. root cmp alloc stitch m))

  clojure.lang.Indexed
  (nth [this i]
    (with-interval-map this
      (node/-kv (tree/node-nth root i))))

  clojure.lang.MapEquivalence

  clojure.lang.Seqable
  (seq [this]
    (with-interval-map this
      (map node/-kv (tree/node-seq root))))

  clojure.lang.Reversible
  (rseq [this]
    (with-interval-map this
      (map node/-kv (tree/node-seq-reverse root))))

  clojure.lang.ILookup
  (valAt [this k not-found]
    (with-interval-map this
      (if-let [found (tree/node-find-intervals root k)]
        (map node/-v found)
      not-found)))
  (valAt [this k]
    (.valAt this k nil))

  clojure.lang.IFn
  (invoke [this k not-found]
    (.valAt this k not-found))
  (invoke [this k]
    (.valAt this k))
  (applyTo [this args]
    (let [n (RT/boundedLength args 2)]
      (case n
        0 (throw (clojure.lang.ArityException. n (.. this (getClass) (getSimpleName))))
        1 (.invoke this (first args))
        2 (.invoke this (first args) (second args))
        3 (throw (clojure.lang.ArityException. n (.. this (getClass) (getSimpleName)))))))

  java.lang.Comparable
  (compareTo [this o]
    (with-interval-map this
      (cond
        (identical? this o) 0
        (.isCompatible this o) (tree/node-map-compare root (.getRoot o))
        true (throw (ex-info "unsupported comparison: " {:this this :o o})))))

  clojure.lang.Counted
  (count [this]
    (tree/node-size root))

  clojure.lang.Associative
  (containsKey [this k]
    (with-interval-map this
      (not (empty? (tree/node-find-intervals root k)))))
  (entryAt [this k]
    (with-interval-map this
      (some->> k (tree/node-find-intervals root) (map node/-kv))))
  (assoc [this k v]
    (with-interval-map this
      (IntervalMap. (tree/node-add root (interval/ordered-pair k) v) cmp alloc stitch _meta)))
  (empty [this]
    (IntervalMap. (node/leaf) cmp alloc stitch {}))

  java.util.Map
  (get [this k]
    (.valAt this k))
  (isEmpty [_]
    (node/leaf? root))
  (size [_]
    (tree/node-size root))
  (keySet [this]
    (with-interval-map this
      (set (tree/node-vec root :accessor :k))))
  (put [_ _ _]
    (throw (UnsupportedOperationException.)))
  (putAll [_ _]
    (throw (UnsupportedOperationException.)))
  (clear [_]
    (throw (UnsupportedOperationException.)))
  (values [this]
    (with-interval-map this
      (tree/node-vec root :accessor :v)))
  (entrySet [this]
    (with-interval-map this
      (set (tree/node-vec root :accessor :kv))))
  (iterator [this]
    (clojure.lang.SeqIterator. (seq this)))

  clojure.lang.IPersistentCollection
  (equiv [this o]
    (with-interval-map this
      (cond
        (identical? this o) 0
        (.isCompatible this o) (and (= (.count this) (.count o))
                                    (zero? (tree/node-map-compare root (.getRoot o))))
        true     (throw (ex-info "unsupported comparison: " {:this this :o o})))))

  (cons [this o]
    (.assoc this (nth o 0) (nth o 1)))

  clojure.lang.IPersistentMap
  (assocEx [this k v]
    (if (contains? this k)
      (throw (RuntimeException. "Key or value already present"))
      (assoc this k v)))
  (without [this k]
    (with-interval-map this
      (IntervalMap. (tree/node-remove root k) cmp alloc stitch _meta))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Literal Representation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod print-method IntervalMap [m w]
  ((get (methods print-method) clojure.lang.IPersistentMap) m w))
