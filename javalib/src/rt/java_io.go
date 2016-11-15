// Generated from Azul Zulu packaged OpenJDK JAR and carries the same
// GPL license with the classpath exception
package rt

type Java__io__Closeable__Dispatch interface {
	Java__lang__AutoCloseable__Dispatch
}

type Java__io__Closeable__Instance interface {
	Java__io__Closeable__Dispatch
}

type Java__io__FilterOutputStream__Static struct{}

var Java__io__FilterOutputStream__Var Java__io__FilterOutputStream__Static

func Java__io__FilterOutputStream() *Java__io__FilterOutputStream__Static {
	return &Java__io__FilterOutputStream__Var
}

func (this *Java__io__FilterOutputStream__Static) New() *Java__io__FilterOutputStream__Instance {
	v := &Java__io__FilterOutputStream__Instance{
		Java__io__OutputStream__Instance: Java__io__OutputStream().New(),
	}
	v.Java__io__FilterOutputStream__InitDispatch(v)
	return v
}

type Java__io__FilterOutputStream__Dispatch interface {
	Java__io__OutputStream__Dispatch
}

func (this *Java__io__FilterOutputStream__Instance) Java__io__FilterOutputStream__InitDispatch(v Java__io__FilterOutputStream__Dispatch) {
	this.Java__io__OutputStream__Instance.Java__io__OutputStream__InitDispatch(v)
	this.Java__io__FilterOutputStream__Dispatch = v
}

type Java__io__FilterOutputStream__Instance struct {
	*Java__io__OutputStream__Instance
	Java__io__FilterOutputStream__Dispatch
	Out *Java__io__OutputStream__Instance
}

func (this *Java__io__FilterOutputStream__Instance) Instance_Init__desc____obj__Java__io__OutputStream__ret__V(var1 *Java__io__OutputStream__Instance) {
	panic("Not Implemented")
}

func (this *Java__io__FilterOutputStream__Instance) Write__desc__I__ret__V(var1 int) {
	panic("Not Implemented")
}

func (this *Java__io__FilterOutputStream__Instance) Write__desc____arr__B__ret__V(var1 []byte) {
	panic("Not Implemented")
}

func (this *Java__io__FilterOutputStream__Instance) Write__desc____arr__B__I__I__ret__V(var1 []byte, var2 int, var3 int) {
	panic("Not Implemented")
}

func (this *Java__io__FilterOutputStream__Instance) Flush__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__io__FilterOutputStream__Instance) Close__desc____ret__V() {
	panic("Not Implemented")
}

type Java__io__Flushable__Dispatch interface {
	Dispatch__Flush__desc____ret__V()
}

type Java__io__Flushable__Instance interface {
	Java__io__Flushable__Dispatch
}

type Java__io__OutputStream__Static struct{}

var Java__io__OutputStream__Var Java__io__OutputStream__Static

func Java__io__OutputStream() *Java__io__OutputStream__Static {
	return &Java__io__OutputStream__Var
}

func (this *Java__io__OutputStream__Static) New() *Java__io__OutputStream__Instance {
	v := &Java__io__OutputStream__Instance{
		Java__lang__Object__Instance: Java__lang__Object().New(),
	}
	v.Java__io__OutputStream__InitDispatch(v)
	return v
}

type Java__io__OutputStream__Dispatch interface {
	Java__lang__Object__Dispatch
	Dispatch__Write__desc__I__ret__V(int)
	Dispatch__Write__desc____arr__B__ret__V([]byte)
	Dispatch__Write__desc____arr__B__I__I__ret__V([]byte, int, int)
}

func (this *Java__io__OutputStream__Instance) Java__io__OutputStream__InitDispatch(v Java__io__OutputStream__Dispatch) {
	this.Java__lang__Object__Instance.Java__lang__Object__InitDispatch(v)
	this.Java__io__OutputStream__Dispatch = v
}

type Java__io__OutputStream__Instance struct {
	Java__io__Closeable__Instance
	Java__io__Flushable__Instance
	*Java__lang__Object__Instance
	Java__io__OutputStream__Dispatch
}

func (this *Java__io__OutputStream__Instance) Instance_Init__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__io__OutputStream__Instance) Write__desc__I__ret__V(var1 int) {
	panic("Not Implemented")
}

func (this *Java__io__OutputStream__Instance) Write__desc____arr__B__ret__V(var1 []byte) {
	panic("Not Implemented")
}

func (this *Java__io__OutputStream__Instance) Write__desc____arr__B__I__I__ret__V(var1 []byte, var2 int, var3 int) {
	panic("Not Implemented")
}

func (this *Java__io__OutputStream__Instance) Flush__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__io__OutputStream__Instance) Close__desc____ret__V() {
	panic("Not Implemented")
}

type Java__io__PrintStream__Static struct{}

var Java__io__PrintStream__Var Java__io__PrintStream__Static

func Java__io__PrintStream() *Java__io__PrintStream__Static {
	return &Java__io__PrintStream__Var
}

func (this *Java__io__PrintStream__Static) New() *Java__io__PrintStream__Instance {
	v := &Java__io__PrintStream__Instance{
		Java__io__FilterOutputStream__Instance: Java__io__FilterOutputStream().New(),
	}
	v.Java__io__PrintStream__InitDispatch(v)
	return v
}

