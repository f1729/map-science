(ns map-science.core
  (:require	[clojure.string :as string]
            [cljs-bean.core :as b]
            [helix.core :as hx :refer [$ <> defnc]]
            [helix.dom :as d]
            [helix.hooks :as hooks]
            ["pigeon-maps" :as Map]
            ["pigeon-overlay" :default Overlay]
            ["react-dom" :as rdom]
            ["react-range" :as rrange :refer [getTrackBackground Range]]
            ["react-router-dom" :as rr]))


(def scientists
  [{:id 18079
    :name "Leonardo Da Vinci"
    :bplace_geonameid {:lat 43.783 :lon 11.25}}
   {:id 18021
    :name "Luis C."
    :bplace_geonameid {:lat 27.783 :lon 1.25}}])

(defn openData [scientist] (js/console.log (:name scientist)))

(defnc RenderTrack
  [{:keys [props children values]}]
  (d/div {:style (merge (:style props)
                        {:width "100%" :height 36 :display "flex"})
          & (dissoc props :style)}
         (d/div {:id "asdia9di9sid9as"
                 :ref (:ref props)
                 :style {:height 5
                         :width "100%"
                         :border-radius 4
                         :display "flex"
                         :align-self "center"
                         :background (getTrackBackground
                                      #js {:values values
                                           :colors #js ["#CCC" "#548BF4" "#CCC"]
                                           :min 1
                                           :max 2020})}}
                children)))

(defnc RenderThumb
  [{:keys [props isDragged]}]
  (d/div {:props props :style (merge (:style props)
                        {:height 42
                         :width 42
                         :border-radius 4
                         :background-color "#FFF"
                         :display "flex"
                         :justify-content "center"
                         :align-items "center"
                         :box-shadow "0px 2px 6px #AAA"})
          & (dissoc props :style)}
         (d/div {:style {:height 16
                         :width 5
                         :background-color (if isDragged "#548BF4" "#CCC")}})))



(defnc RangeComponent []
  (let [[values set-values] (hooks/use-state #js [1409 1509])]
    (d/div
     ($ Range {:values values
               :step 100
               :min 1
               :max 2020
               :onChange #(set-values %)
               :renderTrack #($ RenderTrack {:values values
                                             & (-> (b/bean %)
                                                   (update :props b/bean)
                                                   (update-in [:props :style] b/bean))})
               :renderThumb #($ RenderThumb {:key (.-index %)
                                             & (-> (b/bean %)
                                                   (update :props b/bean)
                                                   (update-in [:props :style] b/bean))})})
     (d/output (let [[init end] values] (str init "-" end))))))


(defnc AppMap
  [{:keys [scientists]}]
  ($ Map {:width 1000 :height 800 :center #js [50.8 4.69] :zoom 2.3}
     (for [[index scientist] (map-indexed vector scientists)]
       ($ Overlay
          {:anchor #js [(:lat (:bplace_geonameid scientist))
                        (:lon (:bplace_geonameid scientist))]
           :key index}
          (d/img {:src (str "https://pantheon.world/images/profile/people/" (:id scientist) ".jpg")
                  :style {:width 20 :height 20 :border-radius "50%"}
                  :on-click #(openData scientist)})))))

(defnc App []
  (let [[state set-state] (hooks/use-state {:name "Helix User"})]
    (d/div
     (d/h1 "Science Timeline!!")
     ;; create elements out of components
     ($ AppMap {:scientists scientists})
     ($ RangeComponent))))


(defn ^:export start
  []
  (rdom/render ($ App) (js/document.getElementById "app")))
