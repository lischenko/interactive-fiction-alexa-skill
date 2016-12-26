(ns alexa.format
   "This namespace contains functions related to Alexa request and response formats. See https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/alexa-skills-kit-interface-reference#response-format for details.")

(defn create-simple-response
  "Creates a skill response with the given title and text. Optionally closes session. 
  The response specifies output speech and card."
  [title text shouldEndSession]
  {"version"           "1.0"
   "sessionAttributes" {}
   "response"          {"outputSpeech"     {"type" "PlainText"
                                            "text" text}

                        "card"             {"type"    "Simple"
                                            "content" text
                                            "title"   title}

                        "shouldEndSession" shouldEndSession}})

(defn extract-intent-name
  "Extracts intent name from the given skill request structure."
  [request & not-found]
  (get-in request ["request" "intent" "name"] not-found))

(defn extract-slot-value
  "Extracts a specified slot's value from the given skill request structure."
  [request slot-name]
  (get-in request ["request" "intent" "slots" slot-name "value"]))

(defn extract-client-id
  "Extracts client id from the given skill request structure."
  [request]
  (get-in request ["session" "user" "userId"]))
