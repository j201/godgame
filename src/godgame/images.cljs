(ns godgame.images)

(def image-map (atom {}))

(def image-names
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

(defn name->path [name]
  (str "res/" name ".png"))

;; untested
(defn load! [callback]
  (let [images-loaded (atom 0)]
    (reset! image-map
            (reduce (fn [image-map image-name]
                      (assoc image-map image-name
                             (let [image-obj (.createElement js/document "img")]
                               (do
                                 (set! (.-src image-obj) (name->path name))
                                 (set! (.-onload image-obj)
                                       (fn []
                                         (if (= @images-loaded (count image-names))
                                           (callback)
                                           (swap! images-loaded inc))))
                                 image-obj))))
                    {}
                    image-names))))

(defn get-image [name]
  (@image-map name))
