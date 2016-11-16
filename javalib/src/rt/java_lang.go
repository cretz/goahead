// Generated from Azul Zulu packaged OpenJDK JAR and carries the same
// GPL license with the classpath exception
package rt

import "sync"

type Java__lang__AbstractStringBuilder__Static struct{}

var Java__lang__AbstractStringBuilder__Var Java__lang__AbstractStringBuilder__Static

func Java__lang__AbstractStringBuilder() *Java__lang__AbstractStringBuilder__Static {
	return &Java__lang__AbstractStringBuilder__Var
}

func (this *Java__lang__AbstractStringBuilder__Static) New() *Java__lang__AbstractStringBuilder__Instance {
	v := &Java__lang__AbstractStringBuilder__Instance{
		Java__lang__Object__Instance: Java__lang__Object().New(),
	}
	v.Java__lang__AbstractStringBuilder__InitDispatch(v)
	return v
}

type Java__lang__AbstractStringBuilder__Dispatch interface {
	Java__lang__Object__Dispatch
	Instance_Init__desc__I__ret__V(int)
	Append__desc__C__ret____obj__Java__lang__AbstractStringBuilder(rune) *Java__lang__AbstractStringBuilder__Instance
	Append__desc__C__ret____obj__Java__lang__Appendable(rune) Java__lang__Appendable__Instance
	Append__desc__D__ret____obj__Java__lang__AbstractStringBuilder(float64) *Java__lang__AbstractStringBuilder__Instance
	Append__desc__F__ret____obj__Java__lang__AbstractStringBuilder(float32) *Java__lang__AbstractStringBuilder__Instance
	Append__desc__I__ret____obj__Java__lang__AbstractStringBuilder(int) *Java__lang__AbstractStringBuilder__Instance
	Append__desc__J__ret____obj__Java__lang__AbstractStringBuilder(int64) *Java__lang__AbstractStringBuilder__Instance
	Append__desc____obj__Java__lang__AbstractStringBuilder__ret____obj__Java__lang__AbstractStringBuilder(*Java__lang__AbstractStringBuilder__Instance) *Java__lang__AbstractStringBuilder__Instance
	Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__AbstractStringBuilder(Java__lang__CharSequence__Instance) *Java__lang__AbstractStringBuilder__Instance
	Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__Appendable(Java__lang__CharSequence__Instance) Java__lang__Appendable__Instance
	Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__AbstractStringBuilder(Java__lang__CharSequence__Instance, int, int) *Java__lang__AbstractStringBuilder__Instance
	Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__Appendable(Java__lang__CharSequence__Instance, int, int) Java__lang__Appendable__Instance
	Append__desc____obj__Java__lang__Object__ret____obj__Java__lang__AbstractStringBuilder(*Java__lang__Object__Instance) *Java__lang__AbstractStringBuilder__Instance
	Append__desc____obj__Java__lang__String__ret____obj__Java__lang__AbstractStringBuilder(*Java__lang__String__Instance) *Java__lang__AbstractStringBuilder__Instance
	Append__desc__Z__ret____obj__Java__lang__AbstractStringBuilder(bool) *Java__lang__AbstractStringBuilder__Instance
	Append__desc____arr__C__ret____obj__Java__lang__AbstractStringBuilder([]rune) *Java__lang__AbstractStringBuilder__Instance
	Append__desc____arr__C__I__I__ret____obj__Java__lang__AbstractStringBuilder([]rune, int, int) *Java__lang__AbstractStringBuilder__Instance
	AppendCodePoint__desc__I__ret____obj__Java__lang__AbstractStringBuilder(int) *Java__lang__AbstractStringBuilder__Instance
	Capacity__desc____ret__I() int
	CharAt__desc__I__ret__C(int) rune
	CodePointAt__desc__I__ret__I(int) int
	CodePointBefore__desc__I__ret__I(int) int
	CodePointCount__desc__I__I__ret__I(int, int) int
	Delete__desc__I__I__ret____obj__Java__lang__AbstractStringBuilder(int, int) *Java__lang__AbstractStringBuilder__Instance
	DeleteCharAt__desc__I__ret____obj__Java__lang__AbstractStringBuilder(int) *Java__lang__AbstractStringBuilder__Instance
	EnsureCapacity__desc__I__ret__V(int)
	ExpandCapacity__desc__I__ret__V(int)
	GetChars__desc__I__I____arr__C__I__ret__V(int, int, []rune, int)
	GetValue__desc____ret____arr__C() []rune
	IndexOf__desc____obj__Java__lang__String__ret__I(*Java__lang__String__Instance) int
	IndexOf__desc____obj__Java__lang__String__I__ret__I(*Java__lang__String__Instance, int) int
	Insert__desc__I__C__ret____obj__Java__lang__AbstractStringBuilder(int, rune) *Java__lang__AbstractStringBuilder__Instance
	Insert__desc__I__D__ret____obj__Java__lang__AbstractStringBuilder(int, float64) *Java__lang__AbstractStringBuilder__Instance
	Insert__desc__I__F__ret____obj__Java__lang__AbstractStringBuilder(int, float32) *Java__lang__AbstractStringBuilder__Instance
	Insert__desc__I__I__ret____obj__Java__lang__AbstractStringBuilder(int, int) *Java__lang__AbstractStringBuilder__Instance
	Insert__desc__I__J__ret____obj__Java__lang__AbstractStringBuilder(int, int64) *Java__lang__AbstractStringBuilder__Instance
	Insert__desc__I____obj__Java__lang__CharSequence__ret____obj__Java__lang__AbstractStringBuilder(int, Java__lang__CharSequence__Instance) *Java__lang__AbstractStringBuilder__Instance
	Insert__desc__I____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__AbstractStringBuilder(int, Java__lang__CharSequence__Instance, int, int) *Java__lang__AbstractStringBuilder__Instance
	Insert__desc__I____obj__Java__lang__Object__ret____obj__Java__lang__AbstractStringBuilder(int, *Java__lang__Object__Instance) *Java__lang__AbstractStringBuilder__Instance
	Insert__desc__I____obj__Java__lang__String__ret____obj__Java__lang__AbstractStringBuilder(int, *Java__lang__String__Instance) *Java__lang__AbstractStringBuilder__Instance
	Insert__desc__I__Z__ret____obj__Java__lang__AbstractStringBuilder(int, bool) *Java__lang__AbstractStringBuilder__Instance
	Insert__desc__I____arr__C__ret____obj__Java__lang__AbstractStringBuilder(int, []rune) *Java__lang__AbstractStringBuilder__Instance
	Insert__desc__I____arr__C__I__I__ret____obj__Java__lang__AbstractStringBuilder(int, []rune, int, int) *Java__lang__AbstractStringBuilder__Instance
	LastIndexOf__desc____obj__Java__lang__String__ret__I(*Java__lang__String__Instance) int
	LastIndexOf__desc____obj__Java__lang__String__I__ret__I(*Java__lang__String__Instance, int) int
	Length__desc____ret__I() int
	OffsetByCodePoints__desc__I__I__ret__I(int, int) int
	Replace__desc__I__I____obj__Java__lang__String__ret____obj__Java__lang__AbstractStringBuilder(int, int, *Java__lang__String__Instance) *Java__lang__AbstractStringBuilder__Instance
	Reverse__desc____ret____obj__Java__lang__AbstractStringBuilder() *Java__lang__AbstractStringBuilder__Instance
	SetCharAt__desc__I__C__ret__V(int, rune)
	SetLength__desc__I__ret__V(int)
	SubSequence__desc__I__I__ret____obj__Java__lang__CharSequence(int, int) Java__lang__CharSequence__Instance
	Substring__desc__I__ret____obj__Java__lang__String(int) *Java__lang__String__Instance
	Substring__desc__I__I__ret____obj__Java__lang__String(int, int) *Java__lang__String__Instance
	TrimToSize__desc____ret__V()
}

