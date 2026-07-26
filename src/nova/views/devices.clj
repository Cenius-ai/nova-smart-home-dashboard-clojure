(ns nova.views.devices
  "Devices list page — grid of device cards with status and latest readings."

  (:require [nova.data :as data]
            [nova.views.layout :as layout]))

(defn- metric-unit
  "Return display unit string for a metric name."
  [metric]
  (case metric
    "temperature" "°C"
    "humidity"    "%"
    "motion"      ""
    "power"       "W"
    "illuminance" "lux"
    ""))

(defn- device-type-icon
  "Return a compact icon label for the device type."
  [type]
  (case type
    "thermostat" "🌡"
    "humidity"   "💧"
    "motion"     "📡"
    "plug"       "🔌"
    "light"      "💡"
    "📦"))

(defn- device-card
  "Render a device card for the grid."
  [d]
  (let [lr (:energy-usage d)
        status-class (str "status-pill "
                          (if (= "active" (:status d))
                            "status-active"
                            "status-offline"))]
    [:a.device-card {:href (str "/history/" (:id d))}
     [:div.device-card-header
      [:span.device-icon (device-type-icon (:type d))]
      [:span {:class status-class} (:status d)]]
     [:div.device-card-body
      [:h3.device-name (:name d)]
      [:p.device-meta
       (:type d) " · " (:room d)]]
     [:div.device-card-footer
      (when lr
        [:span.device-value
         (:value lr)
         [:span.device-unit (metric-unit (:metric lr))]])
      [:span.device-cta "View details →"]]]))

(defn render
  "Render the devices list page at GET /devices."
  [_req]
  (let [devs (data/device-summaries)
        active-count (count (filter #(= "active" (:status %)) devs))]
    (layout/page
      :title "Devices"
      :current-path "/devices"
      :content
      [:div.devices-page
       [:header.page-header
        [:h1 "Devices"]
        [:p.page-subtitle
         (str (count devs) " devices · " active-count " active")]]

       [:div.device-grid
        (for [d devs]
          [:div.device-card-wrapper {:key (:id d)}
           (device-card d)])]])))
