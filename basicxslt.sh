#!/bin/bash

# Set the base path
BASICXSLT_BASE_PATH=.
export BASICXSLT_BASE_PATH

# Set the location of the logj4 configuration file...
BASICXLST_LOGGING_CONFIG_FILE=-Dlog4j.configuration=file:$BASICXSLT_BASE_PATH/src/config/logging/log4j.properties

# Launch the application...
exec java $BASICXLST_LOGGING_CONFIG_FILE \
-classpath $BASICXSLT_BASE_PATH/dist/basicxslt.jar:\
$BASICXSLT_BASE_PATH/envlib/blueslate-commons.jar:\
$BASICXSLT_BASE_PATH/envlib/log4j-1.2.12.jar:\
$BASICXSLT_BASE_PATH/envlib/blueslate-commons-gui-domtree.jar:\
$BASICXSLT_BASE_PATH/envlib/commons-collections-3.1.jar:\
$BASICXSLT_BASE_PATH/envlib/commons-lang-2.0.jar:\
$BASICXSLT_BASE_PATH/envlib/xalan.jar:\
$BASICXSLT_BASE_PATH/envlib/xercesImpl.jar:\
$BASICXSLT_BASE_PATH/envlib/xml-apis.jar:\
$BASICXSLT_BASE_PATH/envlib/commons-io-1.2.jar:\
$BASICXSLT_BASE_PATH/envlib/commons-vfs-20070109.jar:\
$BASICXSLT_BASE_PATH/envlib/commons-logging-1.1.jar:\
$BASICXSLT_BASE_PATH/envlib/commons-httpclient-3.0.1.jar:\
$BASICXSLT_BASE_PATH/envlib/commons-codec-1.3.jar:\
$BASICXSLT_BASE_PATH/envlib/commons-net-1.4.1.jar:\
$BASICXSLT_BASE_PATH/envlib/jakarta-oro-2.0.8.jar:\
$BASICXSLT_BASE_PATH/envlib/log4j-1.2.12.jar \
us.paulevans.basicxslt.BasicXSLTFrame &
