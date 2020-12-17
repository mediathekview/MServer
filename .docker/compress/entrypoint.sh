#!/bin/bash
touch in/filmliste.id
shopt -s nullglob
while inotifywait -e modify in/filmliste.id 2>&1 >>/dev/null; do
  echo "Ok"
  for liste in in/filme*.json; do
    filename_withoutpath=$(basename -- "$liste")
    extension="${filename_withoutpath##*.}"
    filename="${filename_withoutpath%.*}"

    cp $liste work/
    (xz -9 -c work/$filename_withoutpath >compressed/$filename.xz) &
    pids[0]=$!
    (bzip2 --best -c work/$filename_withoutpath >compressed/$filename.bz2) &
    pids[1]=$!
    (gzip --best -c work/$filename_withoutpath >compressed/$filename.gz) &
    pids[2]=$!

    for pid in ${pids[*]}; do
      wait $pid
    done

  done
done
