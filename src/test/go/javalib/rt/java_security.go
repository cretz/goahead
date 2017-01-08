// Generated from Azul Zulu packaged OpenJDK JAR and carries the same GPL license with the classpath exception
package rt

type Java__security__PrivilegedAction__Static struct{}

var Java__security__PrivilegedAction__Var Java__security__PrivilegedAction__Static

func Java__security__PrivilegedAction() *Java__security__PrivilegedAction__Static {
	return &Java__security__PrivilegedAction__Var
}

type Java__security__PrivilegedAction__Instance interface {
	GetClass__desc____ret____obj__Java__lang__Class() Java__lang__Class__Instance
	Finalize__desc____ret__V()
	NotifyAll__desc____ret__V()
	Wait__desc____ret__V()
	HashCode__desc____ret__I() int
	Run__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance
	Wait__desc__J__I__ret__V(int64, int)
	Clone__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance
	Wait__desc__J__ret__V(int64)
	Equals__desc____obj__Java__lang__Object__ret__Z(Java__lang__Object__Instance) bool
	Notify__desc____ret__V()
	ToString__desc____ret____obj__Java__lang__String() Java__lang__String__Instance
	RawPtr__Java__lang__Object() *Java__lang__Object__Impl
}

type Java__security__PrivilegedAction__dynproxy____Static struct{}

var Java__security__PrivilegedAction__dynproxy____Var Java__security__PrivilegedAction__dynproxy____Static

func Java__security__PrivilegedAction__dynproxy__() *Java__security__PrivilegedAction__dynproxy____Static {
	return &Java__security__PrivilegedAction__dynproxy____Var
}

func (this *Java__security__PrivilegedAction__dynproxy____Static) New() *Java__security__PrivilegedAction__dynproxy____Impl {
	v := &Java__security__PrivilegedAction__dynproxy____Impl{
		Java__lang__Object__Impl: Java__lang__Object().New(),
	}
	v.Java__security__PrivilegedAction__dynproxy____InitDispatch(v)
	return v
}

type Java__security__PrivilegedAction__dynproxy____Dispatch interface {
	Java__lang__Object__Dispatch
	Impl__Run__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance
}

func (this *Java__security__PrivilegedAction__dynproxy____Impl) Java__security__PrivilegedAction__dynproxy____InitDispatch(v Java__security__PrivilegedAction__dynproxy____Dispatch) {
	this.Java__lang__Object__Impl.Java__lang__Object__InitDispatch(v)
	this._dispatch = v
}

func (this *Java__security__PrivilegedAction__dynproxy____Impl) Run__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance {
	return this._dispatch.Impl__Run__desc____ret____obj__Java__lang__Object()
}

type Java__security__PrivilegedAction__dynproxy____Instance interface {
	GetClass__desc____ret____obj__Java__lang__Class() Java__lang__Class__Instance
	Finalize__desc____ret__V()
	NotifyAll__desc____ret__V()
	Wait__desc____ret__V()
	HashCode__desc____ret__I() int
	Run__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance
	Wait__desc__J__I__ret__V(int64, int)
	Clone__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance
	Wait__desc__J__ret__V(int64)
	Equals__desc____obj__Java__lang__Object__ret__Z(Java__lang__Object__Instance) bool
	Notify__desc____ret__V()
	ToString__desc____ret____obj__Java__lang__String() Java__lang__String__Instance
	RawPtr__Java__security__PrivilegedAction__dynproxy__() *Java__security__PrivilegedAction__dynproxy____Impl
	RawPtr__Java__lang__Object() *Java__lang__Object__Impl
}

type Java__security__PrivilegedAction__dynproxy____Impl struct {
	*Java__lang__Object__Impl
	_dispatch Java__security__PrivilegedAction__dynproxy____Dispatch
	fn        func() Java__lang__Object__Instance
}

func (this *Java__security__PrivilegedAction__dynproxy____Impl) RawPtr__Java__security__PrivilegedAction__dynproxy__() *Java__security__PrivilegedAction__dynproxy____Impl {
	return this
}

func (this *Java__security__PrivilegedAction__dynproxy____Impl) Impl_Self() Java__lang__Object__Instance {
	return this
}

func (_ *Java__security__PrivilegedAction__Static) DynProxy_Create(fn func() Java__lang__Object__Instance) Java__security__PrivilegedAction__Instance {
	v := &Java__security__PrivilegedAction__dynproxy____Impl{
		Java__lang__Object__Impl: Java__lang__Object().New(),
		fn:                       fn,
	}
	v.Java__security__PrivilegedAction__dynproxy____InitDispatch(v)
	v.Java__lang__Object__Impl.Impl__Instance_Init__desc____ret__V()
	return v
}

func (this *Java__security__PrivilegedAction__dynproxy____Impl) Impl__Run__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance {
	return this.fn()
}

type Java__security__PrivilegedExceptionAction__Static struct{}

