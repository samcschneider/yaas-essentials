(ns yaas-essentials.core
  (:require
   [sablono.core :as sab :include-macros true]
   [reagent.core :as reagent]
   [cljs.core.async :refer [<! put! chan]]
   [yaas-essentials.network :as ynet]
   [yaas-essentials.product :as yproduct]
   )
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-rg]]
   [cljs.core.async.macros :refer [go]]
   ))

(enable-console-print!)

(defn on-click [ratom]
  (swap! ratom update-in [:count] inc)
  ;; use scopes for product details for reference...

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
   [:div "Token response: " (@ynet/token-state :response)]
   [:div "Token: " (@ynet/bearer (yproduct/product-details-config :scopes))]
   [:div
    [:button {:on-click #(on-click counter1-state)}
     "Get Token"]]])

(defn product-click []
  (yproduct/get-products)
  )

(defn products []
  [:div "Request count: " (@counter1-state :count)
   [:div "Product response: " (str (:response @yproduct/product))]
   [:div
    [:button {:on-click #(product-click)}
     "Get Products"]]])

(defonce product-id (reagent/atom ""))


(defn product-detail-click []
  (yproduct/get-products @product-id)
  )

(defn product-detail []

   [:div "Single product response: " (str (:response @yproduct/product-detail))
   [:div
    (row "Product ID" product-id)
    [:button {:on-click #(product-detail-click)}
     "Get Single Product"]]])

(defonce product-sku (reagent/atom ""))
(defonce product-name (reagent/atom ""))
(defonce product-description (reagent/atom ""))

(defn product-create-click[]
  (yproduct/create-product {"name" @product-name "description" @product-description "sku" @product-sku})
  )

(defn product-create []

  [:div "Create product response: " (str (:response @yproduct/product-create))
   [:div
     (row "SKU" product-sku)
     (row "Product Name" product-name)
     (row "Product Description" product-description)
    [:button {:on-click #(product-create-click)}
     "Create Product"]]])

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

            (def product-details-config {:base_url \"https://api.yaas.io/hybris/productdetails/v1/may18sapphire/productdetails/\"
                                         :scopes   [\"hybris.product_read_unpublished\"]})

            ```

            "
            [products]
            {:inspect-data true}
            )

(defcard-rg fetch-single-product
            "Get a **single** product
            ```clojure
            (defn auth-header [scopes] {\"Authorization\"  (str \"Bearer \" (@bearer scopes))})
            ```"
            [product-detail]
            {:inspect-data true}
            )

(defcard-rg create-product
            "Create a **new** product"
            [product-create]
            {:inspect-data true}
            )

(defn main []
  ;; conditionally start the app based on whether the #main-app-area
  ;; node is on the page
  (if-let [node (.getElementById js/document "main-app-area")]
    (.render js/ReactDOM (sab/html [:div "This is working"]) node))
  (yproduct/event-loop-setup)
  )

(main)

;; remember to run lein figwheel and then browse to
;; http://localhost:3449/cards.html

