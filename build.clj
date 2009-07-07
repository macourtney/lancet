(use 'clojure.contrib.shell-out 
     'lancet
     'clojure.contrib.str-utils)

(def clojure-home (or (env :CLOJURE_HOME) "/Users/stuart/repos/clojure"))
(def contrib-home (or (env :CLOJURE_CONTRIB_HOME) "/Users/stuart/repos/clojure-contrib"))

(def lib-dir "lib")
(def build-dir "build")

(def classpath (str-join (java.io.File/pathSeparatorChar)
			 ["lib/ant.jar" 
			  "lib/ant-launcher.jar"
			  "lib/clojure.jar"
			  "lib/clojure-contrib.jar"
			  "."
			  "build"]))

(deftarget build-clojure "Build Clojure from source"
  (with-sh-dir clojure-home
    (system "git svn rebase")
    (system "ant jar")))

(deftarget build-contrib "Build Contrib from source"
  (with-sh-dir contrib-home
    (system "git svn rebase")
    (system "ant clean jar")))

(deftarget init "Prepare for build"
  (mkdir {:dir lib-dir})
  (mkdir {:dir build-dir}))

(deftarget licenses "Copy license info for embedded libs"
  (let [dest-dir (str build-dir "/licenses")]
    (mkdir {:dir dest-dir})
    (copy {:todir dest-dir}
      (fileset {:dir "licenses"}))))
    
(deftarget build-dependencies "Build dependent libraries"
  (init)
  (build-clojure) 
  (build-contrib)
  (copy { :file (str clojure-home "/clojure.jar") 
          :todir lib-dir})
  (copy { :file (str contrib-home "/clojure-contrib.jar") 
          :todir lib-dir}))

(deftarget compile-lancet "compile lancet"
  (init)
  (java { :classname "clojure.lang.Compile"
          :dir build-dir
          :fork "true" }
    [:jvmarg { :value (str "-Dclojure.compile.path=" build-dir) } ]
    [:arg { :value "lancet" } ]))

(deftarget test-lancet "test lancet"
  (compile-lancet)
  (java { :classname "clojure.lang.Script"
          :fork "true"
          :classpath classpath }
    [:arg { :value "lancet/test.clj" } ]))

(deftarget create-jar "jar up lancet"
  (init)
  (licenses)
  (unjar { :src (str lib-dir "/clojure.jar")
           :dest build-dir})
  (unjar { :src (str lib-dir "/clojure-contrib.jar")
           :dest build-dir})
  (unjar { :src (str lib-dir "/ant.jar")
           :dest build-dir})
  (unjar { :src (str lib-dir "/ant-launcher.jar")
           :dest build-dir})
  (compile-lancet)
  (jar { :jarfile (str lib-dir "/lancet.jar")
         :basedir "build"
         :manifest "MANIFEST.MF"}))
         
(deftarget default "Do everything."
  (test-lancet)
  (create-jar))

(if (not-empty *command-line-args*)
  (apply -main *command-line-args*)
  (default))