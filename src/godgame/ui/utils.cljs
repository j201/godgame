(ns godgame.ui.utils)

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
