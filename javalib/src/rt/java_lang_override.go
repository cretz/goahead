package rt

import "strconv"

func (this *Java__lang__Exception__Impl) Impl__Instance_Init__desc____obj__Java__lang__String__ret__V(var0 Java__lang__String__Instance) {
	this.Java__lang__Throwable__Impl.Impl__Instance_Init__desc____obj__Java__lang__String__ret__V(var0)
}

func (this *Java__lang__Exception__Impl) Impl__Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__Throwable__ret__V(var0 Java__lang__String__Instance, var1 Java__lang__Throwable__Instance) {
	// TODO: being lazy, impl this properly one day
	this.Java__lang__Throwable__Impl.Impl__Instance_Init__desc____obj__Java__lang__String__ret__V(var0)
}

func (this *Java__lang__NegativeArraySizeException__Impl) Impl__Instance_Init__desc____obj__Java__lang__String__ret__V(var0 Java__lang__String__Instance) {
	this.Java__lang__Throwable__Impl.Impl__Instance_Init__desc____obj__Java__lang__String__ret__V(var0)
}

func (this *Java__lang__NullPointerException__Impl) Impl__Instance_Init__desc____obj__Java__lang__String__ret__V(var0 Java__lang__String__Instance) {
	this.Java__lang__Throwable__Impl.Impl__Instance_Init__desc____obj__Java__lang__String__ret__V(var0)
}

func (this *Java__lang__Object__Impl) Impl__Instance_Init__desc____ret__V() {

}

func (this *Java__lang__StringBuilder__Impl) Impl__Instance_Init__desc____ret__V() {

}

func (this *Java__lang__StringBuilder__Impl) Impl__ToString__desc____ret____obj__Java__lang__String() Java__lang__String__Instance {
	return NewString(this.Underlying)
}

func (this *Java__lang__StringBuilder__Impl) Impl__Append__desc____obj__Java__lang__String__ret____obj__Java__lang__StringBuilder(var1 Java__lang__String__Instance) Java__lang__StringBuilder__Instance {
	// TODO: put the auto-string conv stuff somewhere
	if var1 == nil {
		this.Underlying += "null"
	} else {
		this.Underlying += var1.(*Java__lang__String__Impl).Underlying
	}
	return this
}

func (this *Java__lang__StringBuilder__Impl) Impl__Append__desc__I__ret____obj__Java__lang__StringBuilder(var1 int) Java__lang__StringBuilder__Instance {
	this.Underlying += strconv.Itoa(var1)
	return this
}

func (this *Java__lang__System__Static) Impl__Static_Init__desc____ret__V() {
	this.Out = Java__io__PrintStream().New()
}

func (this *Java__lang__Throwable__Impl) Error() string {
	return this._dispatch.Impl__GetMessage__desc____ret____obj__Java__lang__String().(*Java__lang__String__Impl).Underlying
}

func (this *Java__lang__Throwable__Impl) Impl__Instance_Init__desc____obj__Java__lang__String__ret__V(var0 Java__lang__String__Instance) {
	this.Message = var0
}

func (this *Java__lang__Throwable__Impl) Impl__GetMessage__desc____ret____obj__Java__lang__String() Java__lang__String__Instance {
	return this.Message
}

func (this *Java__lang__VirtualMachineError__Impl) Impl__Instance_Init__desc____obj__Java__lang__String__ret__V(var0 Java__lang__String__Instance) {
	this.Java__lang__Throwable__Impl.Impl__Instance_Init__desc____obj__Java__lang__String__ret__V(var0)
}
