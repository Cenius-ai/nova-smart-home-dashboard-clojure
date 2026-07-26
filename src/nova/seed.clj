(ns nova.seed
  "Idempotent seed entrypoint. Creates demo data, prints summary, exits."

  (:require [nova.data :as data]))

(defn -main
  [& _args]
  (println "=== Nova Seed ===")
  (data/init!)
  (let [devs (data/devices)]
    (println (str "Devices seeded: " (count devs)))
    (doseq [d devs]
      (let [history-count (count (:history d))
            metrics (distinct (map :metric (:history d)))]
        (println (str "  " (:name d) " [" (:type d) "] — "
                      (:status d) " — "
                      history-count " readings across "
                      (count metrics) " metric(s)"))))
    (println "Seed complete. Demo data ready."))
  (System/exit 0))
