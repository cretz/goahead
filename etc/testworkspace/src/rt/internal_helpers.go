package rt

import "os"

func OSArgs() []*Java__lang__String__Instance {
	ret := make([]*Java__lang__String__Instance, len(os.Args) - 1)
	for i, v := range os.Args[1:] {
		ret[i] = Java__lang__String().Instance_Init__desc____obj__Java__lang__String(v)
	}
	return ret
}