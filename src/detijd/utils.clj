(ns detijd.utils
  (:import [java.time Instant ZoneId ZonedDateTime]
           [java.time.temporal IsoFields]))

(defn week-number []
  (-> (Instant/now)
      (ZonedDateTime/ofInstant (ZoneId/systemDefault))
      (.get IsoFields/WEEK_OF_WEEK_BASED_YEAR)))
