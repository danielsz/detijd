(ns detijd.predicates
  (:require [detijd.core :refer [week-number]]
            [clj-time.core :as t])
  (:import [java.time Instant LocalDate ZoneId Period]
           [java.time.temporal ChronoUnit]
           [org.joda.time DateTime]
           [java.util Date]))

(defprotocol DeTijd
  (last-days? [d x])
  (last-hours? [d x])
  (last-minutes? [d x])
  (last-months? [d x])
  (today? [d])
  (same-week-number? [d])
  (same-year? [d])
  (in-future? [d])
  (in-past? [d])
  (days-elapsed [d])
  (minutes-elapsed [d])
  (hours-elapsed [d])
  (seconds-elapsed [d])
  (days-remain [d]))

(extend-protocol DeTijd

  Number

  (in-past? [d]
    (condp = (count (str d))
      10 (> (.getEpochSecond (Instant/now)) d)
      13 (> (System/currentTimeMillis) d)))

  (in-future? [d]
    (condp = (count (str d))
      10 (< (.getEpochSecond (Instant/now)) d)
      13 (< (System/currentTimeMillis) d)))

  Instant

  (today? [d] (let [today (.truncatedTo (Instant/now) ChronoUnit/DAYS)]
                (= today (.truncatedTo d ChronoUnit/DAYS))))
  (same-week-number? [d] (= (week-number d) (week-number)))
  (same-year? [d] (= (.getYear (LocalDate/ofInstant d (ZoneId/systemDefault))) (.getYear (LocalDate/now))))
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
  (days-elapsed [d] (if (in-past? d)
                      (let [now (Instant/now)]
                        (.between ChronoUnit/DAYS d now))
                      (throw (AssertionError. "Date is not in past"))))
  (minutes-elapsed [d] (if (in-past? d)
                         (let [now (Instant/now)]
                           (.between ChronoUnit/MINUTES d now))
                         (throw (AssertionError. "Date is not in past"))))
  (hours-elapsed [d] (if (in-past? d)
                       (let [now (Instant/now)]
                         (.between ChronoUnit/HOURS d now))
                       (throw (AssertionError. "Date is not in past"))))
  (seconds-elapsed [d] (if (in-past? d)
                         (let [now (Instant/now)]
                           (.between ChronoUnit/SECONDS d now))
                         (throw (AssertionError. "Date is not in past"))))
  (days-remain [d] (if (in-future? d)
                     (let [now (Instant/now)]
                       (.between ChronoUnit/DAYS now d))
                     (throw (AssertionError. "Date is not in future"))))

  Date

  (today? [d] (today? (.toInstant d)))
  (same-week-number? [d] (same-week-number? (.toInstant d)))
  (same-year? [d] (same-year? (.toInstant d)))
  (in-future? [d]
    (in-future? (.toInstant d)))
  (in-past? [d]
    (in-past? (.toInstant d)))
  (last-minutes? [d x] (last-minutes? (.toInstant d) x))
  (last-hours? [d x] (last-hours? (.toInstant d) x))
  (last-days? [d x] (last-days? (.toInstant d) x))
  (last-months? [d x] (last-months? (.toInstant d) x))
  (days-elapsed [d] (days-elapsed (.toInstant d)))
  (minutes-elapsed [d] (minutes-elapsed (.toInstant d)))
  (hours-elapsed [d] (hours-elapsed (.toInstant d)))
  (seconds-elapsed [d] (seconds-elapsed (.toInstant d)))
  (days-remain [d] (days-remain (.toInstant d)))

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
  (same-year? [d] (= (.getYear d) (.getYear (LocalDate/now))))
  (in-future? [d]
    (t/before? (t/now) d))
  (in-past? [d]
    (t/after? (t/now) d))
  (days-elapsed [d] (if (in-past? d)
                      (t/in-days (t/interval d (t/now)))
                      (throw (AssertionError. "Date is not in past"))))
  (days-remain [d] (if (in-past? d)
                     (t/in-days (t/interval (t/now) d))
                     (throw (AssertionError. "Date is not in past"))))
  (seconds-elapsed [d] (if (in-past? d)
                         (t/in-seconds (t/interval d (t/now)))
                         (throw (AssertionError. "Date is not in past"))))
  (hours-elapsed [d] (if (in-past? d)
                       (t/in-hours (t/interval d (t/now)))
                       (throw (AssertionError. "Date is not in past"))))
  (minutes-elapsed [d] (if (in-past? d)
                         (t/in-minutes (t/interval d (t/now)))
                         (throw (AssertionError. "Date is not in past")))))

(defmulti past? (fn [d x unit] unit))
(defmethod past? :day [d x _]
  (last-days? d x))
(defmethod past? :minute [d x _]
  (last-minutes? d x))
(defmethod past? :hour [d x _]
  (last-hours? d x))
(defmethod past? :month [d x _]
  (last-months? d x))
