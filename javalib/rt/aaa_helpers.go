package rt

import (
	"fmt"
	"math"
	"os"
	"runtime"
	"strings"
	"reflect"
)

func OSArgs() ObjectArray__Instance {
	ret := NewObjectArray(len(os.Args) - 1)
	for i, v := range os.Args[1:] {
		ret.Set(i, NewString(v))
	}
	return ret
}

func NewString(str string) Java__lang__String__Instance {
	v := Java__lang__String().New()
	v.Instance_Init__desc____arr__C__I__I__ret__V(CharArray__Impl([]rune(str)))
	return v
}

func CompareDouble(left, right float64, nanMeansOne bool) int {
	if left > right {
		return 1
	} else if left == right {
		return 0
	} else if left < right {
		return -1
	} else if math.IsNaN(left) || math.IsNaN(right) {
		if nanMeansOne {
			return 1
		} else {
			return -1
		}
	} else {
		panic("Unexpected fall through")
	}
}

func CompareFloat(left, right float32, nanMeansOne bool) int {
	if left > right {
		return 1
	} else if left == right {
		return 0
	} else if left < right {
		return -1
	} else if math.IsNaN(float64(left)) || math.IsNaN(float64(right)) {
		if nanMeansOne {
			return 1
		} else {
			return -1
		}
	} else {
		panic("Unexpected fall through")
	}
}

func CompareLong(left, right int64) int {
	if left > right {
		return 1
	} else if left == right {
		return 0
	} else if left < right {
		return -1
	} else {
		panic("Unexpected fall through")
	}
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
		isNpe := strings.HasSuffix(errStr, "invalid memory address or nil pointer dereference") ||
			(strings.HasPrefix(errStr, "interface conversion: ") && strings.Contains(errStr, " is nil, not "))
		if isNpe {
			ret := Java__lang__NullPointerException().New()
			ret.Instance_Init__desc____obj__Java__lang__String__ret__V(NewString(v.Error()))
			return ret
		}
		if strings.HasSuffix(errStr, "makeslice: len out of range") {
			ret := Java__lang__NegativeArraySizeException().New()
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

func NewClassCastEx() Java__lang__ClassCastException__Instance {
	ret := Java__lang__ClassCastException().New()
	ret.Instance_Init__desc____ret__V()
	return ret
}

func SameIdentity(val1, val2 Java__lang__Object__Instance) bool {
	return (val1 == nil && val2 == nil) ||
		(val1 != nil && val2 != nil && reflect.ValueOf(val1).Pointer() == reflect.ValueOf(val2).Pointer())
}