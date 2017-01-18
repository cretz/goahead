package rt

import (
	"os"
	"reflect"
	"log"
)

//goahead:forward-static java.lang.System
type System_Static struct {
	impl *System_hB0pIw_Ś
}

func (this *System_Static) Init() {
	ps := PrintStream_kZ4QkQ().New()
	ps.Init_iYrehg(WriterToOutputStream(os.Stdout))
	this.impl.Out_Øj4tRQ = ps
}

const maxUint = uint64(^uint(0))
const maxInt = maxUint >> 1

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
