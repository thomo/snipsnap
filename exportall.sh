#! /bin/sh
base=`dirname $0`
for appdir in $base/applications/*
do
  app=`basename $appdir`
  echo "Exporting $app to $base/app/$app/$app.snip ..."
  $base/dbexport.sh -config $base/applications/$app/application.conf > $base/applications/$app/$app.snip 2>/dev/null
done
