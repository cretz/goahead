package rt

func (this *Java__lang__Long__Static) Impl__ValueOf__desc__J__ret____obj__Java__lang__Long(var0 int64) Java__lang__Long__Instance {
	v := this.New()
	v.Instance_Init__desc__J__ret__V(var0)
	return v
}

func (this *Java__lang__Long__Impl) Impl__Instance_Init__desc__J__ret__V(var0 int64) {
	this.Value = var0
}

func (this *Java__lang__Long__Impl) Impl__LongValue__desc____ret__J() int64 {
	return this.Value
}