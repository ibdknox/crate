(ns crate.core
  (:require
    [crate.compiler :as compiler]
    [crate.util :as util]))

(defn html [& tags]
  (let [res (map compiler/elem-factory tags)]
    (if (second res)
      res
      (first res))))

(def ^ {:doc "Alias for crate.util/escape-html"}
  h util/escape-html)