func (this *Java__lang__AbstractStringBuilder__Instance) Java__lang__AbstractStringBuilder__InitDispatch(v Java__lang__AbstractStringBuilder__Dispatch) {
	this.Java__lang__Object__Instance.Java__lang__Object__InitDispatch(v)
	this._dispatch = v
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Instance_Init__desc__I__ret__V(var1 int) {
	this._dispatch.Instance_Init__desc__I__ret__V(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Append__desc__C__ret____obj__Java__lang__AbstractStringBuilder(var1 rune) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Append__desc__C__ret____obj__Java__lang__AbstractStringBuilder(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Append__desc__C__ret____obj__Java__lang__Appendable(var1 rune) Java__lang__Appendable__Instance {
	return this._dispatch.Append__desc__C__ret____obj__Java__lang__Appendable(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Append__desc__D__ret____obj__Java__lang__AbstractStringBuilder(var1 float64) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Append__desc__D__ret____obj__Java__lang__AbstractStringBuilder(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Append__desc__F__ret____obj__Java__lang__AbstractStringBuilder(var1 float32) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Append__desc__F__ret____obj__Java__lang__AbstractStringBuilder(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Append__desc__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Append__desc__I__ret____obj__Java__lang__AbstractStringBuilder(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Append__desc__J__ret____obj__Java__lang__AbstractStringBuilder(var1 int64) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Append__desc__J__ret____obj__Java__lang__AbstractStringBuilder(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Append__desc____obj__Java__lang__AbstractStringBuilder__ret____obj__Java__lang__AbstractStringBuilder(var1 *Java__lang__AbstractStringBuilder__Instance) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Append__desc____obj__Java__lang__AbstractStringBuilder__ret____obj__Java__lang__AbstractStringBuilder(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__AbstractStringBuilder(var1 Java__lang__CharSequence__Instance) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__AbstractStringBuilder(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__Appendable(var1 Java__lang__CharSequence__Instance) Java__lang__Appendable__Instance {
	return this._dispatch.Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__Appendable(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1 Java__lang__CharSequence__Instance, var2 int, var3 int) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1, var2, var3)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__Appendable(var1 Java__lang__CharSequence__Instance, var2 int, var3 int) Java__lang__Appendable__Instance {
	return this._dispatch.Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__Appendable(var1, var2, var3)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Append__desc____obj__Java__lang__Object__ret____obj__Java__lang__AbstractStringBuilder(var1 *Java__lang__Object__Instance) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Append__desc____obj__Java__lang__Object__ret____obj__Java__lang__AbstractStringBuilder(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Append__desc____obj__Java__lang__String__ret____obj__Java__lang__AbstractStringBuilder(var1 *Java__lang__String__Instance) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Append__desc____obj__Java__lang__String__ret____obj__Java__lang__AbstractStringBuilder(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Append__desc__Z__ret____obj__Java__lang__AbstractStringBuilder(var1 bool) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Append__desc__Z__ret____obj__Java__lang__AbstractStringBuilder(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Append__desc____arr__C__ret____obj__Java__lang__AbstractStringBuilder(var1 []rune) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Append__desc____arr__C__ret____obj__Java__lang__AbstractStringBuilder(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Append__desc____arr__C__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1 []rune, var2 int, var3 int) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Append__desc____arr__C__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1, var2, var3)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__AppendCodePoint__desc__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.AppendCodePoint__desc__I__ret____obj__Java__lang__AbstractStringBuilder(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Capacity__desc____ret__I() int {
	return this._dispatch.Capacity__desc____ret__I()
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__CharAt__desc__I__ret__C(var1 int) rune {
	return this._dispatch.CharAt__desc__I__ret__C(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__CodePointAt__desc__I__ret__I(var1 int) int {
	return this._dispatch.CodePointAt__desc__I__ret__I(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__CodePointBefore__desc__I__ret__I(var1 int) int {
	return this._dispatch.CodePointBefore__desc__I__ret__I(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__CodePointCount__desc__I__I__ret__I(var1 int, var2 int) int {
	return this._dispatch.CodePointCount__desc__I__I__ret__I(var1, var2)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Delete__desc__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 int) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Delete__desc__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1, var2)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__DeleteCharAt__desc__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.DeleteCharAt__desc__I__ret____obj__Java__lang__AbstractStringBuilder(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__EnsureCapacity__desc__I__ret__V(var1 int) {
	this._dispatch.EnsureCapacity__desc__I__ret__V(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__ExpandCapacity__desc__I__ret__V(var1 int) {
	this._dispatch.ExpandCapacity__desc__I__ret__V(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__GetChars__desc__I__I____arr__C__I__ret__V(var1 int, var2 int, var3 []rune, var4 int) {
	this._dispatch.GetChars__desc__I__I____arr__C__I__ret__V(var1, var2, var3, var4)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__GetValue__desc____ret____arr__C() []rune {
	return this._dispatch.GetValue__desc____ret____arr__C()
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__IndexOf__desc____obj__Java__lang__String__ret__I(var1 *Java__lang__String__Instance) int {
	return this._dispatch.IndexOf__desc____obj__Java__lang__String__ret__I(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__IndexOf__desc____obj__Java__lang__String__I__ret__I(var1 *Java__lang__String__Instance, var2 int) int {
	return this._dispatch.IndexOf__desc____obj__Java__lang__String__I__ret__I(var1, var2)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Insert__desc__I__C__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 rune) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Insert__desc__I__C__ret____obj__Java__lang__AbstractStringBuilder(var1, var2)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Insert__desc__I__D__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 float64) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Insert__desc__I__D__ret____obj__Java__lang__AbstractStringBuilder(var1, var2)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Insert__desc__I__F__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 float32) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Insert__desc__I__F__ret____obj__Java__lang__AbstractStringBuilder(var1, var2)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Insert__desc__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 int) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Insert__desc__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1, var2)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Insert__desc__I__J__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 int64) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Insert__desc__I__J__ret____obj__Java__lang__AbstractStringBuilder(var1, var2)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Insert__desc__I____obj__Java__lang__CharSequence__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 Java__lang__CharSequence__Instance) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Insert__desc__I____obj__Java__lang__CharSequence__ret____obj__Java__lang__AbstractStringBuilder(var1, var2)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Insert__desc__I____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 Java__lang__CharSequence__Instance, var3 int, var4 int) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Insert__desc__I____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1, var2, var3, var4)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Insert__desc__I____obj__Java__lang__Object__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 *Java__lang__Object__Instance) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Insert__desc__I____obj__Java__lang__Object__ret____obj__Java__lang__AbstractStringBuilder(var1, var2)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Insert__desc__I____obj__Java__lang__String__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 *Java__lang__String__Instance) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Insert__desc__I____obj__Java__lang__String__ret____obj__Java__lang__AbstractStringBuilder(var1, var2)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Insert__desc__I__Z__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 bool) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Insert__desc__I__Z__ret____obj__Java__lang__AbstractStringBuilder(var1, var2)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Insert__desc__I____arr__C__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 []rune) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Insert__desc__I____arr__C__ret____obj__Java__lang__AbstractStringBuilder(var1, var2)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Insert__desc__I____arr__C__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 []rune, var3 int, var4 int) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Insert__desc__I____arr__C__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1, var2, var3, var4)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__LastIndexOf__desc____obj__Java__lang__String__ret__I(var1 *Java__lang__String__Instance) int {
	return this._dispatch.LastIndexOf__desc____obj__Java__lang__String__ret__I(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__LastIndexOf__desc____obj__Java__lang__String__I__ret__I(var1 *Java__lang__String__Instance, var2 int) int {
	return this._dispatch.LastIndexOf__desc____obj__Java__lang__String__I__ret__I(var1, var2)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Length__desc____ret__I() int {
	return this._dispatch.Length__desc____ret__I()
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__OffsetByCodePoints__desc__I__I__ret__I(var1 int, var2 int) int {
	return this._dispatch.OffsetByCodePoints__desc__I__I__ret__I(var1, var2)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Replace__desc__I__I____obj__Java__lang__String__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 int, var3 *Java__lang__String__Instance) *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Replace__desc__I__I____obj__Java__lang__String__ret____obj__Java__lang__AbstractStringBuilder(var1, var2, var3)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Reverse__desc____ret____obj__Java__lang__AbstractStringBuilder() *Java__lang__AbstractStringBuilder__Instance {
	return this._dispatch.Reverse__desc____ret____obj__Java__lang__AbstractStringBuilder()
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__SetCharAt__desc__I__C__ret__V(var1 int, var2 rune) {
	this._dispatch.SetCharAt__desc__I__C__ret__V(var1, var2)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__SetLength__desc__I__ret__V(var1 int) {
	this._dispatch.SetLength__desc__I__ret__V(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__SubSequence__desc__I__I__ret____obj__Java__lang__CharSequence(var1 int, var2 int) Java__lang__CharSequence__Instance {
	return this._dispatch.SubSequence__desc__I__I__ret____obj__Java__lang__CharSequence(var1, var2)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Substring__desc__I__ret____obj__Java__lang__String(var1 int) *Java__lang__String__Instance {
	return this._dispatch.Substring__desc__I__ret____obj__Java__lang__String(var1)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__Substring__desc__I__I__ret____obj__Java__lang__String(var1 int, var2 int) *Java__lang__String__Instance {
	return this._dispatch.Substring__desc__I__I__ret____obj__Java__lang__String(var1, var2)
}

func (this *Java__lang__AbstractStringBuilder__Instance) Dispatch__TrimToSize__desc____ret__V() {
	this._dispatch.TrimToSize__desc____ret__V()
}

type Java__lang__AbstractStringBuilder__Instance struct {
	*Java__lang__Object__Instance
	_dispatch Java__lang__AbstractStringBuilder__Dispatch
	Count     int
	Value     []rune
}

func (this *Java__lang__AbstractStringBuilder__Instance) Instance_Init__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Instance_Init__desc__I__ret__V(var1 int) {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Append__desc__C__ret____obj__Java__lang__AbstractStringBuilder(var1 rune) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Append__desc__C__ret____obj__Java__lang__Appendable(var1 rune) Java__lang__Appendable__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Append__desc__D__ret____obj__Java__lang__AbstractStringBuilder(var1 float64) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Append__desc__F__ret____obj__Java__lang__AbstractStringBuilder(var1 float32) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Append__desc__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Append__desc__J__ret____obj__Java__lang__AbstractStringBuilder(var1 int64) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Append__desc____obj__Java__lang__AbstractStringBuilder__ret____obj__Java__lang__AbstractStringBuilder(var1 *Java__lang__AbstractStringBuilder__Instance) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__AbstractStringBuilder(var1 Java__lang__CharSequence__Instance) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__Appendable(var1 Java__lang__CharSequence__Instance) Java__lang__Appendable__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1 Java__lang__CharSequence__Instance, var2 int, var3 int) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__Appendable(var1 Java__lang__CharSequence__Instance, var2 int, var3 int) Java__lang__Appendable__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Append__desc____obj__Java__lang__Object__ret____obj__Java__lang__AbstractStringBuilder(var1 *Java__lang__Object__Instance) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Append__desc____obj__Java__lang__String__ret____obj__Java__lang__AbstractStringBuilder(var1 *Java__lang__String__Instance) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Append__desc__Z__ret____obj__Java__lang__AbstractStringBuilder(var1 bool) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Append__desc____arr__C__ret____obj__Java__lang__AbstractStringBuilder(var1 []rune) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Append__desc____arr__C__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1 []rune, var2 int, var3 int) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) AppendCodePoint__desc__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Capacity__desc____ret__I() int {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) CharAt__desc__I__ret__C(var1 int) rune {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) CodePointAt__desc__I__ret__I(var1 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) CodePointBefore__desc__I__ret__I(var1 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) CodePointCount__desc__I__I__ret__I(var1 int, var2 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Delete__desc__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 int) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) DeleteCharAt__desc__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) EnsureCapacity__desc__I__ret__V(var1 int) {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) ExpandCapacity__desc__I__ret__V(var1 int) {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) GetChars__desc__I__I____arr__C__I__ret__V(var1 int, var2 int, var3 []rune, var4 int) {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) GetValue__desc____ret____arr__C() []rune {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) IndexOf__desc____obj__Java__lang__String__ret__I(var1 *Java__lang__String__Instance) int {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) IndexOf__desc____obj__Java__lang__String__I__ret__I(var1 *Java__lang__String__Instance, var2 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Insert__desc__I__C__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 rune) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Insert__desc__I__D__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 float64) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Insert__desc__I__F__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 float32) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Insert__desc__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 int) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Insert__desc__I__J__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 int64) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Insert__desc__I____obj__Java__lang__CharSequence__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 Java__lang__CharSequence__Instance) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Insert__desc__I____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 Java__lang__CharSequence__Instance, var3 int, var4 int) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Insert__desc__I____obj__Java__lang__Object__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 *Java__lang__Object__Instance) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Insert__desc__I____obj__Java__lang__String__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 *Java__lang__String__Instance) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Insert__desc__I__Z__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 bool) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Insert__desc__I____arr__C__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 []rune) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Insert__desc__I____arr__C__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 []rune, var3 int, var4 int) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) LastIndexOf__desc____obj__Java__lang__String__ret__I(var1 *Java__lang__String__Instance) int {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) LastIndexOf__desc____obj__Java__lang__String__I__ret__I(var1 *Java__lang__String__Instance, var2 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Length__desc____ret__I() int {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) OffsetByCodePoints__desc__I__I__ret__I(var1 int, var2 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Replace__desc__I__I____obj__Java__lang__String__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 int, var3 *Java__lang__String__Instance) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Reverse__desc____ret____obj__Java__lang__AbstractStringBuilder() *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) SetCharAt__desc__I__C__ret__V(var1 int, var2 rune) {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) SetLength__desc__I__ret__V(var1 int) {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) SubSequence__desc__I__I__ret____obj__Java__lang__CharSequence(var1 int, var2 int) Java__lang__CharSequence__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Substring__desc__I__ret____obj__Java__lang__String(var1 int) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) Substring__desc__I__I__ret____obj__Java__lang__String(var1 int, var2 int) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) ToString__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__AbstractStringBuilder__Instance) TrimToSize__desc____ret__V() {
	panic("Not Implemented")
}

type Java__lang__Appendable__Dispatch interface {
	Append__desc__C__ret____obj__Java__lang__Appendable(rune) Java__lang__Appendable__Instance
	Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__Appendable(Java__lang__CharSequence__Instance) Java__lang__Appendable__Instance
	Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__Appendable(Java__lang__CharSequence__Instance, int, int) Java__lang__Appendable__Instance
}

type Java__lang__Appendable__Instance interface {
	Java__lang__Appendable__Dispatch
}

type Java__lang__AutoCloseable__Dispatch interface {
	Close__desc____ret__V()
}

type Java__lang__AutoCloseable__Instance interface {
	Java__lang__AutoCloseable__Dispatch
}

type Java__lang__CharSequence__Dispatch interface {
	CharAt__desc__I__ret__C(int) rune
	Length__desc____ret__I() int
	SubSequence__desc__I__I__ret____obj__Java__lang__CharSequence(int, int) Java__lang__CharSequence__Instance
}

type Java__lang__CharSequence__Instance interface {
	Java__lang__CharSequence__Dispatch
}

type Java__lang__Comparable__Dispatch interface {
	CompareTo__desc____obj__Java__lang__Object__ret__I(*Java__lang__Object__Instance) int
}

type Java__lang__Comparable__Instance interface {
	Java__lang__Comparable__Dispatch
}

type Java__lang__Exception__Static struct {
	SerialVersionUID int64
}

var Java__lang__Exception__Var Java__lang__Exception__Static

func Java__lang__Exception() *Java__lang__Exception__Static {
	return &Java__lang__Exception__Var
}

func (this *Java__lang__Exception__Static) New() *Java__lang__Exception__Instance {
	v := &Java__lang__Exception__Instance{
		Java__lang__Throwable__Instance: Java__lang__Throwable().New(),
	}
	v.Java__lang__Exception__InitDispatch(v)
	return v
}

type Java__lang__Exception__Dispatch interface {
	Java__lang__Throwable__Dispatch
}

func (this *Java__lang__Exception__Instance) Java__lang__Exception__InitDispatch(v Java__lang__Exception__Dispatch) {
	this.Java__lang__Throwable__Instance.Java__lang__Throwable__InitDispatch(v)
	this._dispatch = v
}

type Java__lang__Exception__Instance struct {
	*Java__lang__Throwable__Instance
	_dispatch Java__lang__Exception__Dispatch
}

func (this *Java__lang__Exception__Instance) Instance_Init__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__Exception__Instance) Instance_Init__desc____obj__Java__lang__String__ret__V(var1 *Java__lang__String__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__Exception__Instance) Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__Throwable__ret__V(var1 *Java__lang__String__Instance, var2 *Java__lang__Throwable__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__Exception__Instance) Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__Throwable__Z__Z__ret__V(var1 *Java__lang__String__Instance, var2 *Java__lang__Throwable__Instance, var3 bool, var4 bool) {
	panic("Not Implemented")
}

func (this *Java__lang__Exception__Instance) Instance_Init__desc____obj__Java__lang__Throwable__ret__V(var1 *Java__lang__Throwable__Instance) {
	panic("Not Implemented")
}

type Java__lang__Object__Static struct {
	init sync.Once
}

var Java__lang__Object__Var Java__lang__Object__Static

func Java__lang__Object() *Java__lang__Object__Static {
	Java__lang__Object__Var.init.Do(Java__lang__Object__Var.Static_Init__desc____ret__V)
	return &Java__lang__Object__Var
}

func (this *Java__lang__Object__Static) New() *Java__lang__Object__Instance {
	v := &Java__lang__Object__Instance{}
	v.Java__lang__Object__InitDispatch(v)
	return v
}

func (this *Java__lang__Object__Static) Static_Init__desc____ret__V() {
	
}

type Java__lang__Object__Dispatch interface {
	Instance_Init__desc____ret__V()
	Clone__desc____ret____obj__Java__lang__Object() *Java__lang__Object__Instance
	Equals__desc____obj__Java__lang__Object__ret__Z(*Java__lang__Object__Instance) bool
	Finalize__desc____ret__V()
	HashCode__desc____ret__I() int
	Notify__desc____ret__V()
	NotifyAll__desc____ret__V()
	ToString__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance
	Wait__desc____ret__V()
	Wait__desc__J__ret__V(int64)
	Wait__desc__J__I__ret__V(int64, int)
}

func (this *Java__lang__Object__Instance) Java__lang__Object__InitDispatch(v Java__lang__Object__Dispatch) {
	this._dispatch = v
}

func (this *Java__lang__Object__Instance) Dispatch__Instance_Init__desc____ret__V() {
	this._dispatch.Instance_Init__desc____ret__V()
}

func (this *Java__lang__Object__Instance) Dispatch__Clone__desc____ret____obj__Java__lang__Object() *Java__lang__Object__Instance {
	return this._dispatch.Clone__desc____ret____obj__Java__lang__Object()
}

func (this *Java__lang__Object__Instance) Dispatch__Equals__desc____obj__Java__lang__Object__ret__Z(var1 *Java__lang__Object__Instance) bool {
	return this._dispatch.Equals__desc____obj__Java__lang__Object__ret__Z(var1)
}

func (this *Java__lang__Object__Instance) Dispatch__Finalize__desc____ret__V() {
	this._dispatch.Finalize__desc____ret__V()
}

func (this *Java__lang__Object__Instance) Dispatch__HashCode__desc____ret__I() int {
	return this._dispatch.HashCode__desc____ret__I()
}

func (this *Java__lang__Object__Instance) Dispatch__Notify__desc____ret__V() {
	this._dispatch.Notify__desc____ret__V()
}

func (this *Java__lang__Object__Instance) Dispatch__NotifyAll__desc____ret__V() {
	this._dispatch.NotifyAll__desc____ret__V()
}

func (this *Java__lang__Object__Instance) Dispatch__ToString__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	return this._dispatch.ToString__desc____ret____obj__Java__lang__String()
}

func (this *Java__lang__Object__Instance) Dispatch__Wait__desc____ret__V() {
	this._dispatch.Wait__desc____ret__V()
}

func (this *Java__lang__Object__Instance) Dispatch__Wait__desc__J__ret__V(var1 int64) {
	this._dispatch.Wait__desc__J__ret__V(var1)
}

func (this *Java__lang__Object__Instance) Dispatch__Wait__desc__J__I__ret__V(var1 int64, var2 int) {
	this._dispatch.Wait__desc__J__I__ret__V(var1, var2)
}

type Java__lang__Object__Instance struct {
	_dispatch Java__lang__Object__Dispatch
}

func (this *Java__lang__Object__Instance) Clone__desc____ret____obj__Java__lang__Object() *Java__lang__Object__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) Equals__desc____obj__Java__lang__Object__ret__Z(var1 *Java__lang__Object__Instance) bool {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) Finalize__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) HashCode__desc____ret__I() int {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) Notify__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) NotifyAll__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) ToString__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) Wait__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) Wait__desc__J__ret__V(var1 int64) {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) Wait__desc__J__I__ret__V(var1 int64, var2 int) {
	panic("Not Implemented")
}

