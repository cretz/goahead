package testrt

import (
	"fmt"
	"math"
	"os"
	"runtime"
	"strings"
	"reflect"
)

func OSArgs() ObjectArray__Instance {
	ret := NewObjectArray(len(os.Args) - 1, "java.lang.String")
	for i, v := range os.Args[1:] {
		ret.Set(i, NewString(v))
	}
	return ret
}

func NewString(str string) String_g9YXBQ_Ñ {
	v := String_g9YXBQ().New()
	v.Underlying = str
	return v
}

func BoolToInt(b bool) int {
	if b {
		return 1
	} else {
		return 0
	}
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

func PanicToThrowable(v interface{}) Throwable_r1HNzA_Ñ {
	// TODO: stack traces
	switch v := v.(type) {
	case nil:
		ret := Exception_UnpE2w().New()
		ret.Init_M13Ø3g_Í(NewString("Unknown error"))
		return ret
	case Throwable_r1HNzA_Ñ:
		return v
	case runtime.Error:
		errStr := v.Error()
		isNpe := strings.HasSuffix(errStr, "invalid memory address or nil pointer dereference") ||
			(strings.HasPrefix(errStr, "interface conversion: ") && strings.Contains(errStr, " is nil, not "))
		if isNpe {
			ret := NullPointerException_fnXÞLQ().New()
			ret.Init_M13Ø3g_Í(NewString(v.Error()))
			return ret
		}
		if strings.HasSuffix(errStr, "makeslice: len out of range") {
			ret := NegativeArraySizeException_LQJRfg().New()
			ret.Init_M13Ø3g_Í(NewString(v.Error()))
			return ret
		}
		ret := VirtualMachineError_JV2iOA().New()
		ret.Init_M13Ø3g_Í(NewString(errStr))
		return ret
	case error:
		ret := Exception_UnpE2w().New()
		ret.Init_M13Ø3g_Í(NewString(v.Error()))
		return ret
	default:
		ret := Exception_UnpE2w().New()
		ret.Init_M13Ø3g_Í(NewString(fmt.Sprintf("%v", v)))
		return ret
	}
}

func NewClassCastEx() ClassCastException_x20ÞLA_Ñ {
	ret := ClassCastException_x20ÞLA().New()
	ret.Init_611f1A_Í()
	return ret
}

func SameIdentity(val1, val2 Object_fAFaMw_Ñ) bool {
	return (val1 == nil && val2 == nil) ||
		(val1 != nil && val2 != nil && reflect.ValueOf(val1).Pointer() == reflect.ValueOf(val2).Pointer())
}