(ns crate.form-macros)

(defmacro with-group
  "Group together a set of related form fields for use with the Ring
  nested-params middleware."
  [group & body]
  `(binding [crate.form/*group* (conj crate.form/*group* (crate.util/as-str ~group))]
     (list ~@body)))
