(ns yaas-essentials.utils
  (:require
    [clojure.string :as str]
    [cognitect.transit :as t]
    ))

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

(defn url-encode-component [s]
  "urlencode"
  (js/encodeURIComponent (as-str s)))

(defn url-encode
  "Turn a map of parameters into a urlencoded string."
  [params]
  (str/join "&"
            (for [[k v] params]
              (str (url-encode-component k) "=" (url-encode-component v)))))

;; (str/join " " (map str ["foo" "bar" 1 2 3 {:foo :baz}]))

(defn to-json [m]
  (let [w (t/writer :json-verbose)]
    (t/write w m)
    )
  )

(defn log [msg]
  (.log js/console (str msg))
  )