(ns detijd.predicates-test
  (:require [clojure.test :refer [testing deftest is use-fixtures with-test test-ns]]
            [clj-time.core :as t]
            [detijd.predicates :refer [past? in-future? in-past?]])
  (:import [java.time Instant LocalDate ZoneId ZoneOffset Period]
           [java.time.temporal ChronoUnit]))

(deftest detijd-instant
  (let [now (Instant/now)
        five-minutes-ago (.minus (Instant/now) 5 ChronoUnit/MINUTES)
        five-minutes-later (.plus (Instant/now) 5 ChronoUnit/MINUTES)
        five-hours-ago (.minus (Instant/now) 5 ChronoUnit/HOURS)
        now-local-date (LocalDate/ofInstant (Instant/now) (ZoneId/systemDefault))
        five-days-ago (-> now-local-date
                          (.minusDays 5)
                          (.atStartOfDay ZoneOffset/UTC)
                          (.toInstant))
        five-months-ago (-> now-local-date
                          (.minusMonths 5)
                          (.atStartOfDay ZoneOffset/UTC)
                          (.toInstant))]
    (is (in-future? five-minutes-later))
    (is (in-past? five-minutes-ago))
    (is (past? now 3 :minute))
    (is (not (past? five-minutes-ago 3 :minute)))
    (is (not (past? five-hours-ago 3 :minute)))
    (is (not (past? five-days-ago 3 :day)))
    (is (not (past? five-months-ago 3 :month)))
    (is (past? five-minutes-ago 6 :minute))
    (is (past? five-hours-ago 6 :hour))
    (is (past? five-days-ago 6 :day))
    (is (past? five-months-ago 6 :month))))

(deftest detijd-joda
  (let [now (t/now)
        five-minutes-ago (t/minus now (t/minutes 5))
        five-minutes-later (.plus (Instant/now) 5 ChronoUnit/MINUTES)
        five-hours-ago (t/minus now (t/hours 5))
        five-days-ago (t/minus now (t/days 5))
        five-months-ago (t/minus now (t/months 5))]
    (is (past? now 3 :minute))
    (is (in-future? five-minutes-later))
    (is (in-past? five-minutes-ago))
    (is (not (past? five-minutes-ago 3 :minute)))
    (is (not (past? five-hours-ago 3 :hour)))
    (is (not (past? five-days-ago 3 :day)))
    (is (not (past? five-months-ago 3 :month)))
    (is (past? five-minutes-ago 6 :minute))
    (is (past? five-hours-ago 6 :hour))
    (is (past? five-days-ago 6 :day))
    (is (past? five-months-ago 6 :month))))
