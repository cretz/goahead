package rt

import (
	"strings"
)

//goahead:forward-static java.lang.String
type String_Static struct{}

func (this *String_Static) Init() {}

func (this *String_Static) CopyValueOf_1(data CharArray__Instance) String_g9YXBQ_Ñ {
	return this.ValueOf_8(data)
}

func (this *String_Static) CopyValueOf_2(data CharArray__Instance, offset int32, count int32) String_g9YXBQ_Ñ {
	return this.ValueOf_9(data, offset, count)
}

func (this *String_Static) Format_1(format String_g9YXBQ_Ñ, args ObjectArray__Instance) String_g9YXBQ_Ñ {
	f := Formatter_zKn6ag().New()
	f.Init_611f1A()
	return f.Format_TkP94Q(format, args).ToString_aÞ4cSA()
}

func (this *String_Static) Format_2(l Locale_9yQoug_Ñ, format String_g9YXBQ_Ñ, args ObjectArray__Instance) String_g9YXBQ_Ñ {
	f := Formatter_zKn6ag().New()
	f.Init_Ji2tiQ(l)
	return f.Format_TkP94Q(format, args).ToString_aÞ4cSA()
}

func (this *String_Static) Join_1(delimiter CharSequence_c8RpKw_Ñ, elements Iterable_wiqOZg_Ñ) String_g9YXBQ_Ñ {
	j := StringJoiner_Q5RVKA().New()
	j.Init_ERaVeA(delimiter)
	for it := elements.Iterator_iynYYQ(); it.HasNext_OgOqPQ(); {
		j.Add_weYtlg(it.Next_N0NQYQ().(CharSequence_c8RpKw_Ñ))
	}
	return j.ToString_aÞ4cSA()
}

func (this *String_Static) Join_2(delimiter CharSequence_c8RpKw_Ñ, elements ObjectArray__Instance) String_g9YXBQ_Ñ {
	j := StringJoiner_Q5RVKA().New()
	j.Init_ERaVeA(delimiter)
	for _, e := range elements.Raw() {
		j.Add_weYtlg(e.(CharSequence_c8RpKw_Ñ))
	}
	return j.ToString_aÞ4cSA()
}

func (this *String_Static) ValueOf_1(c rune) String_g9YXBQ_Ñ {
	return NewString(string(c))
}

func (this *String_Static) ValueOf_2(d float64) String_g9YXBQ_Ñ {
	return Double_afgyxQ().ToString_BnuBfA_Í(d)
}

func (this *String_Static) ValueOf_3(f float32) String_g9YXBQ_Ñ {
	return Float_dyK86A().ToString_FtwBOg_Í(f)
}

func (this *String_Static) ValueOf_4(i int32) String_g9YXBQ_Ñ {
	return Integer_28uoyg().ToString_r2zA1w_Í(i)
}

func (this *String_Static) ValueOf_5(l int64) String_g9YXBQ_Ñ {
	return Long_DByqcA().ToString_N50Atg_Í(l)
}

func (this *String_Static) ValueOf_6(obj Object_fAFaMw_Ñ) String_g9YXBQ_Ñ {
	if obj == nil {
		return NewString("null")
	}
	return obj.ToString_aÞ4cSA()
}

func (this *String_Static) ValueOf_7(b bool) string {
	if b {
		return "true"
	}
	return "false"
}

func (this *String_Static) ValueOf_8(data CharArray__Instance) String_g9YXBQ_Ñ {
	s := String_g9YXBQ().New()
	s.Init_1HCHnA(data)
	return s
}

func (this *String_Static) ValueOf_9(data CharArray__Instance, offset int32, count int32) String_g9YXBQ_Ñ {
	s := String_g9YXBQ().New()
	s.Init_BDvBPA(data, offset, count)
	return s
}

//goahead:forward-instance java.lang.String
type String struct {
	impl *String_g9YXBQ_Í
	str  string
	hash int32
}

func (this *String) Init_1() {
}

func (this *String) Init_2(original String_g9YXBQ_Ñ) {
	this.str = original.Raw_g9YXBQ().Fwd_.str
	this.hash = original.Raw_g9YXBQ().Fwd_.hash
}

func (this *String) Init_3(value CharArray__Instance) {
	this.str = string(value.Raw())
}

func (this *String) Init_4(value CharArray__Instance, offset int32, count int32) {
	this.checkBounds(value, offset, count)
	this.str = string(value.Raw()[offset : offset+count])
}

