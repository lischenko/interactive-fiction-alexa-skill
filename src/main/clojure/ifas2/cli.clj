(ns ifas2.cli
  "Provides a simple command-line interaface to story and command requests."
  (:use ifas2.core))

(defn- make-request
  [intent]
  (let [proto {"session" {"user" {"userId" "cli-user"}}
               "request" {"type" "IntentRequest"}}
        handle-request (partial -handleLambda nil)]
    (-> (assoc-in proto ["request" "intent"] intent)
        handle-request
        (get-in ["response" "card" "content"])
        println)))

(defn story
  [name]
  (make-request
   {"name" "InteractiveFictionNewStory"
    "slots" {"storyName" {"name" "storyName"
                          "value" name}}}))

(defn command
  [command]
  (make-request
   {"name" "InteractiveFictionCommandIntent"
    "slots" {"payload" {"name" "payload"
                        "value" command}}}))
