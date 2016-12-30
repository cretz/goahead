package rt

func GetBoolOrByte(arr Java__lang__Object__Instance, i int) int {
	if arr == nil {
		ret := Java__lang__NullPointerException().New()
		ret.Instance_Init__desc____obj__Java__lang__String__ret__V(NewString("Null array ref"))
		panic(ret)
	}
	if v, ok := arr.(BoolArray__Instance); ok {
		if v.Get(i) {
			return 1
		}
		return 0
	}
	return int(arr.(ByteArray__Instance).Get(i))
}

func SetBoolOrByte(arr Java__lang__Object__Instance, i int, val int) {
	if arr == nil {
		ret := Java__lang__NullPointerException().New()
		ret.Instance_Init__desc____obj__Java__lang__String__ret__V(NewString("Null array ref"))
		panic(ret)
	}
	if v, ok := arr.(BoolArray__Instance); ok {
		v.Set(i, val != 0)
	} else {
		arr.(ByteArray__Instance).Set(i, int8(val))
	}
}

type Array__Instance interface {
	Len() int
}

type BoolArray__Instance interface {
	Java__lang__Object__Instance
	Get(int) bool
	Set(int, bool)
	Len() int
	Raw() []bool
}

type BoolArray__Impl []bool

func NewBoolArray(size int) BoolArray__Instance { return make(BoolArray__Impl, size) }

func (a BoolArray__Impl) Clone__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance {
	panic("Not implmented")
}
func (a BoolArray__Impl) Equals__desc____obj__Java__lang__Object__ret__Z(Java__lang__Object__Instance) bool {
	panic("Not implmented")
}
func (a BoolArray__Impl) Finalize__desc____ret__V() { panic("Not implmented") }
func (a BoolArray__Impl) GetClass__desc____ret____obj__Java__lang__Class() Java__lang__Class__Instance {
	panic("Not implmented")
}
func (a BoolArray__Impl) HashCode__desc____ret__I() int { panic("Not implmented") }
func (a BoolArray__Impl) Notify__desc____ret__V()       { panic("Not implmented") }
func (a BoolArray__Impl) NotifyAll__desc____ret__V()    { panic("Not implmented") }
func (a BoolArray__Impl) ToString__desc____ret____obj__Java__lang__String() Java__lang__String__Instance {
	panic("Not implmented")
}
func (a BoolArray__Impl) Wait__desc____ret__V()               { panic("Not implmented") }
func (a BoolArray__Impl) Wait__desc__J__ret__V(int64)         { panic("Not implmented") }
func (a BoolArray__Impl) Wait__desc__J__I__ret__V(int64, int) { panic("Not implmented") }
func (a BoolArray__Impl) Get(i int) bool                      { return a[i] }
func (a BoolArray__Impl) Set(i int, v bool)                   { a[i] = v }
func (a BoolArray__Impl) Len() int                            { return len(a) }
func (a BoolArray__Impl) Raw() []bool                         { return a }
func (a BoolArray__Impl) RawPtr__Java__lang__Object() *Java__lang__Object__Impl { panic("Cannot get raw pointer of array") }

type CharArray__Instance interface {
	Java__lang__Object__Instance
	Get(int) rune
	Set(int, rune)
	Len() int
	Raw() []rune
}

type CharArray__Impl []rune

func NewCharArray(size int) CharArray__Instance { return make(CharArray__Impl, size) }

