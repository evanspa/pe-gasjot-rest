(ns pe-fp-rest.resource.fplog.fplogs-res
  (:require [liberator.core :refer [defresource]]
            [pe-fp-rest.meta :as meta]
            [clojure.tools.logging :as log]
            [pe-rest-utils.macros :refer [defmulti-by-version]]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.meta :as rumeta]
            [pe-user-rest.utils :as userresutils]
            [pe-user-core.core :as usercore]
            [pe-fp-core.validation :as fpval]))

(declare process-fplogs-post!)
(declare new-fplog-validator-fn)
(declare body-data-in-transform-fn)
(declare body-data-out-transform-fn)
(declare save-new-fplog-fn)
(declare next-fplog-id-fn)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Handler
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn handle-fplogs-post!
  [ctx
   db-spec
   base-url
   entity-uri-prefix
   fplogs-uri
   user-id
   plaintext-auth-token
   embedded-resources-fn
   links-fn
   err-notification-mustache-template
   err-subject
   err-from-email
   err-to-email]
  (rucore/put-or-post-invoker ctx
                              :post-as-create
                              db-spec
                              base-url
                              entity-uri-prefix
                              fplogs-uri
                              embedded-resources-fn
                              links-fn
                              [user-id]
                              plaintext-auth-token
                              new-fplog-validator-fn
                              fpval/sfplog-any-issues
                              body-data-in-transform-fn
                              body-data-out-transform-fn
                              next-fplog-id-fn
                              save-new-fplog-fn
                              nil ; save-entity-fn
                              nil ; hdr-establish-session
                              nil ; make-session-fn
                              nil ; post-as-do-fn
                              nil ; if-unmodified-since-hdr
                              (fn [exc-and-params]
                                (usercore/send-email err-notification-mustache-template
                                                     exc-and-params
                                                     err-subject
                                                     err-from-email
                                                     err-to-email))
                              #(identity %)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version new-fplog-validator-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version body-data-in-transform-fn meta/v001)
(defmulti-by-version body-data-out-transform-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Next fuel purchase log id function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version next-fplog-id-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Save new fplog function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version save-new-fplog-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Resource definition
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defresource fplogs-res
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
   links-fn
   err-notification-mustache-template
   err-subject
   err-from-email
   err-to-email]
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
  :post! (fn [ctx] (handle-fplogs-post! ctx
                                        db-spec
                                        base-url
                                        entity-uri-prefix
                                        (:uri (:request ctx))
                                        user-id
                                        (userresutils/get-plaintext-auth-token ctx
                                                                               auth-scheme
                                                                               auth-scheme-param-name)
                                        embedded-resources-fn
                                        links-fn
                                        err-notification-mustache-template
                                        err-subject
                                        err-from-email
                                        err-to-email))
  :handle-created (fn [ctx] (rucore/handle-resp ctx
                                                hdr-auth-token
                                                hdr-error-mask)))