func (this *String) Init_5(value IntArray__Instance, offset int32, count int32) {
	// TODO: test invalid code point
	this.checkBounds(value, offset, count)
	// Just copy it to a char array and call the other init
	chars := NewCharArray(count)
	for i := offset; i < offset+count; i++ {
		chars.Set(int32(i), rune(value.Get(i)))
	}
	this.Init_3(chars)
}

func (this *String) Init_6(byts ByteArray__Instance, offset int32, length int32, charsetName String_g9YXBQ_Ñ) {
	Objects_dkj2tA().RequireNonNull_MJGwVw_Í(charsetName, NewString("Source array is null"))
	this.Init_7(byts, offset, length, Charset_iUTqAQ().ForName_0CnJCg_Í(charsetName))
}

func (this *String) Init_7(byts ByteArray__Instance, offset int32, length int32, charset Charset_iUTqAQ_Ñ) {
	this.checkBounds(byts, offset, length)
	this.Init_3(charset.Decode_KXQoLg(ByteBuffer_ziC58A().Wrap_irLNJA_Í(byts, offset, length)).Array_6H9vcw())
}

func (this *String) Init_8(byts ByteArray__Instance, charsetName String_g9YXBQ_Ñ) {
	this.Init_6(byts, 0, byts.Len(), charsetName)
}

func (this *String) Init_9(byts ByteArray__Instance, charset Charset_iUTqAQ_Ñ) {
	this.Init_7(byts, 0, byts.Len(), charset)
}

func (this *String) Init_10(byts ByteArray__Instance, offset int32, length int32) {
	this.Init_7(byts, offset, length, Charset_iUTqAQ().DefaultCharset_sNz8Kw_Í())
}

func (this *String) Init_11(byts ByteArray__Instance) {
	this.Init_10(byts, 0, byts.Len())
}

func (this *String) Init_12(buffer StringBuffer_ÞmhØpQ_Ñ) {
	this.Init_2(buffer.ToString_aÞ4cSA())
}

func (this *String) Init_13(builder StringBuilder_UtRw1g_Ñ) {
	this.Init_2(builder.ToString_aÞ4cSA())
}

func (this *String) CharAt(index int32) rune {
	return rune(this.str[index])
}

func (this *String) CodePointAt(index int32) int32 {
	return Character_V2YvtQ().CodePointAt_et0zRA_Í(this.impl, index)
}

func (this *String) CodePointBefore(index int32) int32 {
	return Character_V2YvtQ().CodePointBefore_KD1xEA_Í(this.impl, index)
}

func (this *String) CodePointCount(beginIndex int32, endIndex int32) int32 {
	return Character_V2YvtQ().CodePointCount_wuNEEQ_Í(this.impl, beginIndex, endIndex)
}

func (this *String) CompareTo_1(other Object_fAFaMw_Ñ) int32 {
	return this.CompareTo_2(GetString(other.(String_g9YXBQ_Ñ)))
}

func (this *String) CompareTo_2(other string) int32 {
	return int32(strings.Compare(this.str, other))
}

func (this *String) CompareToIgnoreCase(other string) int32 {
	// TODO: this surely can be made faster
	return int32(strings.Compare(strings.ToLower(this.str), strings.ToLower(other)))
}

func (this *String) Concat(s string) string {
	return this.str + s
}

func (this *String) Contains(s CharSequence_c8RpKw_Ñ) bool {
	return this.IndexOf_3(GetString(s.ToString_aÞ4cSA())) > -1
}

func (this *String) ContentEquals_1(cs CharSequence_c8RpKw_Ñ) bool {
	return this.str == GetString(cs.ToString_aÞ4cSA())
}

func (this *String) ContentEquals_2(sb StringBuffer_ÞmhØpQ_Ñ) bool {
	return this.ContentEquals_1(sb)
}

func (this *String) EndsWith(s string) bool {
	return strings.HasSuffix(this.str, s)
}

func (this *String) Equals(obj Object_fAFaMw_Ñ) bool {
	str, ok := obj.(String_g9YXBQ_Ñ)
	return ok && this.str == GetString(str)
}

func (this *String) EqualsIgnoreCase(s string) bool {
	return strings.EqualFold(this.str, s)
}

