(ns godgame.terrain
  (:require [godgame.utils :as utils]))

(def tile-types
  [:arctic
   :deepocean
   :desert
   :forest
   :grassland
   :mountain
   :rainforest
   :savannah
   :selected
   :shallowocean
   :taiga
   :tundra])

;; note: humidity <= temperature guaranteed
(def tile-climates
  {:arctic {:humidity [0 0.1]
            :temperature [0 0.15]}
   :desert {:humidity [0 0.1]
            :temperature [0.4 1]}
   :forest {:humidity [0.4 0.8]
            :temperature [0.4 1]}
   :grassland {:humidity [0.25 0.4]
               :temperature [0.4 1]}
   :rainforest {:humidity [0.8 1]
                :temperature [0.8 1]}
   :savannah {:humidity [0.1 0.25]
              :temperature [0.4 1]}
   :taiga {:humidity [0 0.4]
           :temperature [0.25 0.4]}
   :tundra {:humidity [0 0.2]
            :temperature [0.15 0.25]}})

(def dirs #{[-1 -1] [-1 0] [-1 1] [0 -1] [0 1] [1 -1] [1 0] [1 1]})

(defn borders? [[x1 y1] [x2 y2] w]
  (and (<= (.abs js/Math (- x1 x2)) 1)
       (<= (.abs js/Math (- y1 y2)) 1)
       (let [not-bordering (fn [x1 y1 x2 y2]
                             (if (even? y1)
                               (and (= x2 (inc x1))
                                    (not= y1 y2))
                               (and (= x2 (dec x1))
                                    (not= y1 y2))))]
         (not (or (not-bordering x1 y1 x2 y2)
                  (not-bordering x2 y2 x1 y1))))))

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
                            dirs)))))

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
  {:land true})

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

(defn rand-non-land-point [tiles]
  (let [[w h] (w-h tiles)
        p [(rand-int w) (rand-int h)]]
    (if (:land (tile-at tiles p))
      (rand-non-land-point tiles)
      p)))

(defn latitude
  "from 0 to 1"
  [h y]
  (/ (.abs js/Math (- y (/ h 2)))
     (/ h 2)))

(defn in-range? [x [l u]]
  (and (>= x l)
       (<= x u)))

(defn humidity-bias [x]
  (- 1 (.sin js/Math (* (.-PI js/Math)
                        (- 1 (.sqrt js/Math (- 1 x)))))))

(defn gen-humidity [tiles coord temperature]
  (* temperature
     (+ (* 0.5 (humidity-bias temperature))
       (* 0.5 (if (every? :land (tiles-around tiles coord))
                (* 0.5 (rand))
                (+ 0.5 (* 0.5 (rand))))))))

(defn gen-temperature [tiles [x y]]
  (let [[w h] (w-h tiles)]
    (min 1
         (max 0
              (+ (* 0.15 (rand))
                 (- 1 (latitude h y)))))))

(defn gen-tile-climate [humidity temperature]
  (ffirst
    (filter #(and (in-range? humidity (:humidity (second %)))
                  (in-range? temperature (:temperature (second %))))
            (for [[k v] tile-climates]
              [k v]))))

(defn assign-ocean-type [tiles coord]
  (assoc (tile-at tiles coord) :type
         (if (some :land (tiles-around tiles coord))
           :shallowocean
           :deepocean)))

(defn assign-land-type [tiles coord]
  (assoc (tile-at tiles coord) :type
         (let [temperature (gen-temperature tiles coord)]
           (gen-tile-climate (gen-humidity tiles coord temperature)
                             temperature))))

(defn add-mountain-range [tiles]
  (let [[w h] (w-h tiles)
        start-coord [(rand-int w) (rand-int h)]]
    (if (not (:land (tile-at tiles start-coord)))
      (add-mountain-range tiles)
      (reduce (fn [tiles coord]
                (let [tile (tile-at tiles coord)]
                  (if (:land tile)
                    (assoc-tiles tiles coord
                                 (assoc tile :type :mountain))
                    tiles)))
              tiles
              (take (+ 3 (rand-int 6))
                    (iterate #(rand-nth (vec (points-around % w h)))
                             start-coord))))))

(defn add-mountains [tiles]
  (nth (iterate add-mountain-range tiles)
       (+ 2 (rand-int 4))))

(defn assign-types [tiles]
  (add-mountains
    (for [x (range (count tiles))]
      (for [y (range (count (first tiles)))
            :let [coord [x y]
                  tile (tile-at tiles coord)]]
        (if (:land tile)
          (assign-land-type tiles coord)
          (assign-ocean-type tiles coord))))))

(defn rand-terrain [w h]
  (assign-types (loop [tiles (repeat w (repeat h nil))]
                  (if (> (fraction-land tiles) 0.35)
                    tiles
                    (recur (generate-land-mass tiles (rand-non-land-point tiles) 0))))))
