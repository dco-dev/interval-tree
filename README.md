# com.dean.interval-tree

This library provides a colletion of data structures implemented using a
modular, extensible weight balanaced persistent binary tree:
ordered-sets, ordered-maps, interval-sets, and interval-maps.


### Testing

Testing is accomplished with the standard `lein test`
```
$ time lein test

lein test com.dean.interval-tree.interval-map-test

lein test com.dean.interval-tree.interval-set-test

lein test com.dean.interval-tree.interval-test

lein test com.dean.interval-tree.ordered-set-test

lein test com.dean.interval-tree.tree-test

Ran 28 tests containing 98112 assertions.
0 failures, 0 errors.

real	4m40.752s
user	7m58.382s
sys	    0m3.977s
```


### License

The use and distribution terms for this software are covered by the [Eclipse Public License 1.0](http://opensource.org/licenses/eclipse-1.0.php), which can be found in the file EPL10.txt at the root of this distribution. By using this software in any fashion, you are agreeing to be bound by the terms of this license. You must not remove this notice, or any other, from this software.
