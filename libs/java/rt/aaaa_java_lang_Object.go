package rt

import (
	"fmt"
	"strings"
)

//goahead:forward-static java.lang.Object
type Object_Static struct{}

func (this *Object_Static) Init() {}

//goahead:forward-instance java.lang.Object
type Object struct {
	impl *Object_fAFaMw_Í
}

func (this *Object) Init() {}

func (t *Object) getImplName() string {
	// Need to remove everything after the last dot or star if present
	typ := fmt.Sprintf("%T", t.impl.Self())
	return typ[strings.LastIndexAny(typ, "*.")+1:]
}

func (this *Object) GetClass() Class_dvhEBA_Ñ {
	implName := this.getImplName()
	className := GetClassNameFromJavaImplName(implName)
	if className == "" {
		ex := ClassNotFoundException_LCk4ØA().New()
		ex.Init_M13Ø3g(NewString(fmt.Sprintf("Unable to find Java class for Go type %v", implName)))
		panic(ex)
	}
	return Class_dvhEBA().ForName_xpGqyQ_Í(NewString(className))
}

func (this *Object) HashCode() int32 {
	return objectHashCode(this.impl)
}

func objectHashCode(obj Object_fAFaMw_Ñ) int32 {
	return System_hB0pIw().IdentityHashCode_lcJyvA_Í(obj)
}

func (this *Object) ToString() String_g9YXBQ_Ñ {
	return objectToString(this.impl)
}

func objectToString(obj Object_fAFaMw_Ñ) String_g9YXBQ_Ñ {
	return NewString(fmt.Sprintf("%v@%x", obj.GetClass_9pp3sQ().GetName_uasY3Q(), obj.HashCode_Gyq6fg()))
}

func (this *Object_fAFaMw_Í) String() string {
	if this == nil {
		return "null"
	}
	return GetString(this.ToString_aÞ4cSA())
}

func objectString(obj Object_fAFaMw_Ñ) string {
	if obj == nil {
		return "null"
	}
	return GetString(obj.ToString_aÞ4cSA())
}
