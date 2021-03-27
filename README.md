# com.dean.interval-tree

This library provides a collection of data structures implemented using a
modular, extensible, foldable, weight balanaced persistent binary tree:
ordered-sets, ordered-maps, interval-sets, and interval-maps.

![tests](https://github.com/dco-dev/interval-tree/actions/workflows/clojure.yml/badge.svg)
[![Clojars Project](https://img.shields.io/clojars/v/com.dean/interval-tree.svg)](https://clojars.org/com.dean/interval-tree)

### Usage

To install, add the following dependency to your project or build file:

```[com.dean/interval-tree "0.1.1"]```

### Topics

#### What is an Interval Map?

Imagine you'd like to associate some value with members of a set of
intervals over some continuous domain, such as time or real numbers.
An example of this is shown below. An interval map answers the question,
which intervals overlap at some point on the domain. At 3.14159, in this
case, would be `x4` and `x7`.  The interval map is sparse itself, of
course, and would only need to contain the 8 constituent intervals.

```
 x8:                         +-----+
 x7:                   +-----------------------------------+
 x6:                                                       +
 x5:                                     +-----------+
 x4: +-----------------------------+
 x3:                                                 +-----+
 x2:                         +-----------------+
 x1:       +-----------+

     0=====1=====2=====3=====4=====5=====6=====7=====8=====9
```

#### Efficient Set Operations




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

Ran 30 tests containing 98214 assertions.
0 failures, 0 errors.

real     5m34.487s
user    10m21.397s
sys      0m5.047s
```

### Inspiration

 This is an implementation of a weight-balanced binary interval-tree data
 structure inspired by the following:

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

The use and distribution terms for this software are covered by the [Eclipse Public License 1.0](http://opensource.org/licenses/eclipse-1.0.php), which can be found in the file LICENSE.txt at the root of this distribution. By using this software in any fashion, you are agreeing to be bound by the terms of this license. You must not remove this notice, or any other, from this software.
