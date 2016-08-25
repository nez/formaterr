(defproject formaterr "0.1.0-SNAPSHOT"
  :description "FIXME"
  :url "https://github.com/fractalLabs/formaterr"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-time "0.8.0"]
                 [clojure-csv "2.0.1"]
                 [http-kit "2.1.18"]
                 [org.clojure/data.json "0.2.6"]
                 [com.outpace/clj-excel "0.0.9"]
                 [digitalize "0.1.0-SNAPSHOT"]
                 [org.clojars.hozumi/clj-det-enc "1.0.0-SNAPSHOT"]]
  :repl-options {:init-ns formaterr.core})