func (a CharArray__Impl) Clone__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance {
	panic("Not implmented")
}
func (a CharArray__Impl) Equals__desc____obj__Java__lang__Object__ret__Z(Java__lang__Object__Instance) bool {
	panic("Not implmented")
}
func (a CharArray__Impl) Finalize__desc____ret__V() { panic("Not implmented") }
func (a CharArray__Impl) GetClass__desc____ret____obj__Java__lang__Class() Java__lang__Class__Instance {
	panic("Not implmented")
}
func (a CharArray__Impl) HashCode__desc____ret__I() int { panic("Not implmented") }
func (a CharArray__Impl) Notify__desc____ret__V()       { panic("Not implmented") }
func (a CharArray__Impl) NotifyAll__desc____ret__V()    { panic("Not implmented") }
func (a CharArray__Impl) ToString__desc____ret____obj__Java__lang__String() Java__lang__String__Instance {
	panic("Not implmented")
}
func (a CharArray__Impl) Wait__desc____ret__V()               { panic("Not implmented") }
func (a CharArray__Impl) Wait__desc__J__ret__V(int64)         { panic("Not implmented") }
func (a CharArray__Impl) Wait__desc__J__I__ret__V(int64, int) { panic("Not implmented") }
func (a CharArray__Impl) Get(i int) rune                      { return a[i] }
func (a CharArray__Impl) Set(i int, v rune)                   { a[i] = v }
func (a CharArray__Impl) Len() int                            { return len(a) }
func (a CharArray__Impl) Raw() []rune                         { return a }
func (a CharArray__Impl) RawPtr__Java__lang__Object() *Java__lang__Object__Impl { panic("Cannot get raw pointer of array") }

type FloatArray__Instance interface {
	Java__lang__Object__Instance
	Get(int) float32
	Set(int, float32)
	Len() int
	Raw() []float32
}

type FloatArray__Impl []float32

func NewFloatArray(size int) FloatArray__Instance { return make(FloatArray__Impl, size) }

func (a FloatArray__Impl) Clone__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance {
	panic("Not implmented")
}
func (a FloatArray__Impl) Equals__desc____obj__Java__lang__Object__ret__Z(Java__lang__Object__Instance) bool {
	panic("Not implmented")
}
func (a FloatArray__Impl) Finalize__desc____ret__V() { panic("Not implmented") }
func (a FloatArray__Impl) GetClass__desc____ret____obj__Java__lang__Class() Java__lang__Class__Instance {
	panic("Not implmented")
}
func (a FloatArray__Impl) HashCode__desc____ret__I() int { panic("Not implmented") }
func (a FloatArray__Impl) Notify__desc____ret__V()       { panic("Not implmented") }
func (a FloatArray__Impl) NotifyAll__desc____ret__V()    { panic("Not implmented") }
func (a FloatArray__Impl) ToString__desc____ret____obj__Java__lang__String() Java__lang__String__Instance {
	panic("Not implmented")
}
func (a FloatArray__Impl) Wait__desc____ret__V()               { panic("Not implmented") }
func (a FloatArray__Impl) Wait__desc__J__ret__V(int64)         { panic("Not implmented") }
func (a FloatArray__Impl) Wait__desc__J__I__ret__V(int64, int) { panic("Not implmented") }
func (a FloatArray__Impl) Get(i int) float32                   { return a[i] }
func (a FloatArray__Impl) Set(i int, v float32)                { a[i] = v }
func (a FloatArray__Impl) Len() int                            { return len(a) }
func (a FloatArray__Impl) Raw() []float32                      { return a }
func (a FloatArray__Impl) RawPtr__Java__lang__Object() *Java__lang__Object__Impl { panic("Cannot get raw pointer of array") }

type DoubleArray__Instance interface {
	Java__lang__Object__Instance
	Get(int) float64
	Set(int, float64)
	Len() int
	Raw() []float64
}

type DoubleArray__Impl []float64

func NewDoubleArray(size int) DoubleArray__Instance { return make(DoubleArray__Impl, size) }

