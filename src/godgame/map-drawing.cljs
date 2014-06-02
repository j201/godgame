(ns godgame.map-drawing
  (:require [godgame.images :as images]))

(def hex-side 9)

(def hex-width
  (.sqrt js/Math (* 3 hex-side hex-side)))

(defn hex-centre
  "the pixel point of the center of a hex"
  [[x y]]
  [(if (even? y)
     (+ (/ hex-width 2)
        (* x hex-width))
     (* (inc x) hex-width))
   (+ hex-side
      (* y 3/2 hex-side))])

(defn hex-top-left [coord]
  (let [[cx cy] coord]
    [(- cx (/ hex-width 2))
     (- cy hex-side)]))

(defn tile-at [tiles [x y]]
  (nth (nth tiles x) y))

(defn draw-map! [tiles canvas offset]
  (doseq [coord [(range (count tiles))
                 (range (count (first tiles)))]]
    (let [[px py] (map + offset (hex-top-left coord))]
      (.drawImage canvas
                  (images/get-image (or (:type (tile-at tiles coord))
                                        :deepocean))
                  px py))))
