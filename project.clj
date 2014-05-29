(defproject godgame "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :license {:name "MIT Licence"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2227"]
                 [rm-hull/monet "0.1.10"]]
  :plugins [[lein-cljsbuild "1.0.3"]]
  :cljsbuild { 
    :builds {
      :main {
        :source-paths ["src"]
        :compiler {:output-to "public/cljs.js"
                   ; :output-dir "public"
                   ; :source-map "public/cljs.js.map"
                   :optimizations :whitespace
                   :pretty-print true}}}})
