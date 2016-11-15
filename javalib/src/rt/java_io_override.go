package rt

import "runtime"

func (this *Java__io__PrintStream__Instance) Println__desc____obj__Java__lang__String__ret__V(var1 *Java__lang__String__Instance) {
	print(var1.Underlying)
	if runtime.GOOS == "windows" {
		print("\r\n")
	} else {
		print("\n")
	}
}
