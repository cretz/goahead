package rt

import "os"

func OSArgs() []*Java__lang__String__Instance {
	ret := make([]*Java__lang__String__Instance, len(os.Args) - 1)
	for i, v := range os.Args[1:] {
		ret[i] = NewString(v)
	}
	return ret
}

func NewString(str string) *Java__lang__String__Instance {
	ret := Java__lang__String().New()
	ret.Instance_Init__desc____obj__Java__lang__String(str)
	return ret
}