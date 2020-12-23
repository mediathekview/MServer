#!/bin/bash
shopt -s nullglob
echo "##############################"
echo "## File watcher compressor ##"
echo "##############################"
while inotifywait -e modify in/filmliste.id 2>&1 >>/dev/null; do
  sleep 1m
  echo "Filmlist ID file changed, starting compression"
  for liste in in/filme*.json; do
    filename_withoutpath=$(basename -- "$liste")
    extension="${filename_withoutpath##*.}"
    filename="${filename_withoutpath%.*}"

    echo "Starting compression of $filename_withoutpath ..."

    cp $liste work/
    cp $liste output/
    (xz -9 -c work/$filename_withoutpath >output/$filename.xz) &
    pids[0]=$!
    (bzip2 --best -c work/$filename_withoutpath >output/$filename.bz2) &
    pids[1]=$!
    (gzip --best -c work/$filename_withoutpath >output/$filename.gz) &
    pids[2]=$!

    for pid in ${pids[*]}; do
      wait $pid
    done

    echo "... finished"

  done

  cp in/filmliste.id output/
  echo "Compression finished"
done
