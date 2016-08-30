(ns formaterr.core
  (:use clojure-csv.core)
  (:require [clj-excel.core :refer :all]
            [clojure.data.json :as json]
            [clojure.string :as s]
            [clojure-csv.core :refer :all]
            [digitalize.core :refer :all]
            [hozumi.det-enc :refer :all]))

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
    (map #(zipmap head %) tail)))

(defn parse-csv-str
  "Transform a csv string into a relation"
  [s]
  (let [csv (parse-csv s)
        head (map standard-keyword (first csv))
        tail (rest csv)]
    (map #(zipmap head %) tail)))

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
     (json/read-str o
                    :key-fn standard-keyword)
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