type Java__lang__String__Static struct {
	init sync.Once
}

var Java__lang__String__Var Java__lang__String__Static

func Java__lang__String() *Java__lang__String__Static {
	Java__lang__String__Var.init.Do(Java__lang__String__Var.Static_Init__desc____ret__V)
	return &Java__lang__String__Var
}

func (this *Java__lang__String__Static) New() *Java__lang__String__Instance {
	v := &Java__lang__String__Instance{
		Java__lang__Object__Instance: Java__lang__Object().New(),
	}
	v.Java__lang__String__InitDispatch(v)
	return v
}

func (this *Java__lang__String__Static) Static_Init__desc____ret__V() {
	
}

func (this *Java__lang__String__Static) CopyValueOf__desc____arr__C__ret____obj__Java__lang__String(var0 []rune) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) CopyValueOf__desc____arr__C__I__I__ret____obj__Java__lang__String(var0 []rune, var1 int, var2 int) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) Format__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__lang__String(var0 *Java__lang__String__Instance, var1 []*Java__lang__Object__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) IndexOf__desc____arr__C__I__I____obj__Java__lang__String__I__ret__I(var0 []rune, var1 int, var2 int, var3 *Java__lang__String__Instance, var4 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) IndexOf__desc____arr__C__I__I____arr__C__I__I__I__ret__I(var0 []rune, var1 int, var2 int, var3 []rune, var4 int, var5 int, var6 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) Join__desc____obj__Java__lang__CharSequence____arr____obj__Java__lang__CharSequence__ret____obj__Java__lang__String(var0 Java__lang__CharSequence__Instance, var1 []Java__lang__CharSequence__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) LastIndexOf__desc____arr__C__I__I____obj__Java__lang__String__I__ret__I(var0 []rune, var1 int, var2 int, var3 *Java__lang__String__Instance, var4 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) LastIndexOf__desc____arr__C__I__I____arr__C__I__I__I__ret__I(var0 []rune, var1 int, var2 int, var3 []rune, var4 int, var5 int, var6 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) ValueOf__desc__C__ret____obj__Java__lang__String(var0 rune) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) ValueOf__desc__D__ret____obj__Java__lang__String(var0 float64) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) ValueOf__desc__F__ret____obj__Java__lang__String(var0 float32) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) ValueOf__desc__I__ret____obj__Java__lang__String(var0 int) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) ValueOf__desc__J__ret____obj__Java__lang__String(var0 int64) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) ValueOf__desc____obj__Java__lang__Object__ret____obj__Java__lang__String(var0 *Java__lang__Object__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) ValueOf__desc__Z__ret____obj__Java__lang__String(var0 bool) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) ValueOf__desc____arr__C__ret____obj__Java__lang__String(var0 []rune) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) ValueOf__desc____arr__C__I__I__ret____obj__Java__lang__String(var0 []rune, var1 int, var2 int) *Java__lang__String__Instance {
	panic("Not Implemented")
}

