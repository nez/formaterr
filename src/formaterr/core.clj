(ns formaterr.core
  (:use clojure-csv.core)
  (:require [clj-excel.core :refer :all]
            [clojure.data.json :as json]
            [clojure.string :as s]
            [digitalize.core :refer :all]))

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
  (if (= n 0)
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

(defn parse-csv-str
  "Transform a csv string into a relation"
  [s]
  (let [csv (parse-csv s)
        head (map keyword (first csv))
        tail (rest csv)]
    (map #(zipmap head %) tail)))

(defn parse-tsv-str
  "Transform a TSV string into a relation"
  [s]
  (let [csv (parse-csv s :delimiter \tab)
        head (map keyword (first csv))
        tail (rest csv)]
    (map #(zipmap head %) tail)))

(defn parse-csv-file
  [file-name] (parse-csv-str (slurp file-name)))

(defn keyword-str [o]
  (keyword (str o)))


(defn csv
  ([file-name] (parse-csv-file file-name))
  ([file-name data]
    (csv file-name data (all-keys data)))
  ([file-name data head]
   (spit file-name (csv-str data head))))

(defn tsv
  ([file-name] (parse-tsv-str (slurp file-name))))

(defn json
  ([o]
   (if (string? o)
     (json/read-str o
                    :key-fn keyword)
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

(defmethod xls [:string 0] [frst & args]
  (flatten (digitalize (map vecs->maps (-> frst workbook-hssf lazy-workbook vals)))))
