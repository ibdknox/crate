(ns crate.macros)

(defmacro defpartial
  [name params & body]
  `(let [group# (swap! crate.core/group-id inc)]
     (defn ^{:crateGroup group#} 
       ~name ~params
       (.setAttribute 
         (crate.core/html
           ~@body)
         "crateGroup" 
         group#))
     (set! (.-prototype._crateGroup ~name) group#)))

(defmacro defelem
  "Defines a function that will return a tag vector. If the first argument
  passed to the resulting function is a map, it merges it with the attribute
  map of the returned tag value."
  [name & fdecl]
  `(let [func# (fn ~@fdecl)]
    (def ~name (crate.tags/add-optional-attrs func#))))
