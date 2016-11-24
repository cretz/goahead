// Generated from Azul Zulu packaged OpenJDK JAR and carries the same
// GPL license with the classpath exception
package rt

type Java__io__Closeable__Instance interface {
	Close__desc____ret__V()
}

type Java__io__FilterOutputStream__Static struct{}

var Java__io__FilterOutputStream__Var Java__io__FilterOutputStream__Static

func Java__io__FilterOutputStream() *Java__io__FilterOutputStream__Static {
	return &Java__io__FilterOutputStream__Var
}

func (this *Java__io__FilterOutputStream__Static) New() *Java__io__FilterOutputStream__Impl {
	v := &Java__io__FilterOutputStream__Impl{
		Java__io__OutputStream__Impl: Java__io__OutputStream().New(),
	}
	v.Java__io__FilterOutputStream__InitDispatch(v)
	return v
}

type Java__io__FilterOutputStream__Dispatch interface {
	Java__io__OutputStream__Dispatch
	Impl__Instance_Init__desc____obj__Java__io__OutputStream__ret__V(Java__io__OutputStream__Instance)
}

func (this *Java__io__FilterOutputStream__Impl) Java__io__FilterOutputStream__InitDispatch(v Java__io__FilterOutputStream__Dispatch) {
	this.Java__io__OutputStream__Impl.Java__io__OutputStream__InitDispatch(v)
	this._dispatch = v
}

func (this *Java__io__FilterOutputStream__Impl) Instance_Init__desc____obj__Java__io__OutputStream__ret__V(var0 Java__io__OutputStream__Instance) {
	this._dispatch.Impl__Instance_Init__desc____obj__Java__io__OutputStream__ret__V(var0)
}

type Java__io__FilterOutputStream__Instance interface {
	Clone__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance
	Close__desc____ret__V()
	Equals__desc____obj__Java__lang__Object__ret__Z(Java__lang__Object__Instance) bool
	Finalize__desc____ret__V()
	Flush__desc____ret__V()
	GetClass__desc____ret____obj__Java__lang__Class() Java__lang__Class__Instance
	HashCode__desc____ret__I() int
	Notify__desc____ret__V()
	NotifyAll__desc____ret__V()
	ToString__desc____ret____obj__Java__lang__String() Java__lang__String__Instance
	Wait__desc____ret__V()
	Wait__desc__J__ret__V(int64)
	Wait__desc__J__I__ret__V(int64, int)
	Write__desc__I__ret__V(int)
	Write__desc____arr__B__ret__V([]byte)
	Write__desc____arr__B__I__I__ret__V([]byte, int, int)
	FieldGet__Java__io__FilterOutputStream__Out() Java__io__OutputStream__Instance
	FieldSet__Java__io__FilterOutputStream__Out(v Java__io__OutputStream__Instance)
}

type Java__io__FilterOutputStream__Impl struct {
	*Java__io__OutputStream__Impl
	_dispatch Java__io__FilterOutputStream__Dispatch
	Out       Java__io__OutputStream__Instance
}

func (this *Java__io__FilterOutputStream__Impl) FieldGet__Java__io__FilterOutputStream__Out() Java__io__OutputStream__Instance {
	return this.Out
}

func (this *Java__io__FilterOutputStream__Impl) FieldSet__Java__io__FilterOutputStream__Out(v Java__io__OutputStream__Instance) {
	this.Out = v
}

func (this *Java__io__FilterOutputStream__Impl) Impl__Instance_Init__desc____obj__Java__io__OutputStream__ret__V(var0 Java__io__OutputStream__Instance) {
	panic("Not Implemented - java/io/FilterOutputStream.<init>(Ljava/io/OutputStream;)V")
}

func (this *Java__io__FilterOutputStream__Impl) Impl__Close__desc____ret__V() {
	panic("Not Implemented - java/io/FilterOutputStream.close()V")
}

func (this *Java__io__FilterOutputStream__Impl) Impl__Flush__desc____ret__V() {
	panic("Not Implemented - java/io/FilterOutputStream.flush()V")
}

func (this *Java__io__FilterOutputStream__Impl) Impl__Write__desc__I__ret__V(var0 int) {
	panic("Not Implemented - java/io/FilterOutputStream.write(I)V")
}

