package rt

import (
	"sync"
	"runtime"
	"unicode/utf8"
	"strconv"
)

// java.lang.Exception

type Java__lang__Exception__Static struct{}

var Java__lang__Exception__Var Java__lang__Exception__Static

func Java__lang__Exception() *Java__lang__Exception__Static {
	return &Java__lang__Exception__Var
}

func (this *Java__lang__Exception__Static) New() *Java__lang__Exception__Instance {
	return &Java__lang__Exception__Instance{
		Java__lang__Throwable__Instance: Java__lang__Throwable().New(),
	}
}

type Java__lang__Exception__Instance struct {
	*Java__lang__Throwable__Instance
}

// java.lang.NullPointerException

type Java__lang__NullPointerException__Static struct{}

var Java__lang__NullPointerException__Var Java__lang__NullPointerException__Static

func Java__lang__NullPointerException() *Java__lang__NullPointerException__Static {
	return &Java__lang__NullPointerException__Var
}

func (this *Java__lang__NullPointerException__Static) New() *Java__lang__NullPointerException__Instance {
	return &Java__lang__NullPointerException__Instance{
		Java__lang__Exception__Instance: Java__lang__Exception().New(),
	}
}

type Java__lang__NullPointerException__Instance struct {
	*Java__lang__Exception__Instance
}

func (this *Java__lang__NullPointerException__Instance) Instance_Init__desc__() {
	this.Java__lang__Object__Instance.Instance_Init__desc__()
}

// java.lang.Object

type Java__lang__Object__Static struct{}

var Java__lang__Object__Var Java__lang__Object__Static

func Java__lang__Object() *Java__lang__Object__Static {
	return &Java__lang__Object__Var
}

func (this *Java__lang__Object__Static) New() *Java__lang__Object__Instance {
	return &Java__lang__Object__Instance{}
}

type Java__lang__Object__Instance struct{}

func (this *Java__lang__Object__Instance) Instance_Init__desc__() {
	// Nothing
}

// java.lang.String

type Java__lang__String__Static struct{}

var Java__lang__String__Var Java__lang__String__Static

func Java__lang__String() *Java__lang__String__Static {
	return &Java__lang__String__Var
}

func (this *Java__lang__String__Static) New() *Java__lang__String__Instance {
	return &Java__lang__String__Instance {
		Java__lang__Object__Instance: Java__lang__Object().New(),
	}
}

type Java__lang__String__Instance struct {
	*Java__lang__Object__Instance
	Val string
}

func (this *Java__lang__String__Instance) Instance_Init__desc____obj__Java__lang__String(str string) {
	this.Java__lang__Object__Instance.Instance_Init__desc__()
	this.Val = str
}

func (this *Java__lang__String__Instance) Length__desc__() int {
	return utf8.RuneCountInString(this.Val)
}

// java.lang.StringBuilder

type Java__lang__StringBuilder__Static struct{}

var Java__lang__StringBuilder__Var Java__lang__StringBuilder__Static

func Java__lang__StringBuilder() *Java__lang__StringBuilder__Static {
	return &Java__lang__StringBuilder__Var
}

func (this *Java__lang__StringBuilder__Static) New() *Java__lang__StringBuilder__Instance {
	return &Java__lang__StringBuilder__Instance {
		Java__lang__Object__Instance: Java__lang__Object().New(),
	}
}

type Java__lang__StringBuilder__Instance struct {
	*Java__lang__Object__Instance
	Val string
}

func (this *Java__lang__StringBuilder__Instance) Instance_Init__desc__() {
	this.Java__lang__Object__Instance.Instance_Init__desc__()
}

func (this *Java__lang__StringBuilder__Instance) Append__desc____obj__Java__lang__String(str *Java__lang__String__Instance) *Java__lang__StringBuilder__Instance {
	this.Val += str.Val
	return this
}

func (this *Java__lang__StringBuilder__Instance) Append__desc__I(i int) *Java__lang__StringBuilder__Instance {
	this.Val += strconv.Itoa(i)
	return this
}

func (this *Java__lang__StringBuilder__Instance) ToString__desc__() *Java__lang__String__Instance {
	return NewString(this.Val)
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
	print(arg0.Val)
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

// java.lang.Throwable

type Java__lang__Throwable__Static struct{}

var Java__lang__Throwable__Var Java__lang__Throwable__Static

func Java__lang__Throwable() *Java__lang__Throwable__Static {
	return &Java__lang__Throwable__Var
}

func (this *Java__lang__Throwable__Static) New() *Java__lang__Throwable__Instance {
	return &Java__lang__Throwable__Instance{
		Java__lang__Object__Instance: Java__lang__Object().New(),
	}
}

type Java__lang__Throwable__Instance struct {
	*Java__lang__Object__Instance
}