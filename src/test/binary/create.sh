#!/bin/bash

/home/naoki/llvm-rv32k/build/bin/llvm-mc -arch=rv16k -filetype=obj ${1}.s > ${1}.o

/home/naoki/rv16k-aquamarine/utils/main ${1}.o > ${1}.bin

rm ${1}.o
