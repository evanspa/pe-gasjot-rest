(ns pe-fp-rest.resource.fuelstation.fuelstations-res
  (:require [liberator.core :refer [defresource]]
            [pe-fp-rest.meta :as meta]
            [clojure.tools.logging :as log]
            [pe-rest-utils.macros :refer [defmulti-by-version]]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.meta :as rumeta]
            [pe-user-rest.utils :as userresutils]
            [pe-fp-core.validation :as fpval]))

(declare process-fuelstations-post!)
(declare new-fuelstation-validator-fn)
(declare body-data-in-transform-fn)
(declare body-data-out-transform-fn)
(declare save-new-fuelstation-fn)
(declare next-fuelstation-id-fn)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Handler
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn handle-fuelstations-post!
  [ctx
   db-spec
   base-url
   entity-uri-prefix
   fuelstations-uri
   user-id
   embedded-resources-fn
   links-fn]
  (rucore/put-or-post-invoker ctx
                              :post-as-create
                              db-spec
                              base-url
                              entity-uri-prefix
                              fuelstations-uri
                              embedded-resources-fn
                              links-fn
                              [user-id]
                              new-fuelstation-validator-fn
                              fpval/savefuelstation-any-issues
                              body-data-in-transform-fn
                              body-data-out-transform-fn
                              nil
                              next-fuelstation-id-fn
                              save-new-fuelstation-fn
                              nil
                              nil
                              nil
                              nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version new-fuelstation-validator-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version body-data-in-transform-fn meta/v001)
(defmulti-by-version body-data-out-transform-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Next fuelstation id function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version next-fuelstation-id-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Save new fuelstation function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version save-new-fuelstation-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Resource definition
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defresource fuelstations-res
  [db-spec
   mt-subtype-prefix
   hdr-auth-token
   hdr-error-mask
   auth-scheme
   auth-scheme-param-name
   base-url
   entity-uri-prefix
   user-id
   embedded-resources-fn
   links-fn]
  :available-media-types (rucore/enumerate-media-types (meta/supported-media-types mt-subtype-prefix))
  :available-charsets rumeta/supported-char-sets
  :available-languages rumeta/supported-languages
  :allowed-methods [:post]
  :authorized? (fn [ctx] (userresutils/authorized? ctx
                                                   db-spec
                                                   user-id
                                                   auth-scheme
                                                   auth-scheme-param-name))
  :known-content-type? (rucore/known-content-type-predicate (meta/supported-media-types mt-subtype-prefix))
  :post! (fn [ctx] (handle-fuelstations-post! ctx
                                              db-spec
                                              base-url
                                              entity-uri-prefix
                                              (:uri (:request ctx))
                                              user-id
                                              embedded-resources-fn
                                              links-fn))
  :handle-created (fn [ctx] (rucore/handle-resp ctx
                                                hdr-auth-token
                                                hdr-error-mask)))
