(defproject formaterr "1.0.0-SNAPSHOT"
  :description "Download, parse and write files in various formats"
  :url "https://github.com/nez/formaterr"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-http "3.9.1"]
                 [clj-time "0.15.1"]
                 [clojure-csv "2.0.2"]
                 [org.clojure/data.json "0.2.6"]
                 [com.outpace/clj-excel "0.0.9"]
                 [digitalize "0.1.0-SNAPSHOT"]
                 [org.clojars.smallrivers/juniversalchardet "1.0.3"]]
  :repl-options {:init-ns formaterr.core})
