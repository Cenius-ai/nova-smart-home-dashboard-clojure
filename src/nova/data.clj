(ns nova.data
  "In-memory demo data for Nova smart-home dashboard.
   Provides 5 devices with 24h of simulated telemetry history.")

(defonce app-state (atom nil))

;; ── Deterministic seeded random ──────────────────────────
(defn- seeded-rand
  "Simple seeded pseudo-random in [0,1) using a linear congruential generator."
  ^double [^long seed]
  (let [m 2147483647
        a 16807]
    (double (/ (rem (* a seed) m) m))))

(defn- rand-range
  "Deterministic float in [lo, hi) for a given seed+offset."
  ^double [seed offset lo hi]
  (let [r (seeded-rand (+ seed offset))]
    (+ lo (* r (- hi lo)))))

;; ── History generation ───────────────────────────────────
(defn- generate-history
  "Generate `n` hourly readings going back from `base-ts` (epoch ms).
   Returns [{:timestamp :string, :metric :string, :value :number}]."
  [base-ts n metrics seed]
  (let [hour-ms (* 60 60 1000)]
    (mapcat
      (fn [hour-offset]
        (let [ts (java.time.Instant/ofEpochMilli
                   (- base-ts (* (long hour-offset) hour-ms)))]
          (map (fn [[metric {:keys [base amp phase]}]]
                 (let [raw (+ base
                              (* amp (Math/sin (+ (* 0.3 hour-offset) phase)))
                              (rand-range seed (+ hour-offset phase) -1 1))
                       rounded (/ (Math/round (* 10.0 raw)) 10.0)]
                   {:timestamp (str ts)
                    :metric    (name metric)
                    :value     (double rounded)}))
               metrics)))
      (range n))))

;; ── Device definitions ───────────────────────────────────
(defn generate-devices
  "Returns a vector of 5 device maps, each with 24h of mock history."
  []
  (let [now-ms    (System/currentTimeMillis)
        base-seed 424242]
    [{:id      "dev-001"
      :name    "Living Room Thermostat"
      :type    "thermostat"
      :status  "active"
      :room    "Living Room"
      :history (generate-history now-ms 24
                 {:temperature {:base 21.5 :amp 2.5 :phase 0.0}
                  :humidity    {:base 48.0 :amp 7.0 :phase 1.0}}
                 base-seed)}
     {:id      "dev-002"
      :name    "Kitchen Humidity Sensor"
      :type    "humidity"
      :status  "active"
      :room    "Kitchen"
      :history (generate-history now-ms 24
                 {:humidity    {:base 54.0 :amp 8.0 :phase 2.0}
                  :temperature {:base 23.0 :amp 2.0 :phase 3.0}}
                 (+ base-seed 100))}
     {:id      "dev-003"
      :name    "Front Door Motion Sensor"
      :type    "motion"
      :status  "active"
      :room    "Entryway"
      :history (generate-history now-ms 24
                 {:motion {:base 0.15 :amp 0.15 :phase 0.0}}
                 (+ base-seed 200))}
     {:id      "dev-004"
      :name    "Office Smart Plug"
      :type    "plug"
      :status  "active"
      :room    "Office"
      :history (generate-history now-ms 24
                 {:power {:base 120.0 :amp 100.0 :phase 1.0}}
                 (+ base-seed 300))}
     {:id      "dev-005"
      :name    "Bedroom Light Sensor"
      :type    "light"
      :status  "offline"
      :room    "Bedroom"
      :history (generate-history now-ms 24
                 {:illuminance {:base 300.0 :amp 280.0 :phase 3.0}}
                 (+ base-seed 400))}]))

(defn init!
  "Idempotently seed the in-memory app state."
  []
  (when (nil? @app-state)
    (reset! app-state (generate-devices))
    (println (str "Seeded " (count @app-state) " devices into app state."))))

(defn devices []
  @app-state)

(defn get-device [id]
  (some #(when (= (:id %) id) %) @app-state))

(defn- latest-reading [device metric]
  (let [hist (:history device)]
    (when (seq hist)
      (:value (last (filter #(= (:metric %) (name metric)) hist))))))

(defn device-summaries []
  (map (fn [d]
         (let [primary-metric (case (:type d)
                                "thermostat" :temperature
                                "humidity"   :humidity
                                "motion"     :motion
                                "plug"       :power
                                "light"      :illuminance
                                :temperature)]
           {:id            (:id d)
            :name          (:name d)
            :type          (:type d)
            :status        (:status d)
            :room          (:room d)
            :energy-usage {:metric (name primary-metric)
                             :value  (latest-reading d primary-metric)}}))
       @app-state))

(defn dashboard-summary []
  (let [devs @app-state
        total (count devs)
        active (count (filter #(= "active" (:status %)) devs))
        recent-events [{:deviceId "dev-001" :message "Temperature spike detected (24.2°C)"}
                       {:deviceId "dev-003" :message "Motion detected at front door"}
                       {:deviceId "dev-004" :message "Power consumption peak (342W)"}
                       {:deviceId "dev-005" :message "Device went offline"}]
        default-dev (first devs)
        temp-history (filter #(= "temperature" (:metric %)) (:history default-dev))
        sorted-hist (sort-by :timestamp temp-history)]
    {:totalDevices  total
     :activeDevices active
     :recentEvents  recent-events
     :chartData     {:labels   (mapv :timestamp sorted-hist)
                     :datasets [{:label "Temperature (°C)"
                                 :data  (mapv :value sorted-hist)}]}}))

(defn device-history
  "Return history for a device, optionally filtered by metric."
  [device-id metric-kw]
  (let [device (get-device device-id)]
    (when device
      (let [hist (if metric-kw
                   (filter #(= (:metric %) (name metric-kw)) (:history device))
                   (:history device))]
        {:deviceId   (:id device)
         :deviceName (:name device)
         :metric     (name (or metric-kw :all))
         :history    (sort-by :timestamp hist)}))))
