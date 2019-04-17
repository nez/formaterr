(ns formaterr.encoding
  (:use [digitalize.core :refer :all]
        [digitalize.strings :refer :all])
  (:require [clojure.java.io :as io]))

(def conversions
  {"ÃƒÆ’" "Á"
   "Ãƒâ€°" "É"
   "ÃƒÆ’Ã¢â‚¬Â°" "É"
   "ÃƒÂ" "Í"
   "ÃƒÆ’" "Í"
   "ÃƒÆ’Ã¢â‚¬Å“" "Ó"
   "Ãƒâ€œ" "Ó"
   "ÃƒÆ’Ã…Â¡" "Ú"
   "ÃƒÆ’Ã‚Â¡" "á"
   "ÃƒÆ’Ã‚Â©" "é"
   "ÃƒÆ’Ã‚Â³" "ó"
   "ÃƒÆ’Ã‚Â±" "ñ"
   "ÃƒÆ’Ã¢â‚¬Ëœ" "Ñ"
   "ÃƒÆ’Ã‚Â­" "í"
   "Ãƒâ€˜" "Ñ"
   "ÃƒÂ¡" "á"
   "ÃƒÂ" "á"
   "ÃƒÂ©" "é"
   "ÃƒÆ’©" "é"
   "ÃƒÂ­" "í"
   "ÃƒÂ³" "ó"
   "ÃƒÆ’³" "ó"
   "ÃƒÂº" "ú"
   "ÃƒÂ±" "ñ"
   "Ãƒâ€šª" "ª"
   "Ã‚Â" ""
   "ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œ" ""
   "ÃƒÂ¢Ã¢â€šÂ¬" ""
   })

(defn convert-ugly-encoding [coll]
  (map #(str-replace conversions %) coll))

;; https://github.com/hozumi/clj-det-enc/blob/master/src/det_enc/core.clj

(defn- judge-seq! [buf ^java.io.InputStream istream
                   ^org.mozilla.universalchardet.UniversalDetector detector]
  (let [n        (.read istream buf)
        proceed? (and (pos? n)
                      (not (do (.handleData detector buf 0 n)
                               (.isDone detector))))]
    (when proceed?
      (recur buf istream detector))))

(defn detect
  ([target]
   (detect target nil))
  ([target encodingname-when-unknown]
   (let [buf      (make-array Byte/TYPE 4096)
         detector (org.mozilla.universalchardet.UniversalDetector. nil)]
     (with-open [istream (io/input-stream target)]
       (dorun (judge-seq! buf istream detector))
       (.dataEnd detector)
       (or (.getDetectedCharset detector)
           (if (= encodingname-when-unknown :default)
             (.displayName (java.nio.charset.Charset/defaultCharset))
             encodingname-when-unknown))))))
