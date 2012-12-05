#!/bin/bash 

#echo "Start Crawler..."

# Run Crawler with :
# - Low UNIX priority (10)
# - Making sure that a crawler isn't run twice at the same time
# - using the user 'tomcat'
#
# You can add this script to your crontab, e.g.:
# */5 * * * * /path/to/regain/txt/crawler.sh > /dev/null 2&>1

# These variables are only set if not already set in ENV variables
: CRAWLER_USER=${CRAWLER_USER:="tomcat"}

: LOCKFILE=${LOCKFILE:="/var/run/regain-crawler.run"}
: FOLDER=${FOLDER:="/var/www/regain/regain-svn/build/runtime/crawler"}

COMMAND="cd $FOLDER ; nice -n 10 java -jar regain-crawler.jar"
EXEC_COMMAND="exec /bin/su - $CRAWLER_USER -c '""$COMMAND""' ; rm $LOCKFILE"

/usr/bin/flock -n "$LOCKFILE" -c "$EXEC_COMMAND"

if [ "$?" == "1" ]; then
    echo "ERROR: Unable to acquire the regain crawler lock. Is it already running?"
    echo "Otherwise, delete $LOCKFILE"
    exit 1
fi
