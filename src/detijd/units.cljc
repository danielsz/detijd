(ns detijd.units)

(def unit->ms {:microsecond 0.001
               :microseconds 0.001
               :millisecond 1
               :milliseconds 1
               :second 1000
               :seconds 1000
               :minute 60000
               :minutes 60000
               :hour 3600000
               :hours 3600000
               :day 86400000
               :days 86400000
               :week 604800000
               :weeks 604800000
               :month 2678400000
               :months 2678400000})

(def month->int {:january 1
                 :february 2
                 :march 3
                 :april 4
                 :may 5
                 :june 6
                 :july 7
                 :august 8
                 :september 9
                 :october 10
                 :november 11
                 :december 12})
