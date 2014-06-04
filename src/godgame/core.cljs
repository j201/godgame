(ns godgame.core
  (:require [godgame.utils :as utils]
            [godgame.map-drawing :as map-drawing]
            [godgame.images :as images]
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

(def main-canvas 
  (.querySelector js/document "canvas"))

(def main-ctx
  (.getContext main-canvas "2d"))

(images/load! #(map-drawing/draw-map! (terrain/rand-terrain 50 30)
                                      main-ctx
                                      [0 0]))

; (visualize-tiles! (terrain/rand-terrain 100 30))
