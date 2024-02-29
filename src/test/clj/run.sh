#!/bin/sh

if [ $# -ne 1 ]; then
  echo "Syntax: $0 file.clj"
  echo "  - file.clj will be run by the clojure 1.9 interpreter"
  exit 1
fi

java -cp ./clojure19.jar clojure.main $1

# Eof
