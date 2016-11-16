package rt

import "strconv"

func (this *Java__lang__Object__Instance) Instance_Init__desc____ret__V() {

}

func (this *Java__lang__StringBuilder__Instance) Instance_Init__desc____ret__V() {

}

func (this *Java__lang__StringBuilder__Instance) ToString__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	return NewString(this.Underlying)
}

func (this *Java__lang__StringBuilder__Instance) Append__desc____obj__Java__lang__String__ret____obj__Java__lang__StringBuilder(var1 *Java__lang__String__Instance) *Java__lang__StringBuilder__Instance {
	this.Underlying += var1.Underlying
	return this
}

func (this *Java__lang__StringBuilder__Instance) Append__desc__I__ret____obj__Java__lang__StringBuilder(var1 int) *Java__lang__StringBuilder__Instance {
	this.Underlying += strconv.Itoa(var1)
	return this
}

func (this *Java__lang__System__Static) Static_Init__desc____ret__V() {
	this.Out = Java__io__PrintStream().New()
}