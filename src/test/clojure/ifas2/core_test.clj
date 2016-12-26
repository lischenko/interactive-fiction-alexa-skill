(ns ifas2.core-test
  (:require [clojure.test :refer :all]
            [ifas2.core :refer :all]
            [clojure.data.json :as json])
  (:use ifas2.core))

(def help-request
  (str "{\n"
       "  \"session\": {\n"
       "    \"sessionId\": \"SessionId.b631b703-cac1-414d-95c8-eb599dcd06c3\",\n"
       "    \"application\": {\n"
       "      \"applicationId\": \"amzn1.echo-sdk-ams.app.75f0f37e-aa18-4db0-9976-23798670c600\"\n"
       "    },\n"
       "    \"attributes\": {},\n"
       "    \"user\": {\n"
       "      \"userId\": \"test-user\"\n"
       "    },\n"
       "    \"new\": false\n"
       "  },\n"
       "  \"request\": {\n"
       "    \"type\": \"IntentRequest\",\n"
       "    \"requestId\": \"EdwRequestId.db55bdfb-cc1c-4ef5-a60f-4bd4193a5127\",\n"
       "    \"locale\": \"en-US\",\n"
       "    \"timestamp\": \"2016-10-22T22:22:17Z\",\n"
       "    \"intent\": {\n"
       "      \"name\": \"AMAZON.HelpIntent\",\n"
       "      \"slots\": {}\n"
       "    }\n"
       "  },\n"
       "  \"version\": \"1.0\"\n"
       "}"))

(def story-request
  (str "{"
       "  \"session\": {"
       "    \"sessionId\": \"SessionId.b085969f-4253-403c-9279-e0122d02946e\","
       "    \"application\": {"
       "      \"applicationId\": \"amzn1.ask.skill.8d29ed86-f42b-418d-bcd2-c18c3b974009\""
       "    },"
       "    \"attributes\": {},"
       "    \"user\": {"
       "      \"userId\": \"test-user\""
       "    },"
       "    \"new\": true"
       "  },"
       "  \"request\": {"
       "    \"type\": \"IntentRequest\","
       "    \"requestId\": \"EdwRequestId.4675f1c9-0522-4e59-91a7-2f8cdb56b080\","
       "    \"locale\": \"en-US\","
       "    \"timestamp\": \"2016-10-26T05:20:19Z\","
       "    \"intent\": {"
       "      \"name\": \"InteractiveFictionNewStory\","
       "      \"slots\": {"
       "        \"storyName\": {"
       "          \"name\": \"storyName\","
       "          \"value\": \"galatea\""
       "        }"
       "      }"
       "    }"
       "  },"
       "  \"version\": \"1.0\""
       "}"))

(def command-request
  (str "{"
       "  \"session\": {"
       "    \"sessionId\": \"SessionId.b085969f-4253-403c-9279-e0122d02946e\","
       "    \"application\": {"
       "      \"applicationId\": \"amzn1.ask.skill.8d29ed86-f42b-418d-bcd2-c18c3b974009\""
       "    },"
       "    \"attributes\": {},"
       "    \"user\": {"
       "      \"userId\": \"test-user\""
       "    },"
       "    \"new\": true"
       "  },"
       "  \"request\": {"
       "    \"type\": \"IntentRequest\","
       "    \"requestId\": \"EdwRequestId.4675f1c9-0522-4e59-91a7-2f8cdb56b080\","
       "    \"locale\": \"en-US\","
       "    \"timestamp\": \"2016-10-26T05:20:19Z\","
       "    \"intent\": {"
       "      \"name\": \"InteractiveFictionCommandIntent\","
       "      \"slots\": {"
       "        \"payload\": {"
       "          \"name\": \"payload\","
       "          \"value\": \"read placard\""
       "        }"
       "      }"
       "    }"
       "  },"
       "  \"version\": \"1.0\""
       "}"))

(def stop-request
  (str "{"
       "    \"version\": \"1.0\","
       "    \"session\": {"
       "        \"new\": false,"
       "        \"sessionId\": \"amzn1.echo-api.session.8c3c7580-60fa-4691-8df9-ad848eca800e\","
       "        \"application\": {"
       "            \"applicationId\": \"amzn1.echo-sdk-ams.app.75f0f37e-aa18-4db0-9976-23798670c600\""
       "        },"
       "        \"user\": {"
       "            \"userId\": \"test-user\""
       "        }"
       "    },"
       "    \"context\": {"
       "        \"AudioPlayer\": {"
       "            \"playerActivity\": \"IDLE\""
       "        },"
       "        \"System\": {"
       "            \"application\": {"
       "                \"applicationId\": \"amzn1.echo-sdk-ams.app.75f0f37e-aa18-4db0-9976-23798670c600\""
       "            },"
       "            \"user\": {"
       "                \"userId\": \"test-user\""
       "            },"
       "            \"device\": {"
       "                \"supportedInterfaces\": {"
       "                    \"AudioPlayer\": {}"
       "                }"
       "            }"
       "        }"
       "    },"
       "    \"request\": {"
       "        \"type\": \"IntentRequest\","
       "        \"requestId\": \"amzn1.echo-api.request.fbbe2190-9fc6-4164-9de0-62863d2ef9f6\","
       "        \"timestamp\": \"2016-10-29T02:44:53Z\","
       "        \"locale\": \"en-US\","
       "        \"intent\": {"
       "            \"name\": \"AMAZON.StopIntent\""
       "        }"
       "    }"
       "}"))

