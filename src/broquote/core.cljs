(ns broquote.core
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [cljs.core.async :as async :refer [put! chan alts!]]
   [plumbing.core :as p]
   [reagent.core :as reagent]))

(enable-console-print!)

;;
;; Constants
;;

(def defaults
  {:quote {:quote {:font-size 20
                   :text "Текст цитаты"}
           :author {:font-size 15
                    :text "Автор цитаты"}
           :photo :default
           :quote-font-size 20
           :quote-text "Текст цитаты"
           :author-font-size 15}})

;;
;; State
;;


(defonce state
  (reagent/atom
   {:current-tab :quote
    :quote (:quote defaults)}))

(defonce interaction-chan (chan))

;;
;; Utils
;;

;; see https://github.com/Day8/re-frame/wiki/Beware-Returning-False
(defn dispatch [evt]
  (put! interaction-chan evt)
  nil)

(defn format-time [date]
  (let [fmt (.-SHORT_TIME goog.i18n.DateTimeFormat.Format)]
    (.format (goog.i18n.DateTimeFormat. fmt)
             date)))

;;
;; Components
;;

(defn quote-tab-cmp []
  [:div "quote"])

(defn number-tab-cmp []
  [:div "number"])

(defn title-tab-cmp []
  [:div "title"])

(def tabs
  {:quote {:cmp quote-tab-cmp
           :name "Цитата дня"}
   :number {:cmp number-tab-cmp
            :name "Число дня"}
   :title {:cmp title-tab-cmp
           :name "Заголовок"}})

(defn tab-selector-cmp []
  [:ul (for [[k v] tabs]
         ^{:key k}
         [:li {:on-click #(dispatch [:tab-click k])}
          (:name v)])])

(defn preview-cmp []
  (let [width 500
        height 250
        middle-x (/ width 2)
        pic-width 100
        pic-height 100
        pic-x (- middle-x (/ pic-width 2))
        pic-y 20
        clip-circle-attrs {:cx (+ pic-x (/ pic-width 2))
                           :cy (+ pic-y (/ pic-height 2))
                           :r (/ (min pic-width pic-height) 2)}]
    [:svg
     {:id "previewsvg"
      :width width
      :height height
      :style {:border "1px solid black"
              :font-family "PT Sans"
              :font-weight "bold"
              }}
     [:defs
      [:clipPath {:id "circleClip"}
       [:circle clip-circle-attrs]]]
     [:text {:style {:-webkit-user-select "none"
                     :-moz-user-select "none"}
             :text-anchor "middle"
             :x middle-x :y 20 :font-size 20}
      "Foo"]

     [:g {"dangerouslySetInnerHTML"
          #js{:__html (str "<image xlink:href=\"img/mona.jpg\" "
                           "clip-path=\"url(#circleClip)\" "
                           "x=\"" pic-x "\" y=\"" pic-y "\" "
                           "width=\"" pic-width "\" height=\"" pic-height "\" />")}}]
     [:circle (assoc clip-circle-attrs
                     :stroke "black"
                     :stroke-width "5"
                     :fill-opacity 0)]])
  )

(defn root-cmp []
  (let [current-tab (:current-tab @state)]
    [:div
     [tab-selector-cmp]
     [(-> tabs (p/safe-get current-tab) :cmp)]
     [preview-cmp]
     [:button {:on-click #(dispatch [:save-preview])}
      "Save"]]))

;;
;; Interaction
;;

(defmulti interaction first)

(defmethod interaction :tab-click
  [[_ tab-name]]
  (swap! state assoc :current-tab tab-name))

(defmethod interaction :save-preview
  [_]
  (let [svg-el (.getElementById js/document "previewsvg")
        ;; enc (fn [x] (-> x js/encodeURIComponent js/unescape js/btoa))
        ;; svg-str (reagent/render-to-string [preview-cmp])
        ;; canvas (.createElement js/document "canvas")
        ;; ctx (.getContext canvas "2d")
        ;; img (.createElement js/document "img")
        ;; _ (.setAttribute img "src", (str "data:image/svg+xml;base64,"
        ;;                                  (js/btoa svg-str)))

        ]
    (.toDataURL svg-el "image/png"
                #js{:callback (fn [png-data]
                                (let [el (.createElement js/document "img")]
                                  (.setAttribute el "src" png-data)
                                  (.appendChild (.-body js/document) el)
                                  )
                                )})
    ;; (print svg-str)
    ;; (set! (.-onload img) (fn []
    ;;                        (print "QUUX")
    ;;                      (.drawImage ctx 0 0)
    ;;                      (print (.toDataUrl canvas "image/png"))
    ;;                      ))
    ;; (.appendChild (.-body js/document) img)

    )
  )

(defmethod interaction :default
  [evt]
  (.warn js/console "unhandled event: " (pr-str evt)))

(go
  (while true
    (interaction (<! interaction-chan))))

;;
;; Wiring it all up
;;

(defn -main []
  (reagent/render-component
   [root-cmp]
   (.-body js/document)))
