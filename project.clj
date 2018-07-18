(defproject com.dean/interval-tree "0.1.0-SNAPSHOT"
  :description "Modular, Extensible, Weight-Balanced Tree"
  :url "http://github.com/dco-dev/interval-tree"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]]

  :plugins [[lein-asciidoctor  "0.1.14"]]

  :asciidoc {:sources ["doc/*.adoc"]
             :to-dir "doc/html"
             :toc              :left
             :doctype          :article
             :format           :html5
             :extract-css      true
             :source-highlight true})