type Java__lang__String__Dispatch interface {
	Java__lang__Object__Dispatch
	Instance_Init__desc____obj__Java__lang__String__ret__V(*Java__lang__String__Instance)
	Instance_Init__desc____obj__Java__lang__StringBuilder__ret__V(*Java__lang__StringBuilder__Instance)
	Instance_Init__desc____arr__B__ret__V([]byte)
	Instance_Init__desc____arr__B__I__ret__V([]byte, int)
	Instance_Init__desc____arr__B__I__I__ret__V([]byte, int, int)
	Instance_Init__desc____arr__B__I__I__I__ret__V([]byte, int, int, int)
	Instance_Init__desc____arr__B__I__I____obj__Java__lang__String__ret__V([]byte, int, int, *Java__lang__String__Instance)
	Instance_Init__desc____arr__B____obj__Java__lang__String__ret__V([]byte, *Java__lang__String__Instance)
	Instance_Init__desc____arr__C__ret__V([]rune)
	Instance_Init__desc____arr__C__I__I__ret__V([]rune, int, int)
	Instance_Init__desc____arr__C__Z__ret__V([]rune, bool)
	Instance_Init__desc____arr__I__I__I__ret__V([]int, int, int)
	CharAt__desc__I__ret__C(int) rune
	CodePointAt__desc__I__ret__I(int) int
	CodePointBefore__desc__I__ret__I(int) int
	CodePointCount__desc__I__I__ret__I(int, int) int
	CompareTo__desc____obj__Java__lang__Object__ret__I(*Java__lang__Object__Instance) int
	CompareTo__desc____obj__Java__lang__String__ret__I(*Java__lang__String__Instance) int
	CompareToIgnoreCase__desc____obj__Java__lang__String__ret__I(*Java__lang__String__Instance) int
	Concat__desc____obj__Java__lang__String__ret____obj__Java__lang__String(*Java__lang__String__Instance) *Java__lang__String__Instance
	Contains__desc____obj__Java__lang__CharSequence__ret__Z(Java__lang__CharSequence__Instance) bool
	ContentEquals__desc____obj__Java__lang__CharSequence__ret__Z(Java__lang__CharSequence__Instance) bool
	EndsWith__desc____obj__Java__lang__String__ret__Z(*Java__lang__String__Instance) bool
	EqualsIgnoreCase__desc____obj__Java__lang__String__ret__Z(*Java__lang__String__Instance) bool
	GetBytes__desc____ret____arr__B() []byte
	GetBytes__desc__I__I____arr__B__I__ret__V(int, int, []byte, int)
	GetBytes__desc____obj__Java__lang__String__ret____arr__B(*Java__lang__String__Instance) []byte
	GetChars__desc__I__I____arr__C__I__ret__V(int, int, []rune, int)
	GetChars__desc____arr__C__I__ret__V([]rune, int)
	IndexOf__desc__I__ret__I(int) int
	IndexOf__desc__I__I__ret__I(int, int) int
	IndexOf__desc____obj__Java__lang__String__ret__I(*Java__lang__String__Instance) int
	IndexOf__desc____obj__Java__lang__String__I__ret__I(*Java__lang__String__Instance, int) int
	Intern__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance
	IsEmpty__desc____ret__Z() bool
	LastIndexOf__desc__I__ret__I(int) int
	LastIndexOf__desc__I__I__ret__I(int, int) int
	LastIndexOf__desc____obj__Java__lang__String__ret__I(*Java__lang__String__Instance) int
	LastIndexOf__desc____obj__Java__lang__String__I__ret__I(*Java__lang__String__Instance, int) int
	Length__desc____ret__I() int
	Matches__desc____obj__Java__lang__String__ret__Z(*Java__lang__String__Instance) bool
	OffsetByCodePoints__desc__I__I__ret__I(int, int) int
	RegionMatches__desc__I____obj__Java__lang__String__I__I__ret__Z(int, *Java__lang__String__Instance, int, int) bool
	RegionMatches__desc__Z__I____obj__Java__lang__String__I__I__ret__Z(bool, int, *Java__lang__String__Instance, int, int) bool
	Replace__desc__C__C__ret____obj__Java__lang__String(rune, rune) *Java__lang__String__Instance
	Replace__desc____obj__Java__lang__CharSequence____obj__Java__lang__CharSequence__ret____obj__Java__lang__String(Java__lang__CharSequence__Instance, Java__lang__CharSequence__Instance) *Java__lang__String__Instance
	ReplaceAll__desc____obj__Java__lang__String____obj__Java__lang__String__ret____obj__Java__lang__String(*Java__lang__String__Instance, *Java__lang__String__Instance) *Java__lang__String__Instance
	ReplaceFirst__desc____obj__Java__lang__String____obj__Java__lang__String__ret____obj__Java__lang__String(*Java__lang__String__Instance, *Java__lang__String__Instance) *Java__lang__String__Instance
	Split__desc____obj__Java__lang__String__ret____arr____obj__Java__lang__String(*Java__lang__String__Instance) []*Java__lang__String__Instance
	Split__desc____obj__Java__lang__String__I__ret____arr____obj__Java__lang__String(*Java__lang__String__Instance, int) []*Java__lang__String__Instance
	StartsWith__desc____obj__Java__lang__String__ret__Z(*Java__lang__String__Instance) bool
	StartsWith__desc____obj__Java__lang__String__I__ret__Z(*Java__lang__String__Instance, int) bool
	SubSequence__desc__I__I__ret____obj__Java__lang__CharSequence(int, int) Java__lang__CharSequence__Instance
	Substring__desc__I__ret____obj__Java__lang__String(int) *Java__lang__String__Instance
	Substring__desc__I__I__ret____obj__Java__lang__String(int, int) *Java__lang__String__Instance
	ToCharArray__desc____ret____arr__C() []rune
	ToLowerCase__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance
	ToUpperCase__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance
	Trim__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance
}

func (this *Java__lang__String__Instance) Java__lang__String__InitDispatch(v Java__lang__String__Dispatch) {
	this.Java__lang__Object__Instance.Java__lang__Object__InitDispatch(v)
	this._dispatch = v
}

func (this *Java__lang__String__Instance) Dispatch__Instance_Init__desc____obj__Java__lang__String__ret__V(var1 *Java__lang__String__Instance) {
	this._dispatch.Instance_Init__desc____obj__Java__lang__String__ret__V(var1)
}

func (this *Java__lang__String__Instance) Dispatch__Instance_Init__desc____obj__Java__lang__StringBuilder__ret__V(var1 *Java__lang__StringBuilder__Instance) {
	this._dispatch.Instance_Init__desc____obj__Java__lang__StringBuilder__ret__V(var1)
}

func (this *Java__lang__String__Instance) Dispatch__Instance_Init__desc____arr__B__ret__V(var1 []byte) {
	this._dispatch.Instance_Init__desc____arr__B__ret__V(var1)
}

func (this *Java__lang__String__Instance) Dispatch__Instance_Init__desc____arr__B__I__ret__V(var1 []byte, var2 int) {
	this._dispatch.Instance_Init__desc____arr__B__I__ret__V(var1, var2)
}

func (this *Java__lang__String__Instance) Dispatch__Instance_Init__desc____arr__B__I__I__ret__V(var1 []byte, var2 int, var3 int) {
	this._dispatch.Instance_Init__desc____arr__B__I__I__ret__V(var1, var2, var3)
}

func (this *Java__lang__String__Instance) Dispatch__Instance_Init__desc____arr__B__I__I__I__ret__V(var1 []byte, var2 int, var3 int, var4 int) {
	this._dispatch.Instance_Init__desc____arr__B__I__I__I__ret__V(var1, var2, var3, var4)
}

func (this *Java__lang__String__Instance) Dispatch__Instance_Init__desc____arr__B__I__I____obj__Java__lang__String__ret__V(var1 []byte, var2 int, var3 int, var4 *Java__lang__String__Instance) {
	this._dispatch.Instance_Init__desc____arr__B__I__I____obj__Java__lang__String__ret__V(var1, var2, var3, var4)
}

func (this *Java__lang__String__Instance) Dispatch__Instance_Init__desc____arr__B____obj__Java__lang__String__ret__V(var1 []byte, var2 *Java__lang__String__Instance) {
	this._dispatch.Instance_Init__desc____arr__B____obj__Java__lang__String__ret__V(var1, var2)
}

func (this *Java__lang__String__Instance) Dispatch__Instance_Init__desc____arr__C__ret__V(var1 []rune) {
	this._dispatch.Instance_Init__desc____arr__C__ret__V(var1)
}

func (this *Java__lang__String__Instance) Dispatch__Instance_Init__desc____arr__C__I__I__ret__V(var1 []rune, var2 int, var3 int) {
	this._dispatch.Instance_Init__desc____arr__C__I__I__ret__V(var1, var2, var3)
}

func (this *Java__lang__String__Instance) Dispatch__Instance_Init__desc____arr__C__Z__ret__V(var1 []rune, var2 bool) {
	this._dispatch.Instance_Init__desc____arr__C__Z__ret__V(var1, var2)
}

func (this *Java__lang__String__Instance) Dispatch__Instance_Init__desc____arr__I__I__I__ret__V(var1 []int, var2 int, var3 int) {
	this._dispatch.Instance_Init__desc____arr__I__I__I__ret__V(var1, var2, var3)
}

func (this *Java__lang__String__Instance) Dispatch__CharAt__desc__I__ret__C(var1 int) rune {
	return this._dispatch.CharAt__desc__I__ret__C(var1)
}

func (this *Java__lang__String__Instance) Dispatch__CodePointAt__desc__I__ret__I(var1 int) int {
	return this._dispatch.CodePointAt__desc__I__ret__I(var1)
}

func (this *Java__lang__String__Instance) Dispatch__CodePointBefore__desc__I__ret__I(var1 int) int {
	return this._dispatch.CodePointBefore__desc__I__ret__I(var1)
}

func (this *Java__lang__String__Instance) Dispatch__CodePointCount__desc__I__I__ret__I(var1 int, var2 int) int {
	return this._dispatch.CodePointCount__desc__I__I__ret__I(var1, var2)
}

func (this *Java__lang__String__Instance) Dispatch__CompareTo__desc____obj__Java__lang__Object__ret__I(var1 *Java__lang__Object__Instance) int {
	return this._dispatch.CompareTo__desc____obj__Java__lang__Object__ret__I(var1)
}

func (this *Java__lang__String__Instance) Dispatch__CompareTo__desc____obj__Java__lang__String__ret__I(var1 *Java__lang__String__Instance) int {
	return this._dispatch.CompareTo__desc____obj__Java__lang__String__ret__I(var1)
}

func (this *Java__lang__String__Instance) Dispatch__CompareToIgnoreCase__desc____obj__Java__lang__String__ret__I(var1 *Java__lang__String__Instance) int {
	return this._dispatch.CompareToIgnoreCase__desc____obj__Java__lang__String__ret__I(var1)
}

func (this *Java__lang__String__Instance) Dispatch__Concat__desc____obj__Java__lang__String__ret____obj__Java__lang__String(var1 *Java__lang__String__Instance) *Java__lang__String__Instance {
	return this._dispatch.Concat__desc____obj__Java__lang__String__ret____obj__Java__lang__String(var1)
}

func (this *Java__lang__String__Instance) Dispatch__Contains__desc____obj__Java__lang__CharSequence__ret__Z(var1 Java__lang__CharSequence__Instance) bool {
	return this._dispatch.Contains__desc____obj__Java__lang__CharSequence__ret__Z(var1)
}

func (this *Java__lang__String__Instance) Dispatch__ContentEquals__desc____obj__Java__lang__CharSequence__ret__Z(var1 Java__lang__CharSequence__Instance) bool {
	return this._dispatch.ContentEquals__desc____obj__Java__lang__CharSequence__ret__Z(var1)
}

func (this *Java__lang__String__Instance) Dispatch__EndsWith__desc____obj__Java__lang__String__ret__Z(var1 *Java__lang__String__Instance) bool {
	return this._dispatch.EndsWith__desc____obj__Java__lang__String__ret__Z(var1)
}

func (this *Java__lang__String__Instance) Dispatch__EqualsIgnoreCase__desc____obj__Java__lang__String__ret__Z(var1 *Java__lang__String__Instance) bool {
	return this._dispatch.EqualsIgnoreCase__desc____obj__Java__lang__String__ret__Z(var1)
}

func (this *Java__lang__String__Instance) Dispatch__GetBytes__desc____ret____arr__B() []byte {
	return this._dispatch.GetBytes__desc____ret____arr__B()
}

func (this *Java__lang__String__Instance) Dispatch__GetBytes__desc__I__I____arr__B__I__ret__V(var1 int, var2 int, var3 []byte, var4 int) {
	this._dispatch.GetBytes__desc__I__I____arr__B__I__ret__V(var1, var2, var3, var4)
}

