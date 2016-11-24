package rt

import (
	"os"
	"runtime"
	"fmt"
	"strings"
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

func PanicToThrowable(v interface{}) Java__lang__Throwable__Instance {
	// TODO: stack traces
	switch v := v.(type) {
	case nil:
		ret := Java__lang__Exception().New()
		ret.Instance_Init__desc____obj__Java__lang__String__ret__V(NewString("Unknown error"))
		return ret
	case Java__lang__Throwable__Instance:
		return v
	case runtime.Error:
		errStr := v.Error()
		isNpe := errStr == "invalid memory address or nil pointer dereference" ||
			(strings.HasPrefix(errStr, "interface conversion: ") && strings.Contains(errStr, " is nil, not "))
		if isNpe {
			ret := Java__lang__NullPointerException().New()
			ret.Instance_Init__desc____obj__Java__lang__String__ret__V(NewString(v.Error()))
			return ret
		}
		ret := Java__lang__VirtualMachineError().New()
		ret.Instance_Init__desc____obj__Java__lang__String__ret__V(NewString(errStr))
		return ret
	case error:
		ret := Java__lang__Exception().New()
		ret.Instance_Init__desc____obj__Java__lang__String__ret__V(NewString(v.Error()))
		return ret
	default:
		ret := Java__lang__Exception().New()
		ret.Instance_Init__desc____obj__Java__lang__String__ret__V(NewString(fmt.Sprintf("%v", v)))
		return ret
	}
}
