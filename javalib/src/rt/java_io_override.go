package rt

import (
	"runtime"
	"fmt"
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

func (this *Java__io__PrintStream__Impl) Impl__Println__desc____obj__Java__lang__String__ret__V(var1 Java__lang__String__Instance) {
	if var1 == nil {
		print("null")
	} else {
		print(var1.(*Java__lang__String__Impl).Underlying)
	}
	newline()
}

func (this *Java__io__PrintStream__Impl) Impl__Println__desc__C__ret__V(var0 rune) {
	print(fmt.Sprintf("%c", var0))
	newline()
}

func (this *Java__io__PrintStream__Impl) Impl__Println__desc__D__ret__V(var0 float64) {
	// TODO: we know this sucks, it is a placeholder until we can compile the stdlib
	// Trim off trailing zeros after the first one
	str := fmt.Sprintf("%f", var0)
	decimal := strings.LastIndex(str, ".")
	if len(str) > decimal + 2 {
		str = str[:decimal + 2] + strings.TrimRight(str[decimal + 2:], "0")
	}
	print(str)
	newline()
}

func (this *Java__io__PrintStream__Impl) Impl__Println__desc__F__ret__V(var0 float32) {
	this.Impl__Println__desc__D__ret__V(float64(var0))
}

func (this *Java__io__PrintStream__Impl) Impl__Println__desc__I__ret__V(var0 int) {
	print(var0)
	newline()
}

func (this *Java__io__PrintStream__Impl) Impl__Println__desc__J__ret__V(var0 int64) {
	print(var0)
	newline()
}

func (this *Java__io__PrintStream__Impl) Impl__Println__desc__Z__ret__V(var0 bool) {
	if var0 {
		print("true")
	} else {
		print("false")
	}
	newline()
}