func (this *Java__lang__String__Instance) Dispatch__GetBytes__desc____obj__Java__lang__String__ret____arr__B(var1 *Java__lang__String__Instance) []byte {
	return this._dispatch.GetBytes__desc____obj__Java__lang__String__ret____arr__B(var1)
}

func (this *Java__lang__String__Instance) Dispatch__GetChars__desc__I__I____arr__C__I__ret__V(var1 int, var2 int, var3 []rune, var4 int) {
	this._dispatch.GetChars__desc__I__I____arr__C__I__ret__V(var1, var2, var3, var4)
}

func (this *Java__lang__String__Instance) Dispatch__GetChars__desc____arr__C__I__ret__V(var1 []rune, var2 int) {
	this._dispatch.GetChars__desc____arr__C__I__ret__V(var1, var2)
}

func (this *Java__lang__String__Instance) Dispatch__IndexOf__desc__I__ret__I(var1 int) int {
	return this._dispatch.IndexOf__desc__I__ret__I(var1)
}

func (this *Java__lang__String__Instance) Dispatch__IndexOf__desc__I__I__ret__I(var1 int, var2 int) int {
	return this._dispatch.IndexOf__desc__I__I__ret__I(var1, var2)
}

func (this *Java__lang__String__Instance) Dispatch__IndexOf__desc____obj__Java__lang__String__ret__I(var1 *Java__lang__String__Instance) int {
	return this._dispatch.IndexOf__desc____obj__Java__lang__String__ret__I(var1)
}

func (this *Java__lang__String__Instance) Dispatch__IndexOf__desc____obj__Java__lang__String__I__ret__I(var1 *Java__lang__String__Instance, var2 int) int {
	return this._dispatch.IndexOf__desc____obj__Java__lang__String__I__ret__I(var1, var2)
}

func (this *Java__lang__String__Instance) Dispatch__Intern__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	return this._dispatch.Intern__desc____ret____obj__Java__lang__String()
}

func (this *Java__lang__String__Instance) Dispatch__IsEmpty__desc____ret__Z() bool {
	return this._dispatch.IsEmpty__desc____ret__Z()
}

func (this *Java__lang__String__Instance) Dispatch__LastIndexOf__desc__I__ret__I(var1 int) int {
	return this._dispatch.LastIndexOf__desc__I__ret__I(var1)
}

func (this *Java__lang__String__Instance) Dispatch__LastIndexOf__desc__I__I__ret__I(var1 int, var2 int) int {
	return this._dispatch.LastIndexOf__desc__I__I__ret__I(var1, var2)
}

func (this *Java__lang__String__Instance) Dispatch__LastIndexOf__desc____obj__Java__lang__String__ret__I(var1 *Java__lang__String__Instance) int {
	return this._dispatch.LastIndexOf__desc____obj__Java__lang__String__ret__I(var1)
}

func (this *Java__lang__String__Instance) Dispatch__LastIndexOf__desc____obj__Java__lang__String__I__ret__I(var1 *Java__lang__String__Instance, var2 int) int {
	return this._dispatch.LastIndexOf__desc____obj__Java__lang__String__I__ret__I(var1, var2)
}

func (this *Java__lang__String__Instance) Dispatch__Length__desc____ret__I() int {
	return this._dispatch.Length__desc____ret__I()
}

func (this *Java__lang__String__Instance) Dispatch__Matches__desc____obj__Java__lang__String__ret__Z(var1 *Java__lang__String__Instance) bool {
	return this._dispatch.Matches__desc____obj__Java__lang__String__ret__Z(var1)
}

func (this *Java__lang__String__Instance) Dispatch__OffsetByCodePoints__desc__I__I__ret__I(var1 int, var2 int) int {
	return this._dispatch.OffsetByCodePoints__desc__I__I__ret__I(var1, var2)
}

func (this *Java__lang__String__Instance) Dispatch__RegionMatches__desc__I____obj__Java__lang__String__I__I__ret__Z(var1 int, var2 *Java__lang__String__Instance, var3 int, var4 int) bool {
	return this._dispatch.RegionMatches__desc__I____obj__Java__lang__String__I__I__ret__Z(var1, var2, var3, var4)
}

func (this *Java__lang__String__Instance) Dispatch__RegionMatches__desc__Z__I____obj__Java__lang__String__I__I__ret__Z(var1 bool, var2 int, var3 *Java__lang__String__Instance, var4 int, var5 int) bool {
	return this._dispatch.RegionMatches__desc__Z__I____obj__Java__lang__String__I__I__ret__Z(var1, var2, var3, var4, var5)
}

func (this *Java__lang__String__Instance) Dispatch__Replace__desc__C__C__ret____obj__Java__lang__String(var1 rune, var2 rune) *Java__lang__String__Instance {
	return this._dispatch.Replace__desc__C__C__ret____obj__Java__lang__String(var1, var2)
}

func (this *Java__lang__String__Instance) Dispatch__Replace__desc____obj__Java__lang__CharSequence____obj__Java__lang__CharSequence__ret____obj__Java__lang__String(var1 Java__lang__CharSequence__Instance, var2 Java__lang__CharSequence__Instance) *Java__lang__String__Instance {
	return this._dispatch.Replace__desc____obj__Java__lang__CharSequence____obj__Java__lang__CharSequence__ret____obj__Java__lang__String(var1, var2)
}

func (this *Java__lang__String__Instance) Dispatch__ReplaceAll__desc____obj__Java__lang__String____obj__Java__lang__String__ret____obj__Java__lang__String(var1 *Java__lang__String__Instance, var2 *Java__lang__String__Instance) *Java__lang__String__Instance {
	return this._dispatch.ReplaceAll__desc____obj__Java__lang__String____obj__Java__lang__String__ret____obj__Java__lang__String(var1, var2)
}

func (this *Java__lang__String__Instance) Dispatch__ReplaceFirst__desc____obj__Java__lang__String____obj__Java__lang__String__ret____obj__Java__lang__String(var1 *Java__lang__String__Instance, var2 *Java__lang__String__Instance) *Java__lang__String__Instance {
	return this._dispatch.ReplaceFirst__desc____obj__Java__lang__String____obj__Java__lang__String__ret____obj__Java__lang__String(var1, var2)
}

func (this *Java__lang__String__Instance) Dispatch__Split__desc____obj__Java__lang__String__ret____arr____obj__Java__lang__String(var1 *Java__lang__String__Instance) []*Java__lang__String__Instance {
	return this._dispatch.Split__desc____obj__Java__lang__String__ret____arr____obj__Java__lang__String(var1)
}

func (this *Java__lang__String__Instance) Dispatch__Split__desc____obj__Java__lang__String__I__ret____arr____obj__Java__lang__String(var1 *Java__lang__String__Instance, var2 int) []*Java__lang__String__Instance {
	return this._dispatch.Split__desc____obj__Java__lang__String__I__ret____arr____obj__Java__lang__String(var1, var2)
}

func (this *Java__lang__String__Instance) Dispatch__StartsWith__desc____obj__Java__lang__String__ret__Z(var1 *Java__lang__String__Instance) bool {
	return this._dispatch.StartsWith__desc____obj__Java__lang__String__ret__Z(var1)
}

func (this *Java__lang__String__Instance) Dispatch__StartsWith__desc____obj__Java__lang__String__I__ret__Z(var1 *Java__lang__String__Instance, var2 int) bool {
	return this._dispatch.StartsWith__desc____obj__Java__lang__String__I__ret__Z(var1, var2)
}

func (this *Java__lang__String__Instance) Dispatch__SubSequence__desc__I__I__ret____obj__Java__lang__CharSequence(var1 int, var2 int) Java__lang__CharSequence__Instance {
	return this._dispatch.SubSequence__desc__I__I__ret____obj__Java__lang__CharSequence(var1, var2)
}

func (this *Java__lang__String__Instance) Dispatch__Substring__desc__I__ret____obj__Java__lang__String(var1 int) *Java__lang__String__Instance {
	return this._dispatch.Substring__desc__I__ret____obj__Java__lang__String(var1)
}

func (this *Java__lang__String__Instance) Dispatch__Substring__desc__I__I__ret____obj__Java__lang__String(var1 int, var2 int) *Java__lang__String__Instance {
	return this._dispatch.Substring__desc__I__I__ret____obj__Java__lang__String(var1, var2)
}

func (this *Java__lang__String__Instance) Dispatch__ToCharArray__desc____ret____arr__C() []rune {
	return this._dispatch.ToCharArray__desc____ret____arr__C()
}

func (this *Java__lang__String__Instance) Dispatch__ToLowerCase__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	return this._dispatch.ToLowerCase__desc____ret____obj__Java__lang__String()
}

func (this *Java__lang__String__Instance) Dispatch__ToUpperCase__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	return this._dispatch.ToUpperCase__desc____ret____obj__Java__lang__String()
}

func (this *Java__lang__String__Instance) Dispatch__Trim__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	return this._dispatch.Trim__desc____ret____obj__Java__lang__String()
}

type Java__lang__String__Instance struct {
	*Java__lang__Object__Instance
	_dispatch  Java__lang__String__Dispatch
	Underlying string
}

