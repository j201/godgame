(ns godgame.utils)

(defn map-vals [f m]
  (into {} (for [[k v] m] [k (f v)])))

(defn log-with [f v]
  (.log js/console (f v))
  v)

(def log (partial log-with identity))
(def logj (partial log-with clj->js))
