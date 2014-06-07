(ns godgame.utils)

(defn map-vals [f m]
  (into {} (for [[k v] m] [k (f v)])))

(defn log-with [f v]
  (.log js/console (f v))
  v)

(def log (partial log-with identity))
(def logj (partial log-with clj->js))

(defn memoize-with [f keyf]
  (let [mem (atom {})]
    (fn [& args]
      (let [k (apply keyf args)]
        (if-let [e (find @mem k)]
          (val e)
          (let [ret (apply f args)]
            (swap! mem assoc k ret)
            ret))))))