type Java__io__PrintStream__Dispatch interface {
	Java__io__FilterOutputStream__Dispatch
	Dispatch__CheckError__desc____ret__Z() bool
	Dispatch__SetError__desc____ret__V()
	Dispatch__ClearError__desc____ret__V()
	Dispatch__Print__desc__Z__ret__V(bool)
	Dispatch__Print__desc__C__ret__V(rune)
	Dispatch__Print__desc__I__ret__V(int)
	Dispatch__Print__desc__J__ret__V(int64)
	Dispatch__Print__desc__F__ret__V(float32)
	Dispatch__Print__desc__D__ret__V(float64)
	Dispatch__Print__desc____arr__C__ret__V([]rune)
	Dispatch__Print__desc____obj__Java__lang__String__ret__V(*Java__lang__String__Instance)
	Dispatch__Print__desc____obj__Java__lang__Object__ret__V(*Java__lang__Object__Instance)
	Dispatch__Println__desc____ret__V()
	Dispatch__Println__desc__Z__ret__V(bool)
	Dispatch__Println__desc__C__ret__V(rune)
	Dispatch__Println__desc__I__ret__V(int)
	Dispatch__Println__desc__J__ret__V(int64)
	Dispatch__Println__desc__F__ret__V(float32)
	Dispatch__Println__desc__D__ret__V(float64)
	Dispatch__Println__desc____arr__C__ret__V([]rune)
	Dispatch__Println__desc____obj__Java__lang__Object__ret__V(*Java__lang__Object__Instance)
	Dispatch__Printf__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(*Java__lang__String__Instance, []*Java__lang__Object__Instance) *Java__io__PrintStream__Instance
	Dispatch__Format__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(*Java__lang__String__Instance, []*Java__lang__Object__Instance) *Java__io__PrintStream__Instance
	Dispatch__Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__io__PrintStream(Java__lang__CharSequence__Instance) *Java__io__PrintStream__Instance
	Dispatch__Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__io__PrintStream(Java__lang__CharSequence__Instance, int, int) *Java__io__PrintStream__Instance
	Dispatch__Append__desc__C__ret____obj__Java__io__PrintStream(rune) *Java__io__PrintStream__Instance
}

func (this *Java__io__PrintStream__Instance) Java__io__PrintStream__InitDispatch(v Java__io__PrintStream__Dispatch) {
	this.Java__io__FilterOutputStream__Instance.Java__io__FilterOutputStream__InitDispatch(v)
	this.Java__io__PrintStream__Dispatch = v
}

type Java__io__PrintStream__Instance struct {
	Java__lang__Appendable__Instance
	Java__io__Closeable__Instance
	*Java__io__FilterOutputStream__Instance
	Java__io__PrintStream__Dispatch
}

func (this *Java__io__PrintStream__Instance) Instance_Init__desc____obj__Java__io__OutputStream__ret__V(var1 *Java__io__OutputStream__Instance) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Instance_Init__desc____obj__Java__io__OutputStream__Z__ret__V(var1 *Java__io__OutputStream__Instance, var2 bool) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Instance_Init__desc____obj__Java__io__OutputStream__Z____obj__Java__lang__String__ret__V(var1 *Java__io__OutputStream__Instance, var2 bool, var3 *Java__lang__String__Instance) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Instance_Init__desc____obj__Java__lang__String__ret__V(var1 *Java__lang__String__Instance) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__String__ret__V(var1 *Java__lang__String__Instance, var2 *Java__lang__String__Instance) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Flush__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Close__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) CheckError__desc____ret__Z() bool {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) SetError__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) ClearError__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Write__desc__I__ret__V(var1 int) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Write__desc____arr__B__I__I__ret__V(var1 []byte, var2 int, var3 int) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Print__desc__Z__ret__V(var1 bool) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Print__desc__C__ret__V(var1 rune) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Print__desc__I__ret__V(var1 int) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Print__desc__J__ret__V(var1 int64) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Print__desc__F__ret__V(var1 float32) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Print__desc__D__ret__V(var1 float64) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Print__desc____arr__C__ret__V(var1 []rune) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Print__desc____obj__Java__lang__String__ret__V(var1 *Java__lang__String__Instance) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Print__desc____obj__Java__lang__Object__ret__V(var1 *Java__lang__Object__Instance) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Println__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Println__desc__Z__ret__V(var1 bool) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Println__desc__C__ret__V(var1 rune) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Println__desc__I__ret__V(var1 int) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Println__desc__J__ret__V(var1 int64) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Println__desc__F__ret__V(var1 float32) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Println__desc__D__ret__V(var1 float64) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Println__desc____arr__C__ret__V(var1 []rune) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Println__desc____obj__Java__lang__Object__ret__V(var1 *Java__lang__Object__Instance) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Printf__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(var1 *Java__lang__String__Instance, var2 []*Java__lang__Object__Instance) *Java__io__PrintStream__Instance {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Format__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(var1 *Java__lang__String__Instance, var2 []*Java__lang__Object__Instance) *Java__io__PrintStream__Instance {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__io__PrintStream(var1 Java__lang__CharSequence__Instance) *Java__io__PrintStream__Instance {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__io__PrintStream(var1 Java__lang__CharSequence__Instance, var2 int, var3 int) *Java__io__PrintStream__Instance {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Append__desc__C__ret____obj__Java__io__PrintStream(var1 rune) *Java__io__PrintStream__Instance {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Append__desc__C__ret____obj__Java__lang__Appendable(var1 rune) Java__lang__Appendable__Instance {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__Appendable(var1 Java__lang__CharSequence__Instance, var2 int, var3 int) Java__lang__Appendable__Instance {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__Appendable(var1 Java__lang__CharSequence__Instance) Java__lang__Appendable__Instance {
	panic("Not Implemented")
}

type Java__io__Serializable__Dispatch interface{}

type Java__io__Serializable__Instance interface {
	Java__io__Serializable__Dispatch
}
