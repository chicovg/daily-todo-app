(defproject daily-todo-app "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.908"]
                 [reagent "0.7.0"]
                 [re-frame "0.10.5"]
                 [com.andrewmcveigh/cljs-time "0.5.0"]
                 [org.clojure/core.async "0.2.391"]
                 [re-com "2.1.0"]
                 [secretary "1.2.3"]
                 [garden "1.3.2"]
                 [ns-tracker "0.3.0"]
                 [compojure "1.5.0"]
                 [yogthos/config "0.8"]
                 [ring "1.4.0"]
                 [danlentz/clj-uuid "0.1.7"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]]

  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-garden "0.2.8"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"
                                    "resources/public/css"]

  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler daily-todo-app.handler/dev-handler}

  :garden {:builds [{:id           "screen"
                     :source-paths ["src/clj"]
                     :stylesheet   daily-todo-app.css/screen
                     :compiler     {:output-to     "resources/public/css/screen.css"
                                    :pretty-print? true}}]}

  :profiles {:dev {
                   :dependencies [[binaryage/devtools "0.9.4"]]
                   :plugins      [[lein-figwheel "0.5.13"]
                                  [lein-doo "0.1.8"]]}
             :prod {}}

  :cljsbuild {:builds
              [{:id           "dev"
                :source-paths ["src/cljs"]
                :figwheel     {:on-jsload "daily-todo-app.core/mount-root"}
                :compiler     {:main                 daily-todo-app.core
                               :output-to            "resources/public/js/compiled/app.js"
                               :output-dir           "resources/public/js/compiled/out"
                               :asset-path           "js/compiled/out"
                               :source-map-timestamp true
                               :preloads             [devtools.preload]
                               :external-config      {:devtools/config {:features-to-install :all}}}}


               {:id           "min"
                :source-paths ["src/cljs"]
                :jar true
                :compiler     {:main            daily-todo-app.core
                               :output-to       "resources/public/js/compiled/app.js"
                               :optimizations   :advanced
                               :closure-defines {goog.DEBUG false}
                               :pretty-print    false}}

               {:id           "test"
                :source-paths ["src/cljs" "test/cljs"]
                :compiler     {:main          daily-todo-app.runner
                               :output-to     "resources/public/js/compiled/test.js"
                               :output-dir    "resources/public/js/compiled/test/out"
                               :optimizations :none}}]}


  :main daily-todo-app.server

  :aot [daily-todo-app.server]

  :uberjar-name "daily-todo-app.jar"

  :prep-tasks [["cljsbuild" "once" "min"]["garden" "once"] "compile"])

