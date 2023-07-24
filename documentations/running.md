# Improved README.md 

---

# SpaLod Project
This README.md file provides instructions for starting and stopping the SpaLod application. The start script will run npm, java jar, and GraphDB. The stop script is used to terminate these processes.

## Requirements
- [npm](https://www.npmjs.com/)
- [Java](https://www.java.com/)
- [GraphDB](https://www.ontotext.com/products/graphdb/)
- [Apache Maven](https://maven.apache.org/)

## Starting the Application

The following script starts npm, a java jar, and GraphDB. It saves the process IDs (PIDs) of these processes for later use. Output logs for these processes are created and saved in the `/var/log` directory. 

```bash
 #!/bin/bash

# timestamp
timestamp=$(date "+%Y.%m.%d-%H.%M.%S")

# run npm
cd /home/i3mainz/SpaLod/src/main/vue_js/spalod/
echo "Running npm..."
npm run dev -- --host > /var/log/npm_$timestamp.log 2>&1 &
echo $! > /var/run/npm.pid
cat  /var/run/npm.pid

# run java jar
cd /home/i3mainz/SpaLod/
echo "Running java jar..."
/opt/apache-maven-3.9.3/bin/mvn  package  -DskipTests
java -jar target/spalod-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > /var/log/java_$timestamp.log 2>&1 &
echo $! > /var/run/java.pid
cat /var/run/java.pid

# run graphdb
echo "Running graphdb..."
/home/i3mainz/graphdb-10.2.1/bin/graphdb > /var/log/graphdb_$timestamp.log 2>&1 &
echo $! > /var/run/graphdb.pid
cat  /var/run/graphdb.pid
```

## Stopping the Application

The following script is used to stop the processes started by the start script. It checks if the PID files exist and, if they do, kills the processes and removes the PID files.

```bash
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
```

## Manually Killing Processes

If for any reason, you need to manually kill a process, you can find the PID with the `ps aux | grep node` command, and then use `kill -9 PID`.

```bash
i3mainz@spalod:~$ ps aux | grep node
i3mainz     2497  0.0  1.3 21933760 108896 ?     Sl   Jul23   0:21 node /home/i3mainz/SpaLod/src/main/vue_js/spalod/node_modules/.bin/vite --host

i3mainz@spalod:~$ kill -9 2497
```