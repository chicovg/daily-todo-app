(ns daily-todo-app.db)

;; Schema
; {:id :title :done :scope :list-id :order :completed}

;; Data Model
(def default-db
  {:name "daily-todo"
   :active-user-list nil ;TODO store in local storage
   :active-scope :today ; TODO store in local storage
   :user-lists (sorted-map) ; :id (num) {:id :label :yesterday [uuid] :today [uuid] :tomorrow [uuid]}
   :todos (hash-map) ; :id (uuid) todo{:id :title :done :user-list-id :scope}
   :active-todo-list []
   :daily-histories (sorted-map)})










