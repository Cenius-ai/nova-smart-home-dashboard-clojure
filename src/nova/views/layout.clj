(ns nova.views.layout
  "Shared Hiccup layout and navigation component."

  (:require [hiccup.page :refer [html5]]))

(defn nav-item
  "Render a single nav link."
  [href label current?]
  [:a.nav-link
   (merge {:href href}
          (when current? {:class "nav-link active"}))
   label])

(defn navbar
  "Top navigation bar with links to Dashboard, Devices, Settings."
  [current-path]
  [:nav.navbar
   [:div.navbar-inner
    [:a.navbar-brand {:href "/"}
     [:span.brand-icon "◆"]
     [:span.brand-text "Nova"]]
    [:div.nav-links
     (nav-item "/" "Dashboard" (= current-path "/"))
     (nav-item "/devices" "Devices" (.startsWith current-path "/devices"))
     (nav-item "/settings" "Settings" (= current-path "/settings"))]]])

(defn footer
  "Minimal page footer."
  []
  [:footer.page-footer
   [:div.footer-inner
    [:span "Nova — Smart Home Dashboard"]
    [:span.footer-muted "Simulated IoT data for demonstration"]]])

(defn page
  "Full HTML page shell with layout, navigation, and footer.
   Returns a Ring response map."
  [& {:keys [title current-path content scripts]}]
  {:status  200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    (html5 {:lang "en"}
              [:head
               [:meta {:charset "utf-8"}]
               [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
               [:meta {:name "description" :content "Nova — Smart Home IoT Dashboard"}]
               [:title (str title " — Nova")]
               [:link {:rel "stylesheet" :href "/css/fonts.css"}]
               [:link {:rel "stylesheet" :href "/css/style.css"}]]
              [:body
               (navbar current-path)
               [:main.main-content content]
               (footer)
               [:script {:src "/js/chart.umd.min.js"}]
               (when (seq scripts)
                 (for [s scripts]
                   [:script {:src s}]))
               [:script {:src "/js/app.js"}]])})
