(ns nova.views.settings
  "Settings page — theme toggle and about info."

  (:require [nova.views.layout :as layout]))

(defn render
  "Render settings page at GET /settings."
  [_req]
  (layout/page
    :title "Settings"
    :current-path "/settings"
    :content
    [:div.settings-page
     [:header.page-header
      [:h1 "Settings"]
      [:p.page-subtitle "Customize your dashboard experience"]]

     [:section.settings-section
      [:h2.section-title "Appearance"]
      [:div.setting-row
       [:div.setting-info
        [:h3 "Theme"]
        [:p "Switch between light and dark mode. Your preference is saved locally."]]
       [:button#theme-toggle.btn.btn-secondary
        {:type "button"
         :aria-label "Toggle dark mode"}
        "Toggle Dark Mode"]]]

     [:section.settings-section
      [:h2.section-title "About Nova"]
      [:div.setting-row
       [:div.setting-info
        [:h3 "Nova Smart Home Dashboard"]
        [:p "A simulated IoT dashboard built with Clojure, Ring, and Reitit. "
         "All device data is simulated for demonstration purposes."]
        [:ul.about-list
         [:li [:span.accent-bullet "◆"] " 5 simulated smart-home devices"]
         [:li [:span.accent-bullet "◆"] " 24-hour telemetry history per device"]
         [:li [:span.accent-bullet "◆"] " Interactive Chart.js history charts"]
         [:li [:span.accent-bullet "◆"] " Light and dark theme support"]
         [:li [:span.accent-bullet "◆"] " Responsive design for all screen sizes"]]]]]

     [:section.settings-section
      [:h2.section-title "Technology"]
      [:div.setting-row
       [:div.tech-stack
        [:span.tech-badge "Clojure"]
        [:span.tech-badge "Ring"]
        [:span.tech-badge "Reitit"]
        [:span.tech-badge "Hiccup"]
        [:span.tech-badge "Chart.js"]
        [:span.tech-badge "CSS3"]]]]]))
