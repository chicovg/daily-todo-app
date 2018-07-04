(ns daily-todo-app.views
  (:require [re-frame.core
               :refer [subscribe dispatch-sync]]
            [re-com.core
               :refer [box
                       v-box
                       h-box
                       vertical-pill-tabs
                       horizontal-bar-tabs
                       modal-panel
                       title
                       label
                       checkbox
                       button
                       row-button
                       md-icon-button
                       md-circle-icon-button
                       hyperlink-href]]
            [re-com.util :refer [enumerate]]
            [reagent.core :as r]
            [cljs-time.format :refer [formatter unparse]]
            [clojure.string :as str]
            [daily-todo-app.events :as events]
            [daily-todo-app.subs :as subs]))

;; general views
(defn enter-text-input
  [{:keys [value placeholder on-save on-stop]}]
  (let [val (r/atom value)
        stop #(do (reset! val "")
                  (when on-stop (on-stop)))
        save #(let [v (-> @val str str/trim)]
                (if (not (str/blank? v)) (on-save v))
                (stop))]
    (fn [props]
      [:input (merge (dissoc props :value :placeholder :on-save :on-stop)
                     {:type        "text"
                      :value       @val
                      :placeholder placeholder
                      :on-blur     save
                      :on-change   #(reset! val (-> % .-target .-value))
                      :on-key-down #(case (.-which %)
                                      13 (save)
                                      27 (stop)
                                      nil)})])))
(def completed-format (formatter "MMM dd"))

(defn yesterday-todo-item
  []
  (fn [{:keys [id list-id scope title order completed]} mouse-over]
    (let [mouse-over-row? (identical? @mouse-over id)]
      [h-box
         :class "list-group-item"
         :gap "4px"
         :attr {:on-mouse-over #(reset! mouse-over id)
                :on-mouse-out #(reset! mouse-over nil)}
         :children [[box
                     :size "60px"
                     :child [label
                               :label (unparse
                                        completed-format
                                        completed)]]

                    [box
                     :size "auto"
                     :child [label :label title]]
                    [box
                     :size "20px"
                     :child [row-button
                               :md-icon-name "zmdi-long-arrow-right"
                               :tooltip "move back to today"
                               :disabled? false
                               :mouse-over-row? mouse-over-row?
                               :on-click #(dispatch-sync
                                            [::events/update-todo {:list-id list-id
                                                                   :scope scope
                                                                   :id id}
                                                                  {:scope :today
                                                                   :done false
                                                                   :completed nil}])]]]])))



(defn todo-entry
  []
  (let [active-user-list-id (subscribe [::subs/active-user-list])
        active-scope (subscribe [::subs/active-scope])]
    (fn []
      [h-box
        :children [[enter-text-input
                     {:id "enter-todo"
                      :value ""
                      :placeholder "Enter a new todo..."
                      :on-save #(dispatch-sync
                                  [::events/add-todo {:list-id @active-user-list-id
                                                      :scope @active-scope
                                                      :title %}])}]]])))

