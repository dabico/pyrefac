#!/usr/bin/env bash

if [ $# -ne "4" ]; then
    printf "usage: %s <%s> <%s> [add_comment|rename_literal|rename_function_parameters] <%s>\n" \
      "$(basename "$0")" \
      "Git repository URL" \
      "Path to file in repository" \
      "Relative path to parameters JSON"
    exit 1
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
if uname -s | grep -iq cygwin ; then
    DIR=$(cygpath -w "$DIR")
    PWD=$(cygpath -w "$PWD")
fi

TERM=dumb
"$DIR/gradlew" -q -p "$DIR" runPyRefac \
  -Prepository="$1" \
  -PfilePath="$2" \
  -Prefactoring="$3" \
  -Pparameters="$PWD/$4" \
  -Dorg.gradle.console=plain \
  | sed -n -e '/^diff --git/,$p'
  # FIXME: This is a workaround to suppress all other outputs from IntelliJ.
