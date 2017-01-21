package testrt

import (
	"fmt"
	"runtime"
	"strings"
)

// TODO: most of this is temporary until our compiler is strong enough to
// compile the runtime for us

func newline() {
	if runtime.GOOS == "windows" {
		print("\r\n")
	} else {
		print("\n")
	}
}

func (this *PrintStream_kZ4QkQ_Í) Println_9JG9AQ_Í(var1 String_g9YXBQ_Ñ) {
	if var1 == nil {
		print("null")
	} else {
		print(var1.(*String_g9YXBQ_Í).Underlying)
	}
	newline()
}

func (this *PrintStream_kZ4QkQ_Í) Println_WP2ØKA_Í(var0 rune) {
	print(fmt.Sprintf("%c", var0))
	newline()
}

func (this *PrintStream_kZ4QkQ_Í) Println_stkpaQ_Í(var0 float64) {
	// TODO: we know this sucks, it is a placeholder until we can compile the stdlib
	// Trim off trailing zeros after the first one
	str := fmt.Sprintf("%f", var0)
	decimal := strings.LastIndex(str, ".")
	if len(str) > decimal+2 {
		str = str[:decimal+2] + strings.TrimRight(str[decimal+2:], "0")
	}
	print(str)
	newline()
}

func (this *PrintStream_kZ4QkQ_Í) Println_ZoØ96w_Í(var0 float32) {
	this.Println_stkpaQ_Í(float64(var0))
}

func (this *PrintStream_kZ4QkQ_Í) Println_dCI8rg_Í(var0 int32) {
	print(var0)
	newline()
}

func (this *PrintStream_kZ4QkQ_Í) Println_zf2m7w_Í(var0 int64) {
	print(var0)
	newline()
}

func (this *PrintStream_kZ4QkQ_Í) Println_a7RKØw_Í(var0 bool) {
	if var0 {
		print("true")
	} else {
		print("false")
	}
	newline()
}
