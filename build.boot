(set-env!
 :source-paths    #{"src"}
 :resource-paths  #{"html" "conf" "private"}
 :dependencies    '[[adzerk/boot-cljs "0.0-3269-0"]
                    [adzerk/boot-reload "0.2.6"]
                    [pandeiro/boot-http "0.6.3-SNAPSHOT"]
                    [boot-garden "1.2.5-2"]
                    [tonsky/boot-anybar "0.1.0"]

                    [org.clojure/clojure "1.7.0-beta2"]
                    [org.clojure/clojurescript "0.0-3269"]
                    [garden "1.2.5"]
                    [danielsz/boot-autoprefixer "0.0.2"]

                    [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                    [reagent "0.5.0"]
                    [prismatic/schema "0.4.2"]
                    [prismatic/plumbing "0.4.3"]])

(require
 '[adzerk.boot-cljs   :refer [cljs]]
 '[adzerk.boot-reload :refer [reload]]
 '[pandeiro.boot-http :refer [serve]]
 '[boot-garden.core   :refer [garden]]
 '[danielsz.autoprefixer :refer [autoprefixer]]
 '[tonsky.boot-anybar :refer [anybar]])

(deftask dev []
  (comp
   (serve :dir "target"
          :port 8080)
   (watch)
   (reload :on-jsload 'broquote.core/-main)
   (anybar)
   (autoprefixer :files ["general.css"])
   (cljs :optimizations :none
         :source-map true)))

(deftask release []
  (comp
   (cljs :optimizations :advanced)
   (autoprefixer :files ["general.css"])
   (sift :include #{#"(^index\.html|^main\.js)"
                    #"(^css/.*)"
                    #"(^fonts/.*\.woff)"})))
