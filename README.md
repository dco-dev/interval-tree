# com.dean.interval-tree

This library provides a colletion of data structures implemented using a
modular, extensible weight balanaced persistent binary tree:
ordered-sets, ordered-maps, interval-sets, and interval-maps.


![tests](https://github.com/dco-dev/interval-tree/actions/workflows/clojure.yml/badge.svg)

### Testing

Testing is accomplished with the standard `lein test`
```
$ time lein test

lein test com.dean.interval-tree.interval-map-test

lein test com.dean.interval-tree.interval-set-test

lein test com.dean.interval-tree.interval-test

lein test com.dean.interval-tree.ordered-map-test

lein test com.dean.interval-tree.ordered-set-test

lein test com.dean.interval-tree.tree-test

Ran 30 tests containing 98206 assertions.
0 failures, 0 errors.

real   5m8.457s
user   8m32.432s
sys    0m4.681s
```

### Inspiration

 This is an implementation of a weight-balanced binary interval-tree data
 structure based on the following inspiration:

 -  Adams (1992)
     'Implementing Sets Efficiently in a Functional Language'
     Technical Report CSTR 92-10, University of Southampton.
     <http://groups.csail.mit.edu/mac/users/adams/BB/92-10.ps>

 -  Hirai and Yamamoto (2011)
     'Balancing Weight-Balanced Trees'
     Journal of Functional Programming / 21 (3):
     Pages 287-307
     <https://yoichihirai.com/bst.pdf>

 -  Oleg Kiselyov
     'Towards the best collection API, A design of the overall optimal
     collection traversal interface'
     <http://pobox.com/~oleg/ftp/papers/LL3-collections-enumerators.txt>

 -  Nievergelt and Reingold (1972)
     'Binary Search Trees of Bounded Balance'
     STOC '72 Proceedings
     4th Annual ACM symposium on Theory of Computing
     Pages 137-142

 -  Driscoll, Sarnak, Sleator, and Tarjan (1989)
     'Making Data Structures Persistent'
     Journal of Computer and System Sciences Volume 38 Issue 1, February 1989
     18th Annual ACM Symposium on Theory of Computing
     Pages 86-124

 -  MIT Scheme weight balanced tree as reimplemented by Yoichi Hirai
     and Kazuhiko Yamamoto using the revised non-variant algorithm recommended
     integer balance parameters from (Hirai/Yamomoto 2011).

 -  Wikipedia
     'Interval Tree'
     <https://en.wikipedia.org/wiki/Interval_tree>

 -  Wikipedia
     'Weight Balanced Tree'
     <https://en.wikipedia.org/wiki/Weight-balanced_tree>

 -  Andrew Baine, Rahul Jaine (2007)
     'Purely Functional Data Structures in Common Lisp'
     Google Summer of Code 2007
     <https://common-lisp.net/project/funds/funds.pdf>
     <https://developers.google.com/open-source/gsoc/2007/>

 - Scott L. Burson
     'Functional Set-Theoretic Collections for Common Lisp'
     <https://common-lisp.net/project/fset/>

### License

The use and distribution terms for this software are covered by the [Eclipse Public License 1.0](http://opensource.org/licenses/eclipse-1.0.php), which can be found in the file EPL10.txt at the root of this distribution. By using this software in any fashion, you are agreeing to be bound by the terms of this license. You must not remove this notice, or any other, from this software.
