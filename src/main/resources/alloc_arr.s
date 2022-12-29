    .globl	_alloc_arr                      ; -- Begin function alloc_arr
	.p2align	2

_alloc_arr:                             ; @alloc_arr
; %bb.0:
	sub	sp, sp, #32                     ; =32
	stp	x29, x30, [sp, #16]             ; 16-byte Folded Spill
	add	x29, sp, #16                    ; =16

	lsl	x0, x0, #2
	bl	_malloc

	ldp	x29, x30, [sp, #16]             ; 16-byte Folded Reload
	add	sp, sp, #32                     ; =32
	ret

