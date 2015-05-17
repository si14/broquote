(ns broquote.core
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [clojure.string :as str]
   [cljs.core.async :as async :refer [put! chan alts!]]
   [plumbing.core :as p]
   [reagent.core :as reagent]))

(enable-console-print!)

;;
;; Constants
;;

(def defaults
  {:quote {:quote {:font-size 60
                   :text "Текст цитаты"
                   :line-height 1}
           :author {:font-size 30
                    :text "Автор цитаты"}
           :photo :default}
   :number {:number {:font-size 205
                     :text "42"
                     :margin-top 67
                     :x-offset -7}
            :number-caption {:font-size 50
                             :text "тыс.руб."
                             :margin-top 290}
            :caption {:margin-top 426
                      :font-size 70
                      :text "Средняя цена снусмумрика\nв Москве"
                      :line-height 1}}
   :title {:font-size 60
           :text "Средняя цена фалафеля выросла\nна треть"
           :line-height 1
           :margin-top 220}})

(def captions {:left {:font-size 18
                      :font-weight :bold
                      :text "КОМАНДА НАВАЛЬНОГО"
                      :margin-bottom 22}
               :right {:font-size 30
                       :font-weight :medium
                       :text "navalny.com"
                       :margin-bottom 20}})

(def colors {:signature "#2c7f8c"
             :white "#f9fcfd"
             :gray "#f0f0f0"
             :black "#000000"})

(def sizes {:width 1400
            :height 700})

#_(def fonts {:normal (fn [size] (str size "px PT Sans"))
              :bold (fn [size] (str "bold " size "px PT Sans"))})

(def fonts {:normal (fn [size] (str "400 " size "px RedRing"))
            :regular (fn [size] (str "400 " size "px RedRing"))
            :medium (fn [size] (str "500 " size "px RedRing"))
            :bold (fn [size] (str "700 " size "px RedRing"))})

(def number-layout {:circle {:x (/ (:width sizes) 2)
                             :y 230
                             :r1 160
                             :r2 175}})

;;
;; State
;;


(defonce state
  (reagent/atom
   {:current-tab :title
    :quote (:quote defaults)
    :number (:number defaults)
    :title (:title defaults)}))

(defonce interaction-chan (chan))

;;
;; Utils
;;

;; see https://github.com/Day8/re-frame/wiki/Beware-Returning-False
(defn dispatch
  [evt]
  (put! interaction-chan evt)
  nil)

;;
;; Components
;;

(defn quote-tab-cmp [_]
  (let [quote (:quote @state)]
    [:div
     [:p (-> quote :quote :text)]
     [:input {:type "text"
              :value (-> quote :quote :text)}]]))

(defn number-tab-cmp [{:keys [number number-caption caption]}]
  [:div.form
   [:fieldset
    [:legend "Число"]
    [:p
     [:input {:type "text"
              :value (:text number)
              :on-change #(swap! state assoc-in [:number :number :text]
                                 (-> % .-target .-value))}]]
    [:p
     [:label {:for "number-value-font-size"}
      "Размер шрифта (" (:font-size number) ")"]
     [:input {:id "number-value-font-size"
              :type "range"
              :min 1
              :max 250
              :value (:font-size number)
              :on-change #(swap! state assoc-in [:number :number :font-size]
                                 (-> % .-target .-value js/parseInt))}]]
    [:p
     [:label {:for "number-value-margin-top"}
      "Отступ сверху (" (:margin-top number) ")"]
     [:input {:id "number-value-margin-top"
              :type "range"
              :min 1
              :max 200
              :value (:margin-top number)
              :on-change #(swap! state assoc-in [:number :number :margin-top]
                                 (-> % .-target .-value js/parseInt))}]]
    [:p
     [:label {:for "number-value-x-offset"}
      "Горизонтальный сдвиг (" (:x-offset number) ")"]
     [:input {:id "number-value-margin-top"
              :type "range"
              :min (- (-> number-layout :circle :r1))
              :max (-> number-layout :circle :r1)
              :value (:x-offset number)
              :on-change #(swap! state assoc-in [:number :number :x-offset]
                                 (-> % .-target .-value js/parseInt))}]]]
   [:fieldset
    [:legend "Единицы измерения"]
    [:p
     [:input {:type "text"
              :value (:text number-caption)
              :on-change #(swap! state assoc-in [:number :number-caption :text]
                                 (-> % .-target .-value))}]]
    [:p
     [:label {:for "number-number-caption-font-size"}
      "Размер шрифта (" (:font-size number-caption) ")"]
     [:input {:id "number-number-caption-font-size"
              :type "range"
              :min 1
              :max 250
              :value (:font-size number-caption)
              :on-change #(swap! state assoc-in [:number :number-caption :font-size]
                                 (-> % .-target .-value js/parseInt))}]]
    [:p
     [:label {:for "number-number-caption-margin-top"}
      "Отступ сверху (" (:margin-top number-caption) ")"]
     [:input {:id "number-number-caption-margin-top"
              :type "range"
              :min (- (-> number-layout :circle :y)
                      (-> number-layout :circle :r1))
              :max (+ (-> number-layout :circle :y)
                      (-> number-layout :circle :r1))
              :value (:margin-top number-caption)
              :on-change #(swap! state assoc-in [:number :number-caption :margin-top]
                                 (-> % .-target .-value js/parseInt))}]]]
   [:fieldset
    [:legend "Подпись"]
    [:p
     [:textarea {:value (:text caption)
                 :on-change #(swap! state assoc-in [:number :caption :text]
                                    (-> % .-target .-value))}]]
    [:p
     [:label {:for "number-caption-font-size"}
      "Размер шрифта (" (:font-size caption) ")"]
     [:input {:id "number-caption-font-size"
              :type "range"
              :min 1
              :max 250
              :value (:font-size caption)
              :on-change #(swap! state assoc-in [:number :caption :font-size]
                                 (-> % .-target .-value js/parseInt))}]]
    [:p
     [:label {:for "number-caption-line-height"}
      "Межстрочный интервал (x" (:line-height caption) ")"]
     [:input {:id "number-caption-line-height"
              :type "range"
              :min 0.1
              :max 2
              :step 0.05
              :value (:line-height caption)
              :on-change #(swap! state assoc-in [:number :caption :line-height]
                                 (-> % .-target .-value js/parseFloat))}]]
    [:p
     [:label {:for "number-caption-margin-top"}
      "Отступ сверху (" (:margin-top caption) "px)"]
     [:input {:id "number-caption-margin-top"
              :type "range"
              :min 1
              :max (- (:height sizes)
                      (* (:line-height caption) (:font-size caption)))
              :value (:margin-top caption)
              :on-change #(swap! state assoc-in [:number :caption :margin-top]
                                 (-> % .-target .-value js/parseInt))}]]]])

