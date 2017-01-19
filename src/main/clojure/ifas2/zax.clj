(ns ifas2.zax
  "Provides an interface to ZAX interpreter"
  (:use ifas2.io
        ifas2.zaxui
        ifas2.zax-public-morozov)
  (:require [clojure.core.async :as async :refer [<!! >!! alts!! timeout thread]])
  (:import (ifas.zax MyZaxUi)
           (com.zaxsoft.zmachine ZCPU)
           (java.io File)))

(defn destroy-zax-vm!
  "Destroys the given interpreter."
  [vm-map]
  (zax-quit (:cpu vm-map)))

(defn read-text-from-vm!
  "Given an interpreter, reads available text from it."
  ([vm-map] (->> vm-map :zax-ui .getStoryTextStream (read-text-from-vm! []) (apply str)))
  ([acc chan]
   (let [[txt port] (alts!! [(timeout 100) chan])]
     (if txt
       (read-text-from-vm! (conj acc txt) chan)
       acc))))

(defn write-text-to-vm! 
  "Given an interpreter, writes the specified text to it"
  [vm-map text]
  (thread
    (>!! (.getUserInputStream (:zax-ui vm-map)) text)))

(defn make-dump64 [vm-map]
  (let [{:keys [cpu zax-ui]} vm-map
        file (File/createTempFile "ifas2" "dump")]
    (.deleteOnExit file)
    (zop-save cpu zax-ui (.getAbsolutePath file))
    (file-as-base64 (.getAbsolutePath file))))

(defn create-zax-vm
  "Creates a new interpreter for a given story. If a dump is provided, loads it into the interpreter."
  [story-file-path story-url dump64]
  (let [zax-ui (MyZaxUi.)
        cpu    (ZCPU. zax-ui)]
    (.initialize cpu story-file-path)
    (.start cpu)
    (when dump64 ;;TODO: do we need to pause here to allow some time for the story to load?
      (read-text-from-vm! {:zax-ui zax-ui}) ; discard initial output
      (zop-restore cpu
                   zax-ui
                   (.getAbsolutePath (base64-as-file dump64))))
    {:story-url story-url
     :cpu       cpu
     :zax-ui    zax-ui}))
