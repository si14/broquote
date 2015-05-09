(set-env!
 :source-paths    #{"src" #_"src-clj"}
 :resource-paths  #{"html" "conf"}
 :dependencies    '[[adzerk/boot-cljs   "0.0-2814-4"]
                    [adzerk/boot-reload "0.2.6"]
                    [pandeiro/boot-http "0.6.2"]
                    [boot-garden "1.2.5-2"]

                    [org.clojure/clojurescript "0.0-3211"]
                    [garden "1.2.5"]

                    [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                    [reagent "0.5.0"]
                    [prismatic/schema "0.4.2"]
                    [prismatic/plumbing "0.4.3"]])

(require
 '[adzerk.boot-cljs   :refer [cljs]]
 '[adzerk.boot-reload :refer [reload]]
 '[pandeiro.boot-http :refer [serve]]
 '[boot-garden.core   :refer [garden]])

(deftask dev []
  (comp
   (watch)
   (reload :on-jsload 'broquote.core/-main)
   #_(garden :styles-var 'resptm.styles/screen :pretty-print true)
   (cljs :optimizations :none, :source-map true)
   (serve :dir "target", :port 8080)))

(deftask release []
  (comp
   (cljs :optimizations :advanced)
   (sift :include #{#"(^index\.html|^main\.js)"})))
