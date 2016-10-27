(ns yaas-essentials.product
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [yaas-essentials.utils :refer [log to-json]]
    [yaas-essentials.network :as ynet]
    [cljs.core.async :refer [chan <! >!] ]
    )
  )

(def tenant "may18sapphire") ;; put YOUR tenant here

(def product-details-config {:base_url (str "https://api.yaas.io/hybris/productdetails/v2/" tenant "/productdetails/")
                             :scopes   ["hybris.product_read_unpublished"]})

(def product-config {:base_url (str "https://api.yaas.io/hybris/product/v2/" tenant "/products/")
                     :scopes   ["hybris.product_read_unpublished" "hybris.product_create" "hybris.product_publish"
                                "hybris.product_update" "hybris.product_delete"]})

(defn get-products
  ([reply-chan]
   (get-products {} reply-chan)
    )
  ([params reply-chan]
   (ynet/yget (product-details-config :base_url) {} (product-details-config :scopes) params reply-chan)
    )
  )

(defn get-product
  ([id reply-chan]
   (get-product id {} reply-chan)
    )
  ([id params reply-chan]
   (ynet/yget (str (product-details-config :base_url) id) {} (product-details-config :scopes) params reply-chan)
    )
  )

(defn create-product [product language reply-chan]
  (ynet/ypost (product-config :base_url) product {"Content-Language" language} (product-config :scopes) reply-chan)
  )

(defn update-product [id product language reply-chan]
  (let [url (str (product-config :base_url) id) scopes (product-config :scopes) params {:partial true}]
    (ynet/yput url product {"Content-Language" language} scopes params reply-chan)
    )
  )

(defn delete-product [id reply-chan]
  (let [url (str (product-config :base_url) id) scopes (product-config :scopes)]
      (ynet/ydelete url {} scopes {} reply-chan)
    )
  )