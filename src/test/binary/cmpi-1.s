li x0, 1
li x8, 0xFF
cmpi x0, 1
je L2
L1:
 li x8, 0
 j L1
L2:
 li x8, 1
 j L0

