(ns map-science.core
  (:require	[clojure.string :as string]
            [helix.core :as hx :refer [$ <> defnc]]
            [helix.dom :as d]
            [helix.hooks :as hooks]
            ["pigeon-maps" :as Map]
            ["pigeon-overlay" :default Overlay]
            ["react-dom" :as rdom]
            ["react-router-dom" :as rr]))


(def scientists
  [{
    :id 18079
    :name "Leonardo Da Vinci"
    :img "undefined"
    :bplace_geonameid {:lat 43.783 :lon 11.25}}
   {
    :id 18021
    :name "Luis C."
    :img "undefined"
    :bplace_geonameid {:lat 27.783 :lon 1.25}}])

(defnc AppMap
  [{:keys [scientists]}]
  ($ Map {:width 700 :height 700 :center #js [50 4] :zoom 2.3}
     (map-indexed
      #(let [scientist %2 index %]
         ($ Overlay
            {:anchor #js [(:lat  (:bplace_geonameid scientist))
                          (:lon (:bplace_geonameid scientist))]
             :key index}
            (d/img {:src (str "https://pantheon.world/images/profile/people/" (:id scientist) ".jpg")
                    :style {:width 20}})))
      scientists)))

(defnc App []
  (let [[state set-state] (hooks/use-state {:name "Helix User"})]
    (d/div
     (d/h1 "Science Timeline!!!")
     ;; create elements out of components
     ($ AppMap {:scientists scientists}))))


(defn ^:export start
  []
  (rdom/render ($ App) (js/document.getElementById "app")))
