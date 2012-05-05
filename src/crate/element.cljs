(ns crate.element
  (:use [crate.util :only [to-uri]])
  (:use-macros [crate.def-macros :only [defelem]])
  (:require
    ; Must require crate.compiler for defelem to work.
    [crate.compiler :as compiler]
    [clojure.string :as str]))

(defn javascript-tag
  "Wrap the supplied javascript up in script tags and a CDATA section."
  [script]
  [:script {:type "text/javascript"}
    (str "//<![CDATA[\n" script "\n//]]>")])

(defelem link-to
  [url & content]
  [:a {:href (to-uri url)} content])

(defelem mail-to
  [e-mail & [content]]
  [:a {:href (str "mailto:" e-mail)}
   (or content e-mail)])

(defelem unordered-list
  [coll]
  [:ul (for [x coll] [:li x])])

(defelem ordered-list
  [coll]
  [:ol (for [x coll] [:li x])])

(defelem image
  ([src]     [:img {:src (to-uri src)}])
  ([src alt] [:img {:src (to-uri src), :alt alt}]))
