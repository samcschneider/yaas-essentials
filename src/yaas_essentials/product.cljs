(ns yaas-essentials.product
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [reagent.core :as reagent]
    [yaas-essentials.utils :as utils :refer [log to-json]]
    [yaas-essentials.network :as ynet]
    [cljs.core.async :refer [chan <! >!] ]
    )
  )

(defonce product (reagent/atom {:response "[nil]"}))

(defonce product-create (reagent/atom {:response "[nil]"}))

(defonce product-detail (reagent/atom {:response "[nil]"}))

(def tenant "may18sapphire") ;; put YOUR tenant here

(def product-details-config {:base_url (str "https://api.yaas.io/hybris/productdetails/v1/" tenant "/productdetails/")
                             :scopes   ["hybris.product_read_unpublished"]})

(def product-config {:base_url (str "https://api.yaas.io/hybris/product/v1/" tenant "/products/")
                     :scopes   ["hybris.product_read_unpublished" "hybris.product_create" "hybris.product_publish"]})

(def products-chan (chan))
(def product-detail-chan (chan))
(def product-create-chan (chan))

(defn format-error [response]
  (str "Error returned from server " (:status response) " " (get-in response [:body :message]))
  )

(defn products-event-loop []
  (go-loop []
     (when-let [response (<! products-chan)]
        (log "received data on products channel")
        (log response)
        (if (= (:status response) 200)
          (swap! product merge @product {:response response})
          (swap! product merge @product {:response (format-error response)})
          )
        (recur)
        )
     )
  )

(defn product-detail-event-loop []
  (go-loop []
           (when-let [response (<! product-detail-chan)]
             (log "received data on product detail channel")
             (log response)
             (if (= (:status response) 200)
               (swap! product-detail merge @product-detail {:response response})
               (swap! product merge product-detail {:response (format-error response)})
               )
             (recur)
             )
           )
  )

(defn product-create-event-loop []
  (go-loop []
           (when-let [response (<! product-create-chan)]
             (log "received data on product create channel")
             (log response)
             (if (or (= (:status response) 200) (= (:status response) 201))
               (swap! product-create merge @product-create {:response response})
               (swap! product-create merge @product-create {:response (format-error response)})
               )
             (recur)
             )
           )
  )

(defn event-loop-setup[]
  (products-event-loop)
  (product-detail-event-loop)
  (product-create-event-loop)
  )

(defn get-products
  ([]
   (ynet/yget (product-details-config :base_url) {} (product-details-config :scopes) products-chan)
  )
  ([id]
   (ynet/yget (str (product-details-config :base_url) id) {} (product-details-config :scopes) product-detail-chan)
    )
  )

(defn create-product [product]
  (ynet/ypost (product-config :base_url) product {} (product-config :scopes) product-create-chan)
  )
