I init_files.inc

# master
# |-.gitlet
# |-dir1
#       |-wug.txt
# |-dir2
#       |-wug.txt
# |-dir3
#       |-wug.txt (not staged)
# |-wug.txt

# new
# |-.gitlet
# |-dir1
#       |-wug.txt (notwug.txt content)
# |-dir2
#       |-notwug.txt
# |-dir3
#       |-wug.txt (not staged)
# |-wug.txt

C dir3
C .
+ dir3/wug.txt wug.txt

> add wug.txt
<<<
> commit "master add wug.txt"
<<<

> branch new
<<<

> add dir1/wug.txt
<<<
> commit "master add dir1/wug.txt"
<<<
> add dir2/wug.txt
<<<
> commit "master add dir2/wug.txt"
<<<

> checkout new
<<<
+ dir1/wug.txt notwug.txt
+ dir2/notwug.txt notwug.txt

> add dir1/wug.txt
<<<
> commit "new add dir1/wug.txt"
<<<
> add dir2/notwug.txt
<<<
> commit "new add dir2/notwug.txt"
<<<

> checkout master
<<<
