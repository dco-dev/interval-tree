(ns com.dean.interval-tree.tree.order
  (:refer-clojure :exclude [compare <= >= max]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Comparator
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: note about fanciness

(defn normalize ^long [^long x]
  (if (zero? x)
    x
    (bit-or 1
      (bit-shift-right x 63))))

(defn compare-by
  "Given a predicate that defines a total order over some domain,
  return a three-way comparator built from it."
  [pred]
  (fn [x y]
    (cond
      (pred x y) -1
      (pred y x) +1
      true        0)))

(defn normal-compare ^long [x y]
  (normalize (clojure.core/compare x y)))

(def ^:dynamic *compare* normal-compare)

(defn compare ^long [x y]
  (*compare* x y))

(defn compare< [x y]
  (neg? (compare x y)))

(defn compare<= [x y]
  (not (pos? (compare x y))))

(defn compare> [x y]
  (pos? (compare x y)))

(defn compare>= [x y]
  (not (neg? (compare x y))))

(defn compare= [x y]
  (zero? (compare x y)))

(defn max [x & args]
  (reduce #(if (compare> %1 %2) %1 %2) x args))

(defn <=
  ([x] true)
  ([x y] (compare<= x y))
  ([x y & more]
   (if (compare<= x y)
     (if (next more)
       (recur y (first more) (next more))
       (compare<= y (first more)))
     false)))

(defn >=
  ([x] true)
  ([x y] (compare>= x y))
  ([x y & more]
   (if (compare>= x y)
     (if (next more)
       (recur y (first more) (next more))
       (compare>= y (first more)))
     false)))
