(ns ifas2.zax-public-morozov
  "Uses reflection to access private parts of ZAX. Trading off the ugly hack for unchaned ZAX code."
  (:import
   com.zaxsoft.zmachine.ZCPU

   java.io.IOException
   java.lang.reflect.Field
   java.lang.reflect.InvocationTargetException
   java.lang.reflect.Method)
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]))

(defn- zcpuReflectionCall [cpu method]
  (try
    (let [m (.getDeclaredMethod ZCPU method (make-array Class 0))]
      (.setAccessible m true)
      (.invoke m cpu (into-array [])))
    (catch NoSuchMethodException e (log/error (str "Could not hack ZAX " method) e))
    (catch InvocationTargetException e (log/error (str "Could not hack ZAX " method) e))
    (catch IllegalAccessException e (log/error (str "Could not hack ZAX " method) e))))

(defn- zcpuReflectionFlag [cpu flag val]
  (try
    (let [f (.getDeclaredField (class ZCPU) flag)]
      (.setAccessible f true)
      (.setBoolean f val))
    (catch NoSuchFieldException e (log/error (str "Could not hack ZAX flag " flag) e))
    (catch IllegalAccessException e (log/error (str "Could not hack ZAX flag " flag) e))))

(defn- zcpuPersistenceReflectionHelper [cpu ui dump-filename method]
  (locking ui

    (.setNextFilename ui dump-filename) ;; makes ZaxUI provide proper filename
    (zcpuReflectionCall cpu method)))

(defn zop-save [cpu ui dump-filename]
  (zcpuPersistenceReflectionHelper cpu ui dump-filename "zop_save"))

(defn zop-restore [cpu ui dump-filename]
  (zcpuPersistenceReflectionHelper cpu ui dump-filename "zop_restore"))

(defn zax-quit [cpu]
  (zcpuReflectionFlag cpu "decode_ret_flag" true)
  (zcpuReflectionCall cpu "zop_quit"))
