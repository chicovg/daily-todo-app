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

;; CRUD functions for todos
(defn assoc-todo
  [db {:keys [id title done list-id scope completed order]}]
  (assoc-in db [:todos list-id scope id] {:id id
                                           :title title
                                           :done done
                                           :list-id list-id
                                           :scope scope
                                           :completed completed
                                           :order order}))

(defn dissoc-todo
  [db {:keys [id list-id scope]}]
  (let [scoped-todos (get-in db [:todos list-id scope])]
    (assoc-in db [:todos list-id scope] (dissoc scoped-todos id))))

(defn update-todo
  [db old-todo new-todo]
  (-> db
      (dissoc-todo old-todo)
      (assoc-todo new-todo)))

(defn get-completed-today
  [{:keys [todos active-user-list]}]
  (let [completed-today (get-in todos [active-user-list :today])]
    (filter
      #(= (:done %) true)
      (vals completed-today))))

(defn next-order
  [todos]
  (->> (vals todos)
       (map :order)
       (reduce max 0)
       inc))

;; two steps here
;; update the todo
;; update the one next to it
;; find
(defn find-with-order
  [todo-list order]
  (.log js/console todo-list)
  (.log js/console order)
  (->> todo-list
       (filter #(= (:order %) order))
       first))

(defn move-todo
  [db current-order new-order]
  (.log js/console db)
  (let [todo-list (:active-todo-list db)
        {list-id :list-id scope :scope curr-id :id} (find-with-order todo-list current-order)
        {next-id :id} (find-with-order todo-list new-order)]
    ;(.log js/console current-order)
    ;(.log js/console new-order)
    (-> db
        (assoc-in [:todos list-id scope curr-id :order] new-order)
        (assoc-in [:todos list-id scope next-id :order] current-order))))

