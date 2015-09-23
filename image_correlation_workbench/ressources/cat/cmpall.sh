
for i in 0 1 2 3 4 5 6 7 8 9
do
  for j in 0 1 2 3 4 5 6 7 8 9
  do
    ncc=`compare -metric NCC in-step-$i.png in-screen-$j.png diff.png 2>&1 >/dev/null`
    printf "%d\t%d\t%f\n" $i $j $ncc
  done
done
rm diff.png

