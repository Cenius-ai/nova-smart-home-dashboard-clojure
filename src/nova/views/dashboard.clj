(ns nova.views.dashboard
  "Dashboard page view — summary cards, history chart, recent events."

  (:require [cheshire.core :as json]
            [nova.data :as data]
            [nova.views.layout :as layout]))

(defn- stat-card
  "Render a KPI stat card."
  [label value delta unit]
  [:div.stat-card
   [:span.stat-label label]
   [:span.stat-value
    value
    (when unit [:span.stat-unit unit])]
   (when delta
     [:span.stat-delta
      (if (pos? delta) "▲" "▼")
      (str (when (pos? delta) "+") delta "%")])])

(defn- event-row
  "Render a single recent event row."
  [event]
  (let [device (data/get-device (:deviceId event))]
    [:tr.event-row
     [:td.event-device (:name device)]
     [:td.event-msg (:message event)]]))

(defn- quick-device-row
  "Compact device row linking to detail page."
  [d]
  (let [lr (:energy-usage d)
        status-class (str "status-pill "
                          (if (= "active" (:status d))
                            "status-active"
                            "status-offline"))]
    [:tr.device-quick-row
     [:td
      [:a {:href (str "/devices/" (:id d))} (:name d)]]
     [:td.device-type (:type d)]
     [:td
      [:span {:class status-class} (:status d)]]
     [:td.device-reading
      (when lr
        (str (:value lr) " " (case (:metric lr)
                               "temperature" "°C"
                               "humidity" "%"
                               "motion" ""
                               "power" "W"
                               "illuminance" "lux"
                               "")))]]))

(defn render
  "Render the dashboard page at GET /."
  [_req]
  (let [summary (data/dashboard-summary)
        devs    (data/device-summaries)
        chart-json (json/generate-string (:chartData summary))]
    (layout/page
      :title "Dashboard"
      :current-path "/"
      :content
      [:div.dashboard-page
       [:header.page-header
        [:h1 "Dashboard"]
        [:p.page-subtitle "Smart home at a glance"]]
       [:div.stat-grid
        (stat-card "Total Devices" (str (:totalDevices summary)) nil nil)
        (stat-card "Active" (str (:activeDevices summary))
                   (int (* 100 (/ (:activeDevices summary) (:totalDevices summary)))) nil)
        (stat-card "Offline" (str (- (:totalDevices summary) (:activeDevices summary)))
                   (- (int (* 100 (/ (- (:totalDevices summary) (:activeDevices summary))
                                     (:totalDevices summary))))) nil)
        (stat-card "Readings (24h)" "120" nil "pts")]
       [:section.chart-section
        [:h2.section-title "Temperature — Living Room Thermostat"]
        [:div.chart-container
         [:canvas#dashboard-chart]]
        [:script#dashboard-chart-data {:type "application/json"}
         chart-json]]
       [:div.dashboard-bottom
        [:section.recent-events
         [:h2.section-title "Recent Events"]
         [:table.data-table
          [:thead
           [:tr
            [:th "Device"]
            [:th "Event"]]]
          [:tbody
           (for [ev (:recentEvents summary)]
             (event-row ev))]]]
        [:section.device-quick-list
         [:h2.section-title
          [:a {:href "/devices"} "All Devices →"]]
         [:table.data-table
          [:thead
           [:tr
            [:th "Name"]
            [:th "Type"]
            [:th "Status"]
            [:th.num "Latest"]]]
          [:tbody
           (for [d devs]
             (quick-device-row d))]]]]])))
