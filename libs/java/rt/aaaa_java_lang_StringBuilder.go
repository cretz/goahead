package rt

import "strconv"

//goahead:forward-instance java.lang.StringBuilder
type StringBuilder struct {
	impl *StringBuilder_UtRw1g_Í
	str  string
}

func (this *StringBuilder) Init() {}

func (this *StringBuilder) Append_1(str String_g9YXBQ_Ñ) StringBuilder_UtRw1g_Ñ {
	this.str += str.Raw_g9YXBQ().Fwd_.str
	return this.impl
}

func (this *StringBuilder) Append_2(v int) StringBuilder_UtRw1g_Ñ {
	this.str += strconv.Itoa(v)
	return this.impl
}

func (this *StringBuilder) ToString() String_g9YXBQ_Ñ {
	return NewString(this.str)
}
