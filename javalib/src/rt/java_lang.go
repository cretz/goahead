// Generated from Azul Zulu packaged OpenJDK JAR and carries the same
// GPL license with the classpath exception
package rt

import "sync"

type Java__lang__Appendable__Dispatch interface {
	Dispatch__Append__desc____obj__Java__lang__CharSequence__ret____obj__Java__lang__Appendable(Java__lang__CharSequence__Instance) Java__lang__Appendable__Instance
	Dispatch__Append__desc____obj__Java__lang__CharSequence__I__I__ret____obj__Java__lang__Appendable(Java__lang__CharSequence__Instance, int, int) Java__lang__Appendable__Instance
	Dispatch__Append__desc__C__ret____obj__Java__lang__Appendable(rune) Java__lang__Appendable__Instance
}

type Java__lang__Appendable__Instance interface {
	Java__lang__Appendable__Dispatch
}

type Java__lang__AutoCloseable__Dispatch interface {
	Dispatch__Close__desc____ret__V()
}

type Java__lang__AutoCloseable__Instance interface {
	Java__lang__AutoCloseable__Dispatch
}

type Java__lang__CharSequence__Dispatch interface {
	Dispatch__Length__desc____ret__I() int
	Dispatch__CharAt__desc__I__ret__C(int) rune
	Dispatch__SubSequence__desc__I__I__ret____obj__Java__lang__CharSequence(int, int) Java__lang__CharSequence__Instance
}

type Java__lang__CharSequence__Instance interface {
	Java__lang__CharSequence__Dispatch
}

type Java__lang__Comparable__Dispatch interface {
	Dispatch__CompareTo__desc____obj__Java__lang__Object__ret__I(*Java__lang__Object__Instance) int
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
	this.Java__lang__Exception__Dispatch = v
}

type Java__lang__Exception__Instance struct {
	*Java__lang__Throwable__Instance
	Java__lang__Exception__Dispatch
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

func (this *Java__lang__Exception__Instance) Instance_Init__desc____obj__Java__lang__Throwable__ret__V(var1 *Java__lang__Throwable__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__Exception__Instance) Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__Throwable__Z__Z__ret__V(var1 *Java__lang__String__Instance, var2 *Java__lang__Throwable__Instance, var3 bool, var4 bool) {
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
	Dispatch__HashCode__desc____ret__I() int
	Dispatch__Equals__desc____obj__Java__lang__Object__ret__Z(*Java__lang__Object__Instance) bool
	Dispatch__Clone__desc____ret____obj__Java__lang__Object() *Java__lang__Object__Instance
	Dispatch__ToString__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance
	Dispatch__Notify__desc____ret__V()
	Dispatch__NotifyAll__desc____ret__V()
	Dispatch__Wait__desc__J__ret__V(int64)
	Dispatch__Wait__desc__J__I__ret__V(int64, int)
	Dispatch__Wait__desc____ret__V()
	Dispatch__Finalize__desc____ret__V()
}

func (this *Java__lang__Object__Instance) Java__lang__Object__InitDispatch(v Java__lang__Object__Dispatch) {
	this.Java__lang__Object__Dispatch = v
}

type Java__lang__Object__Instance struct {
	Java__lang__Object__Dispatch
}

func (this *Java__lang__Object__Instance) Instance_Init__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) HashCode__desc____ret__I() int {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) Equals__desc____obj__Java__lang__Object__ret__Z(var1 *Java__lang__Object__Instance) bool {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) Clone__desc____ret____obj__Java__lang__Object() *Java__lang__Object__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) ToString__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) Notify__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) NotifyAll__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) Wait__desc__J__ret__V(var1 int64) {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) Wait__desc__J__I__ret__V(var1 int64, var2 int) {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) Wait__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__Object__Instance) Finalize__desc____ret__V() {
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

func (this *Java__lang__String__Static) IndexOf__desc____arr__C__I__I____obj__Java__lang__String__I__ret__I(var0 []rune, var1 int, var2 int, var3 *Java__lang__String__Instance, var4 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) IndexOf__desc____arr__C__I__I____arr__C__I__I__I__ret__I(var0 []rune, var1 int, var2 int, var3 []rune, var4 int, var5 int, var6 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) LastIndexOf__desc____arr__C__I__I____obj__Java__lang__String__I__ret__I(var0 []rune, var1 int, var2 int, var3 *Java__lang__String__Instance, var4 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) LastIndexOf__desc____arr__C__I__I____arr__C__I__I__I__ret__I(var0 []rune, var1 int, var2 int, var3 []rune, var4 int, var5 int, var6 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) Join__desc____obj__Java__lang__CharSequence____arr____obj__Java__lang__CharSequence__ret____obj__Java__lang__String(var0 Java__lang__CharSequence__Instance, var1 []Java__lang__CharSequence__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) Format__desc____obj__Java__lang__String____arr____obj__Java__lang__Object__ret____obj__Java__lang__String(var0 *Java__lang__String__Instance, var1 []*Java__lang__Object__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) ValueOf__desc____obj__Java__lang__Object__ret____obj__Java__lang__String(var0 *Java__lang__Object__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) ValueOf__desc____arr__C__ret____obj__Java__lang__String(var0 []rune) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) ValueOf__desc____arr__C__I__I__ret____obj__Java__lang__String(var0 []rune, var1 int, var2 int) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) CopyValueOf__desc____arr__C__I__I__ret____obj__Java__lang__String(var0 []rune, var1 int, var2 int) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) CopyValueOf__desc____arr__C__ret____obj__Java__lang__String(var0 []rune) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) ValueOf__desc__Z__ret____obj__Java__lang__String(var0 bool) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) ValueOf__desc__C__ret____obj__Java__lang__String(var0 rune) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) ValueOf__desc__I__ret____obj__Java__lang__String(var0 int) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) ValueOf__desc__J__ret____obj__Java__lang__String(var0 int64) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) ValueOf__desc__F__ret____obj__Java__lang__String(var0 float32) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) ValueOf__desc__D__ret____obj__Java__lang__String(var0 float64) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Static) Static_Init__desc____ret__V() {
	
}

