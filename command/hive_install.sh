tar -xzvf hive-0.7.0-bin.tar.gz  
mv hive-0.7.0-bin hive
hadoop fs -mkdir       /tmp
hadoop fs -mkdir       /user/hive/warehouse
hadoop fs -chmod g+w   /tmp
hadoop fs -chmod g+w   /user/hive/warehouse