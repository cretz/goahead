package rt

import "runtime"

// TODO: most of this is temporary until our compiler is strong enough to
// compile the runtime for us

func newline() {
	if runtime.GOOS == "windows" {
		print("\r\n")
	} else {
		print("\n")
	}
}

func (this *Java__io__PrintStream__Instance) Println__desc____obj__Java__lang__String__ret__V(var1 *Java__lang__String__Instance) {
	print(var1.Underlying)
	newline()
}

func (this *Java__io__PrintStream__Instance) Println__desc__I__ret__V(var1 int) {
	print(var1)
	newline()
}

func (this *Java__io__PrintStream__Instance) Println__desc__Z__ret__V(var1 bool) {
	if var1 {
		print("true")
	} else {
		print("false")
	}
	newline()
}
