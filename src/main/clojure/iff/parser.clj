(ns iff.parser
  "Functions to parse https://en.wikipedia.org/wiki/Interchange_File_Format"
  (:import java.nio.ByteBuffer java.util.Arrays
           (java.io ByteArrayOutputStream FileOutputStream))
  (:require [clojure.java.io :as io]))

;; Duplicating this for the sake of decoupling:
;; http://stackoverflow.com/questions/23018870/how-to-read-a-whole-binary-file-nippy-into-byte-array-in-clojure
(defn- slurp-bytes
  "Slurp the bytes from a slurpable thing"
  [in]
  (with-open [out (ByteArrayOutputStream.)]
    (io/copy (io/input-stream in) out)
    (.toByteArray out)))

(defn- spit-bytes
  "Spit bytes out to a file."
  [in fname]
  (with-open [out (FileOutputStream. fname)]
    (io/copy (io/input-stream in) out)))

(defn parse
  "Parses the given streamabe input as an IFF, returning a map of chunks."
  ([in] (parse 0 (ByteBuffer/wrap (slurp-bytes in)) {}))
  ([pos ba acc]
   (if (>= 0 (.remaining ba))
     acc
     (let [c1  (.get ba)
           c2  (.get ba)
           c3  (.get ba)
           c4  (.get ba)
           len (.getInt ba)
           key (apply str (map char [c1 c2 c3 c4]))
           val  (byte-array len)
           _   (.get ba val)
           new-pos (+ pos len)]
       ;; iff requires chunks to start at even positions
       (recur
        (if (odd? new-pos)
          (inc new-pos)
          new-pos)
        (if (odd? new-pos)
          (do (.get ba) (.slice ba))
          (.slice ba))
        (assoc acc key val))))))

(defn parse-blorb
  "Parses the given streamabe input as a blorb file, returning a map of chunks"
  [in]
  (let [top (parse in)
        form-chunk (top "FORM")
        form-records (Arrays/copyOfRange form-chunk 4 (alength form-chunk))
        form (parse form-records)]
    form))

(defn zblorb->z
  "Extracts zcode from the given zblorb and writes it to a given file. Returns the file."
  [zblorb zfile]
  (let [blorb (parse-blorb zblorb)]
    (spit-bytes (get blorb "ZCOD") zfile)
    zfile))
