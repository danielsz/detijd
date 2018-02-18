(set-env!
 :source-paths   #{"src"}
 :resource-paths #{"src"}
 :dependencies '[[adzerk/boot-test "1.2.0" :scope "test"]
                 [clj-time "0.13.0"]
                 [com.andrewmcveigh/cljs-time "0.5.0-alpha2"]])


(task-options!
 push {:repo-map {:url "https://clojars.org/repo/"}}
 pom {:project 'org.danielsz/detijd
      :version "0.1.2"
      :scm {:name "git"
            :url "https://github.com/danielsz/detijd"}})

(require '[adzerk.boot-test :refer :all])

(deftask build
  []
  (comp (pom) (jar) (install)))

(deftask dev-checkout
  []
  (comp (watch) (build)))

(deftask push-release
  []
  (comp
   (build)
   (push)))

(deftask testing
  "Profile setup for running tests."
  []
  (set-env! :source-paths #{"test"})
  (reset! boot.repl/*default-middleware* nil)
  (comp
   (watch)
   (notify :visual true)
   (test)))
