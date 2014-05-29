(ns godgame.core
  (:require [godgame.utils :as utils]
            [godgame.terrain :as terrain]))

(defn into-div [text]
  (let [div (.createElement js/document "div")]
    (do
      (set! (.-textContent div) text)
      div)))

(defn visualize-tiles! [tiles]
  (.appendChild (.-body js/document)
                (into-div
                  (clojure.string/join "\n"
                                       (map (fn [arr]
                                              (apply str
                                                     (map #(if (:land %) "#" " ")
                                                          arr)))
                                            (apply map list tiles))))))

(visualize-tiles! (terrain/rand-terrain 100 30))
