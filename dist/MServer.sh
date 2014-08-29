#!/bin/sh

#
#  Wird dieses File geändert,
#  muss es umbenannt werden!
#  Es wird sonst bei einem
#  Programmupdate überschrieben.
#

RET=1
dir=`dirname "$0"`
cd "$dir"


# falls das Programm vom letzten Start noch läuft
# wird es beendet
while [ "$(ps aux | grep "[M]Server.jar")" ]
do
	echo
	echo ===========================================
	echo ===========================================
	echo 
	echo 	da lauft noch was!!!!!!
	echo 
	echo ===========================================
	echo ===========================================
	echo

	# Sollte noch ein gleichnamiger Prozess laufen wird er hiermit beendet
	kill $(pgrep -f MServer.jar) > /dev/null 2>&1
	ps aux | grep "[M]Server.jar" | kill -9 $(cut -c 10-14) > /dev/null 2>&1
done


# jetzt gehts mit dem Start weiter
echo %% 
echo %% -------------------------------------
echo %% Pfad: $dir
echo %% -------------------------------------
echo %% 


if [ -n "$JAVA_HOME" ]; then
	$JAVA_HOME/bin/java -Xms256M -Xmx1G -jar ./MServer.jar $dir $*
else
	java -Xms256M -Xmx1G -jar ./MServer.jar $dir $*
fi
RET=$?

echo %% -------------------------------------
echo %% -------------------------------------
echo %% -------------------------------------
echo %% -------------------------------------
echo %% Rueckgabewert MServer: $RET
echo %% -------------------------------------
echo %% -------------------------------------

# Sicherheitshalber nur einman am Tag möglich
if [ $RET -eq 11 ]
then
    echo %% dann gabs ein update
	echo %% ---------------------------------
	echo %% entpacken ...
	
	unzip -o ./MServer_update.zip

	echo %% wieder starten ...
	echo %% ---------------------------------
	if [ -n "$JAVA_HOME" ]; then
		$JAVA_HOME/bin/java -Xms256M -Xmx1G -jar ./MServer.jar $dir $* -update
	else
		java -Xms256M -Xmx1G -jar ./MServer.jar $dir $* -update
	fi
fi

cd $OLDPWD
echo %% und Tschuess
echo %% -----------------------------------------
exit 0

