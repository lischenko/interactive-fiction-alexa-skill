(defproject ifas2 "2.0.1-SNAPSHOT"
  :description "Interactive Fiction - Alexa skill. Version 2: rewrite of a Java app in Clojure."
  :url "https://github.com/lischenko/interactive-fiction-alexa-skill"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.1"]

                 [github-mattkimmel/zax "0.91"]
                 [amazonica "0.3.77" :exclusions [com.amazonaws/aws-java-sdk
                                                  com.amazonaws/amazon-kinesis-client]]
                 [com.amazonaws/aws-java-sdk-core "1.11.26"]
                 [com.amazonaws/aws-java-sdk-dynamodb "1.11.26"]
                 [com.amazonaws/aws-lambda-java-log4j "1.0.0"]]

  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :source-paths      ["src/main/clojure"]
  :java-source-paths ["src/main/java"]  ; Java source is stored separately.
  :test-paths        ["src/test/clojure"])
