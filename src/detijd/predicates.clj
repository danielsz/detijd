(ns detijd.predicates
  (:require [clj-time.core :as t])
  (:import [java.time Instant LocalDate LocalDateTime ZoneId Period]
           [java.time.temporal ChronoUnit]))

(defprotocol DeTijd
  (last-days? [d x])
  (last-hours? [d x])
  (last-minutes? [d x])
  (last-months? [d x])
  (today? [d]))

(extend-type org.joda.time.DateTime
  DeTijd
  (last-days? [d x]
    (t/within? (t/interval (t/minus (t/now) (t/days x)) (t/now)) d))
  (last-hours? [d x]
    (t/within? (t/interval (t/minus (t/now) (t/hours x)) (t/now)) d))
  (last-minutes? [d x]
    (t/within? (t/interval (t/minus (t/now) (t/minutes x)) (t/now)) d))
  (last-months? [d x]
    (t/within? (t/interval (t/minus (t/now) (t/months x)) (t/now)) d))
  (today? [d] (= (.toLocalDate d) (t/today))))

(extend-type java.time.Instant
  DeTijd
  (today? [d] (let [today (.getDayOfWeek (LocalDateTime/ofInstant (Instant/now) (ZoneId/systemDefault)))]
                (= (.getDayOfWeek (LocalDateTime/ofInstant d (ZoneId/systemDefault))) today)))
  (last-minutes? [d x] (let [past (.minus (Instant/now) x ChronoUnit/MINUTES)]
                        (.isAfter d past)))
  (last-hours? [d x] (let [past (.minus (Instant/now) x ChronoUnit/HOURS)]
                      (.isAfter d past)))
  (last-days? [d x] (let [past (.minus (Instant/now) x ChronoUnit/DAYS)]
                     (.isAfter d past)))
  (last-months? [d x] (let [period (Period/between (LocalDate/ofInstant d (ZoneId/systemDefault)) (LocalDate/ofInstant (Instant/now) (ZoneId/systemDefault)))]
                       (> x (.getMonths period)))))

(defmulti past? (fn [d x unit] unit))
(defmethod past? :day [d x _]
  (last-days? d x))
(defmethod past? :minute [d x _]
  (last-minutes? d x))
(defmethod past? :hour [d x _]
  (last-hours? d x))
(defmethod past? :month [d x _]
  (last-months? d x))