type Java__lang__String__Dispatch interface {
	Java__lang__Object__Dispatch
	Dispatch__IsEmpty__desc____ret__Z() bool
	Dispatch__CodePointAt__desc__I__ret__I(int) int
	Dispatch__CodePointBefore__desc__I__ret__I(int) int
	Dispatch__CodePointCount__desc__I__I__ret__I(int, int) int
	Dispatch__OffsetByCodePoints__desc__I__I__ret__I(int, int) int
	Dispatch__GetChars__desc____arr__C__I__ret__V([]rune, int)
	Dispatch__GetChars__desc__I__I____arr__C__I__ret__V(int, int, []rune, int)
	Dispatch__GetBytes__desc__I__I____arr__B__I__ret__V(int, int, []byte, int)
	Dispatch__GetBytes__desc____obj__Java__lang__String__ret____arr__B(*Java__lang__String__Instance) []byte
	Dispatch__GetBytes__desc____ret____arr__B() []byte
	Dispatch__ContentEquals__desc____obj__Java__lang__CharSequence__ret__Z(Java__lang__CharSequence__Instance) bool
	Dispatch__EqualsIgnoreCase__desc____obj__Java__lang__String__ret__Z(*Java__lang__String__Instance) bool
	Dispatch__CompareTo__desc____obj__Java__lang__String__ret__I(*Java__lang__String__Instance) int
	Dispatch__CompareToIgnoreCase__desc____obj__Java__lang__String__ret__I(*Java__lang__String__Instance) int
	Dispatch__RegionMatches__desc__I____obj__Java__lang__String__I__I__ret__Z(int, *Java__lang__String__Instance, int, int) bool
	Dispatch__RegionMatches__desc__Z__I____obj__Java__lang__String__I__I__ret__Z(bool, int, *Java__lang__String__Instance, int, int) bool
	Dispatch__StartsWith__desc____obj__Java__lang__String__I__ret__Z(*Java__lang__String__Instance, int) bool
	Dispatch__StartsWith__desc____obj__Java__lang__String__ret__Z(*Java__lang__String__Instance) bool
	Dispatch__EndsWith__desc____obj__Java__lang__String__ret__Z(*Java__lang__String__Instance) bool
	Dispatch__IndexOf__desc__I__ret__I(int) int
	Dispatch__IndexOf__desc__I__I__ret__I(int, int) int
	Dispatch__LastIndexOf__desc__I__ret__I(int) int
	Dispatch__LastIndexOf__desc__I__I__ret__I(int, int) int
	Dispatch__IndexOf__desc____obj__Java__lang__String__ret__I(*Java__lang__String__Instance) int
	Dispatch__IndexOf__desc____obj__Java__lang__String__I__ret__I(*Java__lang__String__Instance, int) int
	Dispatch__LastIndexOf__desc____obj__Java__lang__String__ret__I(*Java__lang__String__Instance) int
	Dispatch__LastIndexOf__desc____obj__Java__lang__String__I__ret__I(*Java__lang__String__Instance, int) int
	Dispatch__Substring__desc__I__ret____obj__Java__lang__String(int) *Java__lang__String__Instance
	Dispatch__Substring__desc__I__I__ret____obj__Java__lang__String(int, int) *Java__lang__String__Instance
	Dispatch__Concat__desc____obj__Java__lang__String__ret____obj__Java__lang__String(*Java__lang__String__Instance) *Java__lang__String__Instance
	Dispatch__Replace__desc__C__C__ret____obj__Java__lang__String(rune, rune) *Java__lang__String__Instance
	Dispatch__Matches__desc____obj__Java__lang__String__ret__Z(*Java__lang__String__Instance) bool
	Dispatch__Contains__desc____obj__Java__lang__CharSequence__ret__Z(Java__lang__CharSequence__Instance) bool
	Dispatch__ReplaceFirst__desc____obj__Java__lang__String____obj__Java__lang__String__ret____obj__Java__lang__String(*Java__lang__String__Instance, *Java__lang__String__Instance) *Java__lang__String__Instance
	Dispatch__ReplaceAll__desc____obj__Java__lang__String____obj__Java__lang__String__ret____obj__Java__lang__String(*Java__lang__String__Instance, *Java__lang__String__Instance) *Java__lang__String__Instance
	Dispatch__Replace__desc____obj__Java__lang__CharSequence____obj__Java__lang__CharSequence__ret____obj__Java__lang__String(Java__lang__CharSequence__Instance, Java__lang__CharSequence__Instance) *Java__lang__String__Instance
	Dispatch__Split__desc____obj__Java__lang__String__I__ret____arr____obj__Java__lang__String(*Java__lang__String__Instance, int) []*Java__lang__String__Instance
	Dispatch__Split__desc____obj__Java__lang__String__ret____arr____obj__Java__lang__String(*Java__lang__String__Instance) []*Java__lang__String__Instance
	Dispatch__ToLowerCase__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance
	Dispatch__ToUpperCase__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance
	Dispatch__Trim__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance
	Dispatch__ToCharArray__desc____ret____arr__C() []rune
	Dispatch__Intern__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance
}

