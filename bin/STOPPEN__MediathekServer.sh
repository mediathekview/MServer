#!/bin/sh

#
#  Wird dieses File geändert,
#  muss es umbenannt werden!
#  Es wird sonst bei einem
#  Programmupdate überschrieben.
#



# falls das Programm noch läuft
# wird es beendet
while [ "$(ps aux | grep "[M]ediathekServer.jar")" ]
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
	kill $(pgrep -f MediathekServer.jar) > /dev/null 2>&1
	ps aux | grep "[M]ediathekServer.jar" | kill -9 $(cut -c 10-14) > /dev/null 2>&1
done


echo %% und Tschuess
echo %% -----------------------------------------
exit 0

