(ns daily-todo-app.core
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [dispatch-sync clear-subscription-cache!]]
            [cljs-time.core :refer [today-at-midnight]]
            [daily-todo-app.events :as events]
            [daily-todo-app.routes :as routes]
            [daily-todo-app.views :as views]
            [daily-todo-app.config :as config]))

(defn insert-dev-data []
  ;(def home-id (random-uuid))

  ;(dispatch-sync [::events/add-user-list {:id home-id :label "Home"}])

  ;(dispatch-sync [::events/set-active-user-list home-id])

  ;(dispatch-sync [::events/add-todo {:list-id home-id
  ;                                            :scope :today
  ;                                            :title "Build a todo app"}])
  ;(dispatch-sync [::events/add-todo {:list-id home-id
  ;                                            :scope :today
  ;                                            :title "Learn re-frame"}])
  ;(dispatch-sync [::events/add-todo {:list-id home-id
  ;                                            :scope :tomorrow
  ;                                            :title "Design the next app"}])

  ;(def work-id (random-uuid))
  ;(dispatch-sync [::events/add-user-list {:id work-id :label "Work"}]))

  ;(def completed-todos [{:id (random-uuid)
  ;                       :list-id home-id
  ;                       :scope :yesterday
  ;                       :title "Installed Clojure"
  ;                       :done true}
  ;                      {:id (random-uuid)
  ;                       :list-id home-id
  ;                       :scope :yesterday
  ;                       :title "Finished the cljs tutorial"
  ;                       :done true}])
  ;
  ;(dispatch-sync [::events/add-daily-history {:date (today-at-midnight)
  ;                                            :todos completed-todos}])
)

(defn dev-setup []
  (insert-dev-data)
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (routes/app-routes)
  (dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
