(ns daily-todo-app.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::name ;; TODO remove
 (fn [db]
   (:name db)))

(reg-sub
  ::user
  (fn [db]
    (:user db)))

(reg-sub
 ::active-panel ;; TODO remove
 (fn [db _]
   (:active-panel db)))

(reg-sub
  ::active-user-list
  (fn [db _]
    (:active-user-list db)))

(reg-sub
  ::active-scope
  (fn [db _]
    (:active-scope db)))

(reg-sub
  ::tags
  (fn [db _]
    (vals (:tags db))))

(reg-sub
  ::user-lists
  (fn [db _]
    (vals (:user-lists db))))

(reg-sub
  ::active-todo-list
  (fn [db _]
    (:active-todo-list db)))

(reg-sub
  ::editing
  (fn [db _]
    (:editing db)))
