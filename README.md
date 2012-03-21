# crate

Crate is a ClojureScript implementation of the awesome [Hiccup](https://github.com/weavejester/hiccup/) html templating library.

## Usage

```clojure
(ns myapp
 (:use-macros [crate.def-macros :only [defpartial]])
 (:require [crate.core :as crate]))

(crate/html [:p.woot {:id "blah"} "Hey!"])
=> <p class="woot" id="blah">Hey!</p>

(defpartial header []
 [:header
   [:h1 "My app!"]])

(header)
=> <header><h1>My app!</h1></header>

```

## License

Copyright (C) 2011 Chris Granger

Distributed under the Eclipse Public License, the same as Clojure.
