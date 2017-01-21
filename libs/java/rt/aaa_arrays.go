package rt

func GetBoolOrByte(arr Object_fAFaMw_Ñ, i int32) int32 {
	if arr == nil {
		ret := NullPointerException_fnXÞLQ().New()
		ret.Init_M13Ø3g_Í(NewString("Null array ref"))
		panic(ret)
	}
	if v, ok := arr.(BoolArray__Instance); ok {
		if v.Get(i) {
			return 1
		}
		return 0
	}
	return int32(arr.(ByteArray__Instance).Get(i))
}

func SetBoolOrByte(arr Object_fAFaMw_Ñ, i int32, val int32) {
	if arr == nil {
		ret := NullPointerException_fnXÞLQ().New()
		ret.Init_M13Ø3g_Í(NewString("Null array ref"))
		panic(ret)
	}
	if v, ok := arr.(BoolArray__Instance); ok {
		v.Set(i, val != 0)
	} else {
		arr.(ByteArray__Instance).Set(i, int8(val))
	}
}

type Array__Instance interface {
	Object_fAFaMw_Ñ
	Len() int32
	String() string
}

type BoolArray__Instance interface {
	Array__Instance
	Get(int32) bool
	Set(int32, bool)
	Raw() []bool
}

type BoolArray__Impl []bool

func NewBoolArray(size int32) BoolArray__Instance { return make(BoolArray__Impl, size) }

func (a BoolArray__Impl) Clone_KkF6yw() Object_fAFaMw_Ñ {
	panic("Not implemented")
}
func (a BoolArray__Impl) Equals_g011Rw(Object_fAFaMw_Ñ) bool {
	panic("Not implemented")
}
func (a BoolArray__Impl) Finalize_hqp4qA() { panic("Not implemented") }
func (a BoolArray__Impl) GetClass_9pp3sQ() Class_dvhEBA_Ñ {
	return Class_dvhEBA().ForName_xpGqyQ_Í(NewString("[Z"))
}
func (a BoolArray__Impl) HashCode_Gyq6fg() int32           { return objectHashCode(a) }
func (a BoolArray__Impl) Notify_NFqiHQ()                   { panic("Not implemented") }
func (a BoolArray__Impl) NotifyAll_9o437g()                { panic("Not implemented") }
func (a BoolArray__Impl) ToString_aÞ4cSA() String_g9YXBQ_Ñ { return objectToString(a) }
func (a BoolArray__Impl) String() string                   { return objectString(a) }
func (a BoolArray__Impl) Wait_KgmKcQ()                     { panic("Not implemented") }
func (a BoolArray__Impl) Wait_vVGjdQ(int64)                { panic("Not implemented") }
func (a BoolArray__Impl) Wait_OTFAsA(int64, int32)         { panic("Not implemented") }
func (a BoolArray__Impl) Get(i int32) bool                 { return a[i] }
func (a BoolArray__Impl) Set(i int32, v bool)              { a[i] = v }
func (a BoolArray__Impl) Len() int32                       { return int32(len(a)) }
func (a BoolArray__Impl) Raw() []bool                      { return a }
func (a BoolArray__Impl) Raw_fAFaMw() *Object_fAFaMw_Í     { panic("Cannot get raw pointer of array") }

type CharArray__Instance interface {
	Array__Instance
	Get(int32) rune
	Set(int32, rune)
	Raw() []rune
}

type CharArray__Impl []rune

func NewCharArray(size int32) CharArray__Instance { return make(CharArray__Impl, size) }

