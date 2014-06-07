;; TODO: fix points-around/border wrapping

(ns godgame.tiles)

(defn borders? [[x1 y1] [x2 y2] w]
  (or (and (<= (.abs js/Math (- x1 x2)) 1)
           (<= (.abs js/Math (- y1 y2)) 1)
           (let [not-bordering (fn [x1 y1 x2 y2]
                                 (if (even? y1)
                                   (and (= x2 (inc x1))
                                        (not= y1 y2))
                                   (and (= x2 (dec x1))
                                        (not= y1 y2))))]
             (not (or (not-bordering x1 y1 x2 y2)
                      (not-bordering x2 y2 x1 y1)))))
      (and (= 0 x1)
           (borders? [w y1] [x2 y2] w))
      (and (= 0 x2)
           (borders? [x1 y1] [w y2] w))))

(defn coord-exists? [[x y] w h]
  (and (>= x 0) (< x w)
       (>= y 0) (< y h)))

(defn wrap [[x y] w]
  [(mod x w) y])

(defn w-h [tiles]
  [(count tiles) (count (first tiles))])

(defn tile-at [tiles [x y]]
  (nth (nth tiles x) y))

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

(defn assoc-tiles [tiles [x y] v]
  (assoc (vec tiles) x
         (assoc (vec (nth tiles x))
                y v)))
