#/bin/bash

# Test script for the P2P Challenge Task 2015
#
# Usage: p2pct_v2.sh [part] [sleep]
#
# For the CT the following commands will be used:
# 
# *single access*
# p2pct_v2.sh aa 2
#
# *performance test*
# p2pct_v2.sh aa 0
#
#
# *multiple access*
# p2pct_v2.sh aa 2 & p2pct_v2.sh bb 2


PART=$1
SLEEP=$2

echo "create 1kb file"

tr '\0' 'X' < /dev/zero | dd of=xfile-1kb-$PART.txt count=1 bs=1024 &> /dev/null
cp xfile-1kb-$PART.txt xfile-1kb-$PART-1.txt
cp xfile-1kb-$PART.txt xfile-1kb-$PART-2.txt
cp xfile-1kb-$PART.txt xfile-1kb-$PART-3.txt
sleep $SLEEP

echo "create directory, and create a 1mb file"
mkdir csg-$PART
cd csg-$PART
tr '\0' 'Y' < /dev/zero | dd of=yfile-1mb-$PART.txt count=1024 bs=1024 &> /dev/null
sleep $SLEEP

echo "copy and move files, and create a 5mb file"
cp yfile-1mb-$PART.txt yfile-1mb-$PART-copy.txt
mkdir group-$PART
mv yfile-1mb-$PART-copy.txt group-$PART/yfile-1mb-$PART-copy.txt
cd group-$PART
tr '\0' 'Z' < /dev/zero | dd of=zfile-5mb-$PART.txt count=4096 bs=1024 &> /dev/null
sleep $SLEEP

echo "copy file, append data, read files"
cp zfile-5mb-$PART.txt ../zfile-5mb-$PART-copy.txt
cd ..
cat ../xfile-1kb-$PART.txt >> group-$PART/yfile-1mb-$PART-copy.txt
echo "expected: 0b82c7d1b6a136dd14cf2018a1a2af1b57563853 group-$PART/yfile-1mb-$PART-copy.txt"
echo "actual  :" `sha1sum group-$PART/yfile-1mb-$PART-copy.txt`
echo "expected: 9045932103582a1fec559a42dbb5d823973cadfa zfile-5mb-$PART-copy.txt"
echo "actual  :" `sha1sum zfile-5mb-$PART-copy.txt`
cd ..
sleep $SLEEP

echo "create empty file, delete files"
touch delete-$PART.me
rm csg-$PART/group-$PART/yfile-1mb-$PART-copy.txt  csg-$PART/group-$PART/zfile-5mb-$PART.txt
rmdir csg-$PART/group-$PART
rm csg-$PART/zfile-5mb-$PART-copy.txt csg-$PART/yfile-1mb-$PART.txt
rm delete-$PART.me

BEFORE=`ls -d -1 *$PART* | wc -l`
echo "$PART-" `ls -d -1 *$PART*`
rmdir csg-$PART
AFTER1=`ls -d -1 *$PART* | wc -l`
rm xfile-1kb-$PART.txt
AFTER2=`ls -d -1 *$PART* | wc -l`
echo "expected: 5, 4, 3"
echo "actual  : $BEFORE, $AFTER1, $AFTER2"
#this one could be a bit tricky
rm *$PART*.txt
echo "0 ==" `ls -d -1 *$PART* 2> /dev/null | wc -l`