func (a CharArray__Impl) Clone_KkF6yw() Object_fAFaMw_Ñ {
	panic("Not implemented")
}
func (a CharArray__Impl) Equals_g011Rw(Object_fAFaMw_Ñ) bool {
	panic("Not implemented")
}
func (a CharArray__Impl) Finalize_hqp4qA() { panic("Not implemented") }
func (a CharArray__Impl) GetClass_9pp3sQ() Class_dvhEBA_Ñ {
	return Class_dvhEBA().ForName_xpGqyQ_Í(NewString("[C"))
}
func (a CharArray__Impl) HashCode_Gyq6fg() int32           { return objectHashCode(a) }
func (a CharArray__Impl) Notify_NFqiHQ()                   { panic("Not implemented") }
func (a CharArray__Impl) NotifyAll_9o437g()                { panic("Not implemented") }
func (a CharArray__Impl) ToString_aÞ4cSA() String_g9YXBQ_Ñ { return objectToString(a) }
func (a CharArray__Impl) String() string                   { return objectString(a) }
func (a CharArray__Impl) Wait_KgmKcQ()                     { panic("Not implemented") }
func (a CharArray__Impl) Wait_vVGjdQ(int64)                { panic("Not implemented") }
func (a CharArray__Impl) Wait_OTFAsA(int64, int32)         { panic("Not implemented") }
func (a CharArray__Impl) Get(i int32) rune                 { return a[i] }
func (a CharArray__Impl) Set(i int32, v rune)              { a[i] = v }
func (a CharArray__Impl) Len() int32                       { return int32(len(a)) }
func (a CharArray__Impl) Raw() []rune                      { return a }
func (a CharArray__Impl) Raw_fAFaMw() *Object_fAFaMw_Í     { panic("Cannot get raw pointer of array") }

type FloatArray__Instance interface {
	Array__Instance
	Get(int32) float32
	Set(int32, float32)
	Raw() []float32
}

type FloatArray__Impl []float32

func NewFloatArray(size int32) FloatArray__Instance { return make(FloatArray__Impl, size) }

func (a FloatArray__Impl) Clone_KkF6yw() Object_fAFaMw_Ñ {
	panic("Not implemented")
}
func (a FloatArray__Impl) Equals_g011Rw(Object_fAFaMw_Ñ) bool {
	panic("Not implemented")
}
func (a FloatArray__Impl) Finalize_hqp4qA() { panic("Not implemented") }
func (a FloatArray__Impl) GetClass_9pp3sQ() Class_dvhEBA_Ñ {
	return Class_dvhEBA().ForName_xpGqyQ_Í(NewString("[F"))
}
func (a FloatArray__Impl) HashCode_Gyq6fg() int32           { return objectHashCode(a) }
func (a FloatArray__Impl) Notify_NFqiHQ()                   { panic("Not implemented") }
func (a FloatArray__Impl) NotifyAll_9o437g()                { panic("Not implemented") }
func (a FloatArray__Impl) ToString_aÞ4cSA() String_g9YXBQ_Ñ { return objectToString(a) }
func (a FloatArray__Impl) String() string                   { return objectString(a) }
func (a FloatArray__Impl) Wait_KgmKcQ()                     { panic("Not implemented") }
func (a FloatArray__Impl) Wait_vVGjdQ(int64)                { panic("Not implemented") }
func (a FloatArray__Impl) Wait_OTFAsA(int64, int32)         { panic("Not implemented") }
func (a FloatArray__Impl) Get(i int32) float32              { return a[i] }
func (a FloatArray__Impl) Set(i int32, v float32)           { a[i] = v }
func (a FloatArray__Impl) Len() int32                       { return int32(len(a)) }
func (a FloatArray__Impl) Raw() []float32                   { return a }
func (a FloatArray__Impl) Raw_fAFaMw() *Object_fAFaMw_Í     { panic("Cannot get raw pointer of array") }

type DoubleArray__Instance interface {
	Array__Instance
	Get(int32) float64
	Set(int32, float64)
	Raw() []float64
}

type DoubleArray__Impl []float64

func NewDoubleArray(size int32) DoubleArray__Instance { return make(DoubleArray__Impl, size) }

