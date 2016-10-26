(ns yaas-essentials.core
  (:require
   [sablono.core :as sab :include-macros true]
   [reagent.core :as reagent]
   [cljs.core.async :refer [<! put! chan]]
   [yaas-essentials.network :as ynet]
   [yaas-essentials.product :as yproduct]
   [yaas-essentials.product-ui-state :as yproduct-ui]
   [devcards.util.utils :as utils]
   )
  (:require-macros
   [devcards.core :refer [defcard deftest defcard-rg]]
   [cljs.core.async.macros :refer [go]]
   ))

(enable-console-print!)

(defn on-click [ratom]
  (swap! ratom update-in [:count] inc)
  (ynet/renew-token (yproduct/product-details-config :scopes) #())
  )

(defn input [ratom]
  [:input {:type "text"
           :value @ratom
           :on-change #(reset! ratom (-> % .-target .-value))}])

(defn row [label ratom]
  "Creates input elements bound to the atom specified"
  [:div.row
   [:div.col-md-2 [:label label]]
   [:div.col-md-5 (input ratom)] [:span @ratom]])

(defonce counter1-state (reagent/atom {:count 0}))

(defn access-token []
  [:div "Request count: " (@counter1-state :count)
   [:div "Token response: " [:textarea {:value (utils/pprint-str (@ynet/token-state :response)) :cols 120 :rows 15}]]
   [:div "Token: " (@ynet/bearer (yproduct/product-details-config :scopes))]
   [:div
    [:button {:on-click #(on-click counter1-state)}
     "Get Token"]]])

(defn product-click []
  (yproduct/get-products yproduct-ui/products-chan)
  )

(defn products []
  [:div "Request count: " (@counter1-state :count)
   [:div "Product response: " [:textarea {:value (utils/pprint-str (:response @yproduct-ui/product)) :cols 120 :rows 15}]]
   [:div
    [:button {:on-click #(product-click)}
     "Get Products"]]])

(defonce product-id (reagent/atom ""))


(defn product-detail-click []
  (yproduct/get-products @product-id yproduct-ui/product-detail-chan)
  )

(defn product-detail []

   [:div "Single product response: " [:textarea { :value (utils/pprint-str (:response @yproduct-ui/product-detail)) :cols 120 :rows 15}]
   [:div
    (row "Product ID" product-id)
    [:button {:on-click #(product-detail-click)}
     "Get Single Product"]]])

(defonce update-product-id (reagent/atom ""))
(defonce update-product-sku (reagent/atom ""))
(defonce update-product-name (reagent/atom ""))
(defonce update-product-description (reagent/atom ""))

(defn product-update-click[]
  (yproduct/update-product @update-product-id {"name" @update-product-name "description" @update-product-description "sku" @update-product-sku} yproduct-ui/product-update-chan)
  )

(defn product-update []

  [:div "Update product response: " [:textarea {:value (utils/pprint-str (:response @yproduct-ui/product-update)) :cols 120 :rows 15}]
   [:div
    (row "ID" update-product-id)
    (row "SKU" update-product-sku)
     (row "Product Name" update-product-name)
     (row "Product Description" update-product-description)
    [:button {:on-click #(product-update-click)}
     "Update Product"]]])


(defonce product-sku (reagent/atom ""))
(defonce product-name (reagent/atom ""))
(defonce product-description (reagent/atom ""))

(defn product-create-click[]
  (yproduct/create-product {"name" @product-name "description" @product-description "sku" @product-sku} yproduct-ui/product-create-chan)
  )

(defn product-create []

  [:div "Create product response: " [:textarea {:value (utils/pprint-str (:response @yproduct-ui/product-create)) :cols 120 :rows 15}]
   [:div
    (row "SKU" product-sku)
    (row "Product Name" product-name)
    (row "Product Description" product-description)
    [:button {:on-click #(product-create-click)}
     "Create Product"]]])

(defonce delete-product-id (reagent/atom ""))

(defn product-delete-click[]
  (yproduct/delete-product @delete-product-id yproduct-ui/product-delete-chan)
  )

(defn product-delete []

  [:div "Update delete response: " [:textarea {:value (utils/pprint-str (:response @yproduct-ui/product-delete)) :cols 120 :rows 15}]
   [:div
    (row "ID" delete-product-id)
    [:button {:on-click #(product-delete-click)}
     "Delete Product"]]])

(defcard-rg getting-an-access-token
            "Acquire access token from YaaS

            ```clojure
            (def default-env {:client_id \"Your client ID\" :client_secret \"your client secret\"})

            (def token-url \"https://api.yaas.io/hybris/oauth2/v1/token\")

            (def bearer (reagent/atom {:bearer \"[]\"}))

            (defn renew-token [scopes retry-fn]

              (go (let [response (<! (http/post token-url
                {:form-params (merge default-env {:grant_type \"client_credentials\"
                :scope (str/join \" \" scopes)}) :with-credentials? false           }))]
                        (swap! bearer merge @bearer {scopes (:access_token (:body response))})
                        (js/setTimeout retry-fn 200)
                     )
                  )
            )
            ```
            "
            [access-token])

(defcard-rg fetch-all-products
            "Fetch *all* products
            ```clojure
            (defn auth-header [scopes] {\"Authorization\"  (str \"Bearer \" (@bearer scopes))})

            (def tenant \"may18sapphire\") ;; put YOUR tenant here

            (def product-details-config {:base_url (str \"https://api.yaas.io/hybris/productdetails/v1/\" tenant \"/productdetails/\")
                             :scopes   [\"hybris.product_read_unpublished\"]})

            (def products-chan (chan))

            (defn products-event-loop []
              (go-loop []
                (when-let [response (<! products-chan)]
                  (log \"received data on products channel\")
                  (log response)
                  (if (= (:status response) 200)
                    (swap! product merge @product {:response response})
                    (swap! product merge @product {:response (format-error response)})
                  )
                  (recur)
                )
              )
            )

            (defn get-products ([]
              (ynet/yget (product-details-config :base_url) {} (product-details-config :scopes) products-chan)
              )

              ([id]
              (ynet/yget (str (product-details-config :base_url) id) {} (product-details-config :scopes) product-detail-chan)
            )

            (defn yrequest ([scopes request-fn reply-chan]
              (yrequest scopes request-fn reply-chan 0)
              )

              ([scopes request-fn reply-chan tries]
                (go (let [response (<! (request-fn))]
                  (if (and (>= (:status response) 200) (< (:status response) 400)) ;allow 2xx and 3xx responses
                    (>! reply-chan response)
                    (if (or (= (:status response) 401) (= (:status response 503)))
                      (do
                        (log \"Renewing token...\")
                        (let [try-count (inc tries)]
                          (if (< try-count 3)
                            (renew-token scopes (fn [] (yrequest scopes request-fn reply-chan try-count)))
                            (>! reply-chan response)
                          )
                        )
                      )
                    (do
                      (log (str \"Error response or unrecoverable status from server\" response))
                      (>! reply-chan response)
                    )
                  )
                )
              )
            )))

            (defn yget [url headers scopes reply-chan]
              (yrequest scopes (fn[] (http/get url {:headers (merge headers (auth-header scopes)) :with-credentials? false})) reply-chan)
            )

            ```

            "
            [products]
            )

(defcard-rg fetch-single-product
            "Get a **single** product
            Uses same API (network layer as get all products)

            ```clojure
            (defn product-detail-event-loop []
              (go-loop []
                (when-let [response (<! product-detail-chan)]
                (log \"received data on product detail channel\")
                (log response)
                (if (= (:status response) 200)
                  (swap! product-detail merge @product-detail {:response response})
                  (swap! product merge product-detail {:response (format-error response)})
                )
                (recur)
              )
            )

            ```"
            [product-detail]
            )

(defcard-rg create-product
            "Create a **new** product

            ```clojure
            (defn product-create-event-loop []
              (go-loop []
                (when-let [response (<! product-create-chan)]
                  (log \"received data on product create channel\")
                  (log response)
                  (if (or (= (:status response) 200) (= (:status response) 201))
                    (swap! product-create merge @product-create {:response response})
                    (swap! product-create merge @product-create {:response (format-error response)})
                  )
                  (recur)
                )
              )
            )

            (defn product-create-click[]
              (yproduct/create-product {\"name\" @product-name \"description\" @product-description \"sku\" @product-sku})
            )

            (defn create-product [product]
              (ynet/ypost (product-config :base_url) product {} (product-config :scopes) product-create-chan)
              )
            ```
            "
            [product-create]
            )

(defcard-rg update-product
            "Updates an existing product

            "
            [product-update]
            )

(defcard-rg delete-product
            "Deletes an existing product

            "
            [product-delete]
            )

(defn main []
  ;; conditionally start the app based on whether the #main-app-area
  ;; node is on the page
  (if-let [node (.getElementById js/document "main-app-area")]
    (.render js/ReactDOM (sab/html [:div "This is working"]) node))
  (yproduct-ui/event-loop-setup)
  )

(main)

;; remember to run lein figwheel and then browse to
;; http://localhost:3449/cards.html