func (a DoubleArray__Impl) Clone__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance {
	panic("Not implmented")
}
func (a DoubleArray__Impl) Equals__desc____obj__Java__lang__Object__ret__Z(Java__lang__Object__Instance) bool {
	panic("Not implmented")
}
func (a DoubleArray__Impl) Finalize__desc____ret__V() { panic("Not implmented") }
func (a DoubleArray__Impl) GetClass__desc____ret____obj__Java__lang__Class() Java__lang__Class__Instance {
	panic("Not implmented")
}
func (a DoubleArray__Impl) HashCode__desc____ret__I() int { panic("Not implmented") }
func (a DoubleArray__Impl) Notify__desc____ret__V()       { panic("Not implmented") }
func (a DoubleArray__Impl) NotifyAll__desc____ret__V()    { panic("Not implmented") }
func (a DoubleArray__Impl) ToString__desc____ret____obj__Java__lang__String() Java__lang__String__Instance {
	panic("Not implmented")
}
func (a DoubleArray__Impl) Wait__desc____ret__V()               { panic("Not implmented") }
func (a DoubleArray__Impl) Wait__desc__J__ret__V(int64)         { panic("Not implmented") }
func (a DoubleArray__Impl) Wait__desc__J__I__ret__V(int64, int) { panic("Not implmented") }
func (a DoubleArray__Impl) Get(i int) float64                   { return a[i] }
func (a DoubleArray__Impl) Set(i int, v float64)                { a[i] = v }
func (a DoubleArray__Impl) Len() int                            { return len(a) }
func (a DoubleArray__Impl) Raw() []float64                      { return a }
func (a DoubleArray__Impl) RawPtr__Java__lang__Object() *Java__lang__Object__Impl { panic("Cannot get raw pointer of array") }

type ByteArray__Instance interface {
	Java__lang__Object__Instance
	Get(int) int8
	Set(int, int8)
	Len() int
	Raw() []int8
}

type ByteArray__Impl []int8

func NewByteArray(size int) ByteArray__Instance { return make(ByteArray__Impl, size) }

func (a ByteArray__Impl) Clone__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance {
	panic("Not implmented")
}
func (a ByteArray__Impl) Equals__desc____obj__Java__lang__Object__ret__Z(Java__lang__Object__Instance) bool {
	panic("Not implmented")
}
func (a ByteArray__Impl) Finalize__desc____ret__V() { panic("Not implmented") }
func (a ByteArray__Impl) GetClass__desc____ret____obj__Java__lang__Class() Java__lang__Class__Instance {
	panic("Not implmented")
}
func (a ByteArray__Impl) HashCode__desc____ret__I() int { panic("Not implmented") }
func (a ByteArray__Impl) Notify__desc____ret__V()       { panic("Not implmented") }
func (a ByteArray__Impl) NotifyAll__desc____ret__V()    { panic("Not implmented") }
func (a ByteArray__Impl) ToString__desc____ret____obj__Java__lang__String() Java__lang__String__Instance {
	panic("Not implmented")
}
func (a ByteArray__Impl) Wait__desc____ret__V()               { panic("Not implmented") }
func (a ByteArray__Impl) Wait__desc__J__ret__V(int64)         { panic("Not implmented") }
func (a ByteArray__Impl) Wait__desc__J__I__ret__V(int64, int) { panic("Not implmented") }
func (a ByteArray__Impl) Get(i int) int8                      { return a[i] }
func (a ByteArray__Impl) Set(i int, v int8)                   { a[i] = v }
func (a ByteArray__Impl) Len() int                            { return len(a) }
func (a ByteArray__Impl) Raw() []int8                         { return a }
func (a ByteArray__Impl) RawPtr__Java__lang__Object() *Java__lang__Object__Impl { panic("Cannot get raw pointer of array") }

type ShortArray__Instance interface {
	Java__lang__Object__Instance
	Get(int) int16
	Set(int, int16)
	Len() int
	Raw() []int16
}

type ShortArray__Impl []int16

func NewShortArray(size int) ShortArray__Instance { return make(ShortArray__Impl, size) }

