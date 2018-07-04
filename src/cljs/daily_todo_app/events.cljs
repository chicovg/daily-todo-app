(ns daily-todo-app.events
  (:require [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   ->interceptor
                                   debug]]
            [cljs-time.core :refer [now]]
            [daily-todo-app.db :refer [default-db]]
            [com.degel.re-frame-firebase :as firebase]))

(defn set-user
  [db [_ user]]
  (assoc db :user user))

(reg-event-db
  ::set-user
  set-user)

(reg-event-fx
  ::sign-in
  [debug]
  (fn [_ _] {:firebase/google-sign-in nil}))

(reg-event-fx
  ::sign-out
  [debug]
  (fn [_ _] {:firebase/sign-out nil}))

(defn set-active-panel
  [db [_ panel]]
  (assoc db :active-panel panel))

(reg-event-db
  ::set-active-panel
  set-active-panel)

(defn dispatch-set-active-todo-list
  [context]
  (assoc-in context [:effects :dispatch] [::set-active-todo-list]))

(def update-active-list
  (->interceptor
    :id :update-active-list
    :after dispatch-set-active-todo-list))

(reg-event-db
  ::initialize-db
  (fn [_ _]
    default-db))

(defn set-active-user-list
  [db [_ list-id]]
  (assoc db :active-todo-list list-id))

(reg-event-db
  ::set-active-user-list
  [update-active-list]
  set-active-user-list)

(defn set-active-scope
  [db [_ scope]]
  (assoc db :active-scope scope))

(reg-event-db
  ::set-active-scope
  [update-active-list]
  set-active-scope)

(defn set-active-todo-list
  [db _]
  (let [{:keys [todos active-user-list active-scope]} db
        active-todos (->> (get-in todos [active-user-list active-scope])
                          vals
                          (sort #(- (:order %1) (:order %2))))]
      (assoc db :active-todo-list active-todos)))

(reg-event-db
  ::set-active-todo-list
  [debug]
  set-active-todo-list)

(defn add-user-list
  [db [_ {:keys [id label]}]]
  (let [user-lists (:user-lists db)
        list-id (if (nil? id) (random-uuid) id)]
    (assoc db :user-lists (assoc user-lists list-id {:id list-id :label label}))))

(reg-event-db
  ::add-user-list
  add-user-list)

(defn assoc-todo
  [db {:keys [id title done list-id scope completed order]}]
  (assoc-in db [:todos list-id scope id] {:id id
                                          :title title
                                          :done done
                                          :list-id list-id
                                          :scope scope
                                          :completed completed
                                          :order order}))

(defn next-order
  [todos]
  (->> (vals todos)
       (map :order)
       (reduce max 0)
       inc))

(defn add-todo
  [db [_ {:keys [list-id scope title]}]]
  (let [id (random-uuid)
        order (next-order (get-in db [:todos list-id scope]))]
    (assoc-todo db {:id id
                    :title title
                    :done false
                    :list-id list-id
                    :scope scope
                    :order order})))

(reg-event-db
  ::add-todo
  [update-active-list]
  add-todo)

(defn dissoc-todo
  [db {:keys [id list-id scope]}]
  (let [scoped-todos (get-in db [:todos list-id scope])]
    (assoc-in db [:todos list-id scope] (dissoc scoped-todos id))))

(defn replace-todo
  [db old-todo new-todo]
  (-> db
      (dissoc-todo old-todo)
      (assoc-todo new-todo)))

(defn update-todo
  [db [_ {:keys [list-id scope id]} updates]]
  (let [curr-todo (get-in db [:todos list-id scope id])
        new-todo (merge curr-todo updates)]
    (replace-todo db curr-todo new-todo)))

(reg-event-db
  ::update-todo
  [update-active-list]
  update-todo)

(defn delete-todo
  [db [_ todo]]
  (dissoc-todo db todo))

(reg-event-db
  ::delete-todo
  [update-active-list]
  delete-todo)

(defn find-with-order
  [todo-list order]
  (->> todo-list
       (filter #(= (:order %) order))
       first))

(defn move-todo
  [db [_ current-order new-order]]
  (let [todo-list (:active-todo-list db)
        {list-id :list-id scope :scope curr-id :id} (find-with-order todo-list current-order)
        {next-id :id} (find-with-order todo-list new-order)]
    (-> db
        (assoc-in [:todos list-id scope curr-id :order] new-order)
        (assoc-in [:todos list-id scope next-id :order] current-order))))

(reg-event-db
  ::move-todo
  [update-active-list]
  move-todo)

(defn get-completed-today
  [{:keys [todos active-user-list]}]
  (let [completed-today (get-in todos [active-user-list :today])]
    (filter
      #(= (:done %) true)
      (vals completed-today))))

(defn advance-day
  [db [_ _]]
  (let [date (now)]
    (loop [the-db db
           completed (get-completed-today db)]
      (let [old-todo (first completed)
            new-todo (assoc old-todo :completed date
                                     :scope :yesterday)]
        (if (empty? completed)
          the-db
          (recur (replace-todo the-db old-todo new-todo)
               (rest completed)))))))

(reg-event-db
  ::advance-day
  [update-active-list]
  advance-day)
