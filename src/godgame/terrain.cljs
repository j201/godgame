(ns godgame.terrain
  (:require [godgame.utils :as utils]))

(defn borders? [[x1 y1] [x2 y2] w]
  (or (and (<= (.abs js/Math (- x1 x2)) 1)
           (<= (.abs js/Math (- y1 y2)) 1)
           (let [not-bordering (fn [xe ye xo yo]
                                 (and (even? xe)
                                      (= (inc ye) yo)))]
             (not (or (not-bordering x1 y1 x2 y2)
                      (not-bordering x2 y2 x1 y1)))))
      (cond
        (= x1 (dec w)) (borders? [-1 y1] [x2 y2])
        (= x2 (dec w)) (borders? [x1 y1] [-1 y2])
        :else false)))

(defn coord-exists? [[x y] w h]
  (and (>= x 0) (< x w)
       (>= y 0) (< y h)))

(defn wrap [[x y] w]
  [(mod x w) y])

(def cohesiveness 0.11)

(defn w-h [tiles]
  [(count tiles) (count (first tiles))])

(defn points-around [coord w h]
  (filter #(borders? coord % w)
          (filter #(coord-exists? % w h)
                  (map #(wrap % w)
                       (map #(map + coord %)
                            [[-1 -1] [-1 0] [-1 1] [0 -1] [0 1] [1 -1] [1 0] [1 1]])))))

(defn tiles-around [tiles coord]
  (let [[w h] (w-h tiles)]
    (map (fn [[x y]] (nth (nth tiles x) y))
       (points-around coord w h))))

(defn add-land-at? [tiles coord depth]
  (if (< depth 4)
    (< (rand) 0.7)
    (> (+ (rand) (* (count (filter :is-land (tiles-around tiles coord)))
                    cohesiveness))
       0.9)))

(defn land [tiles [x y]]
  {:land true}) ;; TODO: heat, humidity, etc.

(defn assoc-tiles [tiles [x y] v]
  (assoc (vec tiles) x
         (assoc (vec (nth tiles x))
                y v)))

(defn tile-at [tiles [x y]]
  (nth (nth tiles x) y))

(defn fraction-land [tiles]
  (/ (reduce + (map #(count (filter :land %))
                    tiles))
     (apply * (w-h tiles))))

(defn generate-land-mass [tiles coord depth]
  (let [[w h] (w-h tiles)]
    (cond
      (not (coord-exists? coord w h)) tiles
      (not (:land (tile-at tiles coord))) (recur (assoc-tiles tiles coord (land tiles coord)) coord depth)
      :else (loop [candidates (points-around coord w h)
                   current-tiles tiles
                   to-recur-on []]
              (if (seq candidates)
                (let [candidate (first candidates)]
                  (if (:land (tile-at current-tiles candidate))
                    (recur (rest candidates) current-tiles to-recur-on)
                    (if (add-land-at? current-tiles candidate depth)
                      (recur (rest candidates)
                             (assoc-tiles current-tiles candidate (land current-tiles candidate))
                             (conj to-recur-on candidate))
                      (recur (rest candidates) current-tiles to-recur-on))))
                (reduce #(generate-land-mass %1 %2 (inc depth)) current-tiles to-recur-on))))))

(defn rand-terrain [w h]
  (loop [tiles (repeat w (repeat h nil))]
    (if (> (fraction-land tiles) 0.35)
      tiles
      (recur (generate-land-mass tiles [(rand-int w) (rand-int h)] 0)))))
