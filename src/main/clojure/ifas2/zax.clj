(ns ifas2.zax
  "Provides an interface to ZAX interpreter"
  (:import (ifas.zax ZaxPublicMorozov Streams MyZaxUi)
           (com.zaxsoft.zmachine ZCPU))
  (:use ifas2.io))

(defn destroy-zax-vm
  "Destroys the given interpreter."
  [vm-map]
  (ZaxPublicMorozov/zcpuReflectionFlag (:cpu vm-map) "decode_ret_flag" true)
  (ZaxPublicMorozov/zcpuReflectionCall (:cpu vm-map) "zop_quit"))

(defn read-text-from-vm
  "Given an interpreter, reads available text from it."
  [vm-map]
  (try
    (Thread/sleep 100) ;XXX
    (catch InterruptedException e (.getMessage e)))
  (Streams/readStringFromPipe (.getStoryTextStream (:zax-ui vm-map)) (* 1024 1024)))

(defn write-text-to-vm 
  "Given an interpreter, writes the specified text to it"
  [vm-map text]
  (Streams/writeStringToPipe text (.getUserInputStream (:zax-ui vm-map))))

(defn make-dump64 [vm-map]
  (let [{:keys [cpu zax-ui]} vm-map
        file (java.io.File/createTempFile "ifas2" "dump") ;; TODO hide inside zop-save
        ]
    (.deleteOnExit file)
    (ZaxPublicMorozov/zop_save cpu (.getAbsolutePath file) zax-ui)
    (file-as-base64 (.getAbsolutePath file))))

(defn create-zax-vm
  "Creates a new interpreter for a given story. If a dump is provided, loads it into the interpreter."
  [story-file-path story-url dump64]
  (let [zax-ui (MyZaxUi.)
        cpu    (ZCPU. zax-ui)]
    (.initialize cpu story-file-path)
    (.start cpu)
    (when dump64
      (read-text-from-vm {:zax-ui zax-ui}) ; discard initial output
      (ZaxPublicMorozov/zop_restore cpu
                                    zax-ui
                                    (.getAbsolutePath (base64-as-file dump64))))
    {:story-url story-url
     :cpu       cpu
     :zax-ui    zax-ui}))
