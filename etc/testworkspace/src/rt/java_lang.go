package rt

import (
	"sync"
	"runtime"
	"unicode/utf8"
)

// java.lang.NullPointerException

type Java__lang__NullPointerException__Static struct{}

var Java__lang__NullPointerException__Var Java__lang__NullPointerException__Static

func Java__lang__NullPointerException() *Java__lang__NullPointerException__Static {
	return &Java__lang__NullPointerException__Var
}

func (this *Java__lang__NullPointerException__Static) Instance_Init__desc__() *Java__lang__NullPointerException__Instance {
	return &Java__lang__NullPointerException__Instance{}
}

type Java__lang__NullPointerException__Instance struct{}

// java.lang.Object

type Java__lang__Object__Static struct{}

var Java__lang__Object__Var Java__lang__Object__Static

func Java__lang__Object() *Java__lang__Object__Static {
	return &Java__lang__Object__Var
}

func (this *Java__lang__Object__Static) Instance_Init__desc__() *Java__lang__Object__Instance {
	return &Java__lang__Object__Instance{}
}

type Java__lang__Object__Instance struct{}

// java.lang.String

type Java__lang__String__Static struct{}

var Java__lang__String__Var Java__lang__String__Static

func Java__lang__String() *Java__lang__String__Static {
	return &Java__lang__String__Var
}

func (this *Java__lang__String__Static) Instance_Init__desc____obj__Java__lang__String(str string) *Java__lang__String__Instance {
	inst := Java__lang__String__Instance(str)
	return &inst
}

type Java__lang__String__Instance string

func (this *Java__lang__String__Instance) Length__desc__() int {
	return utf8.RuneCountInString(string(*this))
}

// java.lang.System

type Java__lang__System__Static struct{
	init sync.Once
	Out *SystemOut
}

var Java__lang__System__Var Java__lang__System__Static

func Java__lang__System() *Java__lang__System__Static {
	Java__lang__System__Var.init.Do(func() {
		Java__lang__System__Var.Out = &SystemOut{}
	})
	return &Java__lang__System__Var
}

type SystemOut struct{}

func (this *SystemOut) Println() {
	if runtime.GOOS == "windows" {
		print("\r\n")
	} else {
		print("\n")
	}
}

func (this *SystemOut) Println__desc____obj__Java__lang__String(arg0 *Java__lang__String__Instance) {
	print(*arg0)
	this.Println()
}

func (this *SystemOut) Println__desc__I(arg0 int) {
	print(arg0)
	this.Println()
}

func (this *SystemOut) Println__desc__Z(arg0 bool) {
	print(arg0)
	this.Println()
}