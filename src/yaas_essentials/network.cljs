(ns yaas-essentials.network
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [reagent.core :as reagent]
    [clojure.string :as str]
    [yaas-essentials.utils :refer [log]]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<! >!]]
    )
  )

(def default-env {:client_id     "## Your Cliend ID Here ##"
                  :client_secret "## Your Client Secret Here ##"})

(def token-url "https://api.yaas.io/hybris/oauth2/v1/token")

(def bearer (reagent/atom {:bearer "[]"}))

(defonce token-state (reagent/atom {:response ""}))

(defn auth-header [scopes] {"Authorization"  (str "Bearer " (@bearer scopes))})

(defn renew-token [scopes retry-fn]
  (go (let [response (<! (http/post token-url
          {:form-params (merge default-env {:grant_type "client_credentials" :scope (str/join " " scopes)})
           :with-credentials? false
           }))]
        (swap! token-state merge @token-state {:response response})
        (swap! bearer merge @bearer {scopes (:access_token (:body response))})
        (log "Retrying method...")
        ; retry method after a short delay
        (js/setTimeout retry-fn 200)
        )
      )
  )

(defn yrequest

  ([scopes request-fn reply-chan]
   (yrequest scopes request-fn reply-chan 0)
    )
  ([scopes request-fn reply-chan tries]
   (go (let [response (<! (request-fn))]
         (if (and (>= (:status response) 200) (< (:status response) 400)) ;allow 2xx and 3xx responses
                  (>! reply-chan response)
                  (if (or (= (:status response) 401) (= (:status response 503)))
                    (do
                      (log "Renewing token...")
                      (let [try-count (inc tries)]
                        (if (< try-count 3)
                          (renew-token scopes (fn [] (yrequest scopes request-fn reply-chan try-count)))
                          (>! reply-chan response)
                          )
                        )
                      )
                    (do
                      (log (str "Error response or unrecoverable status from server" response))
                      (>! reply-chan response)
                      )
                    )
                  )
           )
         )

       )
    )

(defn yget [url headers scopes reply-chan]
   (yrequest scopes (fn[] (http/get url {:headers (merge headers (auth-header scopes)) :with-credentials? false})) reply-chan)
  )

(defn ypost [url data headers scopes reply-chan]
  (yrequest scopes (fn [] (http/post url {:headers (merge headers (auth-header scopes)) :json-params data :with-credentials? false})) reply-chan)
  )






