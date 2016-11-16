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
	Instance_Init__desc____obj__Java__io__OutputStream__ret__V(*Java__io__OutputStream__Instance)
}

func (this *Java__io__FilterOutputStream__Instance) Java__io__FilterOutputStream__InitDispatch(v Java__io__FilterOutputStream__Dispatch) {
	this.Java__io__OutputStream__Instance.Java__io__OutputStream__InitDispatch(v)
	this._dispatch = v
}

func (this *Java__io__FilterOutputStream__Instance) Forward__Instance_Init__desc____obj__Java__io__OutputStream__ret__V(var1 *Java__io__OutputStream__Instance) {
	this._dispatch.Instance_Init__desc____obj__Java__io__OutputStream__ret__V(var1)
}

type Java__io__FilterOutputStream__Instance struct {
	*Java__io__OutputStream__Instance
	_dispatch Java__io__FilterOutputStream__Dispatch
	Out       *Java__io__OutputStream__Instance
}

func (this *Java__io__FilterOutputStream__Instance) Instance_Init__desc____obj__Java__io__OutputStream__ret__V(var1 *Java__io__OutputStream__Instance) {
	panic("Not Implemented")
}

func (this *Java__io__FilterOutputStream__Instance) Close__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__io__FilterOutputStream__Instance) Flush__desc____ret__V() {
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

type Java__io__Flushable__Dispatch interface {
	Flush__desc____ret__V()
}

type Java__io__Flushable__Instance interface {
	Java__io__Flushable__Dispatch
	Forward__Flush__desc____ret__V()
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
	Close__desc____ret__V()
	Flush__desc____ret__V()
	Write__desc__I__ret__V(int)
	Write__desc____arr__B__ret__V([]byte)
	Write__desc____arr__B__I__I__ret__V([]byte, int, int)
}

func (this *Java__io__OutputStream__Instance) Java__io__OutputStream__InitDispatch(v Java__io__OutputStream__Dispatch) {
	this.Java__lang__Object__Instance.Java__lang__Object__InitDispatch(v)
	this._dispatch = v
}

func (this *Java__io__OutputStream__Instance) Forward__Close__desc____ret__V() {
	this._dispatch.Close__desc____ret__V()
}

func (this *Java__io__OutputStream__Instance) Forward__Flush__desc____ret__V() {
	this._dispatch.Flush__desc____ret__V()
}

func (this *Java__io__OutputStream__Instance) Forward__Write__desc__I__ret__V(var1 int) {
	this._dispatch.Write__desc__I__ret__V(var1)
}

func (this *Java__io__OutputStream__Instance) Forward__Write__desc____arr__B__ret__V(var1 []byte) {
	this._dispatch.Write__desc____arr__B__ret__V(var1)
}

func (this *Java__io__OutputStream__Instance) Forward__Write__desc____arr__B__I__I__ret__V(var1 []byte, var2 int, var3 int) {
	this._dispatch.Write__desc____arr__B__I__I__ret__V(var1, var2, var3)
}

type Java__io__OutputStream__Instance struct {
	*Java__lang__Object__Instance
	_dispatch Java__io__OutputStream__Dispatch
}

func (this *Java__io__OutputStream__Instance) Instance_Init__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__io__OutputStream__Instance) Close__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__io__OutputStream__Instance) Flush__desc____ret__V() {
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
	Instance_Init__desc____obj__Java__io__OutputStream__Z__ret__V(*Java__io__OutputStream__Instance, bool)
	Instance_Init__desc____obj__Java__io__OutputStream__Z____obj__Java__lang__String__ret__V(*Java__io__OutputStream__Instance, bool, *Java__lang__String__Instance)
	Instance_Init__desc____obj__Java__lang__String__ret__V(*Java__lang__String__Instance)
	Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__String__ret__V(*Java__lang__String__Instance, *Java__lang__String__Instance)
	Append__desc__C__ret____obj__Java__io__PrintStream(rune) *Java__io__PrintStream__Instance
	Append__desc__C__ret____obj__Java__lang__Appendable(rune) Java__lang__Appendable__Instance
	Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__io__PrintStream(Java__lang__CharSequence__Instance) *Java__io__PrintStream__Instance
	Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__Appendable(Java__lang__CharSequence__Instance) Java__lang__Appendable__Instance
	Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__io__PrintStream(Java__lang__CharSequence__Instance, int, int) *Java__io__PrintStream__Instance
	Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__Appendable(Java__lang__CharSequence__Instance, int, int) Java__lang__Appendable__Instance
	CheckError__desc____ret__Z() bool
	ClearError__desc____ret__V()
	Format__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(*Java__lang__String__Instance, []*Java__lang__Object__Instance) *Java__io__PrintStream__Instance
	Print__desc__C__ret__V(rune)
	Print__desc__D__ret__V(float64)
	Print__desc__F__ret__V(float32)
	Print__desc__I__ret__V(int)
	Print__desc__J__ret__V(int64)
	Print__desc____obj__Java__lang__Object__ret__V(*Java__lang__Object__Instance)
	Print__desc____obj__Java__lang__String__ret__V(*Java__lang__String__Instance)
	Print__desc__Z__ret__V(bool)
	Print__desc____arr__C__ret__V([]rune)
	Printf__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(*Java__lang__String__Instance, []*Java__lang__Object__Instance) *Java__io__PrintStream__Instance
	Println__desc____ret__V()
	Println__desc__C__ret__V(rune)
	Println__desc__D__ret__V(float64)
	Println__desc__F__ret__V(float32)
	Println__desc__I__ret__V(int)
	Println__desc__J__ret__V(int64)
	Println__desc____obj__Java__lang__Object__ret__V(*Java__lang__Object__Instance)
	Println__desc____obj__Java__lang__String__ret__V(*Java__lang__String__Instance)
	Println__desc__Z__ret__V(bool)
	Println__desc____arr__C__ret__V([]rune)
	SetError__desc____ret__V()
}

