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

(def main-canvas 
  (.querySelector js/document "canvas"))

(def canvas-w (.-width main-canvas))
(def canvas-h (.-height main-canvas))

(def main-ctx
  (.getContext main-canvas "2d"))

(def generated-terrain (terrain/rand-terrain 70 30))

(def x-offset (atom 0))

(defn redraw! []
  (.clearRect main-ctx 0 0 canvas-w canvas-h)
  (map-drawing/draw-map! generated-terrain main-ctx [@x-offset 0] canvas-w false))

(images/load! redraw!)

(def scroll-speed 10)

(.addEventListener
  js/window
  "keydown"
  (fn [e]
    (let [key-code (or (.-which e) (.-keyCode e))]
      (case key-code
        37 (do
             (swap! x-offset #(- % scroll-speed))
             (redraw!))
        39 (do
             (swap! x-offset #(+ % scroll-speed))
             (redraw!))))))

; (visualize-tiles! (terrain/rand-terrain 100 30))
