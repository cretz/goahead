package rt

import "os"

func (this *Java__lang__System__Static) Impl__Static_Init__desc____ret__V() {
	out := Java__io__PrintStream().New()
	out.Instance_Init__desc____obj__Java__io__OutputStream__ret__V(ReaderToOutputStream(os.Stdout))
	this.Out = out
}
