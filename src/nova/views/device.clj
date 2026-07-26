(ns nova.views.device
  "Device detail page — full history chart with metric selector."

  (:require [cheshire.core :as json]
            [nova.data :as data]
            [nova.views.layout :as layout]))

(defn- metric-display-name
  [kw]
  (case (name kw)
    "temperature" "Temperature"
    "humidity"    "Humidity"
    "motion"      "Motion"
    "power"       "Power (W)"
    "illuminance" "Illuminance"
    (name kw)))

(defn- metric-unit-label
  [kw]
  (case (name kw)
    "temperature" "°C"
    "humidity"    "%"
    "motion"      ""
    "power"       "W"
    "illuminance" "lux"
    ""))

(defn- extract-metrics
  "Return sorted distinct metrics from device history."
  [device]
  (->> (:history device)
       (map :metric)
       distinct
       sort))

(defn- stats-section
  "Render the stats summary rows for a device metric."
  [chart-values selected-metric]
  (let [n-readings (count chart-values)]
    (if (seq chart-values)
      (let [avg (/ (apply + chart-values) (count chart-values))
            mn  (apply min chart-values)
            mx  (apply max chart-values)]
        [:div.device-stats
         [:div.stat-row
          [:span.stat-label "Total Readings"]
          [:span.stat-value n-readings]]
         [:div.stat-row
          [:span.stat-label "Min"]
          [:span.stat-value (str mn " " (metric-unit-label selected-metric))]]
         [:div.stat-row
          [:span.stat-label "Max"]
          [:span.stat-value (str mx " " (metric-unit-label selected-metric))]]
         [:div.stat-row
          [:span.stat-label "Average"]
          [:span.stat-value
           (str (double (/ (Math/round (* 10.0 avg)) 10.0))
                " " (metric-unit-label selected-metric))]]])
      [:div.device-stats
       [:div.stat-row
        [:span.stat-label "Total Readings"]
        [:span.stat-value "0"]]])))

(defn render
  "Render device detail page at GET /devices/:id."
  [req]
  (let [device-id (get-in req [:path-params :id])
        device    (data/get-device device-id)]
    (if (not device)
      (assoc (layout/page
               :title "Not Found"
               :current-path "/devices"
               :content
               [:div.not-found
                [:h1 "Device Not Found"]
                [:p "The device you requested does not exist."]
                [:a.btn {:href "/devices"} "← Back to Devices"]])
             :status 404)
      (let [metrics         (extract-metrics device)
            selected-metric (or (some-> (get-in req [:query-params "metric"]) keyword)
                                (first metrics))
            hist-data       (data/device-history device-id selected-metric)
            chart-labels    (mapv :timestamp (:history hist-data))
            chart-values    (mapv :value (:history hist-data))
            chart-json      (json/generate-string
                              {:labels   chart-labels
                               :datasets [{:label (str (metric-display-name selected-metric)
                                                       " (" (metric-unit-label selected-metric) ")")
                                           :data  chart-values}]})
            status-class    (str "status-pill "
                                 (if (= "active" (:status device))
                                   "status-active"
                                   "status-offline"))]
        (layout/page
          :title (:name device)
          :current-path "/devices"
          :content
          [:div.device-detail-page
           [:header.page-header
            [:a.back-link {:href "/devices"} "← All Devices"]
            [:h1 (:name device)]
            [:p.page-subtitle
             (:type device) " · " (:room device) " · "
             [:span {:class status-class} (:status device)]]]
           [:div.metric-controls
            [:label.metric-label {:for "metric-select"} "Metric:"]
            [:select#metric-select.metric-select
             {:onchange "window.location.href = window.location.pathname + '?metric=' + this.value"}
             (for [m metrics]
               [:option {:value (name m)
                         :selected (= m selected-metric)}
                (metric-display-name m)])]]
           [:section.chart-section
            [:h2.section-title
             (str (metric-display-name selected-metric) " — 24 Hour History")]
            [:div.chart-container
             [:canvas#device-history-chart]]
            [:script#device-chart-data {:type "application/json"}
             chart-json]]
           (stats-section chart-values selected-metric)])))))