func (a DoubleArray__Impl) Clone_KkF6yw() Object_fAFaMw_Ñ {
	panic("Not implemented")
}
func (a DoubleArray__Impl) Equals_g011Rw(Object_fAFaMw_Ñ) bool {
	panic("Not implemented")
}
func (a DoubleArray__Impl) Finalize_hqp4qA() { panic("Not implemented") }
func (a DoubleArray__Impl) GetClass_9pp3sQ() Class_dvhEBA_Ñ {
	return Class_dvhEBA().ForName_xpGqyQ_Í(NewString("[D"))
}
func (a DoubleArray__Impl) HashCode_Gyq6fg() int32           { return objectHashCode(a) }
func (a DoubleArray__Impl) Notify_NFqiHQ()                   { panic("Not implemented") }
func (a DoubleArray__Impl) NotifyAll_9o437g()                { panic("Not implemented") }
func (a DoubleArray__Impl) ToString_aÞ4cSA() String_g9YXBQ_Ñ { return objectToString(a) }
func (a DoubleArray__Impl) String() string                   { return objectString(a) }
func (a DoubleArray__Impl) Wait_KgmKcQ()                     { panic("Not implemented") }
func (a DoubleArray__Impl) Wait_vVGjdQ(int64)                { panic("Not implemented") }
func (a DoubleArray__Impl) Wait_OTFAsA(int64, int32)         { panic("Not implemented") }
func (a DoubleArray__Impl) Get(i int32) float64              { return a[i] }
func (a DoubleArray__Impl) Set(i int32, v float64)           { a[i] = v }
func (a DoubleArray__Impl) Len() int32                       { return int32(len(a)) }
func (a DoubleArray__Impl) Raw() []float64                   { return a }
func (a DoubleArray__Impl) Raw_fAFaMw() *Object_fAFaMw_Í     { panic("Cannot get raw pointer of array") }

type ByteArray__Instance interface {
	Array__Instance
	Get(int32) int8
	Set(int32, int8)
	Raw() []int8
}

type ByteArray__Impl []int8

func NewByteArray(size int32) ByteArray__Instance { return make(ByteArray__Impl, size) }

func (a ByteArray__Impl) Clone_KkF6yw() Object_fAFaMw_Ñ {
	panic("Not implemented")
}
func (a ByteArray__Impl) Equals_g011Rw(Object_fAFaMw_Ñ) bool {
	panic("Not implemented")
}
func (a ByteArray__Impl) Finalize_hqp4qA() { panic("Not implemented") }
func (a ByteArray__Impl) GetClass_9pp3sQ() Class_dvhEBA_Ñ {
	return Class_dvhEBA().ForName_xpGqyQ_Í(NewString("[B"))
}
func (a ByteArray__Impl) HashCode_Gyq6fg() int32           { return objectHashCode(a) }
func (a ByteArray__Impl) Notify_NFqiHQ()                   { panic("Not implemented") }
func (a ByteArray__Impl) NotifyAll_9o437g()                { panic("Not implemented") }
func (a ByteArray__Impl) ToString_aÞ4cSA() String_g9YXBQ_Ñ { return objectToString(a) }
func (a ByteArray__Impl) String() string                   { return objectString(a) }
func (a ByteArray__Impl) Wait_KgmKcQ()                     { panic("Not implemented") }
func (a ByteArray__Impl) Wait_vVGjdQ(int64)                { panic("Not implemented") }
func (a ByteArray__Impl) Wait_OTFAsA(int64, int32)         { panic("Not implemented") }
func (a ByteArray__Impl) Get(i int32) int8                 { return a[i] }
func (a ByteArray__Impl) Set(i int32, v int8)              { a[i] = v }
func (a ByteArray__Impl) Len() int32                       { return int32(len(a)) }
func (a ByteArray__Impl) Raw() []int8                      { return a }
func (a ByteArray__Impl) Raw_fAFaMw() *Object_fAFaMw_Í     { panic("Cannot get raw pointer of array") }

type ShortArray__Instance interface {
	Array__Instance
	Get(int32) int16
	Set(int32, int16)
	Raw() []int16
}

type ShortArray__Impl []int16

func NewShortArray(size int32) ShortArray__Instance { return make(ShortArray__Impl, size) }

