#!/bin/bash

source /etc/profile

yesterday(){
        day=$1
        ns=`date -d $day +%s`
        ts=$[ $ns - 86400]
        echo `date -d @$ts +%Y-%m-%d`
}

yesterday2(){
        day=$1
        ns=`date -d $day +%s`
        ts=$[ $ns - 86400]
        echo `date -d @$ts +%Y%m%d`
}

d=`date +%Y-%m-%d`
echo $d
yesd=`yesterday $d`
echo $yesd
yesd2=`yesterday2 $d`
echo $yesd2

mkdir logs/$yesd2
mv logs/radiusInfo_$yesd*.log logs/$yesd2

killall -9 medlda_proxy dict_proxy

ps ax |grep java |grep PosTagProxy | awk '{print $1}' |xargs kill -9
ps ax |grep java |grep SegProxy | awk '{print $1}' |xargs kill -9