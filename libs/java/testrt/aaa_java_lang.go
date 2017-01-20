package testrt

import (
	"strconv"
	"reflect"
)

func (this *ClassCastException_x20ÞLA_Í) Init_611f1A_Í() {

}

func (this *Exception_UnpE2w_Í) Init_M13Ø3g_Í(var0 String_g9YXBQ_Ñ) {
	this.Throwable_r1HNzA_Í.Init_M13Ø3g_Í(var0)
}

func (this *Exception_UnpE2w_Í) Init_DWKv2Q_Í(var0 String_g9YXBQ_Ñ, var1 Throwable_r1HNzA_Ñ) {
	// TODO: being lazy, impl this properly one day
	this.Throwable_r1HNzA_Í.Init_M13Ø3g_Í(var0)
}

func (this *IllegalMonitorStateException_j9dM1g_Í) Init_611f1A_Í() {

}

func (this *NegativeArraySizeException_LQJRfg_Í) Init_M13Ø3g_Í(var0 String_g9YXBQ_Ñ) {
	this.Throwable_r1HNzA_Í.Init_M13Ø3g_Í(var0)
}

func (this *NullPointerException_fnXÞLQ_Í) Init_M13Ø3g_Í(var0 String_g9YXBQ_Ñ) {
	this.Throwable_r1HNzA_Í.Init_M13Ø3g_Í(var0)
}

func (this *Object_fAFaMw_Í) Init_611f1A_Í() {

}

func (this *Object_fAFaMw_Í) GetClass_9pp3sQ_Í() Class_dvhEBA_Ñ {
	// TODO: just a placeholder since javac injects this call for simple null checks
	this.HashCode_Gyq6fg_Í();
	return nil
}

func (this *Object_fAFaMw_Í) HashCode_Gyq6fg_Í() int {
	return System_hB0pIw().IdentityHashCode_lcJyvA_Í(this)
}

func (this *StringBuilder_UtRw1g_Í) Init_611f1A_Í() {

}

func (this *StringBuilder_UtRw1g_Í) ToString_aÞ4cSA_Í() String_g9YXBQ_Ñ {
	return NewString(this.Underlying)
}

func (this *StringBuilder_UtRw1g_Í) Append_UAJg0Q_Í(var1 String_g9YXBQ_Ñ) StringBuilder_UtRw1g_Ñ {
	// TODO: put the auto-string conv stuff somewhere
	if var1 == nil {
		this.Underlying += "null"
	} else {
		this.Underlying += var1.(*String_g9YXBQ_Í).Underlying
	}
	return this
}

func (this *StringBuilder_UtRw1g_Í) Append_RGOMdA_Í(var1 int) StringBuilder_UtRw1g_Ñ {
	this.Underlying += strconv.Itoa(var1)
	return this
}

func (this *System_hB0pIw_Ś) clinit_Mxmluw_Í() {
	this.Out_Øj4tRQ = PrintStream_kZ4QkQ().New()
}

const maxUint = uint64(^uint(0))
const maxInt = maxUint >> 1

func (this *System_hB0pIw_Ś) IdentityHashCode_lcJyvA_Í(var0 Object_fAFaMw_Ñ) int {
	if (var0 == nil) {
		return 0
	}
	// TODO: review
	// We're going to grab a uintptr, do a % (maxuint - 2), sub by maxint and negate if > maxint
	ptr := uint64(reflect.ValueOf(var0).Pointer()) % (maxUint - 2)
	if (ptr == 0) {
		return 1
	} else if (ptr > maxInt) {
		return int(-(ptr - maxInt))
	} else {
		return int(ptr)
	}
}

func (this *Throwable_r1HNzA_Í) Error() string {
	return this._dispatch.GetMessage_iMkxrQ_Í().(*String_g9YXBQ_Í).Underlying
}

func (this *Throwable_r1HNzA_Í) Init_M13Ø3g_Í(var0 String_g9YXBQ_Ñ) {
	this.Message = var0
}

func (this *Throwable_r1HNzA_Í) GetMessage_iMkxrQ_Í() String_g9YXBQ_Ñ {
	return this.Message
}

func (this *VirtualMachineError_JV2iOA_Í) Init_M13Ø3g_Í(var0 String_g9YXBQ_Ñ) {
	this.Throwable_r1HNzA_Í.Init_M13Ø3g_Í(var0)
}
