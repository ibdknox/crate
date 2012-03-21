(ns crate.util-macros)

(defmacro with-base-url
  "Add a base-url that will be added to the output of the to-uri function."
  [base-url & body]
  `(binding [crate.util/*base-url* ~base-url]
     ~@body))
