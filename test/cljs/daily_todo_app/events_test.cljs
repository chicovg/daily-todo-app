(ns daily-todo-app.events-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [daily-todo-app.events :refer [set-active-user-list
                                           set-active-scope
                                           set-active-todo-list
                                           add-user-list
                                           add-todo
                                           delete-todo
                                           find-with-order
                                           move-todo
                                           get-completed-today
                                           advance-day]]))

(deftest test-set-active-user-list
  (testing "active-user-list is set"
    (let [db {}
          list-id (random-uuid)
          result-db (set-active-user-list db [:event list-id])]
      (is (= result-db {:active-todo-list list-id})))))

(deftest test-set-active-scope
  (testing "active-scope is set"
    (let [db {}
          scope :a-scope
          result-db (set-active-scope db [:event scope])]
      (is (= result-db {:active-scope scope})))))

(deftest test-set-active-todo-list
  (testing "active-todo-list is set"
    (let [active-list-id (random-uuid)

          todo-id-1 (random-uuid)
          active-todo-1 {:id todo-id-1 :label "A todo" :order 1}

          todo-id-2 (random-uuid)
          active-todo-2 {:id todo-id-2 :label "A second todo" :order 2}

          active-todos {todo-id-2 active-todo-2
                        todo-id-1 active-todo-1}
          db {:todos {active-list-id {:a-scope active-todos
                                      :other-scope {}}}
              :active-user-list active-list-id
              :active-scope :a-scope
              :active-todo-list []}
          result-db (set-active-todo-list db [:event])]
      (is (= result-db (merge db {:active-todo-list [active-todo-1
                                                     active-todo-2]}))))))

(deftest test-add-user-list
  (testing "a new user list is added"
    (let [db {}
          list {:id (random-uuid) :label "A test list"}
          result-db (add-user-list db [:event list])]
      (is (= result-db {:user-lists {(:id list) list}})))))

(deftest test-add-todo
  (testing "a new todo is added"
    (let [db {:todos {}}
          list-id (random-uuid)
          input {:list-id list-id
                 :scope :a-scope
                 :title "Something amazing!"}
          result-db (add-todo db [:event input])
          added-todo (first (vals (get-in result-db [:todos list-id :a-scope])))]
      (is (= (:list-id added-todo) list-id))
      (is (= (:scope added-todo) :a-scope))
      (is (= (:title added-todo) (:title input)))
      (is (uuid? (:id added-todo)))
      (is (= (:order added-todo) 1)))))

(deftest test-delete-todo
  (testing "an existing todo is deleted"
    (let [list-id (random-uuid)
          scope :a-scope
          todo-id (random-uuid)
          todo {:id todo-id
                :list-id list-id
                :scope scope
                :title "I don't want to do this anymore"}
          db {:todos {list-id {scope {todo-id todo}}}}
          result-db (delete-todo db [:event todo])]
      (is (= result-db {:todos {list-id {:a-scope {}}}})))))

(deftest test-find-with-order
  (testing "finds the todo with the given order"
    (let [todo-list [{:title "Not this one" :order 1}
                     {:title "This one" :order 2}]
          result (find-with-order todo-list 2)]
      (is (= result {:title "This one" :order 2})))))

(deftest test-move-todo
  (testing "moves a todo from one order to another"
    (let [list-id (random-uuid)
          scope :a-scope

          todo-id-1 (random-uuid)
          todo-1 {:id todo-id-1
                  :list-id list-id
                  :scope scope
                  :label "A todo"
                  :order 1}

          todo-id-2 (random-uuid)
          todo-2 {:id todo-id-2
                  :list-id list-id
                  :scope scope
                  :label "A second todo"
                  :order 2}

          db {:active-todo-list [todo-1 todo-2]}

          result-db (move-todo db [:event 1 2])]
      (is (= (get-in result-db [:todos list-id scope todo-id-1 :order]) 2))
      (is (= (get-in result-db [:todos list-id scope todo-id-2 :order]) 1)))))

(deftest test-get-completed-today
  (testing "gets the completed items scoped for today"
    (let [list-id (random-uuid)

          todo-id-1 (random-uuid)
          todo-1 {:id todo-id-1
                  :list-id list-id
                  :scope :today
                  :label "A todo"
                  :order 1}

          todo-id-2 (random-uuid)
          todo-2 {:id todo-id-2
                  :list-id list-id
                  :scope :yesterday
                  :label "A second todo"
                  :order 1
                  :done true}

          todo-id-3 (random-uuid)
          todo-3 {:id todo-id-3
                  :list-id list-id
                  :scope :today
                  :label "A third todo"
                  :order 2
                  :done true}

          db {:active-todo-list [todo-1 todo-2]
              :active-user-list list-id
              :todos {list-id {:today {todo-id-1 todo-1
                                       todo-id-3 todo-3}
                               :yesterday {todo-id-3 todo-3}}}}

          result (get-completed-today db)]
      (is (= result [todo-3])))))
