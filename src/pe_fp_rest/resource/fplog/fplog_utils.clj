(ns pe-fp-rest.resource.fplog.fplog-utils
  (:require [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [pe-rest-utils.core :as rucore]
            [pe-user-rest.utils :as userrestutil]
            [pe-fp-rest.meta :as meta]
            [pe-core-utils.core :as ucore]))

(defn fplog-in-transform
  [{fuelstation-link :fplog/fuelstation vehicle-link :fplog/vehicle :as fplog}]
  (-> fplog
      (ucore/transform-map-val :fplog/carwash-per-gal-discount #(.doubleValue %))
      (ucore/transform-map-val :fplog/num-gallons #(.doubleValue %))
      (ucore/transform-map-val :fplog/gallon-price #(.doubleValue %))
      (ucore/assoc-if-contains fplog :fplog/fuelstation :fplog/fuelstation-id rucore/entity-id-from-uri)
      (ucore/assoc-if-contains fplog :fplog/vehicle     :fplog/vehicle-id     rucore/entity-id-from-uri)
      (ucore/transform-map-val :fplog/purchased-at #(c/from-long (Long. %)))))

(defn fplog-out-transform
  [{user-id :fplog/user-id :as fplog}
   base-url
   entity-url-prefix]
  (-> fplog
      (ucore/assoc-if-contains fplog :fplog/vehicle-id :fplog/vehicle #(userrestutil/make-user-subentity-url base-url
                                                                                                             entity-url-prefix
                                                                                                             user-id
                                                                                                             meta/pathcomp-vehicles
                                                                                                             %))
      (ucore/assoc-if-contains fplog :fplog/fuelstation-id :fplog/fuelstation #(userrestutil/make-user-subentity-url base-url
                                                                                                                     entity-url-prefix
                                                                                                                     user-id
                                                                                                                     meta/pathcomp-fuelstations
                                                                                                                     %))
      (ucore/transform-map-val :fplog/created-at #(c/to-long %))
      (ucore/transform-map-val :fplog/deleted-at #(c/to-long %))
      (ucore/transform-map-val :fplog/updated-at #(c/to-long %))
      (ucore/transform-map-val :fplog/purchased-at #(c/to-long %))
      (dissoc :fplog/updated-count)
      (dissoc :fplog/user-id)))