func (this *Java__io__PrintStream__Instance) Java__io__PrintStream__InitDispatch(v Java__io__PrintStream__Dispatch) {
	this.Java__io__FilterOutputStream__Instance.Java__io__FilterOutputStream__InitDispatch(v)
	this._dispatch = v
}

func (this *Java__io__PrintStream__Instance) Forward__Instance_Init__desc____obj__Java__io__OutputStream__Z__ret__V(var1 *Java__io__OutputStream__Instance, var2 bool) {
	this._dispatch.Instance_Init__desc____obj__Java__io__OutputStream__Z__ret__V(var1, var2)
}

func (this *Java__io__PrintStream__Instance) Forward__Instance_Init__desc____obj__Java__io__OutputStream__Z____obj__Java__lang__String__ret__V(var1 *Java__io__OutputStream__Instance, var2 bool, var3 *Java__lang__String__Instance) {
	this._dispatch.Instance_Init__desc____obj__Java__io__OutputStream__Z____obj__Java__lang__String__ret__V(var1, var2, var3)
}

func (this *Java__io__PrintStream__Instance) Forward__Instance_Init__desc____obj__Java__lang__String__ret__V(var1 *Java__lang__String__Instance) {
	this._dispatch.Instance_Init__desc____obj__Java__lang__String__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__String__ret__V(var1 *Java__lang__String__Instance, var2 *Java__lang__String__Instance) {
	this._dispatch.Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__String__ret__V(var1, var2)
}

func (this *Java__io__PrintStream__Instance) Forward__Append__desc__C__ret____obj__Java__io__PrintStream(var1 rune) *Java__io__PrintStream__Instance {
	return this._dispatch.Append__desc__C__ret____obj__Java__io__PrintStream(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Append__desc__C__ret____obj__Java__lang__Appendable(var1 rune) Java__lang__Appendable__Instance {
	return this._dispatch.Append__desc__C__ret____obj__Java__lang__Appendable(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__io__PrintStream(var1 Java__lang__CharSequence__Instance) *Java__io__PrintStream__Instance {
	return this._dispatch.Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__io__PrintStream(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__Appendable(var1 Java__lang__CharSequence__Instance) Java__lang__Appendable__Instance {
	return this._dispatch.Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__Appendable(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__io__PrintStream(var1 Java__lang__CharSequence__Instance, var2 int, var3 int) *Java__io__PrintStream__Instance {
	return this._dispatch.Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__io__PrintStream(var1, var2, var3)
}

func (this *Java__io__PrintStream__Instance) Forward__Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__Appendable(var1 Java__lang__CharSequence__Instance, var2 int, var3 int) Java__lang__Appendable__Instance {
	return this._dispatch.Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__Appendable(var1, var2, var3)
}

func (this *Java__io__PrintStream__Instance) Forward__CheckError__desc____ret__Z() bool {
	return this._dispatch.CheckError__desc____ret__Z()
}

func (this *Java__io__PrintStream__Instance) Forward__ClearError__desc____ret__V() {
	this._dispatch.ClearError__desc____ret__V()
}

func (this *Java__io__PrintStream__Instance) Forward__Format__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(var1 *Java__lang__String__Instance, var2 []*Java__lang__Object__Instance) *Java__io__PrintStream__Instance {
	return this._dispatch.Format__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(var1, var2)
}

func (this *Java__io__PrintStream__Instance) Forward__Print__desc__C__ret__V(var1 rune) {
	this._dispatch.Print__desc__C__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Print__desc__D__ret__V(var1 float64) {
	this._dispatch.Print__desc__D__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Print__desc__F__ret__V(var1 float32) {
	this._dispatch.Print__desc__F__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Print__desc__I__ret__V(var1 int) {
	this._dispatch.Print__desc__I__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Print__desc__J__ret__V(var1 int64) {
	this._dispatch.Print__desc__J__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Print__desc____obj__Java__lang__Object__ret__V(var1 *Java__lang__Object__Instance) {
	this._dispatch.Print__desc____obj__Java__lang__Object__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Print__desc____obj__Java__lang__String__ret__V(var1 *Java__lang__String__Instance) {
	this._dispatch.Print__desc____obj__Java__lang__String__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Print__desc__Z__ret__V(var1 bool) {
	this._dispatch.Print__desc__Z__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Print__desc____arr__C__ret__V(var1 []rune) {
	this._dispatch.Print__desc____arr__C__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Printf__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(var1 *Java__lang__String__Instance, var2 []*Java__lang__Object__Instance) *Java__io__PrintStream__Instance {
	return this._dispatch.Printf__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(var1, var2)
}

func (this *Java__io__PrintStream__Instance) Forward__Println__desc____ret__V() {
	this._dispatch.Println__desc____ret__V()
}

func (this *Java__io__PrintStream__Instance) Forward__Println__desc__C__ret__V(var1 rune) {
	this._dispatch.Println__desc__C__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Println__desc__D__ret__V(var1 float64) {
	this._dispatch.Println__desc__D__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Println__desc__F__ret__V(var1 float32) {
	this._dispatch.Println__desc__F__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Println__desc__I__ret__V(var1 int) {
	this._dispatch.Println__desc__I__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Println__desc__J__ret__V(var1 int64) {
	this._dispatch.Println__desc__J__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Println__desc____obj__Java__lang__Object__ret__V(var1 *Java__lang__Object__Instance) {
	this._dispatch.Println__desc____obj__Java__lang__Object__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Println__desc____obj__Java__lang__String__ret__V(var1 *Java__lang__String__Instance) {
	this._dispatch.Println__desc____obj__Java__lang__String__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Println__desc__Z__ret__V(var1 bool) {
	this._dispatch.Println__desc__Z__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__Println__desc____arr__C__ret__V(var1 []rune) {
	this._dispatch.Println__desc____arr__C__ret__V(var1)
}

func (this *Java__io__PrintStream__Instance) Forward__SetError__desc____ret__V() {
	this._dispatch.SetError__desc____ret__V()
}

type Java__io__PrintStream__Instance struct {
	*Java__io__FilterOutputStream__Instance
	_dispatch Java__io__PrintStream__Dispatch
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

func (this *Java__io__PrintStream__Instance) Append__desc__C__ret____obj__Java__io__PrintStream(var1 rune) *Java__io__PrintStream__Instance {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Append__desc__C__ret____obj__Java__lang__Appendable(var1 rune) Java__lang__Appendable__Instance {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__io__PrintStream(var1 Java__lang__CharSequence__Instance) *Java__io__PrintStream__Instance {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__Appendable(var1 Java__lang__CharSequence__Instance) Java__lang__Appendable__Instance {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__io__PrintStream(var1 Java__lang__CharSequence__Instance, var2 int, var3 int) *Java__io__PrintStream__Instance {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__Appendable(var1 Java__lang__CharSequence__Instance, var2 int, var3 int) Java__lang__Appendable__Instance {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) CheckError__desc____ret__Z() bool {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) ClearError__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Close__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Flush__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Format__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(var1 *Java__lang__String__Instance, var2 []*Java__lang__Object__Instance) *Java__io__PrintStream__Instance {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Print__desc__C__ret__V(var1 rune) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Print__desc__D__ret__V(var1 float64) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Print__desc__F__ret__V(var1 float32) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Print__desc__I__ret__V(var1 int) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Print__desc__J__ret__V(var1 int64) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Print__desc____obj__Java__lang__Object__ret__V(var1 *Java__lang__Object__Instance) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Print__desc____obj__Java__lang__String__ret__V(var1 *Java__lang__String__Instance) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Print__desc__Z__ret__V(var1 bool) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Print__desc____arr__C__ret__V(var1 []rune) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Printf__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(var1 *Java__lang__String__Instance, var2 []*Java__lang__Object__Instance) *Java__io__PrintStream__Instance {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Println__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Println__desc__C__ret__V(var1 rune) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Println__desc__D__ret__V(var1 float64) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Println__desc__F__ret__V(var1 float32) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Println__desc__J__ret__V(var1 int64) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Println__desc____obj__Java__lang__Object__ret__V(var1 *Java__lang__Object__Instance) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Println__desc____arr__C__ret__V(var1 []rune) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) SetError__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Write__desc__I__ret__V(var1 int) {
	panic("Not Implemented")
}

func (this *Java__io__PrintStream__Instance) Write__desc____arr__B__I__I__ret__V(var1 []byte, var2 int, var3 int) {
	panic("Not Implemented")
}

type Java__io__Serializable__Dispatch interface{}

type Java__io__Serializable__Instance interface {
	Java__io__Serializable__Dispatch
}
