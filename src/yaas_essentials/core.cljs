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

(defonce counter1-state (reagent/atom {:count 0}))

(defn access-token []
  [:div "Request count: " (@counter1-state :count)
   [:div "Token response: " (@ynet/token-state :response)]
   [:div "Token: " (@ynet/bearer :bearer)]
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

(defn product-detail-click []
  (yproduct/get-products "573c8cb7b3d043001d6b0998")
  )

(defn product-detail []

   [:div "Single product response: " (str (:response @yproduct/product-detail))
   [:div
    [:button {:on-click #(product-detail-click)}
     "Get Single Product"]]])

(defonce product-sku (reagent/atom "[sku]"))
(defonce product-name (reagent/atom "[name]"))
(defonce product-description (reagent/atom "[desc]"))

(defn product-create-click[]
  (yproduct/create-product {"name" @product-name "description" @product-description "sku" @product-sku})
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

(defn product-create []

  [:div "Create product response: " (str (:response @yproduct/product-create))
   [:div
     (row "SKU" product-sku)
     (row "Product Name" product-name)
     (row "Product Description" product-description)
    [:button {:on-click #(product-create-click)}
     "Create Product"]]])

(defcard-rg getting-an-access-token
            [access-token])

(defcard-rg fetch-all-products
            "*Some* _Documentation_ for Mr. Greg"
            [products]
            {:inspect-data true}
            )

(defcard-rg fetch-single-product
            "Get a **single** product"
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

