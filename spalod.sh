#!/bin/bash
cd /home/spalod/
java -jar target/spalod-0.0.1-SNAPSHOT.jar &
P1=$!
cd /home/spalod/src/main/vue_js/spalod
npm install
npm run dev -- --host
P2=$!
wait $P1 $P2