func (this *Java__lang__String__Instance) Instance_Init__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____obj__Java__lang__String__ret__V(var1 *Java__lang__String__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____obj__Java__lang__StringBuilder__ret__V(var1 *Java__lang__StringBuilder__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__B__ret__V(var1 []byte) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__B__I__ret__V(var1 []byte, var2 int) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__B__I__I__ret__V(var1 []byte, var2 int, var3 int) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__B__I__I__I__ret__V(var1 []byte, var2 int, var3 int, var4 int) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__B__I__I____obj__Java__lang__String__ret__V(var1 []byte, var2 int, var3 int, var4 *Java__lang__String__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__B____obj__Java__lang__String__ret__V(var1 []byte, var2 *Java__lang__String__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__C__ret__V(var1 []rune) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__C__I__I__ret__V(var1 []rune, var2 int, var3 int) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__C__Z__ret__V(var1 []rune, var2 bool) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__I__I__I__ret__V(var1 []int, var2 int, var3 int) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) CharAt__desc__I__ret__C(var1 int) rune {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) CodePointAt__desc__I__ret__I(var1 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) CodePointBefore__desc__I__ret__I(var1 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) CodePointCount__desc__I__I__ret__I(var1 int, var2 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) CompareTo__desc____obj__Java__lang__Object__ret__I(var1 *Java__lang__Object__Instance) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) CompareTo__desc____obj__Java__lang__String__ret__I(var1 *Java__lang__String__Instance) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) CompareToIgnoreCase__desc____obj__Java__lang__String__ret__I(var1 *Java__lang__String__Instance) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Concat__desc____obj__Java__lang__String__ret____obj__Java__lang__String(var1 *Java__lang__String__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Contains__desc____obj__Java__lang__CharSequence__ret__Z(var1 Java__lang__CharSequence__Instance) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) ContentEquals__desc____obj__Java__lang__CharSequence__ret__Z(var1 Java__lang__CharSequence__Instance) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) EndsWith__desc____obj__Java__lang__String__ret__Z(var1 *Java__lang__String__Instance) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Equals__desc____obj__Java__lang__Object__ret__Z(var1 *Java__lang__Object__Instance) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) EqualsIgnoreCase__desc____obj__Java__lang__String__ret__Z(var1 *Java__lang__String__Instance) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) GetBytes__desc____ret____arr__B() []byte {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) GetBytes__desc__I__I____arr__B__I__ret__V(var1 int, var2 int, var3 []byte, var4 int) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) GetBytes__desc____obj__Java__lang__String__ret____arr__B(var1 *Java__lang__String__Instance) []byte {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) GetChars__desc__I__I____arr__C__I__ret__V(var1 int, var2 int, var3 []rune, var4 int) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) GetChars__desc____arr__C__I__ret__V(var1 []rune, var2 int) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) HashCode__desc____ret__I() int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) IndexOf__desc__I__ret__I(var1 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) IndexOf__desc__I__I__ret__I(var1 int, var2 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) IndexOf__desc____obj__Java__lang__String__ret__I(var1 *Java__lang__String__Instance) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) IndexOf__desc____obj__Java__lang__String__I__ret__I(var1 *Java__lang__String__Instance, var2 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Intern__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) IsEmpty__desc____ret__Z() bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) LastIndexOf__desc__I__ret__I(var1 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) LastIndexOf__desc__I__I__ret__I(var1 int, var2 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) LastIndexOf__desc____obj__Java__lang__String__ret__I(var1 *Java__lang__String__Instance) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) LastIndexOf__desc____obj__Java__lang__String__I__ret__I(var1 *Java__lang__String__Instance, var2 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Length__desc____ret__I() int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Matches__desc____obj__Java__lang__String__ret__Z(var1 *Java__lang__String__Instance) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) OffsetByCodePoints__desc__I__I__ret__I(var1 int, var2 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) RegionMatches__desc__I____obj__Java__lang__String__I__I__ret__Z(var1 int, var2 *Java__lang__String__Instance, var3 int, var4 int) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) RegionMatches__desc__Z__I____obj__Java__lang__String__I__I__ret__Z(var1 bool, var2 int, var3 *Java__lang__String__Instance, var4 int, var5 int) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Replace__desc__C__C__ret____obj__Java__lang__String(var1 rune, var2 rune) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Replace__desc____obj__Java__lang__CharSequence____obj__Java__lang__CharSequence__ret____obj__Java__lang__String(var1 Java__lang__CharSequence__Instance, var2 Java__lang__CharSequence__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) ReplaceAll__desc____obj__Java__lang__String____obj__Java__lang__String__ret____obj__Java__lang__String(var1 *Java__lang__String__Instance, var2 *Java__lang__String__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) ReplaceFirst__desc____obj__Java__lang__String____obj__Java__lang__String__ret____obj__Java__lang__String(var1 *Java__lang__String__Instance, var2 *Java__lang__String__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Split__desc____obj__Java__lang__String__ret____arr____obj__Java__lang__String(var1 *Java__lang__String__Instance) []*Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Split__desc____obj__Java__lang__String__I__ret____arr____obj__Java__lang__String(var1 *Java__lang__String__Instance, var2 int) []*Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) StartsWith__desc____obj__Java__lang__String__ret__Z(var1 *Java__lang__String__Instance) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) StartsWith__desc____obj__Java__lang__String__I__ret__Z(var1 *Java__lang__String__Instance, var2 int) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) SubSequence__desc__I__I__ret____obj__Java__lang__CharSequence(var1 int, var2 int) Java__lang__CharSequence__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Substring__desc__I__ret____obj__Java__lang__String(var1 int) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Substring__desc__I__I__ret____obj__Java__lang__String(var1 int, var2 int) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) ToCharArray__desc____ret____arr__C() []rune {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) ToLowerCase__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) ToString__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) ToUpperCase__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Trim__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

type Java__lang__StringBuilder__Static struct {
	SerialVersionUID int64
}

var Java__lang__StringBuilder__Var Java__lang__StringBuilder__Static

func Java__lang__StringBuilder() *Java__lang__StringBuilder__Static {
	return &Java__lang__StringBuilder__Var
}

func (this *Java__lang__StringBuilder__Static) New() *Java__lang__StringBuilder__Instance {
	v := &Java__lang__StringBuilder__Instance{
		Java__lang__AbstractStringBuilder__Instance: Java__lang__AbstractStringBuilder().New(),
	}
	v.Java__lang__StringBuilder__InitDispatch(v)
	return v
}

type Java__lang__StringBuilder__Dispatch interface {
	Java__lang__AbstractStringBuilder__Dispatch
	Instance_Init__desc____obj__Java__lang__CharSequence__ret__V(Java__lang__CharSequence__Instance)
	Instance_Init__desc____obj__Java__lang__String__ret__V(*Java__lang__String__Instance)
	Append__desc__C__ret____obj__Java__lang__StringBuilder(rune) *Java__lang__StringBuilder__Instance
	Append__desc__D__ret____obj__Java__lang__StringBuilder(float64) *Java__lang__StringBuilder__Instance
	Append__desc__F__ret____obj__Java__lang__StringBuilder(float32) *Java__lang__StringBuilder__Instance
	Append__desc__I__ret____obj__Java__lang__StringBuilder(int) *Java__lang__StringBuilder__Instance
	Append__desc__J__ret____obj__Java__lang__StringBuilder(int64) *Java__lang__StringBuilder__Instance
	Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__StringBuilder(Java__lang__CharSequence__Instance) *Java__lang__StringBuilder__Instance
	Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__StringBuilder(Java__lang__CharSequence__Instance, int, int) *Java__lang__StringBuilder__Instance
	Append__desc____obj__Java__lang__Object__ret____obj__Java__lang__StringBuilder(*Java__lang__Object__Instance) *Java__lang__StringBuilder__Instance
	Append__desc____obj__Java__lang__String__ret____obj__Java__lang__StringBuilder(*Java__lang__String__Instance) *Java__lang__StringBuilder__Instance
	Append__desc__Z__ret____obj__Java__lang__StringBuilder(bool) *Java__lang__StringBuilder__Instance
	Append__desc____arr__C__ret____obj__Java__lang__StringBuilder([]rune) *Java__lang__StringBuilder__Instance
	Append__desc____arr__C__I__I__ret____obj__Java__lang__StringBuilder([]rune, int, int) *Java__lang__StringBuilder__Instance
	AppendCodePoint__desc__I__ret____obj__Java__lang__StringBuilder(int) *Java__lang__StringBuilder__Instance
	Delete__desc__I__I__ret____obj__Java__lang__StringBuilder(int, int) *Java__lang__StringBuilder__Instance
	DeleteCharAt__desc__I__ret____obj__Java__lang__StringBuilder(int) *Java__lang__StringBuilder__Instance
	Insert__desc__I__C__ret____obj__Java__lang__StringBuilder(int, rune) *Java__lang__StringBuilder__Instance
	Insert__desc__I__D__ret____obj__Java__lang__StringBuilder(int, float64) *Java__lang__StringBuilder__Instance
	Insert__desc__I__F__ret____obj__Java__lang__StringBuilder(int, float32) *Java__lang__StringBuilder__Instance
	Insert__desc__I__I__ret____obj__Java__lang__StringBuilder(int, int) *Java__lang__StringBuilder__Instance
	Insert__desc__I__J__ret____obj__Java__lang__StringBuilder(int, int64) *Java__lang__StringBuilder__Instance
	Insert__desc__I____obj__Java__lang__CharSequence__ret____obj__Java__lang__StringBuilder(int, Java__lang__CharSequence__Instance) *Java__lang__StringBuilder__Instance
	Insert__desc__I____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__StringBuilder(int, Java__lang__CharSequence__Instance, int, int) *Java__lang__StringBuilder__Instance
	Insert__desc__I____obj__Java__lang__Object__ret____obj__Java__lang__StringBuilder(int, *Java__lang__Object__Instance) *Java__lang__StringBuilder__Instance
	Insert__desc__I____obj__Java__lang__String__ret____obj__Java__lang__StringBuilder(int, *Java__lang__String__Instance) *Java__lang__StringBuilder__Instance
	Insert__desc__I__Z__ret____obj__Java__lang__StringBuilder(int, bool) *Java__lang__StringBuilder__Instance
	Insert__desc__I____arr__C__ret____obj__Java__lang__StringBuilder(int, []rune) *Java__lang__StringBuilder__Instance
	Insert__desc__I____arr__C__I__I__ret____obj__Java__lang__StringBuilder(int, []rune, int, int) *Java__lang__StringBuilder__Instance
	Replace__desc__I__I____obj__Java__lang__String__ret____obj__Java__lang__StringBuilder(int, int, *Java__lang__String__Instance) *Java__lang__StringBuilder__Instance
	Reverse__desc____ret____obj__Java__lang__StringBuilder() *Java__lang__StringBuilder__Instance
}

