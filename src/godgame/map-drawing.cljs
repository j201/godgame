(ns godgame.map-drawing
  (:require [godgame.images :as images]
            [godgame.utils :as utils]
            [godgame.tiles :refer [tile-at]]))

(def hex-side 9)

(def hex-width
  (* hex-side (.sqrt js/Math 3)))

(defn hex-centre
  "the pixel point of the center of a hex"
  [[x y]]
  [(if (even? y)
     (+ (/ hex-width 2)
        (* x hex-width))
     (* (inc x) hex-width))
   (+ hex-side
      (* y (/ 3 2) hex-side))])

(defn hex-top-left [coord]
  (let [[cx cy] (hex-centre coord)]
    [(- cx (/ hex-width 2))
     (- cy hex-side)]))

(defn set-canvas-w-h! [canvas w h]
  (set! (.-width canvas) w)
  (set! (.-height canvas) h))

(def single-map (.createElement js/document "canvas"))
(def single-map-ctx (.getContext single-map "2d"))
(def drawn-tiles (atom nil))

(def draw-single-map!
  "Draws the map once (so no wrapping, for example)"
  (fn [tiles ctx]
    (doseq [x (range (count tiles))
            y (range (count (first tiles)))
            :let [coord [x y]]]
      (let [[px py] (hex-top-left coord)]
        (.drawImage ctx
                    (images/get-image (or (:type (tile-at tiles coord))
                                          :deepocean))
                    px py)))))

(defn draw-map! [tiles ctx offset w redraw]
  (when (or redraw (not= tiles @drawn-tiles))
    (do (set-canvas-w-h! single-map
                         (* hex-width (inc (count tiles)))
                         (* hex-side 1.5 (inc (count (first tiles)))))
        (draw-single-map! tiles single-map-ctx)
        (reset! drawn-tiles tiles))) 
  (doseq [x-offset (let [tile-draw-w (* hex-width (count tiles))
                         mod-offset (mod (first offset) tile-draw-w)]
                     (map #(+ (- mod-offset tile-draw-w)
                              (* % tile-draw-w))
                          (range (inc (.ceil js/Math (/ w tile-draw-w))))))]
    (.drawImage ctx single-map x-offset (second offset))))
