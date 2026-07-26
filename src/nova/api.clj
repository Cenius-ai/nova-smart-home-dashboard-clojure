(ns nova.api
  "JSON API handlers for the Nova dashboard."

  (:require [cheshire.core :as json]
            [nova.data :as data]))

(defn- json-response
  "Return a Ring response map with JSON body."
  [body status]
  {:status  status
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body    (json/generate-string body)})

(defn dashboard-summary
  "GET /api/dashboard-summary"
  [_req]
  (json-response (data/dashboard-summary) 200))

(defn devices-list
  "GET /api/devices"
  [_req]
  (json-response (data/device-summaries) 200))

(defn device-history
  "GET /api/devices/:id/history?metric=temperature"
  [req]
  (let [device-id (get-in req [:path-params :id])
        metric-str (get-in req [:query-params "metric"])
        metric-kw  (when metric-str (keyword metric-str))]
    (if-let [history (data/device-history device-id metric-kw)]
      (json-response history 200)
      (json-response {:error "Device not found"} 404))))
