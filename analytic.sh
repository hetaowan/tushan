#!/bin/sh
LINE=4
while [ 1 ]
do
echo "loop_"$LINE
echo "restart query_easy_server :`date` " >> runtime.log
tail wordAnalytic/wordAnalytic_0.log > trunk_dir/trunk.txt

java -cp out/jmlp.jar org.jmlp.web.TrunkBat wordAnalytic/bat4.txt trunk_dir/trunk.txt wordAnalytic/bat4_$LINE.txt
java -cp out/jmlp.jar org.jmlp.web.ConcurrentWordAnalytic wordAnalytic/bat4_$LINE.txt 2 $LINE
((LINE=$LINE+1))

if(($LINE==9))
then
  (($LINE=0))
else
  echo $LINE
fi
#exit
#java -cp jmlp.jar cn.clickwise.clickad.feathouse.QueryEasyServer 6579
#ps ax |grep java |grep ConcurrentProcessRadiusClient | awk '{print $1}' |xargs kill -9
sleep 100
done