func (this *Java__lang__StringBuilder__Instance) Java__lang__StringBuilder__InitDispatch(v Java__lang__StringBuilder__Dispatch) {
	this.Java__lang__AbstractStringBuilder__Instance.Java__lang__AbstractStringBuilder__InitDispatch(v)
	this._dispatch = v
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Instance_Init__desc____obj__Java__lang__CharSequence__ret__V(var1 Java__lang__CharSequence__Instance) {
	this._dispatch.Instance_Init__desc____obj__Java__lang__CharSequence__ret__V(var1)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Instance_Init__desc____obj__Java__lang__String__ret__V(var1 *Java__lang__String__Instance) {
	this._dispatch.Instance_Init__desc____obj__Java__lang__String__ret__V(var1)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Append__desc__C__ret____obj__Java__lang__StringBuilder(var1 rune) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Append__desc__C__ret____obj__Java__lang__StringBuilder(var1)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Append__desc__D__ret____obj__Java__lang__StringBuilder(var1 float64) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Append__desc__D__ret____obj__Java__lang__StringBuilder(var1)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Append__desc__F__ret____obj__Java__lang__StringBuilder(var1 float32) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Append__desc__F__ret____obj__Java__lang__StringBuilder(var1)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Append__desc__I__ret____obj__Java__lang__StringBuilder(var1 int) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Append__desc__I__ret____obj__Java__lang__StringBuilder(var1)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Append__desc__J__ret____obj__Java__lang__StringBuilder(var1 int64) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Append__desc__J__ret____obj__Java__lang__StringBuilder(var1)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__StringBuilder(var1 Java__lang__CharSequence__Instance) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__StringBuilder(var1)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__StringBuilder(var1 Java__lang__CharSequence__Instance, var2 int, var3 int) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__StringBuilder(var1, var2, var3)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Append__desc____obj__Java__lang__Object__ret____obj__Java__lang__StringBuilder(var1 *Java__lang__Object__Instance) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Append__desc____obj__Java__lang__Object__ret____obj__Java__lang__StringBuilder(var1)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Append__desc____obj__Java__lang__String__ret____obj__Java__lang__StringBuilder(var1 *Java__lang__String__Instance) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Append__desc____obj__Java__lang__String__ret____obj__Java__lang__StringBuilder(var1)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Append__desc__Z__ret____obj__Java__lang__StringBuilder(var1 bool) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Append__desc__Z__ret____obj__Java__lang__StringBuilder(var1)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Append__desc____arr__C__ret____obj__Java__lang__StringBuilder(var1 []rune) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Append__desc____arr__C__ret____obj__Java__lang__StringBuilder(var1)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Append__desc____arr__C__I__I__ret____obj__Java__lang__StringBuilder(var1 []rune, var2 int, var3 int) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Append__desc____arr__C__I__I__ret____obj__Java__lang__StringBuilder(var1, var2, var3)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__AppendCodePoint__desc__I__ret____obj__Java__lang__StringBuilder(var1 int) *Java__lang__StringBuilder__Instance {
	return this._dispatch.AppendCodePoint__desc__I__ret____obj__Java__lang__StringBuilder(var1)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Delete__desc__I__I__ret____obj__Java__lang__StringBuilder(var1 int, var2 int) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Delete__desc__I__I__ret____obj__Java__lang__StringBuilder(var1, var2)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__DeleteCharAt__desc__I__ret____obj__Java__lang__StringBuilder(var1 int) *Java__lang__StringBuilder__Instance {
	return this._dispatch.DeleteCharAt__desc__I__ret____obj__Java__lang__StringBuilder(var1)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Insert__desc__I__C__ret____obj__Java__lang__StringBuilder(var1 int, var2 rune) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Insert__desc__I__C__ret____obj__Java__lang__StringBuilder(var1, var2)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Insert__desc__I__D__ret____obj__Java__lang__StringBuilder(var1 int, var2 float64) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Insert__desc__I__D__ret____obj__Java__lang__StringBuilder(var1, var2)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Insert__desc__I__F__ret____obj__Java__lang__StringBuilder(var1 int, var2 float32) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Insert__desc__I__F__ret____obj__Java__lang__StringBuilder(var1, var2)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Insert__desc__I__I__ret____obj__Java__lang__StringBuilder(var1 int, var2 int) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Insert__desc__I__I__ret____obj__Java__lang__StringBuilder(var1, var2)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Insert__desc__I__J__ret____obj__Java__lang__StringBuilder(var1 int, var2 int64) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Insert__desc__I__J__ret____obj__Java__lang__StringBuilder(var1, var2)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Insert__desc__I____obj__Java__lang__CharSequence__ret____obj__Java__lang__StringBuilder(var1 int, var2 Java__lang__CharSequence__Instance) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Insert__desc__I____obj__Java__lang__CharSequence__ret____obj__Java__lang__StringBuilder(var1, var2)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Insert__desc__I____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__StringBuilder(var1 int, var2 Java__lang__CharSequence__Instance, var3 int, var4 int) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Insert__desc__I____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__StringBuilder(var1, var2, var3, var4)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Insert__desc__I____obj__Java__lang__Object__ret____obj__Java__lang__StringBuilder(var1 int, var2 *Java__lang__Object__Instance) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Insert__desc__I____obj__Java__lang__Object__ret____obj__Java__lang__StringBuilder(var1, var2)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Insert__desc__I____obj__Java__lang__String__ret____obj__Java__lang__StringBuilder(var1 int, var2 *Java__lang__String__Instance) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Insert__desc__I____obj__Java__lang__String__ret____obj__Java__lang__StringBuilder(var1, var2)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Insert__desc__I__Z__ret____obj__Java__lang__StringBuilder(var1 int, var2 bool) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Insert__desc__I__Z__ret____obj__Java__lang__StringBuilder(var1, var2)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Insert__desc__I____arr__C__ret____obj__Java__lang__StringBuilder(var1 int, var2 []rune) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Insert__desc__I____arr__C__ret____obj__Java__lang__StringBuilder(var1, var2)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Insert__desc__I____arr__C__I__I__ret____obj__Java__lang__StringBuilder(var1 int, var2 []rune, var3 int, var4 int) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Insert__desc__I____arr__C__I__I__ret____obj__Java__lang__StringBuilder(var1, var2, var3, var4)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Replace__desc__I__I____obj__Java__lang__String__ret____obj__Java__lang__StringBuilder(var1 int, var2 int, var3 *Java__lang__String__Instance) *Java__lang__StringBuilder__Instance {
	return this._dispatch.Replace__desc__I__I____obj__Java__lang__String__ret____obj__Java__lang__StringBuilder(var1, var2, var3)
}

func (this *Java__lang__StringBuilder__Instance) Dispatch__Reverse__desc____ret____obj__Java__lang__StringBuilder() *Java__lang__StringBuilder__Instance {
	return this._dispatch.Reverse__desc____ret____obj__Java__lang__StringBuilder()
}

type Java__lang__StringBuilder__Instance struct {
	*Java__lang__AbstractStringBuilder__Instance
	_dispatch  Java__lang__StringBuilder__Dispatch
	Underlying string
}

func (this *Java__lang__StringBuilder__Instance) Instance_Init__desc__I__ret__V(var1 int) {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Instance_Init__desc____obj__Java__lang__CharSequence__ret__V(var1 Java__lang__CharSequence__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Instance_Init__desc____obj__Java__lang__String__ret__V(var1 *Java__lang__String__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc__C__ret____obj__Java__lang__AbstractStringBuilder(var1 rune) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc__C__ret____obj__Java__lang__Appendable(var1 rune) Java__lang__Appendable__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc__C__ret____obj__Java__lang__StringBuilder(var1 rune) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc__D__ret____obj__Java__lang__AbstractStringBuilder(var1 float64) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc__D__ret____obj__Java__lang__StringBuilder(var1 float64) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc__F__ret____obj__Java__lang__AbstractStringBuilder(var1 float32) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc__F__ret____obj__Java__lang__StringBuilder(var1 float32) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc__J__ret____obj__Java__lang__AbstractStringBuilder(var1 int64) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc__J__ret____obj__Java__lang__StringBuilder(var1 int64) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__AbstractStringBuilder(var1 Java__lang__CharSequence__Instance) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__Appendable(var1 Java__lang__CharSequence__Instance) Java__lang__Appendable__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__StringBuilder(var1 Java__lang__CharSequence__Instance) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1 Java__lang__CharSequence__Instance, var2 int, var3 int) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__Appendable(var1 Java__lang__CharSequence__Instance, var2 int, var3 int) Java__lang__Appendable__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__StringBuilder(var1 Java__lang__CharSequence__Instance, var2 int, var3 int) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc____obj__Java__lang__Object__ret____obj__Java__lang__AbstractStringBuilder(var1 *Java__lang__Object__Instance) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc____obj__Java__lang__Object__ret____obj__Java__lang__StringBuilder(var1 *Java__lang__Object__Instance) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc____obj__Java__lang__String__ret____obj__Java__lang__AbstractStringBuilder(var1 *Java__lang__String__Instance) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc__Z__ret____obj__Java__lang__AbstractStringBuilder(var1 bool) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc__Z__ret____obj__Java__lang__StringBuilder(var1 bool) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc____arr__C__ret____obj__Java__lang__AbstractStringBuilder(var1 []rune) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc____arr__C__ret____obj__Java__lang__StringBuilder(var1 []rune) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc____arr__C__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1 []rune, var2 int, var3 int) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Append__desc____arr__C__I__I__ret____obj__Java__lang__StringBuilder(var1 []rune, var2 int, var3 int) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) AppendCodePoint__desc__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) AppendCodePoint__desc__I__ret____obj__Java__lang__StringBuilder(var1 int) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Capacity__desc____ret__I() int {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) CharAt__desc__I__ret__C(var1 int) rune {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) CodePointAt__desc__I__ret__I(var1 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) CodePointBefore__desc__I__ret__I(var1 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) CodePointCount__desc__I__I__ret__I(var1 int, var2 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Delete__desc__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 int) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Delete__desc__I__I__ret____obj__Java__lang__StringBuilder(var1 int, var2 int) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) DeleteCharAt__desc__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) DeleteCharAt__desc__I__ret____obj__Java__lang__StringBuilder(var1 int) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) EnsureCapacity__desc__I__ret__V(var1 int) {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) GetChars__desc__I__I____arr__C__I__ret__V(var1 int, var2 int, var3 []rune, var4 int) {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) IndexOf__desc____obj__Java__lang__String__ret__I(var1 *Java__lang__String__Instance) int {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) IndexOf__desc____obj__Java__lang__String__I__ret__I(var1 *Java__lang__String__Instance, var2 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I__C__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 rune) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I__C__ret____obj__Java__lang__StringBuilder(var1 int, var2 rune) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I__D__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 float64) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I__D__ret____obj__Java__lang__StringBuilder(var1 int, var2 float64) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I__F__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 float32) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I__F__ret____obj__Java__lang__StringBuilder(var1 int, var2 float32) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 int) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I__I__ret____obj__Java__lang__StringBuilder(var1 int, var2 int) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I__J__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 int64) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I__J__ret____obj__Java__lang__StringBuilder(var1 int, var2 int64) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I____obj__Java__lang__CharSequence__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 Java__lang__CharSequence__Instance) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I____obj__Java__lang__CharSequence__ret____obj__Java__lang__StringBuilder(var1 int, var2 Java__lang__CharSequence__Instance) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 Java__lang__CharSequence__Instance, var3 int, var4 int) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__StringBuilder(var1 int, var2 Java__lang__CharSequence__Instance, var3 int, var4 int) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I____obj__Java__lang__Object__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 *Java__lang__Object__Instance) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I____obj__Java__lang__Object__ret____obj__Java__lang__StringBuilder(var1 int, var2 *Java__lang__Object__Instance) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I____obj__Java__lang__String__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 *Java__lang__String__Instance) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I____obj__Java__lang__String__ret____obj__Java__lang__StringBuilder(var1 int, var2 *Java__lang__String__Instance) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I__Z__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 bool) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I__Z__ret____obj__Java__lang__StringBuilder(var1 int, var2 bool) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I____arr__C__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 []rune) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I____arr__C__ret____obj__Java__lang__StringBuilder(var1 int, var2 []rune) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I____arr__C__I__I__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 []rune, var3 int, var4 int) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Insert__desc__I____arr__C__I__I__ret____obj__Java__lang__StringBuilder(var1 int, var2 []rune, var3 int, var4 int) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) LastIndexOf__desc____obj__Java__lang__String__ret__I(var1 *Java__lang__String__Instance) int {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) LastIndexOf__desc____obj__Java__lang__String__I__ret__I(var1 *Java__lang__String__Instance, var2 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Length__desc____ret__I() int {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) OffsetByCodePoints__desc__I__I__ret__I(var1 int, var2 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Replace__desc__I__I____obj__Java__lang__String__ret____obj__Java__lang__AbstractStringBuilder(var1 int, var2 int, var3 *Java__lang__String__Instance) *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Replace__desc__I__I____obj__Java__lang__String__ret____obj__Java__lang__StringBuilder(var1 int, var2 int, var3 *Java__lang__String__Instance) *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Reverse__desc____ret____obj__Java__lang__AbstractStringBuilder() *Java__lang__AbstractStringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Reverse__desc____ret____obj__Java__lang__StringBuilder() *Java__lang__StringBuilder__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) SetCharAt__desc__I__C__ret__V(var1 int, var2 rune) {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) SetLength__desc__I__ret__V(var1 int) {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) SubSequence__desc__I__I__ret____obj__Java__lang__CharSequence(var1 int, var2 int) Java__lang__CharSequence__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Substring__desc__I__ret____obj__Java__lang__String(var1 int) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) Substring__desc__I__I__ret____obj__Java__lang__String(var1 int, var2 int) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__StringBuilder__Instance) TrimToSize__desc____ret__V() {
	panic("Not Implemented")
}

