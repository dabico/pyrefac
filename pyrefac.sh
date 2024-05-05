#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
if uname -s | grep -iq cygwin ; then
    DIR=$(cygpath -w "$DIR")
    PWD=$(cygpath -w "$PWD")
fi

"$DIR/gradlew" -q -p "$DIR" runPyRefac \
  -Prepository="$1" \
  -PfilePath="$2" \
  -Prefactoring="$3" \
  -Pparameters="$PWD/$4"
