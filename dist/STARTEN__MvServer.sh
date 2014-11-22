#! /bin/sh
#

dir=`dirname "$0"`
cd "$dir"

./MvServer.sh > ./log/direktStart__`date "+%Y.%m.%d__%H:%M"`.log &

cd $OLDPWD
echo %% und Tschuess
echo %% -----------------------------------------
exit 0

