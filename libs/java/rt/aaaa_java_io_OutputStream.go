package rt

import (
	"io"
)

type WriterOutputStream struct {
	*OutputStream_AjI4XQ_Í
	w io.Writer
}

func WriterToOutputStream(w io.Writer) OutputStream_AjI4XQ_Ñ {
	ret := &WriterOutputStream{
		OutputStream_AjI4XQ_Í: OutputStream_AjI4XQ().New(),
		w: w,
	}
	ret.OutputStream_AjI4XQ_Í.OutputStream_AjI4XQ_Ð_Init(ret)
	return ret
}

func (this *WriterOutputStream) Write_3grrng(var0 int) {
	_, err := this.w.Write([]byte{byte(var0)})
	if err != nil {
		ex := IOException_6bTB7Q().New()
		ex.Init_M13Ø3g(NewString(err.Error()))
		panic(ex)
	}
}