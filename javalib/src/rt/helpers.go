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
	v := Java__lang__String().New()
	v.Underlying = str
	return v
}
