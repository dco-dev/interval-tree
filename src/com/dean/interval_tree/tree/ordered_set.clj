(ns com.dean.interval-tree.tree.ordered-set
  (:require [clojure.core.reducers       :as r :refer [coll-fold]]
            [clojure.set]
            [com.dean.interval-tree.tree.node     :as node]
            [com.dean.interval-tree.tree.order    :as order]
            [com.dean.interval-tree.tree.protocol :as proto]
            [com.dean.interval-tree.tree.root]
            [com.dean.interval-tree.tree.tree     :as tree])
  (:import  [clojure.lang                RT]
            [com.dean.interval_tree.tree.protocol PExtensibleSet]
            [com.dean.interval_tree.tree.root     INodeCollection
                                         IBalancedCollection
                                         IOrderedCollection]))

;; TODO:
;;  - clojure.lang.Sorted
;;  - ISeq .seqFrom

;; - IReduce, IReduceKV,
;; - IMapIterable:  https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/PersistentHashMap.java
;; - Collection Check: https://github.com/ztellman/collection-check/blob/master/src/collection_check/core.cljc

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Dynamic Environment
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro with-ordered-set [x & body]
  `(binding [order/*compare* (.getCmp ~(with-meta x {:tag 'com.dean.interval_tree.tree.root.IOrderedCollection}))]
     ~@body))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Ordered Set
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftype OrderedSet [root cmp alloc stitch _meta]

  INodeCollection
  (getAllocator [_]
    alloc)
  (getRoot [_]
    root)

  IOrderedCollection
  (getCmp [_]
    cmp)
  (isCompatible [_ o]
    (and (instance? OrderedSet o) (= cmp (.getCmp ^OrderedSet o)) (= stitch (.getStitch ^OrderedSet o))))
  (isSimilar [_ o]
    (set? o))

  IBalancedCollection
  (getStitch [_]
    stitch)

  PExtensibleSet
  (intersection [this that]
    (with-ordered-set this
      (cond
        (identical? this that)    this
        (.isCompatible this that) (new OrderedSet (tree/node-set-intersection root (.getRoot ^OrderedSet that))
                                       cmp alloc stitch {})
        (.isSimilar this that)    (clojure.set/intersection (into #{} this) that)
        true (throw (ex-info "unsupported set operands: " {:this this :that that})))))
  (union [this that]
    (with-ordered-set this
      (cond
        (identical? this that)    this
        (.isCompatible this that) (new OrderedSet (tree/node-set-union root (.getRoot ^OrderedSet that))
                                       cmp alloc stitch {})
        (.isSimilar this that)    (clojure.set/union (into #{} this) that)
        true (throw (ex-info "unsupported set operands: " {:this this :that that})))))
  (difference [this that]
    (with-ordered-set this
      (cond
        (identical? this that)    (.empty this)
        (.isCompatible this that) (new OrderedSet (tree/node-set-difference root (.getRoot ^OrderedSet that))
                                       cmp alloc stitch{})
        (.isSimilar this that)    (clojure.set/difference (into #{} this) that)
        true (throw (ex-info "unsupported set operands: " {:this this :that that})))))
  (subset [this that]
    (with-ordered-set this
      (cond
        (identical? this that)    true
        (.isCompatible this that) (tree/node-subset? (.getRoot ^OrderedSet that) root) ;; Grr. reverse args of tree/subset
        (.isSimilar this that)    (clojure.set/subset? (into #{} this) that)
        true (throw (ex-info "unsupported set operands: " {:this this :that that})))))
  (superset [this that]
    (with-ordered-set this
      (cond
        (identical? this that)    true
        (.isCompatible this that) (tree/node-subset? root (.getRoot ^OrderedSet that)) ;; Grr. reverse args of tree/subset
        (.isSimilar this that)    (clojure.set/subset? that (into #{} this))
        true (throw (ex-info "unsupported set operands: " {:this this :that that})))))

  clojure.lang.IMeta
  (meta [_]
    _meta)

  clojure.lang.IObj
  (withMeta [_ m]
    (new OrderedSet root cmp alloc stitch m))

  clojure.lang.Indexed
  (nth [this i]
    (with-ordered-set this
      (node/-k (tree/node-nth root i))))

  clojure.lang.Seqable
  (seq [this]
    (with-ordered-set this
      (map node/-k (tree/node-seq root))))

  clojure.lang.Reversible
  (rseq [this]
    (with-ordered-set this
      (map node/-k (tree/node-seq-reverse root))))

  clojure.lang.ILookup
  (valAt [this k not-found]
    (with-ordered-set this
      (if-let [found (tree/node-find root k)]
        (node/-k found)
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
    (with-ordered-set this
      (cond
        (identical? this o)   0
        (.isCompatible this o) (tree/node-set-compare root (.getRoot ^OrderedSet o))
        (.isSimilar    this o) (.compareTo ^Comparable (into (empty o) this) o)
        true (throw (ex-info "unsupported comparison: " {:this this :o o})))))

  java.util.Collection
  (toArray [this]
    (with-ordered-set this
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

  java.util.List
  (indexOf [this x]
    (with-ordered-set this
      (tree/node-rank root x)))
  (lastIndexOf [this x]
    (.indexOf this x))

  java.util.Set
  (size [_]
    (tree/node-size root))
  (iterator [this]
    (clojure.lang.SeqIterator. (seq this)))
  (containsAll [this s]
    (with-ordered-set this
      (cond
        (identical? this s)    true
        (.isCompatible this s) (tree/node-subset? root (.getRoot ^OrderedSet s))
        (coll? s)              (every? #(.contains this %) s)
        true     (throw (ex-info "unsupported comparison: " {:this this :s s})))))

  java.util.SortedSet
  (comparator [_]
    cmp)
  (first [this]
    (with-ordered-set this
      (node/-k (tree/node-least root))))
  (last [this]
    (with-ordered-set this
      (node/-k (tree/node-greatest root))))
  (headSet [this x]
    (with-ordered-set this
      (new OrderedSet (tree/node-split-lesser root x) cmp alloc stitch {})))
  (tailSet [this x]
    (with-ordered-set this
      (new OrderedSet (tree/node-split-greater root x) cmp alloc stitch {})))
  (subSet [this from to]
    (with-ordered-set this
      (let [left   (tree/node-split-greater root from)
            right  (tree/node-split-lesser  root to)
            result (tree/node-set-intersection left right)]
        (new OrderedSet result cmp alloc stitch {}))))

  java.util.NavigableSet
  (ceiling [this x]
    (with-ordered-set this
      (let [[_ x' r] (tree/node-split root x)]
        (if (some? x')
          (first x')
          (some-> (tree/node-least r) node/-k)))))
  (floor [this x]
    (with-ordered-set this
      (let [[l x' _] (tree/node-split root x)]
        (if (some? x')
          (first x')
          (some-> (tree/node-greatest l) node/-k)))))

  clojure.lang.IPersistentSet
  (equiv [this o]
    (with-ordered-set this
      (cond
        (identical? this o) true
        (not= (tree/node-size root) (.count ^clojure.lang.Counted o)) false
        (.isCompatible this o) (zero? (tree/node-set-compare root (.getRoot ^OrderedSet o)))
        (.isSimilar    this o) (.equiv ^clojure.lang.IPersistentSet (into (empty o) this) o)
        true     (throw (ex-info "unsupported comparison: " {:this this :o o})))))
  (count [_]
    (tree/node-size root))
  (empty [_]
    (new OrderedSet (node/leaf) cmp alloc stitch {}))
  (contains [this k]
    (with-ordered-set this
      (if (tree/node-find root k) true false)))
  (disjoin [this k]
    (with-ordered-set this
      (new OrderedSet (tree/node-remove root k) cmp alloc stitch _meta)))
  (cons [this k]
    (with-ordered-set this
      (new OrderedSet (tree/node-add root k) cmp alloc stitch _meta)))

  clojure.core.reducers.CollFold
  (coll-fold [this n combinef reducef]
    (with-ordered-set this
      (tree/node-chunked-fold n root combinef
        (fn [acc node] (reducef acc (node/-k node)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Literal Representation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod print-method OrderedSet [s w]
  ((get (methods print-method) clojure.lang.IPersistentSet) s w))



(comment

  ;; W00T!

  (def foo (shuffle (range 500000)))
  (def bar (shuffle (range 1000000)))

  (def s0 (shuffle (range 0 1000000 2)))
  (def s1 (shuffle (range 0 1000000 3)))

  ;; Home Team:

  (time (def x (ordered-set foo)))         ;; 500K: "Elapsed time: 564.248517 msecs"
  (time (def y (ordered-set bar)))         ;;   1M: "Elapsed time: 1187.734211 msecs"
  (time (def s (proto/intersection
                 (ordered-set s0)
                 (ordered-set s1))))       ;; 833K: "Elapsed time: 1242.961445 msecs"
  (time (r/fold + + y))                    ;;   1M: "Elapsed time: 54.363545 msecs"

  ;; Visitors:

  (time (def v (into (sorted-set) foo)))   ;; 500K: "Elapsed time: 839.188189 msecs"
  (time (def w (into (sorted-set) bar)))   ;;   1M: "Elapsed time: 1974.798286 msecs"
  (time (def s (clojure.set/intersection
                 (into (sorted-set) s0)
                 (into (sorted-set) s1)))) ;; 833K: "Elapsed time: 1589.786106 msecs"
  (time (r/fold + + w))                    ;;   1M: "Elapsed time: 167.916539 msecs"


  (require '[criterium.core])

  (criterium.core/bench (def x (ordered-set foo)))

;;   Evaluation count : 120 in 60 samples of 2 calls.
;;              Execution time mean : 612.435645 ms
;;     Execution time std-deviation : 60.421726 ms
;;    Execution time lower quantile : 565.022632 ms ( 2.5%)
;;    Execution time upper quantile : 771.090227 ms (97.5%)
;;                    Overhead used : 1.708588 ns
;;
;; Found 11 outliers in 60 samples (18.3333 %)
;; 	low-severe	 1 (1.6667 %)
;; 	low-mild	 10 (16.6667 %)
;;  Variance from outliers : 68.6890 % Variance is severely inflated by outliers

  (criterium.core/bench (def v (into (sorted-set) foo)))

;;   Evaluation count : 120 in 60 samples of 2 calls.
;;              Execution time mean : 819.376840 ms
;;     Execution time std-deviation : 29.835432 ms
;;    Execution time lower quantile : 789.678093 ms ( 2.5%)
;;    Execution time upper quantile : 907.561055 ms (97.5%)
;;                    Overhead used : 1.708588 ns
;;
;; Found 5 outliers in 60 samples (8.3333 %)
;; 	low-severe	 3 (5.0000 %)
;; 	low-mild	 2 (3.3333 %)
;;  Variance from outliers : 22.2640 % Variance is moderately inflated by outliers

;;;
;; clojure.data.avl

  (require '[clojure.data.avl :as avl])

  (time (def z (into (avl/sorted-set) foo))) ;; 500K: "Elapsed time: 586.862601 msecs"
  (time (def z (into (avl/sorted-set) bar))) ;; 1M:   "Elapsed time: 1399.241718 msecs"

  (criterium.core/bench (def z (into (avl/sorted-set) foo)))

;; Evaluation count : 120 in 60 samples of 2 calls.
;;              Execution time mean : 606.249611 ms
;;     Execution time std-deviation : 16.864172 ms
;;    Execution time lower quantile : 560.393078 ms ( 2.5%)
;;    Execution time upper quantile : 631.176588 ms (97.5%)
;;                    Overhead used : 1.710404 ns
;;
;; Found 4 outliers in 60 samples (6.6667 %)
;; 	low-severe	 3 (5.0000 %)
;; 	low-mild	 1 (1.6667 %)
;;  Variance from outliers : 14.2428 % Variance is moderately inflated by outliers

  (time (r/fold + + z))

  )
