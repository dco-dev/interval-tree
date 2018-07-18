(ns com.dean.interval-tree.tree.interval-set
  (:require [clojure.core.reducers       :as r :refer [coll-fold]]
            [clojure.set]
            [com.dean.interval-tree.tree.interval :as interval]
            [com.dean.interval-tree.tree.node     :as node]
            [com.dean.interval-tree.tree.order    :as order]
            [com.dean.interval-tree.tree.protocol :as proto]
            [com.dean.interval-tree.tree.root]
            [com.dean.interval-tree.tree.tree     :as tree])
  (:import  [clojure.lang                RT]
            [com.dean.interval_tree.tree.protocol PExtensibleSet]
            [com.dean.interval_tree.tree.root     INodeCollection
                                         IBalancedCollection
                                         IOrderedCollection
                                         IIntervalCollection]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Dynamic Environment
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro with-interval-set [x & body]
  `(binding [order/*compare* (.getCmp ~(with-meta x {:tag 'com.dean.interval_tree.tree.root.IOrderedCollection}))
             tree/*t-join*   (.getAllocator ~(with-meta x {:tag 'com.dean.interval_tree.tree.root.INodeCollection}))]
     ~@body))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Interval Set
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftype IntervalSet [root cmp alloc stitch _meta]

  INodeCollection
  (getAllocator [_]
    alloc)
  (getRoot [_]
    root)

  IOrderedCollection
  (getCmp [_]
    cmp)
  (isCompatible [_ o]
    (and (instance? IntervalSet o) (= cmp (.getCmp o))))
  (isSimilar [_ _]
    false)

  IBalancedCollection
  (getStitch [_]
    stitch)

  IIntervalCollection

  ;; TODO: how should these work for interval-set?
  PExtensibleSet
  (intersection [this that]
    (with-interval-set this
      (cond
        (identical? this that)    this
        (.isCompatible this that) (IntervalSet. (tree/node-set-intersection root (.getRoot that))
                                       cmp alloc stitch {})
        true (throw (ex-info "unsupported set operands: " {:this this :that that})))))
  (union [this that]
    (with-interval-set this
      (cond
        (identical? this that)    this
        (.isCompatible this that) (IntervalSet. (tree/node-set-union root (.getRoot that))
                                       cmp alloc stitch {})
        true (throw (ex-info "unsupported set operands: " {:this this :that that})))))
  (difference [this that]
    (with-interval-set this
      (cond
        (identical? this that)    (.empty this)
        (.isCompatible this that) (IntervalSet. (tree/node-set-difference root (.getRoot that))
                                       cmp alloc stitch {})
        true (throw (ex-info "unsupported set operands: " {:this this :that that})))))
  (subset [this that]
    (with-interval-set this
      (cond
        (identical? this that)    true
        (.isCompatible this that) (tree/node-subset? (.getRoot that) root) ;; Grr. reverse args of tree/subset
        true (throw (ex-info "unsupported set operands: " {:this this :that that})))))
  (superset [this that]
    (with-interval-set this
      (cond
        (identical? this that)    true
        (.isCompatible this that) (tree/node-subset? root (.getRoot that)) ;; Grr. reverse args of tree/subset
        true (throw (ex-info "unsupported set operands: " {:this this :that that})))))

  clojure.lang.IMeta
  (meta [_]
    _meta)

  clojure.lang.IObj
  (withMeta [_ m]
    (IntervalSet. root cmp alloc stitch m))

  clojure.lang.Indexed
  (nth [this i]
    (with-interval-set this
      (node/-k (tree/node-nth root i))))

  clojure.lang.Seqable
  (seq [this]
    (with-interval-set this
      (map node/-k (tree/node-seq root))))

  clojure.lang.Reversible
  (rseq [this]
    (with-interval-set this
      (map node/-k (tree/node-seq-reverse root))))

  clojure.lang.ILookup
  (valAt [this k not-found]
    (with-interval-set this
      (if-let [found (seq (tree/node-find-intervals root k))]
        (map node/-k found)
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
    (with-interval-set this
      (cond
        (identical? this o)   0
        (.isCompatible this o) (tree/node-set-compare root (.getRoot o))
        true (throw (ex-info "unsupported comparison: " {:this this :o o})))))

  java.util.Collection
  (toArray [this]
    (with-interval-set this
      (object-array (tree/node-vec root :accessor :k)))) ; better constructor not a priority
  (isEmpty [_]
    (node/leaf? root))
  (add [_ _]
    (throw (UnsupportedOperationException.)))
  (addAll [_ _]
    (throw (UnsupportedOperationException.)))
  (removeAll [_ _]
    (throw (UnsupportedOperationException.)))
  (retainAll [_ _]
    (throw (UnsupportedOperationException.)))

  java.util.Set
  (size [_]
    (tree/node-size root))
  (iterator [this]
    (clojure.lang.SeqIterator. (seq this)))
  (containsAll [this s]  ;; TODO: is this how an interval-set should work?
    (with-interval-set this
      (cond
        (identical? this s)    true
        (coll? s) (every? #(.contains this %) s)
        true      (throw (ex-info "unsupported comparison: " {:this this :s s})))))

  java.util.SortedSet
  (comparator [_]
    cmp)
  (first [this]
    (with-interval-set this
      (node/-k (tree/node-least root))))
  (last [this]
    (with-interval-set this
      (node/-k (tree/node-greatest root))))

  clojure.lang.IPersistentSet
  (equiv [this o]
    (with-interval-set this
      (cond
        (identical? this o) true
        (.isCompatible this o) (and (= (.count this) (.count o))
                                    (zero? (tree/node-set-compare root (.getRoot o))))
        true (throw (ex-info "unsupported comparison: " {:this this :o o})))))
  (count [_]
    (tree/node-size root))
  (empty [_]
    (IntervalSet. (node/leaf) cmp alloc stitch {}))
  (contains [this k]
    (with-interval-set this
      (some? (seq (tree/node-find-intervals this (interval/ordered-pair k))))))
  (disjoin [this k]
    (with-interval-set this
      (IntervalSet. (tree/node-remove root (interval/ordered-pair k)) cmp alloc stitch _meta)))
  (cons [this k]
    (with-interval-set this
      (IntervalSet. (tree/node-add root (interval/ordered-pair k)) cmp alloc stitch _meta)))

  clojure.core.reducers.CollFold
  (coll-fold [this n combinef reducef]
    (with-interval-set this
      (tree/node-chunked-fold n root combinef
        (fn [acc node] (reducef acc (node/-k node)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Literal Representation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod print-method IntervalSet [s w]
  ((get (methods print-method) clojure.lang.IPersistentSet) s w))
