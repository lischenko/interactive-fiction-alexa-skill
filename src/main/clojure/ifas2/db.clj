(ns ifas2.db
  "The skill is hosted in Amazon Lambda which is stateless. The state has to be persisted in some sort of an external storage. The functions from this namespace take care of that."
  (:use [amazonica.aws.dynamodbv2]))

(defn db-find-url!
  "Given a story name, fetches its url (if any) from the database."
  [requested-story-name]
  (get-in (get-item
           :table-name "IfasStoryLinks"
           :key        {:name {:s (-> requested-story-name .toLowerCase .trim)}})
          [:item :url]))

(defn db-find-last-story! 
  "Given a client id, fetches the story they last interacted with from the database."
  [client-id]
  (->>
   (query :table-name "IfasStoryDumps"
          :key-conditions {:customerId {:attribute-value-list [client-id] :comparison-operator "EQ"}})
   :items
   (apply max-key #(% :ts))))

(defn db-update-story-dump!
  "Saves the base64-encoded savefile of the story to the database."
  [client-id url dump64]
  (put-item :table-name "IfasStoryDumps"
            :item {:customerId client-id
                   :url        url
                   :dump64     dump64
                   :ts         (System/currentTimeMillis)}))
