package rt

import "fmt"

//goahead:forward-static java.lang.Object
type Object_Static struct{}

func (this *Object_Static) Init() {}

//goahead:forward-instance java.lang.Object
type Object struct {
	impl *Object_fAFaMw_Í
}

func (this *Object) Init() {}

func (this *Object) GetClass() Class_dvhEBA_Ñ {
	implName := fmt.Sprintf("%T", this.impl.Self())
	className := GetClassNameFromJavaImplName(implName)
	if className == "" {
		ex := ClassNotFoundException_LCk4ØA().New()
		ex.Init_M13Ø3g(NewString(fmt.Sprintf("Unable to find Java class for Go type %v", implName)))
		panic(ex)
	}
	return Class_dvhEBA().ForName_xpGqyQ_Í(NewString(className))
}

func (this *Object) HashCode() int {
	return System_hB0pIw().IdentityHashCode_lcJyvA_Í(this.impl)
}

func (this *Object) ToString() String_g9YXBQ_Ñ {
	return NewString(fmt.Sprintf("%v@%x", this.impl.GetClass_9pp3sQ().GetName_uasY3Q(), this.impl.HashCode_Gyq6fg()))
}