type Java__lang__System__Static struct {
	Err  *Java__io__PrintStream__Instance
	Out  *Java__io__PrintStream__Instance
	init sync.Once
}

var Java__lang__System__Var Java__lang__System__Static

func Java__lang__System() *Java__lang__System__Static {
	Java__lang__System__Var.init.Do(Java__lang__System__Var.Static_Init__desc____ret__V)
	return &Java__lang__System__Var
}

func (this *Java__lang__System__Static) New() *Java__lang__System__Instance {
	v := &Java__lang__System__Instance{
		Java__lang__Object__Instance: Java__lang__Object().New(),
	}
	v.Java__lang__System__InitDispatch(v)
	return v
}

func (this *Java__lang__System__Static) Arraycopy__desc____obj__Java__lang__Object__I____obj__Java__lang__Object__I__I__ret__V(var0 *Java__lang__Object__Instance, var1 int, var2 *Java__lang__Object__Instance, var3 int, var4 int) {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) ClearProperty__desc____obj__Java__lang__String__ret____obj__Java__lang__String(var0 *Java__lang__String__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) CurrentTimeMillis__desc____ret__J() int64 {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) Exit__desc__I__ret__V(var0 int) {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) Gc__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) GetProperty__desc____obj__Java__lang__String__ret____obj__Java__lang__String(var0 *Java__lang__String__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) GetProperty__desc____obj__Java__lang__String____obj__Java__lang__String__ret____obj__Java__lang__String(var0 *Java__lang__String__Instance, var1 *Java__lang__String__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) Getenv__desc____obj__Java__lang__String__ret____obj__Java__lang__String(var0 *Java__lang__String__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) IdentityHashCode__desc____obj__Java__lang__Object__ret__I(var0 *Java__lang__Object__Instance) int {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) LineSeparator__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) Load__desc____obj__Java__lang__String__ret__V(var0 *Java__lang__String__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) LoadLibrary__desc____obj__Java__lang__String__ret__V(var0 *Java__lang__String__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) MapLibraryName__desc____obj__Java__lang__String__ret____obj__Java__lang__String(var0 *Java__lang__String__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) NanoTime__desc____ret__J() int64 {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) RunFinalization__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) RunFinalizersOnExit__desc__Z__ret__V(var0 bool) {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) SetErr__desc____obj__Java__io__PrintStream__ret__V(var0 *Java__io__PrintStream__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) SetOut__desc____obj__Java__io__PrintStream__ret__V(var0 *Java__io__PrintStream__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) SetProperty__desc____obj__Java__lang__String____obj__Java__lang__String__ret____obj__Java__lang__String(var0 *Java__lang__String__Instance, var1 *Java__lang__String__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

type Java__lang__System__Dispatch interface {
	Java__lang__Object__Dispatch
}

func (this *Java__lang__System__Instance) Java__lang__System__InitDispatch(v Java__lang__System__Dispatch) {
	this.Java__lang__Object__Instance.Java__lang__Object__InitDispatch(v)
	this._dispatch = v
}

type Java__lang__System__Instance struct {
	*Java__lang__Object__Instance
	_dispatch Java__lang__System__Dispatch
}

type Java__lang__Throwable__Static struct {
	__dollar__assertionsDisabled bool
	init                         sync.Once
}

var Java__lang__Throwable__Var Java__lang__Throwable__Static

func Java__lang__Throwable() *Java__lang__Throwable__Static {
	Java__lang__Throwable__Var.init.Do(Java__lang__Throwable__Var.Static_Init__desc____ret__V)
	return &Java__lang__Throwable__Var
}

func (this *Java__lang__Throwable__Static) New() *Java__lang__Throwable__Instance {
	v := &Java__lang__Throwable__Instance{
		Java__lang__Object__Instance: Java__lang__Object().New(),
	}
	v.Java__lang__Throwable__InitDispatch(v)
	return v
}

func (this *Java__lang__Throwable__Static) Static_Init__desc____ret__V() {
	
}

type Java__lang__Throwable__Dispatch interface {
	Java__lang__Object__Dispatch
	Instance_Init__desc____obj__Java__lang__String__ret__V(*Java__lang__String__Instance)
	Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__Throwable__ret__V(*Java__lang__String__Instance, *Java__lang__Throwable__Instance)
	Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__Throwable__Z__Z__ret__V(*Java__lang__String__Instance, *Java__lang__Throwable__Instance, bool, bool)
	Instance_Init__desc____obj__Java__lang__Throwable__ret__V(*Java__lang__Throwable__Instance)
	AddSuppressed__desc____obj__Java__lang__Throwable__ret__V(*Java__lang__Throwable__Instance)
	FillInStackTrace__desc____ret____obj__Java__lang__Throwable() *Java__lang__Throwable__Instance
	GetCause__desc____ret____obj__Java__lang__Throwable() *Java__lang__Throwable__Instance
	GetLocalizedMessage__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance
	GetMessage__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance
	GetStackTraceDepth__desc____ret__I() int
	GetSuppressed__desc____ret____arr____obj__Java__lang__Throwable() []*Java__lang__Throwable__Instance
	InitCause__desc____obj__Java__lang__Throwable__ret____obj__Java__lang__Throwable(*Java__lang__Throwable__Instance) *Java__lang__Throwable__Instance
	PrintStackTrace__desc____ret__V()
	PrintStackTrace__desc____obj__Java__io__PrintStream__ret__V(*Java__io__PrintStream__Instance)
}

func (this *Java__lang__Throwable__Instance) Java__lang__Throwable__InitDispatch(v Java__lang__Throwable__Dispatch) {
	this.Java__lang__Object__Instance.Java__lang__Object__InitDispatch(v)
	this._dispatch = v
}

func (this *Java__lang__Throwable__Instance) Dispatch__Instance_Init__desc____obj__Java__lang__String__ret__V(var1 *Java__lang__String__Instance) {
	this._dispatch.Instance_Init__desc____obj__Java__lang__String__ret__V(var1)
}

func (this *Java__lang__Throwable__Instance) Dispatch__Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__Throwable__ret__V(var1 *Java__lang__String__Instance, var2 *Java__lang__Throwable__Instance) {
	this._dispatch.Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__Throwable__ret__V(var1, var2)
}

func (this *Java__lang__Throwable__Instance) Dispatch__Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__Throwable__Z__Z__ret__V(var1 *Java__lang__String__Instance, var2 *Java__lang__Throwable__Instance, var3 bool, var4 bool) {
	this._dispatch.Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__Throwable__Z__Z__ret__V(var1, var2, var3, var4)
}

func (this *Java__lang__Throwable__Instance) Dispatch__Instance_Init__desc____obj__Java__lang__Throwable__ret__V(var1 *Java__lang__Throwable__Instance) {
	this._dispatch.Instance_Init__desc____obj__Java__lang__Throwable__ret__V(var1)
}

func (this *Java__lang__Throwable__Instance) Dispatch__AddSuppressed__desc____obj__Java__lang__Throwable__ret__V(var1 *Java__lang__Throwable__Instance) {
	this._dispatch.AddSuppressed__desc____obj__Java__lang__Throwable__ret__V(var1)
}

func (this *Java__lang__Throwable__Instance) Dispatch__FillInStackTrace__desc____ret____obj__Java__lang__Throwable() *Java__lang__Throwable__Instance {
	return this._dispatch.FillInStackTrace__desc____ret____obj__Java__lang__Throwable()
}

func (this *Java__lang__Throwable__Instance) Dispatch__GetCause__desc____ret____obj__Java__lang__Throwable() *Java__lang__Throwable__Instance {
	return this._dispatch.GetCause__desc____ret____obj__Java__lang__Throwable()
}

func (this *Java__lang__Throwable__Instance) Dispatch__GetLocalizedMessage__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	return this._dispatch.GetLocalizedMessage__desc____ret____obj__Java__lang__String()
}

func (this *Java__lang__Throwable__Instance) Dispatch__GetMessage__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	return this._dispatch.GetMessage__desc____ret____obj__Java__lang__String()
}

func (this *Java__lang__Throwable__Instance) Dispatch__GetStackTraceDepth__desc____ret__I() int {
	return this._dispatch.GetStackTraceDepth__desc____ret__I()
}

func (this *Java__lang__Throwable__Instance) Dispatch__GetSuppressed__desc____ret____arr____obj__Java__lang__Throwable() []*Java__lang__Throwable__Instance {
	return this._dispatch.GetSuppressed__desc____ret____arr____obj__Java__lang__Throwable()
}

func (this *Java__lang__Throwable__Instance) Dispatch__InitCause__desc____obj__Java__lang__Throwable__ret____obj__Java__lang__Throwable(var1 *Java__lang__Throwable__Instance) *Java__lang__Throwable__Instance {
	return this._dispatch.InitCause__desc____obj__Java__lang__Throwable__ret____obj__Java__lang__Throwable(var1)
}

func (this *Java__lang__Throwable__Instance) Dispatch__PrintStackTrace__desc____ret__V() {
	this._dispatch.PrintStackTrace__desc____ret__V()
}

func (this *Java__lang__Throwable__Instance) Dispatch__PrintStackTrace__desc____obj__Java__io__PrintStream__ret__V(var1 *Java__io__PrintStream__Instance) {
	this._dispatch.PrintStackTrace__desc____obj__Java__io__PrintStream__ret__V(var1)
}

type Java__lang__Throwable__Instance struct {
	*Java__lang__Object__Instance
	_dispatch Java__lang__Throwable__Dispatch
}

func (this *Java__lang__Throwable__Instance) Instance_Init__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) Instance_Init__desc____obj__Java__lang__String__ret__V(var1 *Java__lang__String__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__Throwable__ret__V(var1 *Java__lang__String__Instance, var2 *Java__lang__Throwable__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__Throwable__Z__Z__ret__V(var1 *Java__lang__String__Instance, var2 *Java__lang__Throwable__Instance, var3 bool, var4 bool) {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) Instance_Init__desc____obj__Java__lang__Throwable__ret__V(var1 *Java__lang__Throwable__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) AddSuppressed__desc____obj__Java__lang__Throwable__ret__V(var1 *Java__lang__Throwable__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) FillInStackTrace__desc____ret____obj__Java__lang__Throwable() *Java__lang__Throwable__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) GetCause__desc____ret____obj__Java__lang__Throwable() *Java__lang__Throwable__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) GetLocalizedMessage__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) GetMessage__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) GetStackTraceDepth__desc____ret__I() int {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) GetSuppressed__desc____ret____arr____obj__Java__lang__Throwable() []*Java__lang__Throwable__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) InitCause__desc____obj__Java__lang__Throwable__ret____obj__Java__lang__Throwable(var1 *Java__lang__Throwable__Instance) *Java__lang__Throwable__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) PrintStackTrace__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) PrintStackTrace__desc____obj__Java__io__PrintStream__ret__V(var1 *Java__io__PrintStream__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) ToString__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}
