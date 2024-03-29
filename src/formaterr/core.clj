(ns formaterr.core
  (:require [clj-excel.core :refer :all]
            [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.string :as s]
            [clojure-csv.core :refer :all]
            [digitalize.core :refer :all]
            [digitalize.strings :refer :all]
            [clojure.java.io :as io]
            [formaterr.encoding :refer :all]))

(defn download
  "Download a web resource"
  [url]
  (println "Downloading: " url)
  (let [req (http/get url {:as :auto
                           :insecure? true ; Needed to contact a server with an untrusted SSL cert
                           :user-agent "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"
                           :max-redirects 10
                           :conn-timeout 10000
                           :socket-timeout 10000})
        status (:status req)]
    (if (< status 400) ; ojo: esto acepta codigos 200, 300
      (:body req)
      (throw (Exception. (str status))))))

(defn copy
  "Copy a web resource to a file"
  [url file]
  (try
    (io/copy (download url)
             (java.io.File. file))
    (catch Exception e (println "Exception copying: " url))))

(defn all-keys [rel]
  (distinct (flatten (map keys rel))))

(defn le-loop [f a]
  (loop [a a b []]
    (if (empty? a)
        b
        (recur (rest a)
               (f (first a))))))

(defn how-many-exist [k coll]
  (count (filter (partial = k) coll)))

(defn keyword-correspondiente [k n]
  (if (zero? n)
      k
      (keyword (str (name k) "-" n))))

(comment TODO
(defn rename-equal-keys [elems]
  (loop [in elems out []]
    (if (empty? in)
        out
        (recur (rest in)
               ))))
)

(defn maps->vecs
  [rel]
  (let [head (all-keys rel)]
    (map #(map str %)
         (concat [(map name head)]
                 (map #(map % head)
                      rel)))))
(defn csv-str
  "Transform a relation into csv string"
  ([coll] (csv-str coll (all-keys coll)))
  ([coll head]
  (write-csv (map #(map str %)
                  (concat [(map name head)] (map #(map % head) coll))))))


(defn split-lines [s]
  (re-seq #"[^\n]+" s))

(defn re-count [re s]
  (count (re-seq re s)))

(defn remove-first-columns [s] ;TODO QA
  (let [rows (take 8 (split-lines s))
        orig-rows (count rows)
        counts (map #(re-count #"," %) rows)
        max-cols (apply max counts)
        trim-size (count (drop-while #(> max-cols (re-count #"," %)) rows))
        rows-to-trash (- orig-rows trim-size)]
    (s/join "\n" (drop rows-to-trash (split-lines s)))))

(defn vecs->maps
  [vecs]
  (let [head (first vecs)]
    (map #(zipmap head %)
         (rest vecs))))

;;elimina solo las que no tienen. seria mejor quitar todas las que tienen menos q el max?
(defn remove-lines-without-delimiter
  [colls]
  (if (apply = (map count colls))
    colls
    (remove #(= 1 (count %)) colls)))

(defn parse-sv-str
  "Transform a char separated value string into a relation"
  [s delimiter]
  (let [csv (remove-lines-without-delimiter
             (parse-csv s :delimiter delimiter))
        head (map standard-keyword (first csv))
        tail (rest csv)]
    (pmap #(zipmap head %) tail)))

(defn parse-csv-str
  "Transform a csv string into a relation"
  [s]
  (let [csv (parse-csv s)
        head (map standard-keyword (first csv))
        tail (rest csv)]
    (pmap #(zipmap head %) tail)))

(defn parse-csv-file
  [file-name] (parse-csv-str (slurp file-name :encoding (detect file-name))))

(defn keyword-str [o]
  (keyword (str o)))

(defn parse-sv
  [file separator]
  (parse-sv-str (slurp file :encoding (detect file))
                separator))

(defn csv
  ([file]
   (parse-sv file \,))
  ([file data]
    (csv file data (all-keys data)))
  ([file data head]
   (spit file (csv-str data head))))

(defn tsv
  ([file]
   (parse-sv file \tab)))

(defn psv
  ([file]
   (parse-sv file \|)))

(defn json
  ([o]
   (if (string? o)
     (json/read-str o :key-fn standard-keyword)
     (json/write-str o)))
  ([file o]
   (spit file (json o))))

(defn geo-json [x y]
  {:coords
    {:type "Point",
     :coordinates [x y]}})

(defn json->geo-json [m]
  (let [x (or (m :x)
              (m :longitude)
              (m :longitud))
        y (or (m :y)
              (m :latitude)
              (m :latitud))
        geo (geo-json x y)]
    (merge geo
           (dissoc m :x :longitude :longitud :y :latitude :latitud))))

(defmulti xls (fn [frst & args]
                [(cond (string? frst) :string)
                 (count args)]))

;; Read xls
(defmethod xls [:string 0] [frst & args]
  (flatten (digitalize (map vecs->maps (-> frst workbook-hssf lazy-workbook vals)))))

;; Write xls
(defmethod xls [:string 1] [file data]
  (save (build-workbook (workbook-hssf) {"Numbers" (maps->vecs data)})
        file))

(defn slurp-head [f]
  (re-find #"[^\n]+" (slurp f)))