func (a ShortArray__Impl) Clone__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance {
	panic("Not implmented")
}
func (a ShortArray__Impl) Equals__desc____obj__Java__lang__Object__ret__Z(Java__lang__Object__Instance) bool {
	panic("Not implmented")
}
func (a ShortArray__Impl) Finalize__desc____ret__V() { panic("Not implmented") }
func (a ShortArray__Impl) GetClass__desc____ret____obj__Java__lang__Class() Java__lang__Class__Instance {
	panic("Not implmented")
}
func (a ShortArray__Impl) HashCode__desc____ret__I() int { panic("Not implmented") }
func (a ShortArray__Impl) Notify__desc____ret__V()       { panic("Not implmented") }
func (a ShortArray__Impl) NotifyAll__desc____ret__V()    { panic("Not implmented") }
func (a ShortArray__Impl) ToString__desc____ret____obj__Java__lang__String() Java__lang__String__Instance {
	panic("Not implmented")
}
func (a ShortArray__Impl) Wait__desc____ret__V()               { panic("Not implmented") }
func (a ShortArray__Impl) Wait__desc__J__ret__V(int64)         { panic("Not implmented") }
func (a ShortArray__Impl) Wait__desc__J__I__ret__V(int64, int) { panic("Not implmented") }
func (a ShortArray__Impl) Get(i int) int16                     { return a[i] }
func (a ShortArray__Impl) Set(i int, v int16)                  { a[i] = v }
func (a ShortArray__Impl) Len() int                            { return len(a) }
func (a ShortArray__Impl) Raw() []int16                        { return a }
func (a ShortArray__Impl) RawPtr__Java__lang__Object() *Java__lang__Object__Impl { panic("Cannot get raw pointer of array") }

type IntArray__Instance interface {
	Java__lang__Object__Instance
	Get(int) int
	Set(int, int)
	Len() int
}

type IntArray__Impl []int

func NewIntArray(size int) IntArray__Instance { return make(IntArray__Impl, size) }

func (a IntArray__Impl) Clone__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance {
	panic("Not implmented")
}
func (a IntArray__Impl) Equals__desc____obj__Java__lang__Object__ret__Z(Java__lang__Object__Instance) bool {
	panic("Not implmented")
}
func (a IntArray__Impl) Finalize__desc____ret__V() { panic("Not implmented") }
func (a IntArray__Impl) GetClass__desc____ret____obj__Java__lang__Class() Java__lang__Class__Instance {
	panic("Not implmented")
}
func (a IntArray__Impl) HashCode__desc____ret__I() int { panic("Not implmented") }
func (a IntArray__Impl) Notify__desc____ret__V()       { panic("Not implmented") }
func (a IntArray__Impl) NotifyAll__desc____ret__V()    { panic("Not implmented") }
func (a IntArray__Impl) ToString__desc____ret____obj__Java__lang__String() Java__lang__String__Instance {
	panic("Not implmented")
}
func (a IntArray__Impl) Wait__desc____ret__V()               { panic("Not implmented") }
func (a IntArray__Impl) Wait__desc__J__ret__V(int64)         { panic("Not implmented") }
func (a IntArray__Impl) Wait__desc__J__I__ret__V(int64, int) { panic("Not implmented") }
func (a IntArray__Impl) Get(i int) int                       { return a[i] }
func (a IntArray__Impl) Set(i int, v int)                    { a[i] = v }
func (a IntArray__Impl) Len() int                            { return len(a) }
func (a IntArray__Impl) Raw() []int                          { return a }
func (a IntArray__Impl) RawPtr__Java__lang__Object() *Java__lang__Object__Impl { panic("Cannot get raw pointer of array") }

type LongArray__Instance interface {
	Java__lang__Object__Instance
	Get(int) int64
	Set(int, int64)
	Len() int
	Raw() []int64
}

type LongArray__Impl []int64

func NewLongArray(size int) LongArray__Instance { return make(LongArray__Impl, size) }

