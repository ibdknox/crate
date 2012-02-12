(ns crate.page-helpers
  (:use [crate.core :only [resolve-uri as-str]])
  (:use-macros [crate.macros :only [defelem]]))

(defn include-js
  "Include a list of external javascript files."
  [& scripts]
  (for [script scripts]
    [:script {:type "text/javascript", :src (resolve-uri script)}]))

(defn include-css
  "Include a list of external stylesheet files."
  [& styles]
  (for [style styles]
    [:link {:type "text/css", :href (resolve-uri style), :rel "stylesheet"}]))

(defn javascript-tag
  "Wrap the supplied javascript up in script tags and a CDATA section."
  [script]
  [:script {:type "text/javascript"}
    (str "//<![CDATA[\n" script "\n//]]>")])

(defelem link-to
  [url & content]
  [:a {:href (resolve-uri url)} content])

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
  ([src]     [:img {:src (resolve-uri src)}])
  ([src alt] [:img {:src (resolve-uri src), :alt alt}]))

(defn encode [s]
  "urlencode"
  (js/encodeURIComponent (as-str s)))

(defn encode-params
  "Turn a map of parameters into a urlencoded string."
  [params]
  (str/join "&"
    (for [[k v] params]
      (str (encode k) "=" (encode v)))))

(defn url
  "Creates a URL string from a variable list of arguments and an optional
  parameter map as the last argument. For example:
    (url \"/group/\" 4 \"/products\" {:page 9})
    => \"/group/4/products?page=9\""
  [& args]
  (let [params (last args)
        args   (butlast args)]
    (str
      (resolve-uri
        (str (apply str args)
             (if (map? params)
               (str "?" (encode-params params))
               params))))))
