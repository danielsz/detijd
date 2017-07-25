(set-env!
 :source-paths   #{"src"}
 :resource-paths #{"src"}
 :dependencies '[[clj-time "0.13.0"]
                 [com.andrewmcveigh/cljs-time "0.5.0-alpha2"]])


(task-options!
 push {:repo-map {:url "https://clojars.org/repo/"}}
 pom {:project 'org.danielsz/detijd
      :version "0.1.0-SNAPSHOT"
      :scm {:name "git"
            :url "https://github.com/danielsz/detijd"}})

(deftask build
  []
  (comp (pom) (jar) (install)))

(deftask push-release
  []
  (comp
   (build)
   (push)))