(def cancel-request
  (str "{"
       "    \"version\": \"1.0\","
       "    \"session\": {"
       "        \"new\": false,"
       "        \"sessionId\": \"amzn1.echo-api.session.1044e7d8-76cf-4512-8527-dd4e6b12ccc3\","
       "        \"application\": {"
       "            \"applicationId\": \"amzn1.echo-sdk-ams.app.75f0f37e-aa18-4db0-9976-23798670c600\""
       "        },"
       "        \"user\": {"
       "            \"userId\": \"test-user\""
       "        }"
       "    },"
       "    \"context\": {"
       "        \"AudioPlayer\": {"
       "            \"playerActivity\": \"IDLE\""
       "        },"
       "        \"System\": {"
       "            \"application\": {"
       "                \"applicationId\": \"amzn1.echo-sdk-ams.app.75f0f37e-aa18-4db0-9976-23798670c600\""
       "            },"
       "            \"user\": {"
       "                \"userId\": \"test-user\""
       "            },"
       "            \"device\": {"
       "                \"supportedInterfaces\": {"
       "                    \"AudioPlayer\": {}"
       "                }"
       "            }"
       "        }"
       "    },"
       "    \"request\": {"
       "        \"type\": \"IntentRequest\","
       "        \"requestId\": \"amzn1.echo-api.request.2b53c358-cbfd-4700-ad11-511f50b34345\","
       "        \"timestamp\": \"2016-10-29T02:45:14Z\","
       "        \"locale\": \"en-US\","
       "        \"intent\": {"
       "            \"name\": \"AMAZON.CancelIntent\""
       "        }"
       "    }"
       "}"))

(defn- command-req->map
  ([req] (json/read-str req))
  ([req replacement-command]
   (assoc-in (command-req->map req) ["request" "intent" "slots" "payload" "value"] replacement-command)))

(defn- response-match?
  [req s]
  (let [json-request (command-req->map req)
        output (-handleLambda nil json-request)
        response-text (get-in 
                       output
                       ["response" "outputSpeech" "text"])]
    (is (clojure.string/includes? response-text s))))

(deftest help-intent
  (testing "Help intent should return predefined text"
    (response-match?
     help-request
     "You can start a new story")))

(deftest story-intent
  (testing "Story intent should return predefined text"
    (response-match?
     story-request
     "You come around a corner")))

(deftest stop-intent
  (testing "Stop intent should return predefined text"
    (response-match?
     stop-request
     "Okay,")))

(deftest cancel-intent
  (testing "Cancel intent should return predefined text"
    (response-match?
     cancel-request
     "Okay")))

;; (deftest run-story-command
;;   (testing "Command intent should return predefined text"
;;     (is (let [{:keys [text]} (run-story! "test-client" "http://www.ifarchive.org/if-archive/art/if-artshow/year2000/galatea.z5" ;(.getAbsolutePath (-as-file "")
;;                                                nil
;;                                                "read placard")]
;;           (clojure.string/includes? text "Pygmalion of Cyprus")
;; ))))

(deftest command-intent
  (testing "Command intent should return predefined text"
    (-handleLambda nil (json/read-str story-request))

    (response-match?
     command-request
     "Large cream letters on a black ground.")))

;; (deftest galatea-beautify-out
;;   (testing "Galatea story has custom output tweaks"
;;     (response-match?
;;      story-request
;;      "Copyright year two thousand")))

(deftest command-feedback
  (testing "A response to command intent should contain a confirmation of the requested command."
    (-handleLambda nil (json/read-str story-request))

    (response-match?
     command-request
     "Your command: read placard.\n")))

(deftest prompt-next-command
  (testing "Command intent should end with a prompt"
    (-handleLambda nil (json/read-str story-request))

    (response-match?
     command-request
     "What's your next command?")))

(deftest session-open
  (testing "Session should remain open after successful command"
    (let [json-request       (json/read-str command-request)
          output             (-handleLambda nil json-request)
          should-end-session (get-in 
                              output
                              ["response" "shouldEndSession"])]
      (is (not should-end-session)))))

(deftest stop-command-intent
  (testing "'stop', even if it comes as a command intent, should be treated as AMAZON.StopIntent"
    (let [json-request (command-req->map command-request "stop")
        [text _] (handle-intent json-request)]
    (is (= text "Okay, I have saved your progress, you can come back at any time.")))))

(deftest literal-comands
  (testing "Commands starting with 'literally' are sent verbatim (without the word 'literally')"
    (let [json-request (command-req->map command-request "literally stop")
        [text _] (handle-intent json-request)]
    (is (re-matches #"(?s)^Your command: stop.*" text)))))

(deftest garbage-in-literal-comands
  (testing "Address improbabale scenario from certification feedback 2016-12-22. Special characters (!) only (!) in literal request should be treated as help"
    (let [json-request (command-req->map command-request "literally {}")
        [text _] (handle-intent json-request)]
    (is (re-matches #"You can start a new story.*" text)))))
