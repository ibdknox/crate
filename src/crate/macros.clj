(ns crate.macros)

(defmacro defpartial
  [name params & body]
  `(let [group# (swap! crate.core/group-id inc)]
     (defn ^{:crateGroup group#}
       ~name ~params
       (let [elem# (crate.core/html ~@body)]
         (.setAttribute elem# "crateGroup" group#)
         elem#))
     (set! (.-prototype._crateGroup ~name) group#)))

(defmacro defhtml
  [name params & body]
  `(let [group# (swap! crate.core/group-id inc)]
     (defn ~name ~params
       (crate.core/html ~@body))))

(defmacro defelem
  "Defines a function that will return a tag vector. If the first argument
  passed to the resulting function is a map, it merges it with the attribute
  map of the returned tag value."
  [name & fdecl]
  `(let [func# (fn ~@fdecl)]
    (def ~name (crate.core/add-optional-attrs func#))))

(defmacro with-base-url
  "Add a base-url that will be added to the output of the resolve-uri function."
  [base-url & body]
  `(binding [crate.core/*base-url* ~base-url]
     ~@body))

(defmacro with-group
  "Group together a set of related form fields for use with the Ring
  nested-params middleware."
  [group & body]
  `(binding [crate.form-helpers/*group* (conj crate.form-helpers/*group* (crate.core/as-str ~group))]
     (list ~@body)))
