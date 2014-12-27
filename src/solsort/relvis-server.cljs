(ns solsort.relvis-server
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.node :refer [exec each-lines]]
    [solsort.keyval-db :as kvdb]
    [solsort.webserver :as webserver]
    [solsort.config :as config]
    [solsort.util :refer [parse-json-or-nil]]
    [clojure.string :refer [split]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(def data-path "../_visual_relation_server")
;(def fs (if (config/nodejs) (js/require "fs")))
(defn iterateLines [prefix fname swap]
  (go
    (print 'traversing prefix)
    (let [ids (atom #js{})]
      (loop [lines (each-lines fname)
             line (<! lines)
             prevLid nil
             loans []
             cnt 0
             amount 0
             ]
        ; whoops lid should be loan vice versa, anyhow want to make this generic/replace it with function so fix that later
        (if line
          (let
            [lineParts (.split line ",")
             lid (.trim (str (aget lineParts (if swap 1 0))))
             loan (.trim (str (aget lineParts (if swap 0 1))))
             ]
            (if (= lid prevLid)
              (recur lines (<! lines) lid (conj loans loan) cnt (inc amount))
              (do
                (if (and prevLid
                         (< 1 (count loans)))
                  (do
                    (<! (kvdb/store prefix prevLid (clj->js loans)))
                    (aset @ids prevLid amount)
                    ))
                (if (= 0 (rem cnt 100000))
                  (print cnt))
                (recur lines (<! lines) lid [] (inc cnt) 0)
                ))
            )))
      (<! (kvdb/commit prefix))
      @ids)))

(defn generate-coloans-by-lid-csv []
  (go
    (print "ensuring tmp/coloans-by-lid.csv")
    (if (not (.existsSync (js/require "fs") "tmp/coloans-by-lid.csv"))
      (<! (exec  "cat tmp/coloans.csv | sort -k+2 > tmp/coloans-by-lid.csv")))))

(defn generate-coloans-csv []
  (go
    (print "ensuring tmp/coloans.csv")
    (if (not (.existsSync (js/require "fs") "tmp/coloans.csv"))
      (<! (exec (str "xzcat " data-path "/coloans/* | sed -e 's/,/,\t/' | sort -n > tmp/coloans.csv"))))))

(defn make-tmp-dir []
  (go 
    (if (not (.existsSync (js/require "fs") "tmp")) 
      (<! (exec "mkdir tmp")))))

(defn prepare-data-2 []
  (if (not config/nodejs) (throw "error: not on node"))
  (go
    (<! (make-tmp-dir))
    (<! (generate-coloans-csv))
    (<! (generate-coloans-by-lid-csv))
    (print 'counting-lines)
    (print 'line-count
           (loop [input (each-lines "tmp/coloans.csv")
                  current-line (<! input)
                  line-count 0]
             (if current-line
               (recur input (<! input) (inc line-count))
               line-count)))
    ))

(defn prepare-data []
  (if (not config/nodejs)
    (throw "error: not on node"))
  (go
    (if true ;(not (<! (kvdb/fetch :related "10000467")))
      (let [fs (js/require "fs")]
        (print 'prepare-data)
        (if (not (.existsSync fs "tmp")) (<! (exec "mkdir tmp")))
        (if (not (.existsSync fs "tmp/coloans.csv"))
          (do (print "generating coloans.csv" (js/Date.))
              (<! (exec (str "xzcat " data-path "/coloans/* | sed -e 's/,/,\t/' | sort -n > tmp/coloans.csv")))))
        (if (not (.existsSync fs "tmp/coloans-by-lid.csv"))
          (do (print "generating tmp/coloans-by-lid.csv" (js/Date.))
              (<! (exec  "cat tmp/coloans.csv | sort -k+2 > tmp/coloans-by-lid.csv"))))

        (if (not (<! (kvdb/fetch :patrons "000001")))
          (do
            (print "traversing coloans" (js/Date.))
            (print "added " (.-length (js/Object.keys (<! (iterateLines :patrons "tmp/coloans.csv" false)))) " elements")))
        (if (not (<! (kvdb/fetch :lid-count "all")))
          (let [lidCount (<! (iterateLines :lids "tmp/coloans-by-lid.csv" true))]
            (print "no lid-count")
            (<! (kvdb/store :lid-count "all" (js/JSON.stringify lidCount))))
          (print "has lid-count"))
        (let [lidCount (js/JSON.parse (or (<! (kvdb/fetch :lid-count "all")) "{}"))
              lids (js/Object.keys lidCount)
              ]
          (aset js/window "lidCount" lidCount)
          (print "generating coloans")
          (loop [i 0]
            (if (< i (.-length lids))
              (let [lid (aget lids i)
                    patrons (.slice (or (<! (kvdb/fetch :lids lid)) #js[]) 0 3000)
                    coloans (or (<! (kvdb/multifetch :patrons patrons)) #js{})
                    result (atom #js{})
                    ]
                (doall (for [patron (seq patrons)]
                         (doall (for [coloan (seq (aget coloans patron))]
                                  (aset @result coloan (inc (or (aget @result coloan) 0)))))))
                (<! (kvdb/store 
                      :related lid
                      (clj->js
                        (map 
                          (fn [[weight lid dnt total]] {:lid lid :weight (bit-or (- (* weight 1000)) 0)})
                          (take 
                            100
                            (sort
                              (map 
                                (fn [[lid cnt total]] 
                                  [(- (/ cnt (js/Math.log (+ 10 total)))) lid cnt total])
                                (filter (fn [[lid cnt total]] (< 1 cnt))
                                        (for [lid (seq (js/Object.keys @result))] 
                                          [lid (aget @result lid) (aget lidCount lid)])))))))))
                (if (= 0 (rem i 1000))
                  (print i (.-length lids) lid (aget lidCount lid)))
                (recur (inc i)))))
          (print "done preparing data for relvis-server" (js/Date.)))))))

(defn start []
  (go
    (<! (webserver/add "relvis-related" #(go (<! (kvdb/fetch :related (:filename %))))))
    (<! (prepare-data-2))
    (print "starting visual relation server")
    ))
