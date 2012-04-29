(ns crate.def-macros)

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
  `(do
    (declare ~name)
    (let [func# (fn ~@fdecl)]
      (def ~name (crate.compiler/add-optional-attrs func#)))))
