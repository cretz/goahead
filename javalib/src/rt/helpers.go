package rt

import (
	"os"
)

func OSArgs() []Java__lang__String__Instance {
	ret := make([]Java__lang__String__Instance, len(os.Args) - 1)
	for i, v := range os.Args[1:] {
		ret[i] = NewString(v)
	}
	return ret
}

func NewString(str string) Java__lang__String__Instance {
	v := Java__lang__String().New()
	v.Underlying = str
	return v
}

func PanicToThrowable(v interface{}) *Java__lang__Throwable__Impl {
	// TODO: stack traces
	/*
	switch v := v.(type) {
	case nil:
		println("NIL")
		ret := Java__lang__Exception().New()
		ret.Instance_Init__desc____obj__Java__lang__String__ret__V(NewString("Unknown error"))
		return (*Java__lang__Throwable__Instance)(ret)
	case *Java__lang__Throwable__Instance:
		println("THROW")
		return v
	case runtime.Error:
		println("RUNTIME")
		if (v.Error() == "invalid memory address or nil pointer dereference") {
			ret := Java__lang__NullPointerException().New()
			ret.Instance_Init__desc____obj__Java__lang__String__ret__V(NewString(v.Error()))
			return ret
		}
		ret := Java__lang__VirtualMachineError().New()
		ret.Instance_Init__desc____obj__Java__lang__String__ret__V(NewString(v.Error()))
		return ret
	case error:
		println("ERR")
		ret := Java__lang__Exception().New()
		ret.Instance_Init__desc____obj__Java__lang__String__ret__V(NewString(v.Error()))
		return ret
	default:
		println("OTHER")
		ret := Java__lang__Exception().New()
		ret.Instance_Init__desc____obj__Java__lang__String__ret__V(NewString(fmt.Sprintf("%v", v)))
		return ret
	}*/
	panic("TODO")
}
