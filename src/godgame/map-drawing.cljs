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

(def draw-single-map!
  "Draws one map"
  (fn [tiles ctx offset]
    (doseq [x (range (count tiles))
            y (range (count (first tiles)))
            :let [coord [x y]]]
      (let [[px py] (map + offset (hex-top-left coord))]
        (.drawImage ctx
                    (images/get-image (or (:type (tile-at tiles coord))
                                          :deepocean))
                    px py)))))

(defn draw-map! [tiles ctx offset w]
  (doseq [x-offset (let [tile-draw-w (* hex-width (count tiles))
                         mod-offset (mod (first offset) tile-draw-w)]
                     (map #(+ (if (zero? mod-offset) 0 (- mod-offset w))
                              (* % tile-draw-w))
                          (range (.ceil js/Math (/ w tile-draw-w)))))
          :let [offset' [x-offset (second offset)]]]
    (draw-single-map! tiles ctx offset')))
