li x0, 1
li x1, 1
li x8, 0xFF
cmp x0, x1
je L2
L1:
 li x8, 0
 j L1
L2:
 li x8, 1
 j L0

