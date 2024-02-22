#!/bin/sh

set -eux

curl -o sudachi.zip -sSL http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-20240109-full.zip
unzip sudachi.zip
ln -s sudachi-dictionary-20240109/system_full.dic system_core.dic
