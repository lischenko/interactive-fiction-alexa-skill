(ns ifas2.zaxui
  "Provides a java interface for ZAX UI.
  ZAX has its own idea about what IF UI should look like, and oftentimes it is not really suitable for voice UI.
  There are sevearl hacks to adapt the GUI-based API to voice interface."
  (:import java.io.IOException
           (java.awt Dimension Point))
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :as async :refer [<!! >!! chan close! thread]])
  (:gen-class
   :name ifas.zax.MyZaxUi
   :implements [com.zaxsoft.zax.zmachine.ZUserInterface]
   :state state
   :init init-state
   :methods [[getUserInputStream [] Object]
             [getStoryTextStream [] Object]
             [setNextFilename [String] void]]))

(defn -init-state []
  [[] (atom {:story-text-chan (async/chan)
             :user-input-chan (async/chan)
             :next-filename "/tmp/ifas2.save"})])

(defn -getUserInputStream [this] (:user-input-chan @(.state this)))

(defn -getStoryTextStream [this] (:story-text-chan @(.state this)))

(defn -getFilename
  "This was supposed to prompt user for a filename. We replaced it with the :next-filename state variable which is set programmatically using {@link #setNextFilename(String)}."
  [this title suggested saveFlag]
  (:next-filename @(.state this)))
(defn -setNextFilename          [this filename]
  (swap! (.state this) assoc :next-filename filename))

(defn -readLine
  "Implementation of getting a command text from the user."
  [this string-buf time]
  (try
    (let [command (<!! (:user-input-chan @(.state this)))]
      (.append string-buf command)
      (-> command .getBytes count))
    (catch IOException e
      (do
        (log/error "Error while copying story text" e)
        0))))

(defn -readChar [this time] 0)

(defn -showString
  "This method gets called by ZAX when story text updates.
  This implementation forwards them to a channel the skill can read."
  [this str]
  (try
    (thread (>!! (:story-text-chan @(.state this)) str))
    (catch IOException e
      (log/error "Error while copying story text" e))))

(defn -quit [this]
  (try
    (close! (:story-text-chan @(.state this)))
    (close! (:user-input-chan @(.state this)))
    (catch IOException e
      (log/error "Error while quitting" e))))

(defn -getScreenCharacters      [this] (Dimension. 8192 8192))
(defn -getScreenUnits           [this] (Dimension. 8192 8192))
(defn -getFontSize              [this] (Dimension. 1 1))
(defn -getWindowSize            [this int window] (Dimension. 8192 8192))
(defn -getCursorPosition        [this] (Point. 0 0))
(defn -getDefaultForeground     [this] 1)
(defn -getDefaultBackground     [this] 0)
(defn -defaultFontProportional  [this] false)
(defn -hasBoldface              [this] false)
(defn -hasColors                [this] false)
(defn -hasFixedWidth            [this] false)
(defn -hasItalic                [this] false)
(defn -hasStatusLine            [this] false)
(defn -hasTimedInput            [this] false)
(defn -hasUpperWindow           [this] false)
(defn -eraseLine                [this s])
(defn -eraseWindow              [this window])
(defn -fatal                    [this errmsg])
(defn -initialize               [this ver])
(defn -restart                  [this])
(defn -scrollWindow             [this lines])
(defn -setColor                 [this foreground background])
(defn -setCurrentWindow         [this window])
(defn -setCursorPosition        [this x y])
(defn -setFont                  [this font])
(defn -setTerminatingCharacters [this chars])
(defn -setTextStyle             [this style])
(defn -showStatusBar            [this s  a  b flag])
(defn -splitScreen              [this lines])
