(require '[remembrance.database :refer :all]
         '[datomic.api :as d])

(defn provision! []
  (let [schema-tx (read-string (slurp "resources/schema/remem-schema.edn"))
        data-tx (read-string (slurp "resources/schema/initial-data.edn"))]
    ;; Since we're in dev, load the schema and seed data every time:
    @(d/transact connection schema-tx)
    ;;@(d/transact connection data-tx)
    ))

(do
  (provision!))

(println "Provisioned storage.")
(System/exit 0)