func (a LongArray__Impl) Clone__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance {
	panic("Not implmented")
}
func (a LongArray__Impl) Equals__desc____obj__Java__lang__Object__ret__Z(Java__lang__Object__Instance) bool {
	panic("Not implmented")
}
func (a LongArray__Impl) Finalize__desc____ret__V() { panic("Not implmented") }
func (a LongArray__Impl) GetClass__desc____ret____obj__Java__lang__Class() Java__lang__Class__Instance {
	panic("Not implmented")
}
func (a LongArray__Impl) HashCode__desc____ret__I() int { panic("Not implmented") }
func (a LongArray__Impl) Notify__desc____ret__V()       { panic("Not implmented") }
func (a LongArray__Impl) NotifyAll__desc____ret__V()    { panic("Not implmented") }
func (a LongArray__Impl) ToString__desc____ret____obj__Java__lang__String() Java__lang__String__Instance {
	panic("Not implmented")
}
func (a LongArray__Impl) Wait__desc____ret__V()               { panic("Not implmented") }
func (a LongArray__Impl) Wait__desc__J__ret__V(int64)         { panic("Not implmented") }
func (a LongArray__Impl) Wait__desc__J__I__ret__V(int64, int) { panic("Not implmented") }
func (a LongArray__Impl) Get(i int) int64                     { return a[i] }
func (a LongArray__Impl) Set(i int, v int64)                  { a[i] = v }
func (a LongArray__Impl) Len() int                            { return len(a) }
func (a LongArray__Impl) Raw() []int64                        { return a }
func (a LongArray__Impl) RawPtr__Java__lang__Object() *Java__lang__Object__Impl { panic("Cannot get raw pointer of array") }

type ObjectArray__Instance interface {
	Java__lang__Object__Instance
	Get(int) Java__lang__Object__Instance
	Set(int, Java__lang__Object__Instance)
	Len() int
	Raw() []Java__lang__Object__Instance
}

type ObjectArray__Impl []Java__lang__Object__Instance

func NewObjectArray(size int) ObjectArray__Instance { return make(ObjectArray__Impl, size) }

func (a ObjectArray__Impl) Clone__desc____ret____obj__Java__lang__Object() Java__lang__Object__Instance {
	panic("Not implmented")
}
func (a ObjectArray__Impl) Equals__desc____obj__Java__lang__Object__ret__Z(Java__lang__Object__Instance) bool {
	panic("Not implmented")
}
func (a ObjectArray__Impl) Finalize__desc____ret__V() { panic("Not implmented") }
func (a ObjectArray__Impl) GetClass__desc____ret____obj__Java__lang__Class() Java__lang__Class__Instance {
	panic("Not implmented")
}
func (a ObjectArray__Impl) HashCode__desc____ret__I() int { panic("Not implmented") }
func (a ObjectArray__Impl) Notify__desc____ret__V()       { panic("Not implmented") }
func (a ObjectArray__Impl) NotifyAll__desc____ret__V()    { panic("Not implmented") }
func (a ObjectArray__Impl) ToString__desc____ret____obj__Java__lang__String() Java__lang__String__Instance {
	panic("Not implmented")
}
func (a ObjectArray__Impl) Wait__desc____ret__V()                     { panic("Not implmented") }
func (a ObjectArray__Impl) Wait__desc__J__ret__V(int64)               { panic("Not implmented") }
func (a ObjectArray__Impl) Wait__desc__J__I__ret__V(int64, int)       { panic("Not implmented") }
func (a ObjectArray__Impl) Get(i int) Java__lang__Object__Instance    { return a[i] }
func (a ObjectArray__Impl) Set(i int, v Java__lang__Object__Instance) { a[i] = v }
func (a ObjectArray__Impl) Len() int                                  { return len(a) }
func (a ObjectArray__Impl) Raw() []Java__lang__Object__Instance       { return a }
func (a ObjectArray__Impl) RawPtr__Java__lang__Object() *Java__lang__Object__Impl { panic("Cannot get raw pointer of array") }