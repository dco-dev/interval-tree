(ns com.dean.interval-tree.tree.ordered-map
  (:require [clojure.core.reducers       :as r :refer [coll-fold]]
            [com.dean.interval-tree.tree.node     :as node]
            [com.dean.interval-tree.tree.protocol :as proto]
            [com.dean.interval-tree.tree.root]
            [com.dean.interval-tree.tree.tree     :as tree]
            [com.dean.interval-tree.tree.order    :as order])
  (:import  [clojure.lang                RT]
            [com.dean.interval_tree.tree.root     INodeCollection
                                         IBalancedCollection
                                         IOrderedCollection]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Dynamic Environment
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro with-ordered-map [x & body]
  `(binding [order/*compare* (.getCmp ~(with-meta x {:tag 'com.dean.interval_tree.tree.root.IOrderedCollection}))]
     ~@body))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Ordered Map
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftype OrderedMap [root cmp alloc stitch _meta]

  INodeCollection
  (getAllocator [_]
    alloc)
  (getRoot [_]
    root)

  IOrderedCollection
  (getCmp [_]
    cmp)
  (isCompatible [_ o]
    (and (instance? OrderedMap o) (= cmp (.getCmp o)) (= stitch (.getStitch o))))
  (isSimilar [_ o]
    (map? o))

  IBalancedCollection
  (getStitch [_]
    stitch)

  clojure.lang.IMeta
  (meta [_]
    _meta)

  clojure.lang.IObj
  (withMeta [_ m]
    (OrderedMap. root cmp alloc stitch m))

  clojure.lang.Indexed
  (nth [this i]
    (with-ordered-map this
      (node/-kv (tree/node-nth root i))))

  clojure.lang.MapEquivalence

  clojure.lang.Seqable
  (seq [this]
    (with-ordered-map this
      (map node/-kv (tree/node-seq root))))

  clojure.lang.Reversible
  (rseq [this]
    (with-ordered-map this
      (map node/-kv (tree/node-seq-reverse root))))

  clojure.lang.ILookup
  (valAt [this k not-found]
    (with-ordered-map this
      (if-let [found (tree/node-find root k)]
        (node/-v found)
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
    (with-ordered-map this
      (cond
        (identical? this o) 0
        (.isCompatible this o) (tree/node-map-compare root (.getRoot o))
        (.isSimilar    this o) (.compareTo (into (empty o) this) o)
        true (throw (ex-info "unsupported comparison: " {:this this :o o})))))

  clojure.lang.Counted
  (count [this]
    (tree/node-size root))

  clojure.lang.Associative
  (containsKey [this k]
    (with-ordered-map this
      (some? (tree/node-find root k))))
  (entryAt [this k]
    (with-ordered-map this
      (some-> root (tree/node-find k) node/-kv)))
  (assoc [this k v]
    (with-ordered-map this
      (OrderedMap. (tree/node-add root k v) cmp alloc stitch _meta)))
  (empty [this]
    (OrderedMap. (node/leaf) cmp alloc stitch {}))

  java.util.Map
  (get [this k]
    (.valAt this k))
  (isEmpty [_]
    (node/leaf? root))
  (size [_]
    (tree/node-size root))
  (keySet [this]
    (with-ordered-map this
      (set (tree/node-vec root :accessor :k))))
  (put [_ _ _]
    (throw (UnsupportedOperationException.)))
  (putAll [_ _]
    (throw (UnsupportedOperationException.)))
  (clear [_]
    (throw (UnsupportedOperationException.)))
  (values [this]
    (with-ordered-map this
      (tree/node-vec root :accessor :v)))
  (entrySet [this]
    (with-ordered-map this
      (set (tree/node-vec root :accessor :kv))))
  (iterator [this]
    (clojure.lang.SeqIterator. (seq this)))

  clojure.lang.IPersistentCollection
  (equiv [this o]
    (with-ordered-map this
      (cond
        (identical? this o) 0
        (.isCompatible this o) (and (= (.count this) (.count o))
                                    (zero? (tree/node-map-compare root (.getRoot o))))
        (map? o) (.equiv (into (empty o) (tree/node-vec root :accessor :kv)) o)
        true     (throw (ex-info "unsupported comparison: " {:this this :o o})))))

  (cons [this o]
    (.assoc this (nth o 0) (nth o 1)))

  clojure.lang.IPersistentMap
  (assocEx [this k v]               ;; TODO: use `tree/node-add-if`
    (if (contains? this k)
      (throw (Exception. "Key or value already present"))
      (assoc this k v)))
  (without [this k]
    (with-ordered-map this
      (OrderedMap. (tree/node-remove root k) cmp alloc stitch _meta))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Literal Representation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod print-method OrderedMap [m w]
  ((get (methods print-method) clojure.lang.IPersistentMap) m w))
