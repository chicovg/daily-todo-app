(ns daily-todo-app.css
  (:require [garden.def :refer [defstyles]]))

(defstyles screen
           [:.center {:margin "auto"}]
           [:input#enter-todo {:width "100%"}]
           [:.list-group-item {:padding "8px 4px"}])
           ;[:div.list-item-row :label {:font-weight :normal}])

