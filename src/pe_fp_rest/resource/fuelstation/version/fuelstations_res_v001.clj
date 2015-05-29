(ns pe-fp-rest.resource.fuelstation.version.fuelstations-res-v001
  (:require [pe-fp-rest.meta :as meta]
            [clojure.tools.logging :as log]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]
            [pe-fp-rest.resource.fuelstation.fuelstations-res :refer [new-fuelstation-validator-fn
                                                                      body-data-in-transform-fn
                                                                      body-data-out-transform-fn
                                                                      next-fuelstation-id-fn
                                                                      save-new-fuelstation-fn]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod new-fuelstation-validator-fn meta/v001
  [version fuelstation]
  (fpval/create-fuelstation-validation-mask fuelstation))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod body-data-in-transform-fn meta/v001
  [version
   db-spec
   _   ;for 'fuelstations' resource, the 'in' would only ever be a NEW (to-be-created) fuel station, so it by definition wouldn't have an id
   fuelstation]
  (-> fuelstation
      (assoc :fpfuelstation/created-at
             (c/from-long (Long. (:fpfuelstation/created-at fuelstation))))))

(defmethod body-data-out-transform-fn meta/v001
  [version
   db-spec
   fuelstation-id
   fuelstation]
  (-> fuelstation
      (ucore/transform-map-val :fpfuelstation/created-at #(c/to-long %))
      (ucore/transform-map-val :fpfuelstation/updated-at #(c/to-long %))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Next fuelstation id function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod next-fuelstation-id-fn meta/v001
  [version db-spec]
  (fpcore/next-fuelstation-id db-spec))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Save new fuelstation function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-new-fuelstation-fn meta/v001
  [version
   db-spec
   user-id
   new-fuelstation-id
   fuelstation]
  (fpcore/save-new-fuelstation db-spec
                               user-id
                               new-fuelstation-id
                               fuelstation))
