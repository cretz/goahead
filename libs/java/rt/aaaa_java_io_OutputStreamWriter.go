package rt

// TODO: support more than default charset

//goahead:forward-instance java.io.OutputStreamWriter
type OutputStreamWriter struct {
	impl *OutputStreamWriter_RDlPMA_Í
	os   OutputStream_AjI4XQ_Ñ
	buf  string
}

func (this *OutputStreamWriter) Init(var0 OutputStream_AjI4XQ_Ñ) {
	this.os = var0
}

func (this *OutputStreamWriter) Append(var0 CharSequence_c8RpKw_Ñ) Writer_4WcZ7w_Ñ {
	if var0 == nil {
		this.buf += "null"
	} else {
		this.buf += var0.ToString_aÞ4cSA().Raw_g9YXBQ().Fwd_.str
	}
	return this.impl
}

func (this *OutputStreamWriter) Close() {
	this.impl.Flush_jWSxAg()
	this.os.Close_ll5Hjg()
}

func (this *OutputStreamWriter) Flush() {
	this.impl.flushBuffer_uCcd2A()
	this.os.Flush_jWSxAg()
}

func (this *OutputStreamWriter) flushBuffer() {
	if len(this.buf) > 0 {
		this.os.Write_luKvjg(NewString(this.buf).GetBytes_MU8lTg())
		this.buf = ""
	}
}

func (this *OutputStreamWriter) Write_1(var0 int) {
	this.buf += string(var0)
}

func (this *OutputStreamWriter) Write_2(var0 String_g9YXBQ_Ñ, var1 int, var2 int) {
	this.buf += var0.Raw_g9YXBQ().Fwd_.str[var1 : var1+var2]
}

func (this *OutputStreamWriter) Write_3(var0 CharArray__Instance, var1 int, var2 int) {
	this.buf += string(var0.Raw()[var1 : var1+var2])
}