(defn todo-item
  []
  (let [editing (r/atom false)]
    (fn [{:keys [list-id scope id title done order]} mouse-over first? last?]
      (let [mouse-over-row? (identical? @mouse-over id)]
        [h-box
         :class "list-group-item"
         :gap "4px"
         :attr {:on-mouse-over #(reset! mouse-over id)
                :on-mouse-out #(reset! mouse-over nil)}
         :children [[h-box
                       :gap "1px"
                       :size "50px"
                       :children [[row-button
                                     :md-icon-name "zmdi-long-arrow-up"
                                     :tooltip "move up"
                                     :disabled? (and first? mouse-over-row?)
                                     :mouse-over-row? mouse-over-row?
                                     :on-click #(dispatch-sync
                                                  [::events/move-todo order (dec order)])]
                                  [row-button
                                     :md-icon-name "zmdi-long-arrow-down"
                                     :tooltip "move down"
                                     :disabled? (and last? mouse-over-row?)
                                     :mouse-over-row? mouse-over-row?
                                     :on-click #(dispatch-sync
                                                  [::events/move-todo order (inc order)])]
                                  [checkbox
                                     :model done
                                     :on-change #(dispatch-sync
                                                   [::events/update-todo {:list-id list-id
                                                                          :scope scope
                                                                          :id id}
                                                                         {:done %}])]]]
                    [box
                       :size "auto"
                       :child (if @editing
                                [enter-text-input
                                   {:value title
                                    :placeholder "Enter a new value..."
                                    :on-save #(dispatch-sync
                                                [::events/update-todo {:list-id list-id
                                                                       :scope scope
                                                                       :id id}
                                                                      {:title %}])
                                    :on-stop #(reset! editing false)}]
                                [label
                                   :label title
                                   :attr {:on-double-click #(reset! editing true)}])]
                    [h-box
                       :gap "1px"
                       :size "30px"
                       :children [[row-button
                                     :md-icon-name "zmdi-edit"
                                     :tooltip "edit todo"
                                     :disabled? false
                                     :mouse-over-row? mouse-over-row?
                                     :on-click #(reset! editing true)]
                                  [row-button
                                     :md-icon-name "zmdi-delete"
                                     :tooltip "delete todo"
                                     :disabled? false
                                     :mouse-over-row? mouse-over-row?
                                     :on-click #(dispatch-sync
                                                  [::events/delete-todo {:list-id list-id
                                                                         :scope scope
                                                                         :id id}])]]]]]))))

(defn todo-list
  []
  (let [todo-list (subscribe [::subs/active-todo-list])
        active-scope (subscribe [::subs/active-scope])
        mouse-over (r/atom nil)]
    (fn []
      [v-box
        :children (for [[_ todo first? last?] (enumerate @todo-list)]
                    ^{:key (:id todo)}
                    (if (= :yesterday @active-scope)
                      [yesterday-todo-item todo mouse-over]
                      [todo-item todo mouse-over first? last?]))])))

(defn list-scopes
  []
  (let [active-scope (subscribe [::subs/active-scope])]
    [horizontal-bar-tabs
     :model @active-scope
     :tabs [{:id :yesterday :label "Yesterday"}
            {:id :today :label "Today"}
            {:id :tomorrow :label "Tomorrow"}]
     :on-change #(dispatch-sync [::events/set-active-scope %])]))

(defn advance-day-button
  []
  [button
   :class "btn btn-success"
   :label "Advance Day"
   :on-click #(dispatch-sync [::events/advance-day])])

(defn control-bar
  []
  [h-box
     :gap "4px"
     :children [[list-scopes]
                [advance-day-button]]])

(defn app-title
  []
  [title
    :level :level2
    :label "daily todo"
    :underline? true?])

; to do panel
(defn todo-panel
  []
  [v-box
     :class "center"
     :gap "8px"
     :children [[app-title]
                [control-bar]
                [todo-entry]
                [todo-list]]])

(defn sign-in-buttons
  []
  (let [user (subscribe [::subs/user])]
    [v-box
       :children [[label :label (str "Hello " (:display-name @user) "!")]
                  [button
                     :class "btn"
                     :label "Sign In"
                     :on-click #(dispatch-sync [::events/sign-in])]
                  [button
                     :class "btn"
                     :label "Sign Out"
                     :on-click #(dispatch-sync [::events/sign-out])]]]))

; home panel
(defn home-panel
  []
  [v-box
     :class "center"
     :gap "8px"
     :children [[app-title]
                [sign-in-buttons]]])


;; about
(defn about-title []
  [title
   :label "This is the About Page."
   :level :level1])

(defn link-to-home-page []
  [hyperlink-href
   :label "go to Home Page"
   :href "#/"])

(defn about-panel []
  [v-box
   :gap "1em"
   :children [[about-title] [link-to-home-page]]])

;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :todo-panel [todo-panel]
    :about-panel [about-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (subscribe [::subs/active-panel])]
    [v-box
       :height "100%"
       :children [[panels @active-panel]]]))
