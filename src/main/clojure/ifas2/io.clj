(ns ifas2.io
  (:import java.io.ByteArrayOutputStream
           java.io.File
           java.lang.Class
           java.util.Base64)
  (:require [clojure.java.io :as io]))

(def cache-dir "/tmp/ifas-cache/")

;; http://stackoverflow.com/questions/23018870/how-to-read-a-whole-binary-file-nippy-into-byte-array-in-clojure
(defn- slurp-bytes
  "Slurp the bytes from a slurpable thing"
  [in]
  (with-open [out (ByteArrayOutputStream.)
              in  (io/input-stream in)]
    (io/copy in out)
    (.toByteArray out)))

;; (defn- spit-bytes
;;   [bs out]
;;   (with-open [in  (io/input-stream bs)
;;               out (io/output-stream out)]
;;     (io/copy in out)
;;     out))

(defn- as-file
  "Wraps the given byte array into a temporary file."
  [data]
  {:pre [(= (type data) (Class/forName "[B"))]}
  (let [tmp-file (File/createTempFile "ifas2" ".data")]
    (.deleteOnExit tmp-file)
    (with-open [in  (io/input-stream data)
                out (io/output-stream tmp-file)]
      (io/copy in out))
    tmp-file))

(defn base64-as-file
  "Adapts the given base64 string to a binary file."
  [base64]
  (->
   (Base64/getDecoder)
   (.decode base64)
   as-file))

(defn file-as-base64
  "Given a binary file, reads it and returns a base64 string of data it contains."
  [filename]
  (->> filename
       slurp-bytes      
       (.encodeToString (Base64/getEncoder))))

(defn- download-impl
  "Downloads the given url and returns a path to a file with its contents."
  [url]
  (let [file-name (-> url (.replaceAll "/" "-") (.replaceAll "\\.\\." "bobby"))
        file-path (File. (str cache-dir) (str file-name))]
    (io/make-parents file-path)
    (with-open [in (io/input-stream url)
                out (io/output-stream file-path)]
      (io/copy in out)
      file-path)))
(def download (memoize download-impl))

