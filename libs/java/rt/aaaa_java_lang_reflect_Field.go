package rt

import (
	"fmt"
	"reflect"
	"strings"
)

//goahead:forward-instance java.lang.reflect.Field
type Field struct {
	impl               *Field_qRaptA_Í
	cls                Class_dvhEBA_Ñ
	info               *FieldInfo
	overrideAccessible bool
}

func (this *Field) Equals(obj Object_fAFaMw_Ñ) bool {
	if obj == nil {
		return false
	}
	other, ok := obj.(Field_qRaptA_Ñ)
	if !ok {
		return false
	}
	return this.GetDeclaringClass().Equals_g011Rw(other.GetDeclaringClass_beGXzA()) &&
		NewString(this.GetName()).Equals_g011Rw(other.GetName_uasY3Q()) &&
		this.GetType().Equals_g011Rw(other.GetType_HZ5SAw())
}

func (this *Field) Get(obj Object_fAFaMw_Ñ) Object_fAFaMw_Ñ {
	return reflectValueToObject(this.getReflectValue(obj), this.info.RawTypeClassName)
}

func (this *Field) GetAnnotatedType() AnnotatedType_gjnerA_Ñ {
	panic("Method not implemented - java/lang/reflect/Field::getAnnotatedType()Ljava/lang/reflect/AnnotatedType;")
}

func (this *Field) GetAnnotation(cls Class_dvhEBA_Ñ) Annotation_3r3c2w_Ñ {
	return this.info.Annotations[GetString(cls.GetName_uasY3Q())]
}

func (this *Field) GetAnnotationsByType(cls Class_dvhEBA_Ñ) ObjectArray__Instance {
	return getAnnotationsByTypeFromDeclared(this.impl, cls)
}

func (this *Field) GetBoolean(obj Object_fAFaMw_Ñ) bool {
	v := this.getReflectValue(obj)
	if v.Kind() != reflect.Bool {
		ex := IllegalArgumentException_cljØMA().New()
		ex.Init_M13Ø3g(NewString("Field value not a boolean"))
		panic(ex)
	}
	return v.Bool()
}

func (this *Field) GetByte(obj Object_fAFaMw_Ñ) int8 {
	v := this.getReflectValue(obj)
	if v.Kind() != reflect.Int8 {
		ex := IllegalArgumentException_cljØMA().New()
		ex.Init_M13Ø3g(NewString("Field value not a byte"))
		panic(ex)
	}
	return int8(v.Int())
}

func (this *Field) GetChar(obj Object_fAFaMw_Ñ) rune {
	v := this.getReflectValue(obj)
	if v.Kind() != reflect.Int32 || this.info.RawTypeClassName != "<primitive>char" {
		ex := IllegalArgumentException_cljØMA().New()
		ex.Init_M13Ø3g(NewString("Field value not a char"))
		panic(ex)
	}
	return rune(v.Int())
}

func (this *Field) GetDeclaredAnnotations() ObjectArray__Instance {
	arr := NewObjectArray(int32(len(this.info.Annotations)), "java.lang.Annotation")
	var index int32
	for _, v := range this.info.Annotations {
		arr.Set(index, v)
		index++
	}
	return arr
}

func (this *Field) GetDeclaringClass() Class_dvhEBA_Ñ {
	return this.cls
}

func (this *Field) GetDouble(obj Object_fAFaMw_Ñ) float64 {
	v := this.getReflectValue(obj)
	switch v.Kind() {
	case reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64:
		return float64(v.Int())
	case reflect.Float32, reflect.Float64:
		return v.Float()
	default:
		ex := IllegalArgumentException_cljØMA().New()
		ex.Init_M13Ø3g(NewString("Field value not a double"))
		panic(ex)
	}
}

func (this *Field) GetFloat(obj Object_fAFaMw_Ñ) float32 {
	v := this.getReflectValue(obj)
	switch v.Kind() {
	case reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64:
		return float32(v.Int())
	case reflect.Float32:
		return float32(v.Float())
	default:
		ex := IllegalArgumentException_cljØMA().New()
		ex.Init_M13Ø3g(NewString("Field value not a float"))
		panic(ex)
	}
}

