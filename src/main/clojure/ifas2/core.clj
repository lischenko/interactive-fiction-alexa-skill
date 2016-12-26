(ns ifas2.core
  "Main namespace of the skill. The entry point to this is the -handleLambda function called by Amazon AWS Lambda. The AWS lambda function is configured with ifas.ZLambdaFunction::handleLambda"
  (:gen-class
   :name ifas.ZLambdaFunction
   :methods [[handleLambda [java.util.Map] java.util.Map]])
  (:import java.util.Base64
           java.io.File)
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log])
  (:use alexa.format
        ifas2.beautify
        ifas2.zax
        ifas2.db
        ifas2.io
        iff.parser))

(def skill-name "Interactive Fiction")
(def sample-stories ["Galatea" "Nine oh five" "Night at the computer center"])

(defn- randomized-help-text []
  (let [random-story (rand-nth sample-stories)]
    (str "You can start a new story by saying 'Ask " skill-name " to download' and then the story name, for example \"" random-story  "\". Try it now, say \"Download " random-story "\".")))

(defn- decorate-command-output
  "Just adds command feedback in the beginning and prompt for next command at the end of the given text"
  [text command]
  (str
   "Your command: " command ".\n"
   text
   ".\nWhat's your next command?"))

(defn- decorate-story-output
  "Just adds story name to the front and prompt for next command at the end of the given text"
  [text story]
  (str
   "New story: " story ".\n"
   text
   ".\nWhat's your next command?"))

(defn- literal-transform
  "Handles literal transformations. Basically chops off leading 'literally' or 'verbatim'."
  [c]
  (str/replace-first c #"^(literally|verbatim),?\s+" ""))

(defn- sanitize-command [c] (str/trim (str/replace c #"[^a-zA-Z ]" "")))

(defn- story->z
  "Given a z- or zblorb- story file, returns a z file. For z files it is the identity function, for zblorb it is an extraction function."
  [f]
  (let [path  (.getAbsolutePath f)
        fname (last (str/split path #"/"))]
    (if (re-matches #".*\.z\d" path)
      f
      (zblorb->z f (File/createTempFile fname ".zcod")))))

(defn run-story!
  "Downloads the story, loads it into the interpreter, optionally restores story state from a dump, optionally sends command(s) and returns the output.
  Applies beautifications along the way."
  ([client-id url] (run-story! client-id url nil nil))
  ([client-id url dump64 next-command]
   (log/infof "client '%s' is running story '%s' with command '%s'" client-id  url next-command)
   (let [story-file (.getAbsolutePath (story->z (ifas2.io/download url)))
         vm-map     (create-zax-vm story-file url dump64)]

     (when next-command
       (write-text-to-vm vm-map (beautify-in url next-command)))

     (let [raw-texts (beautify-story-sequence url vm-map)
           raw-text  (str/join "\n" raw-texts)
           text      (beautify-out url raw-text)
           dump-new  (make-dump64 vm-map)]

       (db-update-story-dump! client-id url dump-new)
       
       {:raw-texts raw-texts
        :raw-text  raw-text
        :text      text
        :dump      dump-new}))))

(defmulti handle-intent
  "A multimethod to handle incoming Alexa intents. Usually dispatches by declared intent name, but also recognizes
  special command intents (like 'stop' and 'help') which Alexa reports incorrectly for some reason."
  (fn [req]
    (let [alexa-intent (-> req extract-intent-name keyword)
          command      (extract-slot-value req "payload")]
      ;; some combinations have special meanings and override the alexa-reported intent type
      (if (= alexa-intent :InteractiveFictionCommandIntent)
        (case (sanitize-command command)
          ("stop")                           :AMAZON.StopIntent
          ("cancel" "forget it" "ignore it") :AMAZON.CancelIntent
          ("help" "literally")               :AMAZON.HelpIntent ;;"literally" alone does not make sense, call help
          alexa-intent)
        alexa-intent))))

(defmethod handle-intent :AMAZON.StopIntent [request]
  ["Okay, I have saved your progress, you can come back at any time." true])

(defmethod handle-intent :AMAZON.CancelIntent [request]
  ["Okay" true])

(defmethod handle-intent :InteractiveFictionNewStory [request]
  (let [client-id            (extract-client-id request)
        requested-story-name (extract-slot-value request "storyName")
        url                  (db-find-url! requested-story-name)
        text                 (if url
                               (decorate-story-output (:text (run-story! client-id url)) requested-story-name)
                               (str "I could not find the story " requested-story-name "."))]
    [text (not (boolean url))]))

(defmethod handle-intent :InteractiveFictionCommandIntent [request]
  (let [client-id            (extract-client-id request)
        command              (literal-transform (extract-slot-value request "payload"))
        {:keys [url dump64]} (db-find-last-story! client-id)
        text                 (decorate-command-output (:text (run-story! client-id url dump64 command)) command)]
    [text false]))

(defmethod handle-intent :AMAZON.HelpIntent [_]
  [(randomized-help-text) false])

(defmethod handle-intent :default [_]
  [(randomized-help-text) false])

(defn -handleLambda
  "Entry point for Amazon Lambda call triggered by Alexa.
  Alexa passes in its input as json (transparently parsed to a Map).
  The expected output is Alexa response in the form of a Map (which too will be converted to json by Jackson under the hood)."
  [obj input]
  (log/debug "INPUT:" input)
  (log/spyf :info "OUTPUT: %s"
            (apply (partial create-simple-response skill-name) (handle-intent input)))) 