func (this *Java__lang__String__Instance) Java__lang__String__InitDispatch(v Java__lang__String__Dispatch) {
	this.Java__lang__Object__Instance.Java__lang__Object__InitDispatch(v)
	this.Java__lang__String__Dispatch = v
}

type Java__lang__String__Instance struct {
	Java__io__Serializable__Instance
	Java__lang__Comparable__Instance
	Java__lang__CharSequence__Instance
	*Java__lang__Object__Instance
	Java__lang__String__Dispatch
	Underlying string
}

func (this *Java__lang__String__Instance) Instance_Init__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____obj__Java__lang__String__ret__V(var1 *Java__lang__String__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__C__ret__V(var1 []rune) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__C__I__I__ret__V(var1 []rune, var2 int, var3 int) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__I__I__I__ret__V(var1 []int, var2 int, var3 int) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__B__I__I__I__ret__V(var1 []byte, var2 int, var3 int, var4 int) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__B__I__ret__V(var1 []byte, var2 int) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__B__I__I____obj__Java__lang__String__ret__V(var1 []byte, var2 int, var3 int, var4 *Java__lang__String__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__B____obj__Java__lang__String__ret__V(var1 []byte, var2 *Java__lang__String__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__B__I__I__ret__V(var1 []byte, var2 int, var3 int) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__B__ret__V(var1 []byte) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Instance_Init__desc____arr__C__Z__ret__V(var1 []rune, var2 bool) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Length__desc____ret__I() int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) IsEmpty__desc____ret__Z() bool {
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

func (this *Java__lang__String__Instance) OffsetByCodePoints__desc__I__I__ret__I(var1 int, var2 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) GetChars__desc____arr__C__I__ret__V(var1 []rune, var2 int) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) GetChars__desc__I__I____arr__C__I__ret__V(var1 int, var2 int, var3 []rune, var4 int) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) GetBytes__desc__I__I____arr__B__I__ret__V(var1 int, var2 int, var3 []byte, var4 int) {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) GetBytes__desc____obj__Java__lang__String__ret____arr__B(var1 *Java__lang__String__Instance) []byte {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) GetBytes__desc____ret____arr__B() []byte {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Equals__desc____obj__Java__lang__Object__ret__Z(var1 *Java__lang__Object__Instance) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) ContentEquals__desc____obj__Java__lang__CharSequence__ret__Z(var1 Java__lang__CharSequence__Instance) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) EqualsIgnoreCase__desc____obj__Java__lang__String__ret__Z(var1 *Java__lang__String__Instance) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) CompareTo__desc____obj__Java__lang__String__ret__I(var1 *Java__lang__String__Instance) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) CompareToIgnoreCase__desc____obj__Java__lang__String__ret__I(var1 *Java__lang__String__Instance) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) RegionMatches__desc__I____obj__Java__lang__String__I__I__ret__Z(var1 int, var2 *Java__lang__String__Instance, var3 int, var4 int) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) RegionMatches__desc__Z__I____obj__Java__lang__String__I__I__ret__Z(var1 bool, var2 int, var3 *Java__lang__String__Instance, var4 int, var5 int) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) StartsWith__desc____obj__Java__lang__String__I__ret__Z(var1 *Java__lang__String__Instance, var2 int) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) StartsWith__desc____obj__Java__lang__String__ret__Z(var1 *Java__lang__String__Instance) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) EndsWith__desc____obj__Java__lang__String__ret__Z(var1 *Java__lang__String__Instance) bool {
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

func (this *Java__lang__String__Instance) LastIndexOf__desc__I__ret__I(var1 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) LastIndexOf__desc__I__I__ret__I(var1 int, var2 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) IndexOf__desc____obj__Java__lang__String__ret__I(var1 *Java__lang__String__Instance) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) IndexOf__desc____obj__Java__lang__String__I__ret__I(var1 *Java__lang__String__Instance, var2 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) LastIndexOf__desc____obj__Java__lang__String__ret__I(var1 *Java__lang__String__Instance) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) LastIndexOf__desc____obj__Java__lang__String__I__ret__I(var1 *Java__lang__String__Instance, var2 int) int {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Substring__desc__I__ret____obj__Java__lang__String(var1 int) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Substring__desc__I__I__ret____obj__Java__lang__String(var1 int, var2 int) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) SubSequence__desc__I__I__ret____obj__Java__lang__CharSequence(var1 int, var2 int) Java__lang__CharSequence__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Concat__desc____obj__Java__lang__String__ret____obj__Java__lang__String(var1 *Java__lang__String__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Replace__desc__C__C__ret____obj__Java__lang__String(var1 rune, var2 rune) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Matches__desc____obj__Java__lang__String__ret__Z(var1 *Java__lang__String__Instance) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Contains__desc____obj__Java__lang__CharSequence__ret__Z(var1 Java__lang__CharSequence__Instance) bool {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) ReplaceFirst__desc____obj__Java__lang__String____obj__Java__lang__String__ret____obj__Java__lang__String(var1 *Java__lang__String__Instance, var2 *Java__lang__String__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) ReplaceAll__desc____obj__Java__lang__String____obj__Java__lang__String__ret____obj__Java__lang__String(var1 *Java__lang__String__Instance, var2 *Java__lang__String__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Replace__desc____obj__Java__lang__CharSequence____obj__Java__lang__CharSequence__ret____obj__Java__lang__String(var1 Java__lang__CharSequence__Instance, var2 Java__lang__CharSequence__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Split__desc____obj__Java__lang__String__I__ret____arr____obj__Java__lang__String(var1 *Java__lang__String__Instance, var2 int) []*Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Split__desc____obj__Java__lang__String__ret____arr____obj__Java__lang__String(var1 *Java__lang__String__Instance) []*Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) ToLowerCase__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) ToUpperCase__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Trim__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) ToString__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) ToCharArray__desc____ret____arr__C() []rune {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) Intern__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__String__Instance) CompareTo__desc____obj__Java__lang__Object__ret__I(var1 *Java__lang__Object__Instance) int {
	panic("Not Implemented")
}