func (this *Field) GetGenericType() Type_T82HQA_Ñ {
	panic("Method not implemented - java/lang/reflect/Field::getGenericType()Ljava/lang/reflect/Type;")
}

func (this *Field) GetInt(obj Object_fAFaMw_Ñ) int32 {
	v := this.getReflectValue(obj)
	switch v.Kind() {
	case reflect.Int8, reflect.Int16, reflect.Int32:
		return int32(v.Int())
	default:
		ex := IllegalArgumentException_cljØMA().New()
		ex.Init_M13Ø3g(NewString("Field value not an int"))
		panic(ex)
	}
}

func (this *Field) GetLong(obj Object_fAFaMw_Ñ) int64 {
	v := this.getReflectValue(obj)
	switch v.Kind() {
	case reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64:
		return v.Int()
	default:
		ex := IllegalArgumentException_cljØMA().New()
		ex.Init_M13Ø3g(NewString("Field value not a long"))
		panic(ex)
	}
}

func (this *Field) GetModifiers() int32 {
	return this.info.Modifiers
}

func (this *Field) GetName() string {
	return this.info.Name
}

func (this *Field) GetShort(obj Object_fAFaMw_Ñ) int16 {
	v := this.getReflectValue(obj)
	switch v.Kind() {
	case reflect.Int8, reflect.Int16:
		return int16(v.Int())
	default:
		ex := IllegalArgumentException_cljØMA().New()
		ex.Init_M13Ø3g(NewString("Field value not a short"))
		panic(ex)
	}
}

func (this *Field) GetType() Class_dvhEBA_Ñ {
	return Class_dvhEBA().ForName_xpGqyQ_Í(NewString(this.info.RawTypeClassName))
}

func (this *Field) HashCode() int32 {
	// return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
	return this.GetDeclaringClass().GetName_uasY3Q().HashCode_Gyq6fg() ^ this.impl.GetName_uasY3Q().HashCode_Gyq6fg()
}

func (this *Field) IsEnumConstant() bool {
	return this.GetModifiers()&Modifier_FvÞH3Q().C_ENUM_aq8PWA != 0
}

func (this *Field) IsSynthetic() bool {
	return Modifier_FvÞH3Q().IsSynthetic_yOFd6g_Í(this.GetModifiers())
}

func (this *Field) Set(obj Object_fAFaMw_Ñ, value Object_fAFaMw_Ñ) {
	this.setWithCallback(obj, func(v reflect.Value) {
		setReflectValueFromObject(value, v, this.info.RawTypeClassName)
	})
}

func (this *Field) SetAccessible(flag bool) {
	this.overrideAccessible = flag
}

func (this *Field) SetBoolean(obj Object_fAFaMw_Ñ, v bool) {
	// TODO: stop being lazy?
	this.Set(obj, box(v, this.info.RawTypeClassName))
}

func (this *Field) SetByte(obj Object_fAFaMw_Ñ, v int8) {
	this.Set(obj, box(v, this.info.RawTypeClassName))
}

func (this *Field) SetChar(obj Object_fAFaMw_Ñ, v rune) {
	this.Set(obj, box(v, this.info.RawTypeClassName))
}

func (this *Field) SetDouble(obj Object_fAFaMw_Ñ, v float64) {
	this.Set(obj, box(v, this.info.RawTypeClassName))
}

func (this *Field) SetFloat(obj Object_fAFaMw_Ñ, v float32) {
	this.Set(obj, box(v, this.info.RawTypeClassName))
}

func (this *Field) SetInt(obj Object_fAFaMw_Ñ, v int32) {
	this.Set(obj, box(v, this.info.RawTypeClassName))
}

func (this *Field) SetLong(obj Object_fAFaMw_Ñ, v int64) {
	this.Set(obj, box(v, this.info.RawTypeClassName))
}

