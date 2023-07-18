screen -S spalod-vue
npm run dev -- --host &

ctrl+A+D
 screen -r spalod-vue

```
 scripts/spalod-check.sh
#!/bin/bash

# check npm
if [ -f /var/run/npm.pid ]; then
    if ps -p $(cat /var/run/npm.pid) > /dev/null; then
        echo "npm is running."
    else
        echo "npm is not running."
    fi
else
    echo "npm is not running."
fi

# check java jar
if [ -f /var/run/java.pid ]; then
    if ps -p $(cat /var/run/java.pid) > /dev/null; then
        echo "java jar is running."
    else
        echo "java jar is not running."
    fi
else
    echo "java jar is not running."
fi

# check graphdb
if [ -f /var/run/graphdb.pid ]; then
    if ps -p $(cat /var/run/graphdb.pid) > /dev/null; then
        echo "graphdb is running."
    else
        echo "graphdb is not running."
    fi
else
    echo "graphdb is not running."
fi

scripts/spalod-run.sh
 #!/bin/bash

# timestamp
timestamp=$(date "+%Y.%m.%d-%H.%M.%S")

# run npm
cd /home/i3mainz/SpaLod/src/main/vue_js/spalod/
echo "Running npm..."
npm run dev -- --host > /var/log/npm_$timestamp.log 2>&1 &
echo $! > /var/run/npm.pid

# run java jar
cd /home/i3mainz/SpaLod/
echo "Running java jar..."
java -jar target/spalod-0.0.1-SNAPSHOT.jar > /var/log/java_$timestamp.log 2>&1 &
echo $! > /var/run/java.pid

# run graphdb
echo "Running graphdb..."
/home/i3mainz/graphdb-10.2.1/bin/graphdb > /var/log/graphdb_$timestamp.log 2>&1 &
echo $! > /var/run/graphdb.pid



scripts/spalod-stop.sh
#!/bin/bash

# stop npm
if [ -f /var/run/npm.pid ]; then
    echo "Stopping npm..."
    kill -9 $(cat /var/run/npm.pid)
    rm /var/run/npm.pid
fi

# stop java jar
if [ -f /var/run/java.pid ]; then
    echo "Stopping java jar..."
    kill -9 $(cat /var/run/java.pid)
    rm /var/run/java.pid
fi

# stop graphdb
if [ -f /var/run/graphdb.pid ]; then
    echo "Stopping graphdb..."
    kill -9 $(cat /var/run/graphdb.pid)
    rm /var/run/graphdb.pid
fi