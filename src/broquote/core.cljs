(ns broquote.core
  (:require
   [clojure.string :as str]
   [plumbing.core :as p]
   [reagent.core :as reagent]))

(enable-console-print!)

;;
;; Constants
;;

(def defaults
  {:quote {:photo {:x-offset -37
                   :y-offset 2
                   :scale 0.7
                   :image (.getElementById js/document "mona")}
           :author {:font-size 27
                    :text "Мона Лиззи"}
           :author-comment {:margin-top 20
                            :font-size 26
                            :text "известная актриса о ситуации в России"}
           :quote {:font-size 71
                   :text "Под брусчаткой — пляж,\nотвечаю"
                   :line-height 1.0
                   :margin-top 314}}
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

(def middle-x (/ (:width sizes) 2))

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

(def quote-layout {:circle {:x (/ (:width sizes) 2)
                            :y 97
                            :r 75}
                   :author {:x (/ (:width sizes) 2)
                            :y 216}})

;;
;; State
;;


(defonce state
  (reagent/atom
   {:current-tab :title
    :quote (:quote defaults)
    :number (:number defaults)
    :title (:title defaults)}))

;;
;; Components
;;

(defn quote-tab-cmp [{:keys [photo author author-comment quote]}]
  [:div.form
   [:fieldset
    [:legend "Фото"]
    [:p
     [:input {:type "file"
              :size 5
              :style {:max-width "200px"}
              :on-change
              (fn [evt]
                (when-let [file (-> evt .-target .-files (aget 0))]
                  (when (-> file .-type (.match "image.()"))
                    (let [reader (js/FileReader.)]
                      (set! (.-onload reader)
                            (fn [evt]
                              (let [url (-> evt .-target .-result)
                                    img (js/Image.)]
                                (set! (.-src img) url)
                                (.log js/console img)
                                (swap! state assoc-in [:quote :photo :image]
                                       img))))
                      (.readAsDataURL reader file)))))}]]
    [:p
     [:label {:for "quote-photo-x-offset"}
      "Горизонтальный сдвиг (" (:x-offset photo) ")"]
     [:input {:id "quote-photo-x-offset"
              :type "range"
              :min -500
              :max 500
              :value (:x-offset photo)
              :on-change #(swap! state assoc-in [:quote :photo :x-offset]
                                 (-> % .-target .-value js/parseInt))}]]
    [:p
     [:label {:for "quote-photo-y-offset"}
      "Вертикальный сдвиг (" (:y-offset photo) ")"]
     [:input {:id "quote-photo-y-offset"
              :type "range"
              :min -500
              :max 500
              :value (:y-offset photo)
              :on-change #(swap! state assoc-in [:quote :photo :y-offset]
                                 (-> % .-target .-value js/parseInt))}]]
    [:p
     [:label {:for "quote-photo-scale"}
      "Масштаб (x" (:scale photo) ")"]
     [:input {:id "quote-photo-scale"
              :type "range"
              :min 0.1
              :max 1.3
              :step 0.05
              :value (:scale photo)
              :on-change #(swap! state assoc-in [:quote :photo :scale]
                                 (-> % .-target .-value js/parseFloat))}]]]
   [:fieldset
    [:legend "Автор"]
    [:p
     [:input {:type "text"
              :value (:text author)
              :on-change #(swap! state assoc-in [:quote :author :text]
                                 (-> % .-target .-value))}]]]
   [:fieldset
    [:legend "Комментарий"]
    [:p
     [:textarea {:value (:text author-comment)
                 :on-change #(swap! state assoc-in [:quote :author-comment :text]
                                    (-> % .-target .-value))}]]
    [:p
     [:label {:for "quote-author-comment-font-size"}
      "Размер шрифта (" (:font-size author-comment) ")"]
     [:input {:id "quote-author-comment-font-size"
              :type "range"
              :min 10
              :max 50
              :value (:font-size author-comment)
              :on-change #(swap! state assoc-in [:quote :author-comment :font-size]
                                 (-> % .-target .-value js/parseInt))}]]
    [:p
     [:label {:for "quote-author-comment-margin-top"}
      "Отступ сверху (" (:margin-top author-comment) "px)"]
     [:input {:id "quote-author-comment-margin-top"
              :type "range"
              :min 1
              :max 60
              :value (:margin-top author-comment)
              :on-change #(swap! state assoc-in [:quote :author-comment :margin-top]
                                 (-> % .-target .-value js/parseInt))}]]]
   [:fieldset
    [:legend "Цитата"]
    [:p
     [:textarea {:value (:text quote)
                 :on-change #(swap! state assoc-in [:quote :quote :text]
                                    (-> % .-target .-value))}]]
    [:p
     [:label {:for "quote-quote-font-size"}
      "Размер шрифта (" (:font-size quote) ")"]
     [:input {:id "quote-quote-font-size"
              :type "range"
              :min 1
              :max 250
              :value (:font-size quote)
              :on-change #(swap! state assoc-in [:quote :quote :font-size]
                                 (-> % .-target .-value js/parseInt))}]]
    [:p
     [:label {:for "quote-quote-line-height"}
      "Межстрочный интервал (x" (:line-height quote) ")"]
     [:input {:id "quote-quote-line-height"
              :type "range"
              :min 0.1
              :max 2
              :step 0.05
              :value (:line-height quote)
              :on-change #(swap! state assoc-in [:quote :quote :line-height]
                                 (-> % .-target .-value js/parseFloat))}]]
    [:p
     [:label {:for "quote-quote-margin-top"}
      "Отступ сверху (" (:margin-top quote) "px)"]
     [:input {:id "quote-quote-margin-top"
              :type "range"
              :min 200
              :max 500
              :value (:margin-top quote)
              :on-change #(swap! state assoc-in [:quote :quote :margin-top]
                                 (-> % .-target .-value js/parseInt))}]]]
   ])

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
   [:fieldset
    [:legend "Заголовок"]
    [:p
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
                                 (-> % .-target .-value js/parseInt))}]]]])