func (this *Field) SetShort(obj Object_fAFaMw_Ñ, v int16) {
	this.Set(obj, box(v, this.info.RawTypeClassName))
}

func (this *Field) ToGenericString() string {
	modStr := ""
	if this.GetModifiers() != 0 {
		modStr = GetString(Modifier_FvÞH3Q().ToString_r2zA1w_Í(this.GetModifiers())) + " "
	}
	return fmt.Sprintf("%s%s %s.%s", modStr, this.GetGenericType().GetTypeName_WhvcNw(),
		this.GetDeclaringClass().GetTypeName_WhvcNw(), this.GetName())
}

func (this *Field) ToString() string {
	modStr := ""
	if this.GetModifiers() != 0 {
		modStr = GetString(Modifier_FvÞH3Q().ToString_r2zA1w_Í(this.GetModifiers())) + " "
	}
	return fmt.Sprintf("%s%s %s.%s", modStr, this.GetType().GetTypeName_WhvcNw(),
		this.GetDeclaringClass().GetTypeName_WhvcNw(), this.GetName())
}

// Helpers

func (f *Field) checkAndGetParentStruct(obj Object_fAFaMw_Ñ) reflect.Value {
	if !Modifier_FvÞH3Q().IsStatic_IY2bÞw_Í(f.GetModifiers()) {
		Objects_dkj2tA().RequireNonNull_e1F5YQ_Í(obj)
	}
	if !f.cls.IsInstance_8Oe9Jw(obj) {
		ex := IllegalArgumentException_cljØMA().New()
		ex.Init_M13Ø3g(NewString("Not instance of declaring class"))
		panic(ex)
	}
	// Check accessibility
	if !f.overrideAccessible {
		inaccessible := Modifier_FvÞH3Q().IsPrivate_nH2mvA_Í(f.GetModifiers()) ||
			(modifierPackagePrivate(f.GetModifiers()) && !IsCallerSamePackage(f.cls))
		if inaccessible {
			ex := IllegalAccessException_ZÞ3j6Q().New()
			ex.Init_M13Ø3g(NewString("Field is inaccessible"))
			panic(ex)
		}
	}
	if Modifier_FvÞH3Q().IsStatic_IY2bÞw_Í(f.GetModifiers()) {
		f.cls.Raw_dvhEBA().Fwd_.init()
		return reflect.ValueOf(GetStaticRefFromClassName(GetString(obj.GetClass_9pp3sQ().GetName_uasY3Q())))
	} else {
		return reflect.ValueOf(obj)
	}
}

func (f *Field) getReflectValue(obj Object_fAFaMw_Ñ) reflect.Value {
	return f.checkAndGetParentStruct(obj).FieldByName(f.info.InternalName)
}

func (f *Field) setWithCallback(obj Object_fAFaMw_Ñ, valueFn func(v reflect.Value)) {
	parentStruct := f.checkAndGetParentStruct(obj)
	isExported := Modifier_FvÞH3Q().IsPublic_FOQp9g_Í(f.GetModifiers()) ||
		Modifier_FvÞH3Q().IsProtected_IroxZw_Í(f.GetModifiers())
	if Modifier_FvÞH3Q().IsStatic_IY2bÞw_Í(f.GetModifiers()) {
		// TODO: add setters for static fields?
		if !isExported {
			ex := IllegalAccessException_ZÞ3j6Q().New()
			ex.Init_M13Ø3g(NewString("Private and package-private static field setting not yet supported"))
			panic(ex)
		}
		valueFn(parentStruct.FieldByName(f.info.InternalName))
		return
	}
	// Use the setter on instance fields
	setterName := "set" + strings.ToTitle(f.info.InternalName)
	if isExported {
		setterName = strings.ToTitle(setterName)
	}
	params := []reflect.Value{zeroReflectValue(f.info.RawTypeClassName)}
	valueFn(params[0])
	parentStruct.MethodByName(setterName).Call(params)
}
