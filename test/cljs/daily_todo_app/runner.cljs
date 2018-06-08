(ns daily-todo-app.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [daily-todo-app.core-test]))

(doo-tests 'daily-todo-app.core-test)
