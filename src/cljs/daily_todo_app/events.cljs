(ns daily-todo-app.events
  (:require [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   ->interceptor
                                   debug]]
            [cljs-time.core :refer [now]]
            [daily-todo-app.db :refer [default-db
                                       assoc-todo
                                       dissoc-todo
                                       update-todo
                                       get-completed-today
                                       next-order
                                       move-todo]]))

(def update-active-list
  (->interceptor
    :id :update-active-list
    :after (fn [context]
             (assoc-in context [:effects :dispatch] [::set-active-todo-list]))))

(reg-event-db
  ::initialize-db
  (fn [_ _]
    default-db))

(reg-event-db
  ::set-active-panel ;; TODO remove
  (fn [db [_ active-panel]]
    (assoc db :active-panel active-panel)))

(reg-event-db
  ::set-active-user-list
  [update-active-list]
  (fn [db [_ list-id]]
    (assoc db :active-user-list list-id)))

(reg-event-db
  ::set-active-scope
  [update-active-list]
  (fn [db [_ scope]]
    (assoc db :active-scope scope)))

(reg-event-db
  ::set-active-todo-list
  [debug]
  (fn [db _]
    (let [{:keys [todos active-user-list active-scope]} db
          active-todos (->> (get-in todos [active-user-list active-scope])
                            vals
                            (sort #(- (:order %1) (:order %2))))]
      (assoc db :active-todo-list active-todos))))

(reg-event-db
  ::add-user-list
  (fn [db [_ {:keys [id label]}]]
    (let [user-lists (:user-lists db)
          list-id (if (nil? id) (random-uuid) id)]
      (assoc db :user-lists (assoc user-lists list-id {:id list-id :label label})))))

(reg-event-db
  ::add-todo
  [update-active-list]
  (fn [db [_ {:keys [list-id scope title]}]]
    (let [id (random-uuid)
          order (next-order (get-in db [:todos list-id scope]))]
      (assoc-todo db {:id id
                      :title title
                      :done false
                      :list-id list-id
                      :scope scope
                      :order order}))))

(reg-event-db
  ::update-todo
  [update-active-list]
  (fn [db [_ todo]]
    (assoc-todo db todo)))

(reg-event-db
  ::delete-todo
  [update-active-list]
  (fn [db [_ todo]]
    (dissoc-todo db todo)))

(reg-event-db
  ::move-todo
  [update-active-list]
  (fn [db [_ curr-order new-order]]
    (move-todo db curr-order new-order)))

(reg-event-db
  ::advance-day
  [update-active-list]
  (fn [db [_ _]]
    (let [date (now)]
      (loop [the-db db
             completed (get-completed-today db)]
        (let [old-todo (first completed)
              new-todo (assoc old-todo :completed date
                                       :scope :yesterday)]
          (if (empty? completed)
            the-db
            (recur (update-todo the-db old-todo new-todo)
                 (rest completed))))))))