package rt

//goahead:forward-static java.lang.Throwable
type Throwable_Static struct {
	impl Throwable_r1HNzA_Ś
}

func (this *Throwable_Static) Init() {
}

//goahead:forward-instance java.lang.Throwable
type Throwable struct {
	impl Throwable_r1HNzA_Í
	detailMessage String_g9YXBQ_Ñ
}

func (this *Throwable) Init_1() {

}

func (this *Throwable) Init_2(str String_g9YXBQ_Ñ) {
	this.detailMessage = str
}

func (this *Throwable) GetMessage() String_g9YXBQ_Ñ {
	return this.detailMessage
}

func (this *Throwable) FillInStackTrace() Throwable_r1HNzA_Í {
	// TODO
	return this.impl
}