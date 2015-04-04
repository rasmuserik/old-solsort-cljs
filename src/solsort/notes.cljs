(ns solsort.notes
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.mbox :refer [route]]
    [solsort.system :as system :refer [warn log is-browser fs source-file exit is-nodejs set-immediate]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

(defn process-daylog [markdown]
  (let [[_ notes & _] (.split markdown (js/RegExp. "^#[^#]" "m"))
        notes notes]
    (if (= (.slice notes 0 12) "Public Notes")
      (.writeFile fs "misc/autogenerated-notes.md" (.slice notes 12) "utf8"))
    (warn 'notes "error processing daylog")))

(if is-nodejs
  (set-immediate
    #(let [markdown
           (try
             (.readFileSync fs "/home/rasmuserik/notes/daylog.md" "utf8")
             (catch js/Object e nil))]
       (if markdown (process-daylog markdown)))))

(defn canonize-string [s]
  (.replace (.trim (.toLowerCase s)) (js/RegExp. "[^a-z0-9]" "g") ""))

(def all-notes 
  (memoize 
    (fn []
      (if is-nodejs
        (let [notes (.readFileSync fs "misc/autogenerated-notes.md" "utf8")
              [_ & notes] (.split notes (js/RegExp "^## " "m"))
              converter (aget (js/require "showdown") "converter")
              converter (converter.)
              notes (map (fn [note] 
                           (let [title (aget (.split note "\n") 0)]
                             [(canonize-string title) 
                              {:title title
                               :markdown (str "## " note)
                               :html ((aget converter "makeHtml") (str "##" note))
                               }]))
                         notes)]
          (into {} notes))
        {}))))

(log 'notes (keys (all-notes)))

(defn note [note-name]
  (go
    (let [note (get (all-notes) (canonize-string note-name))]
      (if note 
        {:type "html"
         :title (str (:title note) " - solsort.com")
         :css {".solsortLogoText" { :textDecoration :none} 
               ".container" { :maxWidth "72ex" :display "inline-block"}
               "body" {:margin "1ex 10% 0 10%" 
                       :padding 0}}
         :rawhtml (str
                    "<div class=\"container\">"
                    "<a href=\"/\" class=\"solsortLogoText\"><img src=\"/img/logicon.png\"> solsort.com</img></a>"
                    "<div>" (:html note) "</div></div>")}
        {}))))
(route "notes" note)
(route "writings" note)
