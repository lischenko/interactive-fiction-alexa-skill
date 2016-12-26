(ns ifas2.beautify
  "This is the layer of transformations between the interpreter and the voice interface. There are three kinds of transformations available: input, sequence, and output."
  (:use ifas2.zax))

(defmulti beautify-in
  "This is a chance to modify the input before it is sent to the interpeter. For example, the voice interface might recognize numerals as words, e.g. 'one', 'two', 'tree'. If the story expects digits 1, 2, 3, instead then a method for the story could make the replacement."
  (fn [story-url in] story-url))

(defmethod beautify-in :default [story-url in] in)

(defmulti beautify-story-sequence
  "Sequence transformations — for lack of a better name — can be used to override the default one-command:one-response logic. For example, a story might output a chunk of text, then make a 'dramatic pause' by waiting for a key press, and then output the rest of the text. For voice interface, it would be better to skip the pause by emulating a key press and merge the two chunks of text into one response.
  This function receives a handle to the interpreter after a command has been sent to it but before the output is read. It is expected that the implementation would read the output and then optionally send commands and read further output and finally return the aggregate raw-text."
  (fn [url vm-map] url))

(defmethod beautify-story-sequence :default [url vm-map]
  [(read-text-from-vm vm-map)])

(defn- standard-beautify-out 
  "Replaces a bunch of weird special characters often occuring within stories with something more suitable for voice interfaces (often empty)."
  [text]
  (-> text
      (.replaceAll "(?m)^[     >]+" "")
      (.replaceAll "[  ]{2,}" "\n")
      (.replaceAll "(?m)\\n{3,}" "\n\n")
      (.replaceAll "(?m)^\\." "")
      (.replaceAll "(?m).*[    ]*--$" "...")
      .trim))

(defmulti beautify-out
  "This multimethod can be used to modify output of the interpreter before it goes to the voice rendering. Perhaps the most obvious example is the traditional prompt character '>' that may get rendered as 'greater than' in voice. You may want to delete it instead."
  (fn [url text] url))

(defmethod beautify-out :default [url text]
  (standard-beautify-out text))

;; (defmethod beautify-out "http://www.ifarchive.org/if-archive/art/if-artshow/year2000/galatea.z5" [url text]
;;   "This is a sample of what we would do in Galatea if we had permission to modify the story output."
;;   (-> text
;;       standard-beautify-out
;;       (.replaceAll "Copyright \\(c\\) 2000 by Emily Short\\." "Copyright year two thousand by Emily Short.")
;;       (.replaceAll "\\(First-time users should type 'help'.\\)" "(First-time users should say 'help')")
;;       (.replaceAll "Release \\d+ / Serial number \\d+ / Inform v[\\d\\.]+ Library [\\d/]+" "")))