(defn title-tab-cmp [{:keys [text font-size line-height margin-top]}]
  [:div.form
   [:p
    [:label {:for "title-text"} "Текст заголовка"]
    [:textarea {:id "title-text"
                :on-change #(swap! state assoc-in [:title :text]
                                   (-> % .-target .-value))
                :value text}]]
   [:p
    [:label {:for "title-font-size"}
     "Размер шрифта (" font-size "px)"]
    [:input {:id "title-font-size"
             :type "range"
             :min 1
             :max 250
             :value font-size
             :on-change #(swap! state assoc-in [:title :font-size]
                                (-> % .-target .-value js/parseInt))}]]
   [:p
    [:label {:for "title-line-height"}
     "Межстрочный интервал (x" line-height ")"]
    [:input {:id "title-line-height"
             :type "range"
             :min 0.1
             :max 2
             :step 0.05
             :value line-height
             :on-change #(swap! state assoc-in [:title :line-height]
                                (-> % .-target .-value js/parseFloat))}]]
   [:p
    [:label {:for "title-margin-top"}
     "Отступ сверху (" margin-top "px)"]
    [:input {:id "title-margin-top"
             :type "range"
             :min 1
             :max (- (:height sizes)
                     (* line-height font-size))
             :value margin-top
             :on-change #(swap! state assoc-in [:title :margin-top]
                                (-> % .-target .-value js/parseInt))}]]])

(defn quote-canvas [ctx substate]
  (.fillText ctx "quote" 10 10))

