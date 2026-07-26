(ns nova.core
  "Nova — Smart Home Dashboard.
   Main entry point: initialises demo data, sets up Ring/Reitit routes,
   and starts the Jetty server on 0.0.0.0:3000."

  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [reitit.ring :as ring]
            [nova.data :as data]
            [nova.api :as api]
            [nova.views.dashboard :as dashboard]
            [nova.views.devices :as devices]
            [nova.views.device :as device]
            [nova.views.settings :as settings]))

;; ── Route definitions ────────────────────────────────────
(defn app-routes
  []
  [["/"                  {:get dashboard/render}]
   ["/devices"           {:get devices/render}]
   ["/devices/:id"       {:get device/render}]
   ["/history/:id"       {:get device/render}]
   ["/settings"          {:get settings/render}]
   ["/api/dashboard-summary"     {:get api/dashboard-summary}]
   ["/api/devices"               {:get api/devices-list}]
   ["/api/devices/:id/history"   {:get api/device-history}]])

;; ── App construction ─────────────────────────────────────
(defn create-app
  []
  (let [router (ring/router (app-routes))]
    (-> (ring/ring-handler router
          (ring/create-default-handler
            {:not-found (constantly
                          {:status 404
                           :headers {"Content-Type" "text/html; charset=utf-8"}
                           :body "<!doctype html><html><head><title>404 — Nova</title></head><body><h1>404 Not Found</h1><p>The page you requested does not exist.</p><p><a href=\"/\">Back to Dashboard</a></p></body></html>"})}))
        (wrap-resource "public")
        (wrap-params))))

;; ── Server entry point ───────────────────────────────────
(defn -main
  [& _args]
  (println "╔══════════════════════════════════════════╗")
  (println "║  Nova — Smart Home Dashboard            ║")
  (println "║  Clojure · Ring · Reitit · Chart.js     ║")
  (println "╚══════════════════════════════════════════╝")
  (data/init!)
  (let [port (try
               (Integer/parseInt (or (System/getenv "PORT") "3000"))
               (catch NumberFormatException _ 3000))]
    (println (str "▶  Starting server on 0.0.0.0:" port))
    (flush)
    (jetty/run-jetty (create-app)
                     {:host "0.0.0.0"
                      :port port
                      :join? true})))
