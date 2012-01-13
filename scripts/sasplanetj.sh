#!/bin/sh
REAL_PATH=`readlink -fn "$0"`
DIRNAME=`dirname "$REAL_PATH"`
env LD_LIBRARY_PATH=${DIRNAME}:${LD_LIBRARY_PATH} ${DIRNAME}/sasplanetj $@