func (a ShortArray__Impl) Clone_KkF6yw() Object_fAFaMw_Ñ {
	panic("Not implemented")
}
func (a ShortArray__Impl) Equals_g011Rw(Object_fAFaMw_Ñ) bool {
	panic("Not implemented")
}
func (a ShortArray__Impl) Finalize_hqp4qA() { panic("Not implemented") }
func (a ShortArray__Impl) GetClass_9pp3sQ() Class_dvhEBA_Ñ {
	return Class_dvhEBA().ForName_xpGqyQ_Í(NewString("[S"))
}
func (a ShortArray__Impl) HashCode_Gyq6fg() int32           { return objectHashCode(a) }
func (a ShortArray__Impl) Notify_NFqiHQ()                   { panic("Not implemented") }
func (a ShortArray__Impl) NotifyAll_9o437g()                { panic("Not implemented") }
func (a ShortArray__Impl) ToString_aÞ4cSA() String_g9YXBQ_Ñ { return objectToString(a) }
func (a ShortArray__Impl) String() string                   { return objectString(a) }
func (a ShortArray__Impl) Wait_KgmKcQ()                     { panic("Not implemented") }
func (a ShortArray__Impl) Wait_vVGjdQ(int64)                { panic("Not implemented") }
func (a ShortArray__Impl) Wait_OTFAsA(int64, int32)         { panic("Not implemented") }
func (a ShortArray__Impl) Get(i int32) int16                { return a[i] }
func (a ShortArray__Impl) Set(i int32, v int16)             { a[i] = v }
func (a ShortArray__Impl) Len() int32                       { return int32(len(a)) }
func (a ShortArray__Impl) Raw() []int16                     { return a }
func (a ShortArray__Impl) Raw_fAFaMw() *Object_fAFaMw_Í     { panic("Cannot get raw pointer of array") }

type IntArray__Instance interface {
	Array__Instance
	Get(int32) int32
	Set(int32, int32)
	Raw() []int32
}

type IntArray__Impl []int32

func NewIntArray(size int32) IntArray__Instance { return make(IntArray__Impl, size) }

func (a IntArray__Impl) Clone_KkF6yw() Object_fAFaMw_Ñ {
	panic("Not implemented")
}
func (a IntArray__Impl) Equals_g011Rw(Object_fAFaMw_Ñ) bool {
	panic("Not implemented")
}
func (a IntArray__Impl) Finalize_hqp4qA() { panic("Not implemented") }
func (a IntArray__Impl) GetClass_9pp3sQ() Class_dvhEBA_Ñ {
	return Class_dvhEBA().ForName_xpGqyQ_Í(NewString("[I"))
}
func (a IntArray__Impl) HashCode_Gyq6fg() int32           { return objectHashCode(a) }
func (a IntArray__Impl) Notify_NFqiHQ()                   { panic("Not implemented") }
func (a IntArray__Impl) NotifyAll_9o437g()                { panic("Not implemented") }
func (a IntArray__Impl) ToString_aÞ4cSA() String_g9YXBQ_Ñ { return objectToString(a) }
func (a IntArray__Impl) String() string                   { return objectString(a) }
func (a IntArray__Impl) Wait_KgmKcQ()                     { panic("Not implemented") }
func (a IntArray__Impl) Wait_vVGjdQ(int64)                { panic("Not implemented") }
func (a IntArray__Impl) Wait_OTFAsA(int64, int32)         { panic("Not implemented") }
func (a IntArray__Impl) Get(i int32) int32                { return a[i] }
func (a IntArray__Impl) Set(i int32, v int32)             { a[i] = v }
func (a IntArray__Impl) Len() int32                       { return int32(len(a)) }
func (a IntArray__Impl) Raw() []int32                     { return a }
func (a IntArray__Impl) Raw_fAFaMw() *Object_fAFaMw_Í     { panic("Cannot get raw pointer of array") }

type LongArray__Instance interface {
	Array__Instance
	Get(int32) int64
	Set(int32, int64)
	Raw() []int64
}

type LongArray__Impl []int64

func NewLongArray(size int32) LongArray__Instance { return make(LongArray__Impl, size) }

