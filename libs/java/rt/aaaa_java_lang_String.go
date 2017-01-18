package rt

//goahead:forward-static java.lang.String
type String_Static struct{}

func (this *String_Static) Init() {}

func (this *String_Static) ValueOf(obj Object_fAFaMw_Ñ) String_g9YXBQ_Ñ {
	if (obj == nil) {
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

func (this *String) Length() int {
	return len(this.str)
}

func (this *String) ToString() String_g9YXBQ_Ñ {
	return this.impl
}
