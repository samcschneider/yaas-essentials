(ns yaas-essentials.product
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [reagent.core :as reagent]
    [yaas-essentials.utils :as utils :refer [log to-json]]
    [yaas-essentials.network :as ynet]
    [cljs.core.async :refer [chan <! >!] ]
    )
  )



(def tenant "may18sapphire") ;; put YOUR tenant here

(def product-details-config {:base_url (str "https://api.yaas.io/hybris/productdetails/v1/" tenant "/productdetails/")
                             :scopes   ["hybris.product_read_unpublished"]})

(def product-config {:base_url (str "https://api.yaas.io/hybris/product/v1/" tenant "/products/")
                     :scopes   ["hybris.product_read_unpublished" "hybris.product_create" "hybris.product_publish"]})



(defn get-products
  ([reply-chan]
   (ynet/yget (product-details-config :base_url) {} (product-details-config :scopes) reply-chan)
  )
  ([id reply-chan]
   (ynet/yget (str (product-details-config :base_url) id) {} (product-details-config :scopes) reply-chan)
    )
  )

(defn create-product [product reply-chan]
  (ynet/ypost (product-config :base_url) product {} (product-config :scopes) reply-chan)
  )
