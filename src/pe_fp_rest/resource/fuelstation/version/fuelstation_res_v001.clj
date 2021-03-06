(ns pe-fp-rest.resource.fuelstation.version.fuelstation-res-v001
  (:require [pe-fp-rest.meta :as meta]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [pe-core-utils.core :as ucore]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]
            [pe-fp-rest.resource.fuelstation.fuelstation-utils :as fsresutils]
            [pe-fp-rest.resource.fuelstation.fuelstation-res :refer [save-fuelstation-validator-fn
                                                                     body-data-in-transform-fn
                                                                     body-data-out-transform-fn
                                                                     save-fuelstation-fn
                                                                     delete-fuelstation-fn
                                                                     load-fuelstation-fn]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-fuelstation-validator-fn meta/v001
  [version fuelstation]
  (fpval/save-fuelstation-validation-mask fuelstation))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod body-data-in-transform-fn meta/v001
  [version
   user-id
   fuelstation-id
   fuelstation]
  (identity fuelstation))

(defmethod body-data-out-transform-fn meta/v001
  [version
   db-spec
   user-id
   fuelstation-id
   base-url
   entity-uri-prefix
   entity-uri
   fuelstation]
  (fsresutils/fuelstation-out-transform fuelstation))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Save fuel station function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-fuelstation-fn meta/v001
  [version
   db-spec
   user-id
   fuelstation-id
   plaintext-auth-token
   fuelstation
   if-unmodified-since]
  (fpcore/save-fuelstation db-spec
                           fuelstation-id
                           (assoc fuelstation :fpfuelstation/user-id user-id)
                           if-unmodified-since))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Delete fuelstation function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod delete-fuelstation-fn meta/v001
  [version
   db-spec
   user-id
   fuelstation-id
   delete-reason
   plaintext-auth-token
   if-unmodified-since]
  (fpcore/mark-fuelstation-as-deleted db-spec fuelstation-id if-unmodified-since))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 Load fuelstation function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod load-fuelstation-fn meta/v001
  [ctx
   version
   db-spec
   user-id
   fuelstation-id
   plaintext-auth-token
   if-modified-since]
  (fpcore/fuelstation-by-id db-spec fuelstation-id true))