func (this *Java__io__FilterOutputStream__Impl) Impl__Write__desc____arr__B__ret__V(var0 []byte) {
	panic("Not Implemented - java/io/FilterOutputStream.write([B)V")
}

func (this *Java__io__FilterOutputStream__Impl) Impl__Write__desc____arr__B__I__I__ret__V(var0 []byte, var1 int, var2 int) {
	panic("Not Implemented - java/io/FilterOutputStream.write([BII)V")
}

type Java__io__Flushable__Instance interface {
	Flush__desc____ret__V()
}

type Java__io__OutputStream__Static struct{}

var Java__io__OutputStream__Var Java__io__OutputStream__Static

func Java__io__OutputStream() *Java__io__OutputStream__Static {
	return &Java__io__OutputStream__Var
}

func (this *Java__io__OutputStream__Static) New() *Java__io__OutputStream__Impl {
	v := &Java__io__OutputStream__Impl{
		Java__lang__Object__Impl: Java__lang__Object().New(),
	}
	v.Java__io__OutputStream__InitDispatch(v)
	return v
}

type Java__io__OutputStream__Dispatch interface {
	Java__lang__Object__Dispatch
	Impl__Close__desc____ret__V()
	Impl__Flush__desc____ret__V()
	Impl__Write__desc__I__ret__V(int)
	Impl__Write__desc____arr__B__ret__V([]byte)
	Impl__Write__desc____arr__B__I__I__ret__V([]byte, int, int)
}

func (this *Java__io__OutputStream__Impl) Java__io__OutputStream__InitDispatch(v Java__io__OutputStream__Dispatch) {
	this.Java__lang__Object__Impl.Java__lang__Object__InitDispatch(v)
	this._dispatch = v
}

func (this *Java__io__OutputStream__Impl) Close__desc____ret__V() {
	this._dispatch.Impl__Close__desc____ret__V()
}

func (this *Java__io__OutputStream__Impl) Flush__desc____ret__V() {
	this._dispatch.Impl__Flush__desc____ret__V()
}

func (this *Java__io__OutputStream__Impl) Write__desc__I__ret__V(var0 int) {
	this._dispatch.Impl__Write__desc__I__ret__V(var0)
}

func (this *Java__io__OutputStream__Impl) Write__desc____arr__B__ret__V(var0 []byte) {
	this._dispatch.Impl__Write__desc____arr__B__ret__V(var0)
}

func (this *Java__io__OutputStream__Impl) Write__desc____arr__B__I__I__ret__V(var0 []byte, var1 int, var2 int) {
	this._dispatch.Impl__Write__desc____arr__B__I__I__ret__V(var0, var1, var2)
}

type Java__io__OutputStream__Instance interface {
	Clone__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance
	Close__desc____ret__V()
	Equals__desc____obj__Java__lang__Object__ret__Z(Java__lang__Object__Instance) bool
	Finalize__desc____ret__V()
	Flush__desc____ret__V()
	GetClass__desc____ret____obj__Java__lang__Class() Java__lang__Class__Instance
	HashCode__desc____ret__I() int
	Notify__desc____ret__V()
	NotifyAll__desc____ret__V()
	ToString__desc____ret____obj__Java__lang__String() Java__lang__String__Instance
	Wait__desc____ret__V()
	Wait__desc__J__ret__V(int64)
	Wait__desc__J__I__ret__V(int64, int)
	Write__desc__I__ret__V(int)
	Write__desc____arr__B__ret__V([]byte)
	Write__desc____arr__B__I__I__ret__V([]byte, int, int)
}

type Java__io__OutputStream__Impl struct {
	*Java__lang__Object__Impl
	_dispatch Java__io__OutputStream__Dispatch
}

func (this *Java__io__OutputStream__Impl) Impl__Instance_Init__desc____ret__V() {
	panic("Not Implemented - java/io/OutputStream.<init>()V")
}

func (this *Java__io__OutputStream__Impl) Impl__Close__desc____ret__V() {
	panic("Not Implemented - java/io/OutputStream.close()V")
}

func (this *Java__io__OutputStream__Impl) Impl__Flush__desc____ret__V() {
	panic("Not Implemented - java/io/OutputStream.flush()V")
}

