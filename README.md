# yaas-essentials

**Getting Started**

From within YaaS.io:

* If not regisered, do so first
* Create an organization (beta is fine, no need for commercial organizations)
* Create a new project
* Subscribe to the **Product Content** package https://market.yaas.io/beta/all/Product-Content-(Beta)/c3c5e9fe-2409-4caf-b007-095420e4b6e7
* Create a client and add all required scopes from the **Product Content** package

Next...

* Clone this repo
* Update **network.cljs** with your client ID and client Secret (obtainable from the yaas.io builder UI)
* Change the **tenant** to your project id / tenant in **product.cljs**
* Run **lein figwheel** from the root of this project. This should open up a browser, if not go to: http://localhost:3449/cards.html#!/yaas_essentials.core


Fun things you can do within the Clojurescript repl (launches with **lein figwheel**)

```clojure
    (in-ns 'yaas-essentials.product-ui-state)
    
    ;; define a few inspection helper functions

    (require 'cljs.pprint)
    
    (defn p[o] (cjjs.pprint/pprint o))

    (defn pc[o] (.log js/console (with-out-str (cljs.pprint/pprint o))))
    
    ;; print latest result from product call
    (p @product)
    
    ;; print to javascript console
    (pc @product)
    
   ;; after fetching products, you can inspect them and extract various components
   (map (comp :description :product) (get-in @product [:response :body]))
   
   ;; alternatively, save this value and print it to the console (or do something else with it...)
   (def last-products-descriptions (map (comp :description :product) (get-in @product [:response :body])))
   
   (pc last-products-descriptions)
    
```