(ns crate.tags
  (:require-macros [crate.macros :as crate]))

;; From Weavejester's Hiccup: https://github.com/weavejester/hiccup/blob/master/src/hiccup/core.clj#L284
(defn add-optional-attrs
  "Add an optional attribute argument to a function that returns a vector tag."
  [func]
  (fn [& args]
    (if (map? (first args))
      (let [[tag & body] (apply func (rest args))]
        (if (map? (first body))
          (apply vector tag (merge (first body) (first args)) (rest body))
          (apply vector tag (first args) body)))
      (apply func args))))

(crate/defelem form-to [[method action] & content]
  [:form {:method (name method)
          :action action}
   content])

(crate/defelem input-field [tpe nme value]
  [:input {:type tpe
           :name (or nme nil)
           :id (or nme nil)
           :value (or value "")}])

(crate/defelem text-field [nme value]
  (input-field "text" nme value))

(crate/defelem password-field [nme value]
  (input-field "password" nme value))

(crate/defelem label [for text]
  [:label {:for for} text])

(crate/defelem submit-button [val]
  (input-field "submit" nil val))

(crate/defelem link-to [url & content]
  [:a {:href url} content])

