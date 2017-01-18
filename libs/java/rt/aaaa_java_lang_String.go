package rt

//goahead:forward-static java.lang.String
type String_Static struct{}

func (this *String_Static) Init() {}

func (this *String_Static) ValueOf(obj Object_fAFaMw_Ñ) String_g9YXBQ_Ñ {
	if obj == nil {
		return NewString("null")
	}
	return obj.ToString_aÞ4cSA()
}

//goahead:forward-instance java.lang.String
type String struct {
	impl *String_g9YXBQ_Í
	str  string
}

func (this *String) Init() {
}

func (this *String) GetChars(srcBegin int, srcEnd int, dst CharArray__Instance, dstBegin int) {
	if srcBegin < 0 {
		ex := StringIndexOutOfBoundsException_SWP1Ag().New()
		ex.Init_zJ0QMQ(srcBegin)
		panic(ex)
	}
	if srcEnd > len(this.str) {
		ex := StringIndexOutOfBoundsException_SWP1Ag().New()
		ex.Init_zJ0QMQ(srcEnd)
		panic(ex)
	}
	if srcBegin > srcEnd {
		ex := StringIndexOutOfBoundsException_SWP1Ag().New()
		ex.Init_zJ0QMQ(srcEnd - srcBegin)
	}
	System_hB0pIw().Arraycopy_fsFLrQ_Í(CharArray__Impl([]rune(this.str)), srcBegin, dst, dstBegin, srcEnd-srcBegin)
}

func (this *String) Length() int {
	return len(this.str)
}

func (this *String) ToString() String_g9YXBQ_Ñ {
	return this.impl
}
