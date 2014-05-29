(ns godgame.naming
  (:require [godgame.utils :as utils]
            [clojure.string :refer [replace]]))

; A p-map is a map from values to their probability
; The sum of the probabilities in a p-map should equal 1

(defn p-normalize
  "Divides the probabilities in a p-map to make their sum equal 1"
  [ps]
  (let [sum (reduce + (vals ps))]
    (utils/map-vals #(/ % sum) ps)))

(def single-vowels
  (p-normalize
    {"a" 20
     "e" 40
     "i" 25
     "o" 25
     "u" 10
     "y" 3}))

(def double-vowels
  (p-normalize
    {"ae" 10
     "ai" 20
     "ao" 5
     "au" 15
     "ea" 15
     "ee" 25
     "ei" 15
     "eu" 10
     "ia" 20
     "ie" 15
     "oa" 10
     "oe" 5
     "oi" 15
     "oo" 20
     "ou" 10
     "ua" 5}))

(def vowels
  (p-normalize
    (merge single-vowels
           (utils/map-vals (partial * 0.2) double-vowels))))

(def soft-consonants
  (p-normalize
    {"r" 20
     "l" 15
     "w" 5
     "n" 25
     "y" 5 }))

(def hard-consonants
  (p-normalize
    {"k" 10
     "c" 15
     "ch" 10
     "sh" 10
     "b" 10
     "d" 15
     "f" 7
     "g" 10
     "h" 10
     "j" 7
     "m" 15
     "p" 15
     "q" 5
     "s" 20
     "t" 20
     "v" 10
     "x" 5
     "z" 7
     "st" 7
     "th" 5
     "ph" 5 }))

(def consonants
  (p-normalize (merge (utils/map-vals (partial * 0.3) soft-consonants) hard-consonants)))

(def syllables
  (p-normalize
    {1 4
     2 10 
     3 6
     4 1 }))

(defn rand-from-p-map
  "Takes a random element from a p-map based on the probabilities in it"
  [p-map]
  (loop [r (rand)
         pairs (into [] p-map)]
    (let [pair (first pairs)
          pairs-rest (rest pairs)]
      (if (or (empty? pairs-rest)
              (<= r (second pair)))
        (first pair)
        (recur (- r (second pair))
               pairs-rest)))))

; random word: (c)(v)(cs)(cv(cs))*ch(v)

(defn rand-start-syllable []
  (str
    (when (< (rand) 0.7) (rand-from-p-map consonants))
    (rand-from-p-map vowels)))

(defn rand-middle-syllable []
  (str
    (if (< (rand) 0.3)
      (str (rand-from-p-map soft-consonants) (rand-from-p-map hard-consonants))
      (rand-from-p-map consonants))
    (rand-from-p-map vowels)))

(defn rand-final-syllable []
  (str
    (if (< (rand) 0.3)
      (str (rand-from-p-map soft-consonants) (rand-from-p-map hard-consonants))
      (rand-from-p-map consonants))
    (when (< (rand) 0.3) (rand-from-p-map vowels))))

(defn rand-word []
  (str
    (rand-start-syllable)
    (apply str (take (dec (rand-from-p-map syllables))
                     (repeatedly rand-middle-syllable)))
    (rand-final-syllable)))

(defn demonym [word]
  (if (#{\a \e \i \o \u \y} (last word))
    (str (replace word #"[aeiouy]+$" "")
         "ians")
    (str word "ans")))
