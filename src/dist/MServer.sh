#!/bin/sh

dir=`dirname "$0"`
cd "$dir"

# falls das Programm vom letzten Start noch läuft
# wird es beendet
while [ "$(ps aux | grep "[M]Server-.*.jar")" ]
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
	kill $(pgrep -f "[M]Server-.*.jar") > /dev/null 2>&1
done


# jetzt gehts mit dem Start weiter
echo %% 
echo %% -------------------------------------
echo %% Pfad: $dir
echo %% -------------------------------------
echo %% 


bin/MServer $dir $*

cd $OLDPWD

echo %% und Tschuess
echo %% -----------------------------------------
exit 0


