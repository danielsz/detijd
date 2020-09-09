(ns detijd.predicates
  (:require [clj-time.core :as t]
            [detijd.core :refer [week-number]])
  (:import [java.time Instant LocalDate LocalDateTime ZoneId Period]
           [java.time.temporal ChronoUnit]
           [org.joda.time DateTime]))

(defprotocol DeTijd
  (last-days? [d x])
  (last-hours? [d x])
  (last-minutes? [d x])
  (last-months? [d x])
  (today? [d])
  (same-week-number? [d])
  (in-future? [d])
  (in-past? [d])
  (days-elapsed? [d])
  (days-remain? [d]))

(extend-protocol DeTijd

  DateTime

  (last-days? [d x]
    (t/within? (t/interval (t/minus (t/now) (t/days x)) (t/now)) d))
  (last-hours? [d x]
    (t/within? (t/interval (t/minus (t/now) (t/hours x)) (t/now)) d))
  (last-minutes? [d x]
    (t/within? (t/interval (t/minus (t/now) (t/minutes x)) (t/now)) d))
  (last-months? [d x]
    (t/within? (t/interval (t/minus (t/now) (t/months x)) (t/now)) d))
  (today? [d] (= (.toLocalDate d) (t/today)))
  (same-week-number? [d] (= (.getWeekOfWeekyear d) (.getWeekOfWeekyear (t/today))))
  (in-future? [d]
    (t/before? (t/now) d))
  (in-past? [d]
    (t/after? (t/now) d))
  (days-elapsed? [d] (if (in-past? d)
                       (t/in-days (t/interval d (t/now)))
                       (throw (AssertionError. "Date is not in past"))))

  Instant

  (today? [d] (let [today (.truncatedTo (Instant/now) ChronoUnit/DAYS)]
                (= today (.truncatedTo d ChronoUnit/DAYS))))
  (same-week-number? [d] (= (week-number d) (week-number)))
  (in-future? [d]
    (.isBefore (Instant/now) d))
  (in-past? [d]
    (.isAfter (Instant/now) d))
  (last-minutes? [d x] (let [past (.minus (Instant/now) x ChronoUnit/MINUTES)]
                         (.isAfter d past)))
  (last-hours? [d x] (let [past (.minus (Instant/now) x ChronoUnit/HOURS)]
                       (.isAfter d past)))
  (last-days? [d x] (let [past (.minus (Instant/now) x ChronoUnit/DAYS)]
                      (.isAfter d past)))
  (last-months? [d x] (let [period (Period/between (LocalDate/ofInstant d (ZoneId/systemDefault)) (LocalDate/ofInstant (Instant/now) (ZoneId/systemDefault)))]
                        (> x (.getMonths period))))
  (days-elapsed? [d] (if (in-past? d)
                       (let [now (Instant/now)]
                         (.between ChronoUnit/DAYS d now))
                       (throw (AssertionError. "Date is not in past"))))
  (days-remain? [d] (if (in-future? d)
                      (let [now (Instant/now)]
                        (.between ChronoUnit/DAYS now d))
                      (throw (AssertionError. "Date is not in future")))))

(defmulti past? (fn [d x unit] unit))
(defmethod past? :day [d x _]
  (last-days? d x))
(defmethod past? :minute [d x _]
  (last-minutes? d x))
(defmethod past? :hour [d x _]
  (last-hours? d x))
(defmethod past? :month [d x _]
  (last-months? d x))
