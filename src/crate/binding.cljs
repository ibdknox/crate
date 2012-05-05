(ns crate.binding)

(let [id (atom 0)]
  (defn watch-id []
    (keyword (str "binding" (swap! id inc)))))

(defprotocol bindable
  (-value [this] "get the current value of this binding")
  (-on-change [this func] "On change of this binding execute func"))

(deftype atom-binding [atm value-func]
  bindable
  (-value [this] (value-func @atm))
  (-on-change [this func]
    (add-watch atm (watch-id) #(func (-value this)))))

(defn binding? [b]
  (satisfies? bindable b))

(defn value [b]
  (-value b))

(defn on-change [b func]
  (-on-change b func))

(defn bound [atm func]
  (atom-binding. atm func))
