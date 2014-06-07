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

(def canvas-w (.-width main-canvas))
(def canvas-h (.-height main-canvas))

(def main-ctx
  (.getContext main-canvas "2d"))

(def generated-terrain (terrain/rand-terrain 30 30))

(def x-offset (atom 0))

(defn redraw! []
  (.clearRect main-ctx 0 0 canvas-w canvas-h)
  (map-drawing/draw-map! generated-terrain
                         main-ctx
                         [@x-offset 0]
                         canvas-w))

(images/load! redraw!)

(.addEventListener
  js/window
  "keydown"
  (fn [e]
    (let [key-code (or (.-which e) (.-keyCode e))]
      (case key-code
        37 (do
             (swap! x-offset dec)
             (redraw!))
        39 (do
             (swap! x-offset inc)
             (redraw!))))))

; (visualize-tiles! (terrain/rand-terrain 100 30))
