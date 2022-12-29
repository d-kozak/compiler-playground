    .globl _my_assert
    .p2align 2

_my_assert:
    sub sp, sp, #32
    stp x29, x30, [sp, #16]
    add x29, sp, #16
    cbnz w0, _succ

    adrp x0, l_.str.1@PAGE
    add x0,x0, l_.str.1@PAGEOFF
    bl _printf
    mov x0, #1
    bl _exit

_succ:
    ldp x29, x30, [sp, #16]
    add sp, sp, #32
    ret