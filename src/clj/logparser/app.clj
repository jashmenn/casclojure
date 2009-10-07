(ns logparser.app
  (:gen-class)
  (:import 
     (java.util Properties)
     (cascading.flow Flow FlowConnector)
     (cascading.operation.regex RegexParser)
     (cascading.pipe Each Pipe)
     (cascading.scheme TextLine)
     (cascading.tap Hfs Lfs Tap)
     (cascading.tuple Fields)
     (org.apache.log4j Logger)))

(def apacheFields (new Fields (into-array ["ip" "time" "method" "event" "status" "size"])))

(def apacheRegex 
  "^([^ ]*) +[^ ]* +[^ ]* +\\[([^]]*)\\] +\\\"([^ ]*) ([^ ]*) [^ ]*\\\" ([^ ]*) ([^ ]*).*$")

(def allGroups (int-array [1 2 3 4 5 6]))

(def parser (new RegexParser apacheFields apacheRegex allGroups))

(def importPipe (new Each "parser" (new Fields (into-array ["line"])) parser))

(def properties (new Properties))

(FlowConnector/setApplicationJarClass properties logparser.app)

(defn -main [& args]
 (let [localLogTap (new Lfs (new TextLine) (first args))
       remoteLogTap (new Hfs (new TextLine) (last args))
       parsedLogFlow (. (new FlowConnector properties) connect localLogTap remoteLogTap importPipe)]
 (dorun
  (. parsedLogFlow start)
  (. parsedLogFlow complete))))

