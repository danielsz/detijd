(ns detijd.core
  (:require
   [clojure.walk :as w]
   [clj-time.coerce :as c]
   [clj-time.core :as t]
   [clj-time.format :as f]
   [clj-time.periodic :as p]
   [clj-time.predicates :as pr])
  #?(:clj (:import [java.time Instant ZoneId ZonedDateTime LocalDate Duration DayOfWeek]
                   [java.time.temporal TemporalAdjusters IsoFields ChronoUnit]
                   [org.joda.time DateTime])))

(defn coerce-dates-to-long [m]
  (let [f (fn [[k v]]
            (if (= DateTime (type v))
              [k (c/to-long v)]                                           
              [k v]))]
    (w/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(def minutes-to-chime-hour
  #(let [next-time (t/plus (t/now) (t/hours 1))
         next-hour (t/date-time (t/year next-time) (t/month next-time) (t/day next-time) (t/hour next-time))]
     (t/in-minutes (t/interval (t/now) next-hour))))

(defn hours-to-schedule [n]
  (let [now (t/hour (t/now))]
    (loop [acc 0]
      (if (= (mod (+ now acc) n) 0)
        acc
        (recur (inc acc))))))

(def today-str #(f/unparse (f/formatters :date) (t/now)))
(def a-month-from-today-str #(f/unparse (f/formatters :date) (t/plus (t/now) (t/months 1))))

(defn in-future? [date]
  (t/before? (t/now) date))

(def in-past? (complement in-future?))

(defn today? [date]
  (= (.toLocalDate date) (t/today)))

(defn last-days? [date n]
  (t/within? (t/interval (t/minus (t/now) (t/days n)) (t/now)) date))

(defn last-hours? [date n]
  (t/within? (t/interval (t/minus (t/now) (t/hours n)) (t/now)) date))

(defn last-minutes? [date n]
  (t/within? (t/interval (t/minus (t/now) (t/minutes n)) (t/now)) date))

(defn this-week? [date]
  (and (= (t/year (t/now)) (t/year date))
       (= (t/week-number-of-year (t/now)) (t/week-number-of-year date))))

(defn this-month? [date]
  (and (= (t/year (t/now)) (t/year date))
       (= (t/month (t/now)) (t/month date))))

(defn this-year? [date]
  (= (t/year (t/now)) (t/year date)))

(defn last-months? [date n]
  (t/within? (t/interval (t/minus (t/now) (t/months n)) (t/now)) date))

(defn days-until [date]
  (let [multi-parser (f/formatter (t/default-time-zone) "YYYY-MM-dd" "YYYY/MM/dd")
        date (f/parse multi-parser date)]
    (t/in-days (t/interval (t/now) date))))

(def first-quarter? (some-fn pr/january? pr/february? pr/march?))
(def second-quarter? (some-fn pr/april? pr/may? pr/june?))
(def third-quarter? (some-fn pr/july? pr/august? pr/september?))
(def last-quarter? (some-fn pr/october? pr/november? pr/december?))
(defn quarter?
  ([] (quarter? (t/now)))
  ([date]
   (cond 
     (first-quarter? date) :first-quarter
     (second-quarter? date) :second-quarter
     (third-quarter? date) :third-quarter
     (last-quarter? date) :last-quarter)) )

(defn pred-to-date [pred]
  (loop [months (take 12 (p/periodic-seq (t/date-time 2011 1 1) (t/months 1)))]
    (if (pred (first months))
      (first months)
      (recur (next months)))))

(defn pred-to-date-alt [pred]
  (let [months (take 12 (p/periodic-seq (t/date-time 2011 1 1) (t/months 1)))]
    (some #(when (pred %) %) months)))

#?(:clj (defn week-number
          ([]
           (week-number (Instant/now)))
          ([instant]
           (-> instant
               (ZonedDateTime/ofInstant (ZoneId/systemDefault))
               (.get IsoFields/WEEK_OF_WEEK_BASED_YEAR)))))

#?(:clj (defn seconds-to-first-day-of-next-month []
          (let [now (ZonedDateTime/now (ZoneId/of "Asia/Jerusalem"))
                next-run (-> (.with now (TemporalAdjusters/firstDayOfNextMonth))
                             .toLocalDate
                             (.atStartOfDay (ZoneId/of "Asia/Jerusalem")))
                duration (Duration/between now next-run)]
            (.getSeconds duration))))

#?(:clj (defn next-weekday-in-days [day]
          {:pre [(= DayOfWeek (type day))]}
          (let [now (ZonedDateTime/now (ZoneId/of "Asia/Jerusalem"))
                today-next-week (.with now (TemporalAdjusters/next day))]
            (.between (ChronoUnit/DAYS) now today-next-week))))
