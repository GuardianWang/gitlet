I init.inc

> branch init
<<<

+ wug.txt wug.txt
> add wug.txt
<<<
+ notwug.txt notwug.txt
> add notwug.txt
<<<
C d1
+ wug.txt wug.txt
> add wug.txt
<<<
C d2
+ wug.txt wug.txt
> add wug.txt
<<<
C d4
+ wug.txt wug.txt
> add wug.txt
<<<
C d5
+ wug.txt wug.txt
> add wug.txt
<<<
C d8
+ wug.txt wug.txt
> add wug.txt
<<<
C d9
+ wug.txt wug.txt
> add wug.txt
<<<
C d10
+ wug.txt wug.txt
> add wug.txt
<<<
C d11
+ wug.txt wug.txt
> add wug.txt
<<<
C d12
+ wug.txt wug.txt
> add wug.txt
<<<
C d13
+ wug.txt wug.txt
> add wug.txt
<<<
C .
> commit "split point"
<<<

> branch new
<<<
> branch split
<<<

+ wug.txt wug2.txt
> add wug.txt
<<<
+ wug2.txt wug2.txt
> add wug2.txt
<<<
C d1
+ wug.txt wug2.txt
> add wug.txt
<<<
C d2
> rm wug.txt
<<<
C d3
+ wug.txt wug.txt
> add wug.txt
<<<
C d4
> rm wug.txt
<<<
C d7
+ wug.txt wug.txt
> add wug.txt
<<<
C d8
+ wug.txt wug2.txt
> add wug.txt
<<<
C d9
> rm wug.txt
<<<
C d11
+ wug.txt wug2.txt
> add wug.txt
<<<
C d13
+ wug.txt wug2.txt
> add wug.txt
<<<
C .
> commit "master"
<<<

> checkout new
<<<

+ wug.txt notwug.txt
> add wug.txt
<<<
+ wug2.txt wug2.txt
> add wug2.txt
<<<
C d1
> rm wug.txt
<<<
C d2
+ wug.txt wug2.txt
> add wug.txt
<<<
C d3
+ wug.txt wug2.txt
> add wug.txt
<<<
C d5
> rm wug.txt
<<<
C d6
+ wug.txt wug.txt
> add wug.txt
<<<
C d8
+ wug.txt wug2.txt
> add wug.txt
<<<
C d9
> rm wug.txt
<<<
C d10
+ wug.txt wug2.txt
> add wug.txt
<<<
C d12
+ wug.txt wug2.txt
> add wug.txt
<<<
C .
> commit "new"
<<<

> checkout master
<<<
