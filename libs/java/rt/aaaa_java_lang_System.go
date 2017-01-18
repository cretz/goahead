package rt

import (
	"log"
	"os"
	"reflect"
	"runtime"
	"time"
)

//goahead:forward-static java.lang.System
type System_Static struct {
	impl *System_hB0pIw_Ś
}

func (this *System_Static) Init() {

	ps := PrintStream_kZ4QkQ().New()
	ps.Init_iYrehg(WriterToOutputStream(os.Stdout))
	this.impl.Out_Øj4tRQ = ps

	if runtime.GOOS == "windows" {
		this.impl.lineSeparator_d8DonA = NewString("\r\n")
	} else {
		this.impl.lineSeparator_d8DonA = NewString("\n")
	}

	log.Printf("Sys initialized\n")
}

func (this *System_Static) InitializeSystemClass() {}

const maxUint = uint64(^uint(0))
const maxInt = maxUint >> 1

// func (this *System_hB0pIw_Ś) Arraycopy_fsFLrQ_Í(var0 Object_fAFaMw_Ñ, var1 int, var2 Object_fAFaMw_Ñ, var3 int, var4 int) {
func (this *System_Static) ArrayCopy(src Object_fAFaMw_Ñ, srcPos int, dest Object_fAFaMw_Ñ, destPos int, length int) {
	Objects_dkj2tA().RequireNonNull_MJGwVw_Í(src, NewString("Source array is null"))
	Objects_dkj2tA().RequireNonNull_MJGwVw_Í(dest, NewString("Dest array is null"))
	srcArr, ok := src.(Array__Instance)
	if !ok {
		ex := ArrayStoreException_UcFIsw().New()
		ex.Init_M13Ø3g(NewString("Source is not array"))
		panic(ex)
	}
	destArr, ok := dest.(Array__Instance)
	if !ok {
		ex := ArrayStoreException_UcFIsw().New()
		ex.Init_M13Ø3g(NewString("Dest is not array"))
		panic(ex)
	}
	if reflect.TypeOf(src) != reflect.TypeOf(dest) {
		ex := ArrayStoreException_UcFIsw().New()
		ex.Init_M13Ø3g(NewString("Arrays of different types"))
		panic(ex)
	}
	if srcPos < 0 {
		ex := IndexOutOfBoundsException_YnUJEw().New()
		ex.Init_zJ0QMQ(srcPos)
		panic(ex)
	}
	if destPos < 0 {
		ex := IndexOutOfBoundsException_YnUJEw().New()
		ex.Init_zJ0QMQ(destPos)
		panic(ex)
	}
	if length < 0 {
		ex := IndexOutOfBoundsException_YnUJEw().New()
		ex.Init_zJ0QMQ(length)
		panic(ex)
	}
	if srcPos+length > srcArr.Len() {
		ex := IndexOutOfBoundsException_YnUJEw().New()
		ex.Init_zJ0QMQ(srcPos + length)
		panic(ex)
	}
	if destPos+length > destArr.Len() {
		ex := IndexOutOfBoundsException_YnUJEw().New()
		ex.Init_zJ0QMQ(destPos + length)
		panic(ex)
	}
	switch s := src.(type) {
	case BoolArray__Impl:
		copy(dest.(BoolArray__Impl)[destPos:], s[srcPos:srcPos+length])
	case CharArray__Impl:
		copy(dest.(CharArray__Impl)[destPos:], s[srcPos:srcPos+length])
	case FloatArray__Impl:
		copy(dest.(FloatArray__Impl)[destPos:], s[srcPos:srcPos+length])
	case DoubleArray__Impl:
		copy(dest.(DoubleArray__Impl)[destPos:], s[srcPos:srcPos+length])
	case ByteArray__Impl:
		copy(dest.(ByteArray__Impl)[destPos:], s[srcPos:srcPos+length])
	case ShortArray__Impl:
		copy(dest.(ShortArray__Impl)[destPos:], s[srcPos:srcPos+length])
	case IntArray__Impl:
		copy(dest.(IntArray__Impl)[destPos:], s[srcPos:srcPos+length])
	case LongArray__Impl:
		copy(dest.(LongArray__Impl)[destPos:], s[srcPos:srcPos+length])
	case *ObjectArray__Impl:
		// TODO: check every value to make sure it is assignable?
		copy(dest.(ObjectArray__Instance).Raw()[destPos:], s.Raw()[srcPos:srcPos+length])
	}
}

func (this *System_Static) CurrentTimeMillis() int64 {
	return time.Now().UnixNano() / 1000000
}

func (this *System_Static) IdentityHashCode(var0 Object_fAFaMw_Ñ) int {
	if var0 == nil {
		return 0
	}
	// TODO: review
	// We're going to grab a uintptr, do a % (maxuint - 2), sub by maxint and negate if > maxint
	ptr := uint64(reflect.ValueOf(var0).Pointer()) % (maxUint - 2)
	if ptr == 0 {
		return 1
	} else if ptr > maxInt {
		return int(-(ptr - maxInt))
	} else {
		return int(ptr)
	}
}

func (this *System_Static) InitProperties(var0 Properties_fgd3kw_Ñ) Properties_fgd3kw_Ñ {
	panic("Init properties not yet impld")
}

func (this *System_Static) MapLibraryName(libname String_g9YXBQ_Ñ) String_g9YXBQ_Ñ {
	switch runtime.GOOS {
	case "windows":
		return NewString(libname.Raw_g9YXBQ().Fwd_.str + ".dll")
	case "darwin":
		return NewString("lib" + libname.Raw_g9YXBQ().Fwd_.str + ".dylib")
	default:
		return NewString("lib" + libname.Raw_g9YXBQ().Fwd_.str + ".so")
	}
}

var fixedTime = time.Now()

func (this *System_Static) NanoTime() int64 {
	// Just use the diff from fixed time
	return time.Now().Sub(fixedTime).Nanoseconds()
}

func (this *System_Static) RegisterNatives()                  {}
func (this *System_Static) setErr0(var0 PrintStream_kZ4QkQ_Ñ) {}
func (this *System_Static) setIn0(var0 InputStream_kz6r7g_Ñ)  {}
func (this *System_Static) setOut0(var0 PrintStream_kZ4QkQ_Ñ) {}
