

# container starten

docker run -d --name mymserver -e NOCOMPRESS=y mediathekview/mserver:latest


# volume erstellen

docker volume create mymserverdata

# container mit einem volume verbinden

Erst nutzbar, sobald der Branch hotfix/docker gemergt wurde.


docker run -d --name mymserver -v mymserverdata:/opt/mserver/data -e NOCOMPRESS=y mediathekview/mserver:latest

# An docker container anhängen (log verfolgen)

docker attach mymserver

# In docker container per bash gehen

docker exec -it mymserver bash


# container liste

docker ps -a


# docker container wieder löschen

docker rm mymserver


# volume löschen (erst, wenn container gelöscht ist)

docker volume rm mymserverdata