func (this *Java__io__OutputStream__Impl) Impl__Write__desc__I__ret__V(var0 int) {
	panic("Not Implemented - java/io/OutputStream.write(I)V")
}

func (this *Java__io__OutputStream__Impl) Impl__Write__desc____arr__B__ret__V(var0 []byte) {
	panic("Not Implemented - java/io/OutputStream.write([B)V")
}

func (this *Java__io__OutputStream__Impl) Impl__Write__desc____arr__B__I__I__ret__V(var0 []byte, var1 int, var2 int) {
	panic("Not Implemented - java/io/OutputStream.write([BII)V")
}

type Java__io__PrintStream__Static struct{}

var Java__io__PrintStream__Var Java__io__PrintStream__Static

func Java__io__PrintStream() *Java__io__PrintStream__Static {
	return &Java__io__PrintStream__Var
}

func (this *Java__io__PrintStream__Static) New() *Java__io__PrintStream__Impl {
	v := &Java__io__PrintStream__Impl{
		Java__io__FilterOutputStream__Impl: Java__io__FilterOutputStream().New(),
	}
	v.Java__io__PrintStream__InitDispatch(v)
	return v
}

type Java__io__PrintStream__Dispatch interface {
	Java__io__FilterOutputStream__Dispatch
	Impl__Instance_Init__desc____obj__Java__io__OutputStream__Z__ret__V(Java__io__OutputStream__Instance, bool)
	Impl__Instance_Init__desc____obj__Java__io__OutputStream__Z____obj__Java__lang__String__ret__V(Java__io__OutputStream__Instance, bool, Java__lang__String__Instance)
	Impl__Instance_Init__desc____obj__Java__lang__String__ret__V(Java__lang__String__Instance)
	Impl__Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__String__ret__V(Java__lang__String__Instance, Java__lang__String__Instance)
	Impl__Append__desc__C__ret____obj__Java__io__PrintStream(rune) Java__io__PrintStream__Instance
	Impl__Append__desc__C__ret____obj__Java__lang__Appendable(rune) Java__lang__Appendable__Instance
	Impl__Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__io__PrintStream(Java__lang__CharSequence__Instance) Java__io__PrintStream__Instance
	Impl__Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__Appendable(Java__lang__CharSequence__Instance) Java__lang__Appendable__Instance
	Impl__Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__io__PrintStream(Java__lang__CharSequence__Instance, int, int) Java__io__PrintStream__Instance
	Impl__Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__Appendable(Java__lang__CharSequence__Instance, int, int) Java__lang__Appendable__Instance
	Impl__CheckError__desc____ret__Z() bool
	Impl__ClearError__desc____ret__V()
	Impl__Format__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(Java__lang__String__Instance, []Java__lang__Object__Instance) Java__io__PrintStream__Instance
	Impl__Print__desc__C__ret__V(rune)
	Impl__Print__desc__D__ret__V(float64)
	Impl__Print__desc__F__ret__V(float32)
	Impl__Print__desc__I__ret__V(int)
	Impl__Print__desc__J__ret__V(int64)
	Impl__Print__desc____obj__Java__lang__Object__ret__V(Java__lang__Object__Instance)
	Impl__Print__desc____obj__Java__lang__String__ret__V(Java__lang__String__Instance)
	Impl__Print__desc__Z__ret__V(bool)
	Impl__Print__desc____arr__C__ret__V([]rune)
	Impl__Printf__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(Java__lang__String__Instance, []Java__lang__Object__Instance) Java__io__PrintStream__Instance
	Impl__Println__desc____ret__V()
	Impl__Println__desc__C__ret__V(rune)
	Impl__Println__desc__D__ret__V(float64)
	Impl__Println__desc__F__ret__V(float32)
	Impl__Println__desc__I__ret__V(int)
	Impl__Println__desc__J__ret__V(int64)
	Impl__Println__desc____obj__Java__lang__Object__ret__V(Java__lang__Object__Instance)
	Impl__Println__desc____obj__Java__lang__String__ret__V(Java__lang__String__Instance)
	Impl__Println__desc__Z__ret__V(bool)
	Impl__Println__desc____arr__C__ret__V([]rune)
	Impl__SetError__desc____ret__V()
}

