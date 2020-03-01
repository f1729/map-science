(ns map-science.core
  (:require	[clojure.string :as string]
            [cljs-bean.core :as b]
            [helix.core :refer [$ <> defnc]]
            [helix.dom :as d]
            [helix.hooks :as hooks]
            ;; How import libraries?
            ;; Import a NPM library is really easy,
            ;; just want to type the name and use :as
            ;; maybe you will have to use :default instead of :as
            ;; depends of how this library was did exported.
            ;; ["some-library" :as ains]
            ;; For it, in case :as throw an error, use (js/console.log ains)
            ;; to see what kind of exportation have.
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
  ;; Here is the classic style="{{ ...props.style, height: 100, width: 100 }}"
  ;; We don't have ... operator in clojure but we have the merge function
  ;; so first you need to get the 'style' from props (:style props)
  ;; and then merge them with new values that you want to add.
  (d/div {:style (merge (:style props)
                          {:width "100%" :height 36 :display "flex"})}
         (d/div {:id "asdia9di9sid9as"
                 :ref (:ref props)
                 :style {:height 5
                         :width "100%"
                         :border-radius 4
                         :display "flex"
                         :align-self "center"
                         ;; Remember this function is from a NPM library
                         ;; you need to pass js objects, use #js
                         :background (getTrackBackground
                                      #js {:values values
                                           :colors #js ["#CCC" "#548BF4" "#CCC"]
                                           :min 1
                                           :max 2020})}}
                children)))

(defnc RenderThumb
  [{:keys [props isDragged]}]
  (d/div {:style (merge (:style props)
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
                         
(defn get-local-storage-item [key]
  (.parse js/JSON (.getItem js/localStorage key)))

(defn set-local-storage-item [key value]
  (.setItem js/localStorage key (.stringify js/JSON value)))

;; Creating own React hooks
(defn use-local-state [key initialValue]
  (let [[state set-state] 
    (hooks/use-state 
      (if-let [localValue (get-local-storage-item key)] 
        localValue 
        initialValue))]
    (hooks/use-effect [state]
      (set-local-storage-item key state))
    [state set-state]))

(defnc RangeComponent []
  (let [[values set-values] (use-local-state "range" #js [1409 1509]) ]
    ;; The library we’re using uses a very complex pattern here
    ;; you hand it a function that returns a component, and then it gives you the props to pass to your component
    ;; If you are using external library components that pass you data/props/etc.
    ;; frequently, then you will need to handle that data appropriately
    ;; If you want to dynamically pass props to a component in helix, it must be a CLJS map-like thing.

    ;; so the complicated mess is boiled down to:
    ;;  1. library gives you props data
    ;;  2. turn props data into a CLJS map structure
    ;;  3. pass it to your component
    ;; when you run into patterns like that, then yes you’ll probably want to use cljs-bean
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
  ;; props to NPM Libraries need JS Objects,
  ;; so you need to use #js before pass a vector or a list
  ;; values like strings, numbers don't need that.
  ($ Map {:width 1000 :height 800 :center #js [50.8 4.69] :zoom 2.3}
     (for [[index scientist] (map-indexed vector scientists)]
       ($ Overlay
          {:anchor #js [(:lat (:bplace_geonameid scientist))
                        (:lon (:bplace_geonameid scientist))]
           :key index}
          (d/img {:src (str "https://pantheon.world/images/profile/people/" (:id scientist) ".jpg")
                  :style {:width 20 :height 20 :border-radius "50%"}
                  :on-click #(openData scientist)})))))

(defnc Navbar []
  (d/div {:style {:display "flex"
                   :justify-content "center"
                   :box-shadow "1px 1px 2px #c7c7c7"
                   :position "relative"
                   :z-index 1}}
         (d/h1 {:style { :color "#47556f"} } "Science Timeline")))

(defnc App []
  (d/div
    ($ Navbar)
    ;; create elements out of components
    ($ AppMap {:scientists scientists})
    ($ RangeComponent)))


(defn ^:export start
  []
  (rdom/render ($ App) (js/document.getElementById "app")))
