
for i in high-res/in-step-*.png
do
  for j in high-res/in-screen-*.png
  do
    #echo "compare -metric NCC $i $j diff.png"
    ncc=`compare -metric NCC $i $j diff.png 2>&1 >/dev/null`
    if [ $? -eq 1 ]
    then
      printf "%s\t%s\t%f\n" $i $j $ncc
    fi
  done
done
rm diff.png

