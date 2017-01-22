package rt

import "strconv"

//goahead:forward-instance java.lang.StringBuilder
type StringBuilder struct {
	impl *StringBuilder_UtRw1g_Í
	str  string
}

func (this *StringBuilder) Init() {}

func (this *StringBuilder) Append_1(str string) StringBuilder_UtRw1g_Ñ {
	this.str += str
	return this.impl
}

func (this *StringBuilder) Append_2(v int32) StringBuilder_UtRw1g_Ñ {
	this.str += strconv.Itoa(int(v))
	return this.impl
}

func (this *StringBuilder) ToString() string {
	return this.str
}
