#!/bin/sh

set -e

root=$(cd "$(dirname "$0")"; pwd)
pkg=se.liu.ida.nlp.sdp.

java -cp $root/target/sdp-1.0-SNAPSHOT.jar $pkg$@