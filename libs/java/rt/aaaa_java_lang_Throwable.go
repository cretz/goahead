package rt

// Have throwable implement errors.Error
func (this *Throwable_r1HNzA_Í) Error() string {
	detailMessage := ""
	if this.Fwd_.detailMessage != nil {
		detailMessage = GetString(this.Fwd_.detailMessage)
	}
	return GetString(this.GetClass_9pp3sQ().GetName_uasY3Q()) + ": " + detailMessage
}

//goahead:forward-static java.lang.Throwable
type Throwable_Static struct{}

func (this *Throwable_Static) Init() {
}

//goahead:forward-instance java.lang.Throwable
type Throwable struct {
	impl          *Throwable_r1HNzA_Í
	detailMessage String_g9YXBQ_Ñ
	cause         Throwable_r1HNzA_Ñ
}

func (this *Throwable) Init_1() {
	//fmt.Printf("Created new %T\nSTACK: %v",
	//	this.impl,
	//	string(debug.Stack()))
}

func (this *Throwable) Init_2(str String_g9YXBQ_Ñ) {
	this.detailMessage = str
	//fmt.Printf("Created new %T with message %v\nSTACK: %v",
	//	this.impl,
	//	str.Raw_g9YXBQ().Fwd_.str,
	//	string(debug.Stack()))
}

func (this *Throwable) Init_3(str String_g9YXBQ_Ñ, cause Throwable_r1HNzA_Ñ) {
	this.Init_2(str)
	this.cause = cause
}

func (this *Throwable) GetMessage() String_g9YXBQ_Ñ {
	return this.detailMessage
}

func (this *Throwable) GetCause() Throwable_r1HNzA_Ñ {
	return this.cause
}