func (this *Java__io__PrintStream__Impl) Java__io__PrintStream__InitDispatch(v Java__io__PrintStream__Dispatch) {
	this.Java__io__FilterOutputStream__Impl.Java__io__FilterOutputStream__InitDispatch(v)
	this._dispatch = v
}

func (this *Java__io__PrintStream__Impl) Instance_Init__desc____obj__Java__io__OutputStream__Z__ret__V(var0 Java__io__OutputStream__Instance, var1 bool) {
	this._dispatch.Impl__Instance_Init__desc____obj__Java__io__OutputStream__Z__ret__V(var0, var1)
}

func (this *Java__io__PrintStream__Impl) Instance_Init__desc____obj__Java__io__OutputStream__Z____obj__Java__lang__String__ret__V(var0 Java__io__OutputStream__Instance, var1 bool, var2 Java__lang__String__Instance) {
	this._dispatch.Impl__Instance_Init__desc____obj__Java__io__OutputStream__Z____obj__Java__lang__String__ret__V(var0, var1, var2)
}

func (this *Java__io__PrintStream__Impl) Instance_Init__desc____obj__Java__lang__String__ret__V(var0 Java__lang__String__Instance) {
	this._dispatch.Impl__Instance_Init__desc____obj__Java__lang__String__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__String__ret__V(var0 Java__lang__String__Instance, var1 Java__lang__String__Instance) {
	this._dispatch.Impl__Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__String__ret__V(var0, var1)
}

func (this *Java__io__PrintStream__Impl) Append__desc__C__ret____obj__Java__io__PrintStream(var0 rune) Java__io__PrintStream__Instance {
	return this._dispatch.Impl__Append__desc__C__ret____obj__Java__io__PrintStream(var0)
}

func (this *Java__io__PrintStream__Impl) Append__desc__C__ret____obj__Java__lang__Appendable(var0 rune) Java__lang__Appendable__Instance {
	return this._dispatch.Impl__Append__desc__C__ret____obj__Java__lang__Appendable(var0)
}

func (this *Java__io__PrintStream__Impl) Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__io__PrintStream(var0 Java__lang__CharSequence__Instance) Java__io__PrintStream__Instance {
	return this._dispatch.Impl__Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__io__PrintStream(var0)
}

func (this *Java__io__PrintStream__Impl) Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__Appendable(var0 Java__lang__CharSequence__Instance) Java__lang__Appendable__Instance {
	return this._dispatch.Impl__Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__Appendable(var0)
}

func (this *Java__io__PrintStream__Impl) Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__io__PrintStream(var0 Java__lang__CharSequence__Instance, var1 int, var2 int) Java__io__PrintStream__Instance {
	return this._dispatch.Impl__Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__io__PrintStream(var0, var1, var2)
}

func (this *Java__io__PrintStream__Impl) Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__Appendable(var0 Java__lang__CharSequence__Instance, var1 int, var2 int) Java__lang__Appendable__Instance {
	return this._dispatch.Impl__Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__Appendable(var0, var1, var2)
}

func (this *Java__io__PrintStream__Impl) CheckError__desc____ret__Z() bool {
	return this._dispatch.Impl__CheckError__desc____ret__Z()
}

func (this *Java__io__PrintStream__Impl) ClearError__desc____ret__V() {
	this._dispatch.Impl__ClearError__desc____ret__V()
}

func (this *Java__io__PrintStream__Impl) Format__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(var0 Java__lang__String__Instance, var1 []Java__lang__Object__Instance) Java__io__PrintStream__Instance {
	return this._dispatch.Impl__Format__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(var0, var1)
}

func (this *Java__io__PrintStream__Impl) Print__desc__C__ret__V(var0 rune) {
	this._dispatch.Impl__Print__desc__C__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) Print__desc__D__ret__V(var0 float64) {
	this._dispatch.Impl__Print__desc__D__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) Print__desc__F__ret__V(var0 float32) {
	this._dispatch.Impl__Print__desc__F__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) Print__desc__I__ret__V(var0 int) {
	this._dispatch.Impl__Print__desc__I__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) Print__desc__J__ret__V(var0 int64) {
	this._dispatch.Impl__Print__desc__J__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) Print__desc____obj__Java__lang__Object__ret__V(var0 Java__lang__Object__Instance) {
	this._dispatch.Impl__Print__desc____obj__Java__lang__Object__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) Print__desc____obj__Java__lang__String__ret__V(var0 Java__lang__String__Instance) {
	this._dispatch.Impl__Print__desc____obj__Java__lang__String__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) Print__desc__Z__ret__V(var0 bool) {
	this._dispatch.Impl__Print__desc__Z__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) Print__desc____arr__C__ret__V(var0 []rune) {
	this._dispatch.Impl__Print__desc____arr__C__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) Printf__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(var0 Java__lang__String__Instance, var1 []Java__lang__Object__Instance) Java__io__PrintStream__Instance {
	return this._dispatch.Impl__Printf__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(var0, var1)
}

func (this *Java__io__PrintStream__Impl) Println__desc____ret__V() {
	this._dispatch.Impl__Println__desc____ret__V()
}

func (this *Java__io__PrintStream__Impl) Println__desc__C__ret__V(var0 rune) {
	this._dispatch.Impl__Println__desc__C__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) Println__desc__D__ret__V(var0 float64) {
	this._dispatch.Impl__Println__desc__D__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) Println__desc__F__ret__V(var0 float32) {
	this._dispatch.Impl__Println__desc__F__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) Println__desc__I__ret__V(var0 int) {
	this._dispatch.Impl__Println__desc__I__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) Println__desc__J__ret__V(var0 int64) {
	this._dispatch.Impl__Println__desc__J__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) Println__desc____obj__Java__lang__Object__ret__V(var0 Java__lang__Object__Instance) {
	this._dispatch.Impl__Println__desc____obj__Java__lang__Object__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) Println__desc____obj__Java__lang__String__ret__V(var0 Java__lang__String__Instance) {
	this._dispatch.Impl__Println__desc____obj__Java__lang__String__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) Println__desc__Z__ret__V(var0 bool) {
	this._dispatch.Impl__Println__desc__Z__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) Println__desc____arr__C__ret__V(var0 []rune) {
	this._dispatch.Impl__Println__desc____arr__C__ret__V(var0)
}

func (this *Java__io__PrintStream__Impl) SetError__desc____ret__V() {
	this._dispatch.Impl__SetError__desc____ret__V()
}

type Java__io__PrintStream__Instance interface {
	Append__desc__C__ret____obj__Java__io__PrintStream(rune) Java__io__PrintStream__Instance
	Append__desc__C__ret____obj__Java__lang__Appendable(rune) Java__lang__Appendable__Instance
	Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__io__PrintStream(Java__lang__CharSequence__Instance) Java__io__PrintStream__Instance
	Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__Appendable(Java__lang__CharSequence__Instance) Java__lang__Appendable__Instance
	Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__io__PrintStream(Java__lang__CharSequence__Instance, int, int) Java__io__PrintStream__Instance
	Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__Appendable(Java__lang__CharSequence__Instance, int, int) Java__lang__Appendable__Instance
	CheckError__desc____ret__Z() bool
	ClearError__desc____ret__V()
	Clone__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance
	Close__desc____ret__V()
	Equals__desc____obj__Java__lang__Object__ret__Z(Java__lang__Object__Instance) bool
	Finalize__desc____ret__V()
	Flush__desc____ret__V()
	Format__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(Java__lang__String__Instance, []Java__lang__Object__Instance) Java__io__PrintStream__Instance
	GetClass__desc____ret____obj__Java__lang__Class() Java__lang__Class__Instance
	HashCode__desc____ret__I() int
	Notify__desc____ret__V()
	NotifyAll__desc____ret__V()
	Print__desc__C__ret__V(rune)
	Print__desc__D__ret__V(float64)
	Print__desc__F__ret__V(float32)
	Print__desc__I__ret__V(int)
	Print__desc__J__ret__V(int64)
	Print__desc____obj__Java__lang__Object__ret__V(Java__lang__Object__Instance)
	Print__desc____obj__Java__lang__String__ret__V(Java__lang__String__Instance)
	Print__desc__Z__ret__V(bool)
	Print__desc____arr__C__ret__V([]rune)
	Printf__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(Java__lang__String__Instance, []Java__lang__Object__Instance) Java__io__PrintStream__Instance
	Println__desc____ret__V()
	Println__desc__C__ret__V(rune)
	Println__desc__D__ret__V(float64)
	Println__desc__F__ret__V(float32)
	Println__desc__I__ret__V(int)
	Println__desc__J__ret__V(int64)
	Println__desc____obj__Java__lang__Object__ret__V(Java__lang__Object__Instance)
	Println__desc____obj__Java__lang__String__ret__V(Java__lang__String__Instance)
	Println__desc__Z__ret__V(bool)
	Println__desc____arr__C__ret__V([]rune)
	SetError__desc____ret__V()
	ToString__desc____ret____obj__Java__lang__String() Java__lang__String__Instance
	Wait__desc____ret__V()
	Wait__desc__J__ret__V(int64)
	Wait__desc__J__I__ret__V(int64, int)
	Write__desc__I__ret__V(int)
	Write__desc____arr__B__ret__V([]byte)
	Write__desc____arr__B__I__I__ret__V([]byte, int, int)
	FieldGet__Java__io__FilterOutputStream__Out() Java__io__OutputStream__Instance
	FieldSet__Java__io__FilterOutputStream__Out(v Java__io__OutputStream__Instance)
}

type Java__io__PrintStream__Impl struct {
	*Java__io__FilterOutputStream__Impl
	_dispatch Java__io__PrintStream__Dispatch
}

func (this *Java__io__PrintStream__Impl) Impl__Instance_Init__desc____obj__Java__io__OutputStream__ret__V(var0 Java__io__OutputStream__Instance) {
	panic("Not Implemented - java/io/PrintStream.<init>(Ljava/io/OutputStream;)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Instance_Init__desc____obj__Java__io__OutputStream__Z__ret__V(var0 Java__io__OutputStream__Instance, var1 bool) {
	panic("Not Implemented - java/io/PrintStream.<init>(Ljava/io/OutputStream;Z)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Instance_Init__desc____obj__Java__io__OutputStream__Z____obj__Java__lang__String__ret__V(var0 Java__io__OutputStream__Instance, var1 bool, var2 Java__lang__String__Instance) {
	panic("Not Implemented - java/io/PrintStream.<init>(Ljava/io/OutputStream;ZLjava/lang/String;)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Instance_Init__desc____obj__Java__lang__String__ret__V(var0 Java__lang__String__Instance) {
	panic("Not Implemented - java/io/PrintStream.<init>(Ljava/lang/String;)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__String__ret__V(var0 Java__lang__String__Instance, var1 Java__lang__String__Instance) {
	panic("Not Implemented - java/io/PrintStream.<init>(Ljava/lang/String;Ljava/lang/String;)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Append__desc__C__ret____obj__Java__io__PrintStream(var0 rune) Java__io__PrintStream__Instance {
	panic("Not Implemented - java/io/PrintStream.append(C)Ljava/io/PrintStream;")
}

func (this *Java__io__PrintStream__Impl) Impl__Append__desc__C__ret____obj__Java__lang__Appendable(var0 rune) Java__lang__Appendable__Instance {
	panic("Not Implemented - java/io/PrintStream.append(C)Ljava/lang/Appendable;")
}

func (this *Java__io__PrintStream__Impl) Impl__Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__io__PrintStream(var0 Java__lang__CharSequence__Instance) Java__io__PrintStream__Instance {
	panic("Not Implemented - java/io/PrintStream.append(Ljava/lang/CharSequence;)Ljava/io/PrintStream;")
}

func (this *Java__io__PrintStream__Impl) Impl__Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__Appendable(var0 Java__lang__CharSequence__Instance) Java__lang__Appendable__Instance {
	panic("Not Implemented - java/io/PrintStream.append(Ljava/lang/CharSequence;)Ljava/lang/Appendable;")
}

func (this *Java__io__PrintStream__Impl) Impl__Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__io__PrintStream(var0 Java__lang__CharSequence__Instance, var1 int, var2 int) Java__io__PrintStream__Instance {
	panic("Not Implemented - java/io/PrintStream.append(Ljava/lang/CharSequence;II)Ljava/io/PrintStream;")
}

func (this *Java__io__PrintStream__Impl) Impl__Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__Appendable(var0 Java__lang__CharSequence__Instance, var1 int, var2 int) Java__lang__Appendable__Instance {
	panic("Not Implemented - java/io/PrintStream.append(Ljava/lang/CharSequence;II)Ljava/lang/Appendable;")
}

func (this *Java__io__PrintStream__Impl) Impl__CheckError__desc____ret__Z() bool {
	panic("Not Implemented - java/io/PrintStream.checkError()Z")
}

func (this *Java__io__PrintStream__Impl) Impl__ClearError__desc____ret__V() {
	panic("Not Implemented - java/io/PrintStream.clearError()V")
}

func (this *Java__io__PrintStream__Impl) Impl__Close__desc____ret__V() {
	panic("Not Implemented - java/io/PrintStream.close()V")
}

func (this *Java__io__PrintStream__Impl) Impl__Flush__desc____ret__V() {
	panic("Not Implemented - java/io/PrintStream.flush()V")
}

func (this *Java__io__PrintStream__Impl) Impl__Format__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(var0 Java__lang__String__Instance, var1 []Java__lang__Object__Instance) Java__io__PrintStream__Instance {
	panic("Not Implemented - java/io/PrintStream.format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/io/PrintStream;")
}

func (this *Java__io__PrintStream__Impl) Impl__Print__desc__C__ret__V(var0 rune) {
	panic("Not Implemented - java/io/PrintStream.print(C)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Print__desc__D__ret__V(var0 float64) {
	panic("Not Implemented - java/io/PrintStream.print(D)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Print__desc__F__ret__V(var0 float32) {
	panic("Not Implemented - java/io/PrintStream.print(F)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Print__desc__I__ret__V(var0 int) {
	panic("Not Implemented - java/io/PrintStream.print(I)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Print__desc__J__ret__V(var0 int64) {
	panic("Not Implemented - java/io/PrintStream.print(J)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Print__desc____obj__Java__lang__Object__ret__V(var0 Java__lang__Object__Instance) {
	panic("Not Implemented - java/io/PrintStream.print(Ljava/lang/Object;)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Print__desc____obj__Java__lang__String__ret__V(var0 Java__lang__String__Instance) {
	panic("Not Implemented - java/io/PrintStream.print(Ljava/lang/String;)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Print__desc__Z__ret__V(var0 bool) {
	panic("Not Implemented - java/io/PrintStream.print(Z)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Print__desc____arr__C__ret__V(var0 []rune) {
	panic("Not Implemented - java/io/PrintStream.print([C)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Printf__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__io__PrintStream(var0 Java__lang__String__Instance, var1 []Java__lang__Object__Instance) Java__io__PrintStream__Instance {
	panic("Not Implemented - java/io/PrintStream.printf(Ljava/lang/String;[Ljava/lang/Object;)Ljava/io/PrintStream;")
}

func (this *Java__io__PrintStream__Impl) Impl__Println__desc____ret__V() {
	panic("Not Implemented - java/io/PrintStream.println()V")
}

func (this *Java__io__PrintStream__Impl) Impl__Println__desc__C__ret__V(var0 rune) {
	panic("Not Implemented - java/io/PrintStream.println(C)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Println__desc__D__ret__V(var0 float64) {
	panic("Not Implemented - java/io/PrintStream.println(D)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Println__desc__F__ret__V(var0 float32) {
	panic("Not Implemented - java/io/PrintStream.println(F)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Println__desc__J__ret__V(var0 int64) {
	panic("Not Implemented - java/io/PrintStream.println(J)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Println__desc____obj__Java__lang__Object__ret__V(var0 Java__lang__Object__Instance) {
	panic("Not Implemented - java/io/PrintStream.println(Ljava/lang/Object;)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Println__desc____arr__C__ret__V(var0 []rune) {
	panic("Not Implemented - java/io/PrintStream.println([C)V")
}

func (this *Java__io__PrintStream__Impl) Impl__SetError__desc____ret__V() {
	panic("Not Implemented - java/io/PrintStream.setError()V")
}

func (this *Java__io__PrintStream__Impl) Impl__Write__desc__I__ret__V(var0 int) {
	panic("Not Implemented - java/io/PrintStream.write(I)V")
}

func (this *Java__io__PrintStream__Impl) Impl__Write__desc____arr__B__I__I__ret__V(var0 []byte, var1 int, var2 int) {
	panic("Not Implemented - java/io/PrintStream.write([BII)V")
}

type Java__io__Serializable__Instance interface{}
