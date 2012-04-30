(ns crate.core
  (:require [goog.dom :as gdom]
            [clojure.string :as string]))

(def xmlns {:xhtml "http://www.w3.org/1999/xhtml"
            :svg "http://www.w3.org/2000/svg"})

;; ********************************************
;; Element creation via Hiccup-like vectors
;; ********************************************

(declare elem-factory)
(declare as-content)

(def group-id (atom 0))

(defn dom-attr
  ([elem attrs]
   (when elem
     (if-not (map? attrs)
       (. elem (getAttribute (name attrs)))
       (do
         (doseq [[k v] attrs]
           (dom-attr elem k v))
         elem))))
  ([elem k v]
   (. elem (setAttribute (name k) v))
   elem))

(defmulti -as-content
  (fn [content parent]
    (cond
     (nil? content) nil
     (map? content) :map
     (vector? content) :vector
     (seq? content) :seq
     (.-nodeName content) :dom
     :else (type content))))

(defmethod -as-content nil [_ _] nil)

(defmethod -as-content :map [c _]
  (throw "Maps cannot be used as content"))

(defmethod -as-content js/String [c _]
  (gdom/createTextNode c))

(defmethod -as-content :vector [c _]
  (elem-factory c))

(defmethod -as-content :seq [c parent]
  (as-content parent c))

(defmethod -as-content :dom [c _] c)

(defn as-content [parent content]
  (doseq [c content]
    (when-let [child (-as-content c parent)]
      (gdom/appendChild parent child))))

;; From Weavejester's Hiccup: https://github.com/weavejester/hiccup/blob/master/src/hiccup/core.clj#L57
(def ^{:doc "Regular expression that parses a CSS-style id and class from a tag name." :private true}
  re-tag #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

(defn- normalize-element
  "Ensure a tag vector is of the form [tag-name attrs content]."
  [[tag & content]]
  (when (not (or (keyword? tag) (symbol? tag) (string? tag)))
    (throw (str tag " is not a valid tag name.")))
  (let [[_ tag id class] (re-matches re-tag (name tag))
        [nsp tag]     (let [[nsp t] (string/split tag #":")
                               ns-xmlns (xmlns (keyword nsp))]
                           (if t
                             [(or ns-xmlns nsp) t]
                             [(:xhtml xmlns) nsp]))
        tag-attrs        (into {}
                               (filter #(not (nil? (second %)))
                                       {:id (or id nil)
                                        :class (if class (string/replace class #"\." " "))}))
        map-attrs        (first content)]
    (if (map? map-attrs)
      [nsp tag (merge tag-attrs map-attrs) (next content)]
      [nsp tag tag-attrs content])))

(defn parse-content [elem content]
  (let [attrs (first content)]
  (if (map? attrs)
    (do
      (dom-attr elem attrs)
      (rest content))
    content)))

(def create-elem (if (.-createElementNS js/document)
                   (fn [nsp tag]
                     (.createElementNS js/document nsp tag))
                   (fn [_ tag]
                     (.createElement js/document tag))))

(defn elem-factory [tag-def]
  (let [[nsp tag attrs content] (normalize-element tag-def)
        elem (create-elem nsp tag)]
    (dom-attr elem attrs)
    (as-content elem content)
    elem))

(defn html [& tags]
  (let [res (map elem-factory tags)]
    (if (second res)
      res
      (first res))))

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

(defn as-str
  ([] "")
  ([x]
    ; TODO: Maybe use something like (satisfies? INamed x) instead?
    (if (or (symbol? x) (keyword? x))
      (name x)
      (str x)))
  ([x & xs]
    ((fn [s more]
       (if more
         (recur (str s (as-str (first more))) (next more))
         s))
     (as-str x) xs)))

(def ^:dynamic *html-mode* :xml)

(defn escape-html
  "Change special characters into HTML character entities."
  [text]
  (.. (as-str text)
    (replace "&"  "&amp;")
    (replace "<"  "&lt;")
    (replace ">"  "&gt;")
    (replace "\"" "&quot;")))

(def ^:dynamic *base-url* nil)

(defn resolve-uri
  "Prepends the base-url to the supplied URI."
  [uri]
  (if (re-matches #"^\w+:.*" uri)
    uri
    (str *base-url* uri)))
