(defproject com.dean/interval-tree "0.1.2"
  :description "Modular, Extensible, Foldable Weight-Balanced Tree"
  :url "http://github.com/dco-dev/interval-tree"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/math.combinatorics "0.1.4"]]

  :plugins [[lein-asciidoctor  "0.1.14"]
            [lein-codox "0.10.7"]]

  :signing  {:gpg-key "3A2F2AA9"}

  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                    :username :env/clojars_user
                                    :password  :env/clojars_pass
                                    :sign-releases false}]]

  :codox    {:output-path  "doc/api"
             :src-dir-uri "https://github.com/dco-dev/interval-tree/blob/master/"
             :src-linenum-anchor-prefix "L"
             :project {:name "com.dean/interval-tree"}}

  :asciidoctor {:sources ["doc/*.adoc"]
                :to-dir "doc/html"
                :toc              :left
                :doctype          :article
                :format           :html5
                :extract-css      true
                :source-highlight true})
