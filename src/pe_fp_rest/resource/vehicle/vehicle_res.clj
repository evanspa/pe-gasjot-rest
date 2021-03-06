(ns pe-fp-rest.resource.vehicle.vehicle-res
  (:require [liberator.core :refer [defresource]]
            [pe-fp-rest.meta :as meta]
            [clojure.tools.logging :as log]
            [pe-rest-utils.macros :refer [defmulti-by-version]]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.meta :as rumeta]
            [pe-user-rest.utils :as userresutils]
            [pe-user-core.core :as usercore]
            [pe-fp-core.core :as fpcore]
            [pe-fp-core.validation :as fpval]))

(declare save-vehicle-validator-fn)
(declare body-data-in-transform-fn)
(declare body-data-out-transform-fn)
(declare save-vehicle-fn)
(declare delete-vehicle-fn)
(declare load-vehicle-fn)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Handler
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn handle-vehicle-put!
  [ctx
   db-spec
   base-url
   entity-uri-prefix
   vehicle-uri
   user-id
   vehicle-id
   plaintext-auth-token
   embedded-resources-fn
   links-fn
   if-unmodified-since-hdr
   err-notification-mustache-template
   err-subject
   err-from-email
   err-to-email]
  (rucore/put-or-post-invoker ctx
                              :put
                              db-spec
                              base-url
                              entity-uri-prefix
                              vehicle-uri
                              embedded-resources-fn
                              links-fn
                              [user-id vehicle-id]
                              plaintext-auth-token
                              save-vehicle-validator-fn
                              fpval/sv-any-issues
                              body-data-in-transform-fn
                              body-data-out-transform-fn
                              nil ; next-entity-id-fn
                              nil ; save-new-entity-fn
                              save-vehicle-fn
                              nil ; hdr-establish-session
                              nil ; make-session-fn
                              nil ; post-as-do-fn
                              if-unmodified-since-hdr
                              (fn [exc-and-params]
                                (usercore/send-email err-notification-mustache-template
                                                     exc-and-params
                                                     err-subject
                                                     err-from-email
                                                     err-to-email))
                              #(identity %)))

(defn handle-vehicle-delete!
  [ctx
   db-spec
   base-url
   entity-uri-prefix
   vehicle-uri
   user-id
   vehicle-id
   plaintext-auth-token
   embedded-resources-fn
   links-fn
   if-unmodified-since-hdr
   err-notification-mustache-template
   err-subject
   err-from-email
   err-to-email]
  (rucore/delete-invoker ctx
                         db-spec
                         base-url
                         entity-uri-prefix
                         vehicle-uri
                         embedded-resources-fn
                         links-fn
                         [user-id vehicle-id]
                         plaintext-auth-token
                         body-data-out-transform-fn
                         delete-vehicle-fn
                         nil ; delete-reason-hdr
                         if-unmodified-since-hdr
                         (fn [exc-and-params]
                           (usercore/send-email err-notification-mustache-template
                                                exc-and-params
                                                err-subject
                                                err-from-email
                                                err-to-email))))

(defn handle-vehicle-get
  [ctx
   db-spec
   base-url
   entity-uri-prefix
   vehicle-uri
   user-id
   vehicle-id
   plaintext-auth-token
   embedded-resources-fn
   links-fn
   if-modified-since-hdr
   resp-gen-fn
   err-notification-mustache-template
   err-subject
   err-from-email
   err-to-email]
  (rucore/get-invoker ctx
                      db-spec
                      base-url
                      entity-uri-prefix
                      vehicle-uri
                      embedded-resources-fn
                      links-fn
                      [user-id vehicle-id]
                      plaintext-auth-token
                      body-data-out-transform-fn
                      load-vehicle-fn
                      if-modified-since-hdr
                      :fpvehicle/updated-at
                      resp-gen-fn
                      (fn [exc-and-params]
                        (usercore/send-email err-notification-mustache-template
                                             exc-and-params
                                             err-subject
                                             err-from-email
                                             err-to-email))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Validator function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version save-vehicle-validator-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version body-data-in-transform-fn meta/v001)
(defmulti-by-version body-data-out-transform-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Save new vehicle function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version save-vehicle-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Delete vehicle function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version delete-vehicle-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Load vehicle function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version load-vehicle-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Resource definition
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defresource vehicle-res
  [db-spec
   mt-subtype-prefix
   hdr-auth-token
   hdr-error-mask
   auth-scheme
   auth-scheme-param-name
   base-url
   entity-uri-prefix
   user-id
   vehicle-id
   embedded-resources-fn
   links-fn
   if-unmodified-since-hdr
   if-modified-since-hdr
   err-notification-mustache-template
   err-subject
   err-from-email
   err-to-email]
  :available-media-types (rucore/enumerate-media-types (meta/supported-media-types mt-subtype-prefix))
  :available-charsets rumeta/supported-char-sets
  :available-languages rumeta/supported-languages
  :allowed-methods [:put :delete :get]
  :authorized? (fn [ctx] (userresutils/authorized? ctx
                                                   db-spec
                                                   user-id
                                                   auth-scheme
                                                   auth-scheme-param-name))
  :known-content-type? (rucore/known-content-type-predicate (meta/supported-media-types mt-subtype-prefix))
  :can-put-to-missing? false
  :new? false
  :respond-with-entity? true
  :multiple-representations? false
  :put! (fn [ctx]
          (handle-vehicle-put! ctx
                               db-spec
                               base-url
                               entity-uri-prefix
                               (:uri (:request ctx))
                               user-id
                               vehicle-id
                               (userresutils/get-plaintext-auth-token ctx
                                                                      auth-scheme
                                                                      auth-scheme-param-name)
                               embedded-resources-fn
                               links-fn
                               if-unmodified-since-hdr
                               err-notification-mustache-template
                               err-subject
                               err-from-email
                               err-to-email))
  :delete! (fn [ctx]
             (handle-vehicle-delete! ctx
                                     db-spec
                                     base-url
                                     entity-uri-prefix
                                     (:uri (:request ctx))
                                     user-id
                                     vehicle-id
                                     (userresutils/get-plaintext-auth-token ctx
                                                                            auth-scheme
                                                                            auth-scheme-param-name)
                                     embedded-resources-fn
                                     links-fn
                                     if-unmodified-since-hdr
                                     err-notification-mustache-template
                                     err-subject
                                     err-from-email
                                     err-to-email))
  :handle-ok (fn [ctx]
               (if (= (get-in ctx [:request :request-method]) :get)
                 (handle-vehicle-get ctx
                                     db-spec
                                     base-url
                                     entity-uri-prefix
                                     (:uri (:request ctx))
                                     user-id
                                     vehicle-id
                                     (userresutils/get-plaintext-auth-token ctx
                                                                            auth-scheme
                                                                            auth-scheme-param-name)
                                     embedded-resources-fn
                                     links-fn
                                     if-modified-since-hdr
                                     #(rucore/handle-resp % hdr-auth-token hdr-error-mask)
                                     err-notification-mustache-template
                                     err-subject
                                     err-from-email
                                     err-to-email)
                 (rucore/handle-resp ctx hdr-auth-token hdr-error-mask))))