(defn number-canvas [ctx {:keys [number number-caption caption]}]
  (let [middle-x (/ (:width sizes) 2)]
    (set! (.-fillStyle ctx) (:gray colors))
    (.fillRect ctx 0 0 (:width sizes) (:height sizes))

    ;; circle and it's content
    (.save ctx) ;; save to remove clipping later
    (let [{:keys [x y r1 r2]} (:circle number-layout)]
        (.beginPath ctx)
        (.arc ctx x y r2 0 (* 2 Math/PI))
        (.clip ctx)

        (set! (.-fillStyle ctx) (:white colors))
        (.beginPath ctx)
        (.arc ctx x y r2 0 (* 2 Math/PI))
        (.fill ctx)

        (set! (.-fillStyle ctx) (:signature colors))
        (.beginPath ctx)
        (.arc ctx x y r1 0 (* 2 Math/PI))
        (.fill ctx))
    (let [{:keys [font-size text margin-top x-offset]} number]
      (set! (.-fillStyle ctx) (:white colors))
      (set! (.-textAlign ctx) "center")
      (set! (.-textBaseline ctx) "top")
      (set! (.-font ctx) ((:bold fonts) font-size))
      (.fillText ctx text (+ middle-x x-offset) margin-top))
    (let [{:keys [font-size text margin-top]} number-caption]
      (set! (.-fillStyle ctx) (:white colors))
      (set! (.-textAlign ctx) "center")
      (set! (.-textBaseline ctx) "top")
      (set! (.-font ctx) ((:bold fonts) font-size))
      (.fillText ctx text middle-x margin-top))
    (.restore ctx)

    (let [{:keys [margin-top font-size text line-height]} caption
          line-height-px (* line-height font-size)
          line-parts (str/split-lines text)
          ys (take (count line-parts)
                   (iterate (partial + line-height-px) margin-top))]
      (set! (.-fillStyle ctx) (:black colors))
      (set! (.-textAlign ctx) "center")
      (set! (.-textBaseline ctx) "top")
      (set! (.-font ctx) ((:normal fonts) font-size))
      (dorun (map #(.fillText ctx %1 middle-x %2) line-parts ys)))

    (let [{:keys [font-size font-weight text margin-bottom]} (:left captions)
          bottom-y (- (:height sizes) margin-bottom)
          left-x margin-bottom]
      (set! (.-fillStyle ctx) (:signature colors))
      (set! (.-textBaseline ctx) "alphabetic")
      (set! (.-textAlign ctx) "left")
      (set! (.-font ctx) ((p/safe-get fonts font-weight) font-size))
      (.fillText ctx text left-x bottom-y))

    (let [{:keys [font-size font-weight text margin-bottom]} (:right captions)
          bottom-y (- (:height sizes) margin-bottom)
          right-x (- (:width sizes) margin-bottom)]
      (set! (.-fillStyle ctx) (:signature colors))
      (set! (.-textBaseline ctx) "alphabetic")
      (set! (.-textAlign ctx) "right")
      (set! (.-font ctx) ((p/safe-get fonts font-weight) font-size))
      (.fillText ctx text right-x bottom-y))
    )
  )

(defn title-canvas [ctx {:keys [font-size text line-height margin-top]}]
  (let [middle-x (/ (:width sizes) 2)]
    (set! (.-fillStyle ctx) (:signature colors))
    (.fillRect ctx 0 0 (:width sizes) (:height sizes))

    (let [line-height-px (* line-height font-size)
          line-parts (str/split-lines text)
          ys (take (count line-parts)
                   (iterate (partial + line-height-px) margin-top))]
      (set! (.-fillStyle ctx) (:white colors))
      (set! (.-textAlign ctx) "center")
      (set! (.-textBaseline ctx) "top")
      (set! (.-font ctx) ((:medium fonts) font-size))
      (dorun (map #(.fillText ctx %1 middle-x %2) line-parts ys)))

    (let [{:keys [font-weight text margin-bottom]} (:left captions)
          bottom-y (- (:height sizes) margin-bottom)]
      (set! (.-textBaseline ctx) "bottom")
      (set! (.-font ctx) ((p/safe-get fonts font-weight) 24))
      (.fillText ctx text middle-x bottom-y))))

(def tabs
  {:quote {:cmp quote-tab-cmp
           :canvas quote-canvas
           :name "Цитата дня"}
   :number {:cmp number-tab-cmp
            :canvas number-canvas
            :name "Число дня"}
   :title {:cmp title-tab-cmp
           :canvas title-canvas
           :name "Заголовок"}})

(defn tab-selector-cmp []
  (let [current-tab (:current-tab @state)]
    [:ul.tabs
     (for [[k v] tabs]
       ^{:key k}
       [:li {:on-click #(swap! state assoc :current-tab k)
             :class (if (= current-tab k)
                      "active"
                      "")}
        (:name v)])]))

(defn canvas-cmp [tab substate]
  (let [render-canvas
        (fn [canvas tab substate]
          (set! (.-width canvas) (:width sizes))
          (set! (.-height canvas) (:height sizes))
          (let [ctx (.getContext canvas "2d")]
            (.clearRect ctx 0 0 (:width sizes) (:height sizes))
            ((p/safe-get-in tabs [tab :canvas]) ctx substate)))]
    (reagent/create-class
     {:reagent-render
      (fn [_ _] [:canvas {:style {:width (str (-> sizes :width (/ 2)) "px")
                                  :height (str (-> sizes :height (/ 2)) "px")}}])
      :component-did-mount
      (fn [this]
        (let [[_ tab substate] (reagent/argv this)]
          (render-canvas (reagent/dom-node this) tab substate)))
      :component-did-update
      (fn [this old-argv]
        (let [[_ tab substate] (reagent/argv this)]
          (render-canvas (reagent/dom-node this) tab substate)))})))

(defn preview-cmp []
  (let [current-tab (:current-tab @state)
        current-substate (p/safe-get @state current-tab)]
    [canvas-cmp current-tab current-substate]))

(defn root-cmp []
  (let [current-tab (:current-tab @state)
        substate (p/safe-get @state current-tab)]
    [:div.container
     [:div.controls-container
      [tab-selector-cmp]
      [:div.clear]
      [:div.controls
       [(-> tabs (p/safe-get current-tab) :cmp) substate]]]
     [:div.preview
      [preview-cmp]]
     [:a.cred {:href "https://github.com/si14"}
      "♥"]
     ]))

;;
;; Run the thing
;;

(defn -main []
  (reagent/render-component
   [root-cmp]
   (.-body js/document)))
