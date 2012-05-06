(ns crate.binding
  (:require [clojure.set :as set]))

(let [id (atom 0)]
  (defn watch-id []
    (keyword (str "binding" (swap! id inc)))))

(defprotocol bindable-coll)

(defprotocol bindable
  (-value [this] "get the current value of this binding")
  (-on-change [this func] "On change of this binding execute func"))

(deftype atom-binding [atm value-func]
  bindable
  (-value [this] (value-func @atm))
  (-on-change [this func]
    (add-watch atm (watch-id) #(func (-value this)))))

(deftype notifier-binding [notif value-func]
  bindable
  (-value [this] nil)
  (-on-change [this func]
    (add-watch notif (watch-id) (fn [_ _ _ v] (func (value-func v))))))

(deftype notifier [watches]
  bindable
  (-value [this] nil)
  (-on-change [this func]
    (add-watch this (watch-id) (fn [_ _ _ v] (func v))))

  ILookup
  (-lookup [o k] (notifier. {}))
  (-lookup [o k not-found] not-found)

  IHash
  (-hash [t] nil)

  IWatchable
  (-notify-watches [this oldval newval]
    (doseq [[key f] watches]
      (f key this nil newval)))
  (-add-watch [this key f]
    (set! (.-watches this) (assoc watches key f)))
  (-remove-watch [this key]
    (set! (.-watches this) (dissoc watches key))))

(deftype bound-collection [notif hash opts stuff]
  bindable-coll
  bindable
  (-value [this] (map :elem (vals (.-stuff this))))
  (-on-change [this func]
    (add-watch notif (watch-id) (fn [_ _ _ [event el v]]
                                  (func event el v)))))

(defn- bc-add [bc key {:keys [value hash]}]
  (let [notif (notifier. nil)
        elem ((opt bc :as) notif)]
    (set! (.-stuff bc) (assoc (.-stuff bc) key {:hash hash
                                                :elem elem
                                                :notif notif}))
    (notify notif value)
    (notify (.-notif bc) [:add elem value])))

(defn- bc-remove [bc key]
  (let [notif (.-notif bc)
        prev  ((.-stuff bc) key)]
    (set! (.-stuff bc) (dissoc (.-stuff bc) key))
    (notify (.-notif bc) [:remove (:elem prev) nil])) )

(defn- bc-change [bc key {:keys [hash value]}]
  (let [prev ((.-stuff bc) key)]
    (set! (.-stuff bc) (assoc (.-stuff bc) key (assoc prev :hash hash)))
    (notify (:notif prev) value)))

(defn opt [bc k]
  ((.-opts bc) k))

(defn ->keyed [coll keyfn]
  (reduce
    (fn [res v]
      (assoc res (keyfn v) {:value v
                            :hash (hash v)}))
    {}
    coll))

(defn- bc-compare [bc neue keyfn]
  (let [prev (.-stuff bc)
        nkeyed (->keyed neue keyfn)
        pset (into #{} (keys prev))
        nset (into #{} (keys nkeyed))
        added (set/difference nset pset)
        removed (set/difference pset nset)
        changed? (set/intersection pset nset)]
    (doseq [a added]
      (bc-add bc a (nkeyed a)))
    (doseq [r removed]
      (bc-remove bc r))
    (doseq [c changed?]
      (let [latest (nkeyed c)
            old (prev c)]
        (when-not (= (:hash latest) (:hash old))
          (bc-change bc c latest))))
    (set! (.-hash bc) (hash neue))))

(defn notify [notif v]
  (-notify-watches notif nil v))

(defn from-path [atm path]
  (let [v (cond
            (satisfies? IDeref atm) @atm
            :else atm)]
    (if-not path
      v
      (path v))))

(defn notifier? [n]
  (instance? crate.binding.notifier n))

(defn bound-coll [atm & [path opts]]
  (let [[path opts] (if opts
                      [path opts]
                      [nil path])
        keyfn (or (:keyfn opts) hash)
        bc (bound-collection. (notifier. nil) nil (or opts {}) {})]
    (add-watch atm (watch-id) (fn [_ _ _ v]
                                (let [neue (from-path v path)
                                      neue-hash (hash neue)]
                                  (when-not (= neue-hash (.-hash bc))
                                    (set! (.-hash bc) neue-hash)
                                    (bc-compare bc neue keyfn)))))
    (when-not (notifier? atm)
      (bc-compare bc (from-path atm path) keyfn))
    bc))


(defn binding? [b]
  (satisfies? bindable b))

(defn binding-coll? [b]
  (satisfies? bindable-coll b))

(defn value [b]
  (-value b))

(defn on-change [b func]
  (-on-change b func))

(defn bound [atm & [func]]
  (let [func (or func identity)]
    (if-not (binding? atm)
      (atom-binding. atm func {})
      (notifier-binding. atm func))))

(comment

  stuff {:elem el
         :hash h
         :notif n}

  (def x (atom [{:name "chris"}
                {:name "john"}]))

  (defpartial named [n]
    [:li [:p (bound n :name)]])

  [:ul (bound-collection x {:as named})]

  )
