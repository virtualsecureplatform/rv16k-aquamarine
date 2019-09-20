nop
hnit:
        j main

fib_init:
        li s0, 0
        swsp s0, 2(sp)
        li s1, 1
        swsp s1, 2(sp)
        li s2, 50
        jr ra

fib_main:
        add s0 ,s1
        cmp s2, s1
        jl fib_return
        swsp s0, 2(sp)
        add s1, s0
        cmp s2, s1
        jb fib_return
        swsp s1, 2(sp)
        j fib_main
fib_return:
        jr ra
main:
        jal fib_init
        jal fib_main
        swsp s0, 0(sp)
        lwsp x8, 0(sp)
        nop