type Java__lang__System__Static struct {
	Out  *Java__io__PrintStream__Instance
	Err  *Java__io__PrintStream__Instance
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

func (this *Java__lang__System__Static) SetOut__desc____obj__Java__io__PrintStream__ret__V(var0 *Java__io__PrintStream__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) SetErr__desc____obj__Java__io__PrintStream__ret__V(var0 *Java__io__PrintStream__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) CurrentTimeMillis__desc____ret__J() int64 {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) NanoTime__desc____ret__J() int64 {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) Arraycopy__desc____obj__Java__lang__Object__I____obj__Java__lang__Object__I__I__ret__V(var0 *Java__lang__Object__Instance, var1 int, var2 *Java__lang__Object__Instance, var3 int, var4 int) {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) IdentityHashCode__desc____obj__Java__lang__Object__ret__I(var0 *Java__lang__Object__Instance) int {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) LineSeparator__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) GetProperty__desc____obj__Java__lang__String__ret____obj__Java__lang__String(var0 *Java__lang__String__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) GetProperty__desc____obj__Java__lang__String____obj__Java__lang__String__ret____obj__Java__lang__String(var0 *Java__lang__String__Instance, var1 *Java__lang__String__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) SetProperty__desc____obj__Java__lang__String____obj__Java__lang__String__ret____obj__Java__lang__String(var0 *Java__lang__String__Instance, var1 *Java__lang__String__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) ClearProperty__desc____obj__Java__lang__String__ret____obj__Java__lang__String(var0 *Java__lang__String__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) Getenv__desc____obj__Java__lang__String__ret____obj__Java__lang__String(var0 *Java__lang__String__Instance) *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) Exit__desc__I__ret__V(var0 int) {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) Gc__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) RunFinalization__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__System__Static) RunFinalizersOnExit__desc__Z__ret__V(var0 bool) {
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

type Java__lang__System__Dispatch interface {
	Java__lang__Object__Dispatch
}

func (this *Java__lang__System__Instance) Java__lang__System__InitDispatch(v Java__lang__System__Dispatch) {
	this.Java__lang__Object__Instance.Java__lang__Object__InitDispatch(v)
	this.Java__lang__System__Dispatch = v
}

type Java__lang__System__Instance struct {
	*Java__lang__Object__Instance
	Java__lang__System__Dispatch
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
	Dispatch__GetMessage__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance
	Dispatch__GetLocalizedMessage__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance
	Dispatch__GetCause__desc____ret____obj__Java__lang__Throwable() *Java__lang__Throwable__Instance
	Dispatch__InitCause__desc____obj__Java__lang__Throwable__ret____obj__Java__lang__Throwable(*Java__lang__Throwable__Instance) *Java__lang__Throwable__Instance
	Dispatch__PrintStackTrace__desc____ret__V()
	Dispatch__PrintStackTrace__desc____obj__Java__io__PrintStream__ret__V(*Java__io__PrintStream__Instance)
	Dispatch__FillInStackTrace__desc____ret____obj__Java__lang__Throwable() *Java__lang__Throwable__Instance
	Dispatch__GetStackTraceDepth__desc____ret__I() int
	Dispatch__AddSuppressed__desc____obj__Java__lang__Throwable__ret__V(*Java__lang__Throwable__Instance)
	Dispatch__GetSuppressed__desc____ret____arr____obj__Java__lang__Throwable() []*Java__lang__Throwable__Instance
}

func (this *Java__lang__Throwable__Instance) Java__lang__Throwable__InitDispatch(v Java__lang__Throwable__Dispatch) {
	this.Java__lang__Object__Instance.Java__lang__Object__InitDispatch(v)
	this.Java__lang__Throwable__Dispatch = v
}

type Java__lang__Throwable__Instance struct {
	Java__io__Serializable__Instance
	*Java__lang__Object__Instance
	Java__lang__Throwable__Dispatch
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

func (this *Java__lang__Throwable__Instance) Instance_Init__desc____obj__Java__lang__Throwable__ret__V(var1 *Java__lang__Throwable__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) Instance_Init__desc____obj__Java__lang__String____obj__Java__lang__Throwable__Z__Z__ret__V(var1 *Java__lang__String__Instance, var2 *Java__lang__Throwable__Instance, var3 bool, var4 bool) {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) GetMessage__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) GetLocalizedMessage__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) GetCause__desc____ret____obj__Java__lang__Throwable() *Java__lang__Throwable__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) InitCause__desc____obj__Java__lang__Throwable__ret____obj__Java__lang__Throwable(var1 *Java__lang__Throwable__Instance) *Java__lang__Throwable__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) ToString__desc____ret____obj__Java__lang__String() *Java__lang__String__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) PrintStackTrace__desc____ret__V() {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) PrintStackTrace__desc____obj__Java__io__PrintStream__ret__V(var1 *Java__io__PrintStream__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) FillInStackTrace__desc____ret____obj__Java__lang__Throwable() *Java__lang__Throwable__Instance {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) GetStackTraceDepth__desc____ret__I() int {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) AddSuppressed__desc____obj__Java__lang__Throwable__ret__V(var1 *Java__lang__Throwable__Instance) {
	panic("Not Implemented")
}

func (this *Java__lang__Throwable__Instance) GetSuppressed__desc____ret____arr____obj__Java__lang__Throwable() []*Java__lang__Throwable__Instance {
	panic("Not Implemented")
}