(defn quote-canvas [ctx {:keys [photo author author-comment quote]}]
  (let [tmpl (.getElementById js/document "template-quote")]
    (.drawImage ctx tmpl 0 0))

  (.save ctx)
  (let [{:keys [x y r]} (:circle quote-layout)
        {:keys [x-offset y-offset scale image]} photo]
    (.beginPath ctx)
    (.arc ctx x y r 0 (* 2 Math/PI))
    (.clip ctx)
    (.drawImage ctx image
                (+ (- x r) x-offset) (+ (- y r) y-offset)
                (* scale (.-width image)) (* scale (.-height image)))
    (set! (.-strokeStyle ctx) (:white colors))
    (set! (.-lineWidth ctx) 4)
    (.beginPath ctx)
    (.arc ctx x y (- r 2) 0 (* 2 Math/PI))
    (.stroke ctx))
  (.restore ctx)

  (let [{:keys [x y]} (:author quote-layout)
        {:keys [font-size text]} author
        text (str/upper-case text)]
    (set! (.-fillStyle ctx) (:white colors))
    (set! (.-textAlign ctx) "center")
    (set! (.-textBaseline ctx) "baseline")
    (set! (.-font ctx) ((:bold fonts) font-size))
    (.fillText ctx text x y))

  (let [{:keys [margin-top font-size text line-height]} author-comment
        x (/ (:width sizes) 2)
        y (+ margin-top (-> quote-layout :author :y))]
    (set! (.-fillStyle ctx) (:black colors))
    (set! (.-textAlign ctx) "center")
    (set! (.-textBaseline ctx) "top")
    (set! (.-font ctx) ((:normal fonts) font-size))
    (.fillText ctx text x y))

  (let [{:keys [margin-top font-size text line-height]} quote
        line-height-px (* line-height font-size)
        line-parts (str/split-lines text)
        ys (take (count line-parts)
                 (iterate (partial + line-height-px) margin-top))]
    (set! (.-fillStyle ctx) (:black colors))
    (set! (.-textAlign ctx) "center")
    (set! (.-textBaseline ctx) "top")
    (set! (.-font ctx) ((:medium fonts) font-size))
    (dorun (map #(.fillText ctx %1 middle-x %2) line-parts ys))))

(defn number-canvas [ctx {:keys [number number-caption caption]}]
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
  {:title {:cmp title-tab-cmp
           :canvas title-canvas
           :name "Заголовок"}
   :number {:cmp number-tab-cmp
            :canvas number-canvas
            :name "Число дня"}
   :quote {:cmp quote-tab-cmp
           :canvas quote-canvas
           :name "Цитата дня"}})

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
      (fn [_ _] [:canvas
                 {:id "the-canvas"
                  :style {:width (str (-> sizes :width (/ 2)) "px")
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
    [:div.left-pane
     [:div.controls-container
      [tab-selector-cmp]
      [:div.clear]
      [:div.controls
       [(-> tabs (p/safe-get current-tab) :cmp) substate]]]
     [:div.right-pane
      [:div.preview
       [preview-cmp]]
      [:a.download
       {:on-click (fn [evt]
                    (let [a (.-target evt)
                          canvas (.getElementById js/document "the-canvas")
                          link (.toDataURL canvas)
                          filename (str (name current-tab) ".png")]
                      (set! (.-href a) link)
                      (set! (.-download a) filename)))
        :target "_blank"}
       "Сохранить картинку"]]
     [:a.cred {:href "https://github.com/si14"}
      "♥"]
     ]))

;;
;; Run the thing
;;

(defn -main []
  (reagent/render-component
   [root-cmp]
   (.getElementById js/document "app")
   #_(.-body js/document)))