func (this *String) GetBytes_1() ByteArray__Instance {
	// TODO: switch to GetBytes_4 when charsets are in place
	ret := NewByteArray(this.Length())
	for i := 0; i < len(this.str); i++ {
		ret.Set(int32(i), int8(this.str[i]))
	}
	return ret
}

func (this *String) GetBytes_2(srcBegin int32, srcEnd int32, dst ByteArray__Instance, dstBegin int32) {
	for i := srcBegin; i < srcEnd; i++ {
		dst.Set(dstBegin+i, int8(this.str[i]))
	}
}

func (this *String) GetBytes_3(charsetName String_g9YXBQ_Ñ) ByteArray__Instance {
	return this.GetBytes_4(Charset_iUTqAQ().ForName_0CnJCg_Í(charsetName))
}

func (this *String) GetBytes_4(charset Charset_iUTqAQ_Ñ) ByteArray__Instance {
	return charset.Encode_dCQtÞw(this.impl).Array_Þfxo8g()
}

func (this *String) GetChars(srcBegin int32, srcEnd int32, dst CharArray__Instance, dstBegin int32) {
	if srcBegin < 0 {
		ex := StringIndexOutOfBoundsException_SWP1Ag().New()
		ex.Init_zJ0QMQ(srcBegin)
		panic(ex)
	}
	if srcEnd > this.Length() {
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

func (this *String) HashCode() int32 {
	h := this.hash
	if h == 0 && len(this.str) > 0 {
		for i := 0; i < len(this.str); i++ {
			h = 31*h + int32(this.str[i])
		}
		this.hash = h
	}
	return h
}

func (this *String) IndexOf_1(ch int32) int32 {
	return int32(strings.IndexRune(this.str, rune(ch)))
}

func (this *String) IndexOf_2(ch int32, fromIndex int32) int32 {
	if fromIndex <= 0 {
		return this.IndexOf_1(ch)
	}
	return int32(strings.IndexRune(this.str[fromIndex:], rune(ch)))
}

func (this *String) IndexOf_3(str string) int32 {
	return int32(strings.Index(this.str, str))
}

func (this *String) IndexOf_4(str string, fromIndex int32) int32 {
	if fromIndex <= 0 {
		return this.IndexOf_3(str)
	}
	return int32(strings.Index(this.str[fromIndex:], str))
}

func (this *String) Intern() String_g9YXBQ_Ñ {
	// TODO: we want a weak map of strings here, but do I really want to create a finalizer for
	// every string intern?
	// Ref: https://groups.google.com/forum/#!topic/golang-nuts/PYWxjT2v6ps
	// Ref: https://github.com/josharian/intern/
	// Ref: https://play.golang.org/p/HtarEI4kCS
	panic("Intern not yet impld")
}

func (this *String) IsEmpty() bool {
	return this.str == ""
}

func (this *String) LastIndexOf_1(ch int32) int32 {
	return int32(strings.LastIndex(this.str, string(ch)))
}

func (this *String) LastIndexOf_2(ch int32, fromIndex int32) int32 {
	if fromIndex+1 >= this.Length() {
		return this.LastIndexOf_1(ch)
	}
	return int32(strings.LastIndex(this.str[:fromIndex+1], string(ch)))
}

func (this *String) LastIndexOf_3(str string) int32 {
	return int32(strings.LastIndex(this.str, str))
}

func (this *String) LastIndexOf_4(str string, fromIndex int32) int32 {
	if fromIndex+1 >= this.Length() {
		return this.LastIndexOf_3(str)
	}
	return int32(strings.LastIndex(this.str[:fromIndex+1], str))
}

func (this *String) Length() int32 {
	return int32(len(this.str))
}

func (this *String) Matches(var0 String_g9YXBQ_Ñ) bool {
	// TODO
	panic("Method not implemented - java/lang/String::matches(Ljava/lang/String;)Z")
}

func (this *String) OffsetByCodePoints(index int32, codePointOffset int32) int32 {
	return Character_V2YvtQ().OffsetByCodePoints_oEpl4w_Í(this.impl, index, codePointOffset)
}

func (this *String) RegionMatches_1(toffset int32, other string, ooffset int32, length int32) bool {
	return toffset > 0 && ooffset > 0 && toffset+length <= this.Length() && ooffset+length <= this.Length() &&
		this.str[toffset:toffset+length] == other[ooffset:ooffset+length]
}

func (this *String) RegionMatches_2(ignoreCase bool, toffset int32, other string, ooffset int32, length int32) bool {
	if toffset > 0 && ooffset > 0 && toffset+length <= this.Length() && ooffset+length <= this.Length() {
		return false
	}
	if ignoreCase {
		return strings.EqualFold(this.str[toffset:toffset+length], other[ooffset:ooffset+length])
	}
	return this.str[toffset:toffset+length] == other[ooffset:ooffset+length]
}

func (this *String) Replace_1(oldChar rune, newChar rune) string {
	return strings.Replace(this.str, string(oldChar), string(newChar), -1)
}

func (this *String) Replace_2(target CharSequence_c8RpKw_Ñ, replacement CharSequence_c8RpKw_Ñ) string {
	return strings.Replace(this.str, GetString(target.ToString_aÞ4cSA()), GetString(replacement.ToString_aÞ4cSA()), -1)
}

func (this *String) ReplaceAll(regex String_g9YXBQ_Ñ, replacement String_g9YXBQ_Ñ) String_g9YXBQ_Ñ {
	// TODO
	panic("Method not implemented - java/lang/String::replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")
}

func (this *String) ReplaceFirst(regex String_g9YXBQ_Ñ, replacement String_g9YXBQ_Ñ) String_g9YXBQ_Ñ {
	// TODO
	panic("Method not implemented - java/lang/String::replaceFirst(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")
}

func (this *String) Split_1(regex String_g9YXBQ_Ñ) ObjectArray__Instance {
	// TODO
	panic("Method not implemented - java/lang/String::split(Ljava/lang/String;)[Ljava/lang/String;")
}

func (this *String) Split_2(regex String_g9YXBQ_Ñ, limit int32) ObjectArray__Instance {
	// TODO
	panic("Method not implemented - java/lang/String::split(Ljava/lang/String;I)[Ljava/lang/String;")
}

func (this *String) StartsWith_1(prefix string) bool {
	return strings.HasPrefix(this.str, prefix)
}

func (this *String) StartsWith_2(prefix string, toffset int32) bool {
	if toffset < 0 || toffset > this.Length() {
		return false
	}
	return strings.HasPrefix(this.str[toffset:], prefix)
}

func (this *String) SubSequence(beginIndex int32, endIndex int32) CharSequence_c8RpKw_Ñ {
	return NewString(this.Substring_2(beginIndex, endIndex))
}

func (this *String) Substring_1(beginIndex int32) string {
	return this.Substring_2(beginIndex, this.Length())
}

func (this *String) Substring_2(beginIndex int32, endIndex int32) string {
	return this.str[beginIndex:endIndex]
}

func (this *String) ToCharArray() CharArray__Instance {
	return CharArray__Impl([]rune(this.str))
}

func (this *String) ToLowerCase_1() string {
	return strings.ToLower(this.str)
}

func (this *String) ToLowerCase_2(var0 Locale_9yQoug_Ñ) String_g9YXBQ_Ñ {
	// TODO
	panic("Method not implemented - java/lang/String::toLowerCase(Ljava/util/Locale;)Ljava/lang/String;")
}

func (this *String) ToString() String_g9YXBQ_Ñ {
	return this.impl
}

func (this *String) ToUpperCase_1() string {
	return strings.ToUpper(this.str)
}

func (this *String) ToUpperCase_2(var0 Locale_9yQoug_Ñ) String_g9YXBQ_Ñ {
	// TODO
	panic("Method not implemented - java/lang/String::toUpperCase(Ljava/util/Locale;)Ljava/lang/String;")
}

func (this *String) Trim() string {
	// Follow Java rules of anything <= 32
	return strings.TrimFunc(this.str, func(r rune) bool { return r <= 32 })
}

// Helpers

func (t *String) checkBounds(arr Array__Instance, offset int32, count int32) {
	if offset < 0 {
		ex := StringIndexOutOfBoundsException_SWP1Ag().New()
		ex.Init_zJ0QMQ(offset)
		panic(ex)
	}
	if count < 0 {
		ex := StringIndexOutOfBoundsException_SWP1Ag().New()
		ex.Init_zJ0QMQ(count)
		panic(ex)
	}
	if offset > arr.Len()-count {
		ex := StringIndexOutOfBoundsException_SWP1Ag().New()
		ex.Init_zJ0QMQ(offset + count)
		panic(ex)
	}

}