var Java__security__PrivilegedExceptionAction__Var Java__security__PrivilegedExceptionAction__Static

func Java__security__PrivilegedExceptionAction() *Java__security__PrivilegedExceptionAction__Static {
	return &Java__security__PrivilegedExceptionAction__Var
}

type Java__security__PrivilegedExceptionAction__Instance interface {
	GetClass__desc____ret____obj__Java__lang__Class() Java__lang__Class__Instance
	Finalize__desc____ret__V()
	NotifyAll__desc____ret__V()
	Wait__desc____ret__V()
	HashCode__desc____ret__I() int
	Run__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance
	Wait__desc__J__I__ret__V(int64, int)
	Clone__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance
	Wait__desc__J__ret__V(int64)
	Equals__desc____obj__Java__lang__Object__ret__Z(Java__lang__Object__Instance) bool
	Notify__desc____ret__V()
	ToString__desc____ret____obj__Java__lang__String() Java__lang__String__Instance
	RawPtr__Java__lang__Object() *Java__lang__Object__Impl
}

type Java__security__PrivilegedExceptionAction__dynproxy____Static struct{}

var Java__security__PrivilegedExceptionAction__dynproxy____Var Java__security__PrivilegedExceptionAction__dynproxy____Static

func Java__security__PrivilegedExceptionAction__dynproxy__() *Java__security__PrivilegedExceptionAction__dynproxy____Static {
	return &Java__security__PrivilegedExceptionAction__dynproxy____Var
}

func (this *Java__security__PrivilegedExceptionAction__dynproxy____Static) New() *Java__security__PrivilegedExceptionAction__dynproxy____Impl {
	v := &Java__security__PrivilegedExceptionAction__dynproxy____Impl{
		Java__lang__Object__Impl: Java__lang__Object().New(),
	}
	v.Java__security__PrivilegedExceptionAction__dynproxy____InitDispatch(v)
	return v
}

type Java__security__PrivilegedExceptionAction__dynproxy____Dispatch interface {
	Java__lang__Object__Dispatch
	Impl__Run__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance
}

func (this *Java__security__PrivilegedExceptionAction__dynproxy____Impl) Java__security__PrivilegedExceptionAction__dynproxy____InitDispatch(v Java__security__PrivilegedExceptionAction__dynproxy____Dispatch) {
	this.Java__lang__Object__Impl.Java__lang__Object__InitDispatch(v)
	this._dispatch = v
}

func (this *Java__security__PrivilegedExceptionAction__dynproxy____Impl) Run__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance {
	return this._dispatch.Impl__Run__desc____ret____obj__Java__lang__Object()
}

type Java__security__PrivilegedExceptionAction__dynproxy____Instance interface {
	GetClass__desc____ret____obj__Java__lang__Class() Java__lang__Class__Instance
	Finalize__desc____ret__V()
	NotifyAll__desc____ret__V()
	Wait__desc____ret__V()
	HashCode__desc____ret__I() int
	Run__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance
	Wait__desc__J__I__ret__V(int64, int)
	Clone__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance
	Wait__desc__J__ret__V(int64)
	Equals__desc____obj__Java__lang__Object__ret__Z(Java__lang__Object__Instance) bool
	Notify__desc____ret__V()
	ToString__desc____ret____obj__Java__lang__String() Java__lang__String__Instance
	RawPtr__Java__security__PrivilegedExceptionAction__dynproxy__() *Java__security__PrivilegedExceptionAction__dynproxy____Impl
	RawPtr__Java__lang__Object() *Java__lang__Object__Impl
}

type Java__security__PrivilegedExceptionAction__dynproxy____Impl struct {
	*Java__lang__Object__Impl
	_dispatch Java__security__PrivilegedExceptionAction__dynproxy____Dispatch
	fn        func() Java__lang__Object__Instance
}

func (this *Java__security__PrivilegedExceptionAction__dynproxy____Impl) RawPtr__Java__security__PrivilegedExceptionAction__dynproxy__() *Java__security__PrivilegedExceptionAction__dynproxy____Impl {
	return this
}

func (this *Java__security__PrivilegedExceptionAction__dynproxy____Impl) Impl_Self() Java__lang__Object__Instance {
	return this
}

func (_ *Java__security__PrivilegedExceptionAction__Static) DynProxy_Create(fn func() Java__lang__Object__Instance) Java__security__PrivilegedExceptionAction__Instance {
	v := &Java__security__PrivilegedExceptionAction__dynproxy____Impl{
		Java__lang__Object__Impl: Java__lang__Object().New(),
		fn:                       fn,
	}
	v.Java__security__PrivilegedExceptionAction__dynproxy____InitDispatch(v)
	v.Java__lang__Object__Impl.Impl__Instance_Init__desc____ret__V()
	return v
}

func (this *Java__security__PrivilegedExceptionAction__dynproxy____Impl) Impl__Run__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance {
	return this.fn()
}
