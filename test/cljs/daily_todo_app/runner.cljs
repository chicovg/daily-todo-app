(ns daily-todo-app.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [daily-todo-app.core-test]
              [daily-todo-app.events-test]))

(doo-tests 'daily-todo-app.core-test
           'daily-todo-app.events-test)
