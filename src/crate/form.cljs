(ns crate.form
  (:use [crate.util :only [escape-html to-uri as-str]])
  (:use-macros [crate.def-macros :only [defelem]])
  ; Must require crate.compiler for defelem to work.
  (:require [crate.compiler :as compiler]))

(def ^:dynamic *group* [])

(defn- make-name
  "Create a field name from the supplied argument the current field group."
  [name]
  (reduce #(str %1 "[" %2 "]")
          (conj *group* (as-str name))))

(defn make-id
  "Create a field id from the supplied argument and current field group."
  [name]
  (reduce #(str %1 "-" %2)
          (conj *group* (as-str name))))

(defn- input-field
  "Creates a new <input> element."
  [type name value]
  [:input {:type  type
           :name  (make-name name)
           :id    (make-id name)
           :value value}])

(defelem hidden-field
  ([name] (hidden-field name nil))
  ([name value] (input-field "hidden" name value)))

(defelem text-field
  ([name] (text-field name nil))
  ([name value] (input-field "text" name value)))

(defelem password-field
  ([name] (password-field name nil))
  ([name value] (input-field "password" name value)))

(defelem email-field
  ([name] (email-field name nil))
  ([name value] (input-field "email" name value)))

(defelem check-box
  ([name] (check-box name nil))
  ([name checked?] (check-box name checked? "true"))
  ([name checked? value]
    [:input {:type "checkbox"
             :name (make-name name)
             :id   (make-id name)
             :value value
             :checked checked?}]))

(defelem radio-button
  ([group] (radio-button group nil))
  ([group checked?] (radio-button group checked? "true"))
  ([group checked? value]
    [:input {:type "radio"
             :name (make-name group)
             :id   (make-id (str (as-str group) "-" (as-str value)))
             :value value
             :checked checked?}]))

(defelem select-options
  ([coll] (select-options coll nil))
  ([coll selected]
    (for [x coll]
      (if (sequential? x)
        (let [[text val] x]
          [:option {:value val :selected (= val selected)} text])
        [:option {:selected (= x selected)} x]))))

(defelem drop-down
  ([name options] (drop-down name options nil))
  ([name options selected]
    [:select {:name (make-name name), :id (make-id name)}
      (select-options options selected)]))

(defelem text-area
  ([name] (text-area name nil))
  ([name value]
    [:textarea {:name (make-name name), :id (make-id name)}
      (escape-html value)]))

(defelem file-upload
  [name]
  (input-field "file" name nil))

(defelem label
  [name text]
  [:label {:for (make-id name)} text])

(defelem submit-button
  [text]
  [:input {:type "submit" :value text}])

(defelem reset-button
  [text]
  [:input {:type "reset" :value text}])

(defelem form-to
  [[method action] & body]
  (let [method-str (.toUpperCase (name method))
        action-uri (to-uri action)]
    (-> (if (contains? #{:get :post} method)
          [:form {:method method-str, :action action-uri}]
          [:form {:method "POST", :action action-uri}
            (hidden-field "_method" method-str)])
        (concat body)
        (vec))))
