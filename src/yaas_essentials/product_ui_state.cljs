(ns yaas-essentials.product-ui-state
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [reagent.core :as reagent]
    [yaas-essentials.utils :refer [log]]
    [cljs.core.async :refer [chan <! >!] ]
    )
  )

(defonce product (reagent/atom {:response ""}))
(defonce product-create (reagent/atom {:response ""}))
(defonce product-update (reagent/atom {:response ""}))
(defonce product-detail (reagent/atom {:response ""}))
(defonce product-delete (reagent/atom {:response ""}))

(def products-chan (chan))
(def product-detail-chan (chan))
(def product-create-chan (chan))
(def product-update-chan (chan))
(def product-delete-chan (chan))

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
               (swap! product-detail merge @product-detail {:response (format-error response)})
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

(defn product-update-event-loop []
  (go-loop []
           (when-let [response (<! product-update-chan)]
             (log "received data on product update channel")
             (log response)
             (if (= (:status response) 200)
               (swap! product-update merge @product-update {:response response})
               (swap! product-update merge @product-update {:response (format-error response)})
               )
             (recur)
             )
           )
  )

(defn product-delete-event-loop []
  (go-loop []
           (when-let [response (<! product-delete-chan)]
             (log "received data on product delete channel")
             (log response)
             (if (= (:status response) 204)
               (swap! product-delete merge @product-delete {:response response})
               (swap! product-delete merge @product-delete {:response (format-error response)})
               )
             (recur)
             )
           )
  )

(defn event-loop-setup []
  (products-event-loop)
  (product-detail-event-loop)
  (product-create-event-loop)
  (product-update-event-loop)
  (product-delete-event-loop)
  )