func (a LongArray__Impl) Clone_KkF6yw() Object_fAFaMw_Ñ {
	panic("Not implemented")
}
func (a LongArray__Impl) Equals_g011Rw(Object_fAFaMw_Ñ) bool {
	panic("Not implemented")
}
func (a LongArray__Impl) Finalize_hqp4qA() { panic("Not implemented") }
func (a LongArray__Impl) GetClass_9pp3sQ() Class_dvhEBA_Ñ {
	return Class_dvhEBA().ForName_xpGqyQ_Í(NewString("[J"))
}
func (a LongArray__Impl) HashCode_Gyq6fg() int32           { return objectHashCode(a) }
func (a LongArray__Impl) Notify_NFqiHQ()                   { panic("Not implemented") }
func (a LongArray__Impl) NotifyAll_9o437g()                { panic("Not implemented") }
func (a LongArray__Impl) ToString_aÞ4cSA() String_g9YXBQ_Ñ { return objectToString(a) }
func (a LongArray__Impl) String() string                   { return objectString(a) }
func (a LongArray__Impl) Wait_KgmKcQ()                     { panic("Not implemented") }
func (a LongArray__Impl) Wait_vVGjdQ(int64)                { panic("Not implemented") }
func (a LongArray__Impl) Wait_OTFAsA(int64, int32)         { panic("Not implemented") }
func (a LongArray__Impl) Get(i int32) int64                { return a[i] }
func (a LongArray__Impl) Set(i int32, v int64)             { a[i] = v }
func (a LongArray__Impl) Len() int32                       { return int32(len(a)) }
func (a LongArray__Impl) Raw() []int64                     { return a }
func (a LongArray__Impl) Raw_fAFaMw() *Object_fAFaMw_Í     { panic("Cannot get raw pointer of array") }

type ObjectArray__Instance interface {
	Array__Instance
	Get(int32) Object_fAFaMw_Ñ
	Set(int32, Object_fAFaMw_Ñ)
	Raw() []Object_fAFaMw_Ñ
}

type ObjectArray__Impl struct {
	arr           []Object_fAFaMw_Ñ
	componentName string
}

func NewObjectArray(size int32, componentName string) ObjectArray__Instance {
	return &ObjectArray__Impl{
		arr:           make([]Object_fAFaMw_Ñ, size),
		componentName: componentName,
	}
}

func (a *ObjectArray__Impl) Clone_KkF6yw() Object_fAFaMw_Ñ {
	panic("Not implemented")
}
func (a *ObjectArray__Impl) Equals_g011Rw(Object_fAFaMw_Ñ) bool {
	panic("Not implemented")
}
func (a *ObjectArray__Impl) Finalize_hqp4qA() { panic("Not implemented") }
func (a *ObjectArray__Impl) GetClass_9pp3sQ() Class_dvhEBA_Ñ {
	return Class_dvhEBA().ForName_xpGqyQ_Í(NewString("[" + a.componentName))
}
func (a *ObjectArray__Impl) HashCode_Gyq6fg() int32           { return objectHashCode(a) }
func (a *ObjectArray__Impl) Notify_NFqiHQ()                   { panic("Not implemented") }
func (a *ObjectArray__Impl) NotifyAll_9o437g()                { panic("Not implemented") }
func (a *ObjectArray__Impl) ToString_aÞ4cSA() String_g9YXBQ_Ñ { return objectToString(a) }
func (a *ObjectArray__Impl) String() string                   { return objectString(a) }
func (a *ObjectArray__Impl) Wait_KgmKcQ()                     { panic("Not implemented") }
func (a *ObjectArray__Impl) Wait_vVGjdQ(int64)                { panic("Not implemented") }
func (a *ObjectArray__Impl) Wait_OTFAsA(int64, int32)         { panic("Not implemented") }
func (a *ObjectArray__Impl) Get(i int32) Object_fAFaMw_Ñ      { return a.arr[i] }
func (a *ObjectArray__Impl) Set(i int32, v Object_fAFaMw_Ñ)   { a.arr[i] = v }
func (a *ObjectArray__Impl) Len() int32                       { return int32(len(a.arr)) }
func (a *ObjectArray__Impl) Raw() []Object_fAFaMw_Ñ           { return a.arr }
func (a *ObjectArray__Impl) Raw_fAFaMw() *Object_fAFaMw_Í     { panic("Cannot get raw pointer of array") }
