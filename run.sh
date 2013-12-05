#!/bin/sh

set -e

root=$(cd "$(dirname "$0")"; pwd)
pkg=sdp.tools.

java -cp $root/dist/sdp.jar $pkg$@