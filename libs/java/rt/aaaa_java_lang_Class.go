package rt

import (
	"fmt"
	"strings"
	"sync"
)

//goahead:forward-static java.lang.Class
type Class_Static struct {
	// This will never contain nil...a fresh lookup can happen every time, that's fine
	classCache map[string]*Class_dvhEBA_Í
	lock       sync.RWMutex
}

func (this *Class_Static) Init() {
	this.classCache = map[string]*Class_dvhEBA_Í{}
}

func (this *Class_Static) ForName_1(str string) Class_dvhEBA_Ñ {
	return this.ForName_2(str, true, nil)
}

func (this *Class_Static) ForName_2(str string, init bool, loader ClassLoader_4EhNNw_Ñ) Class_dvhEBA_Ñ {
	// TODO: care about the loader?
	// We would much rather doubly call GetClassInfo than put a full non-read lock on every access
	cls := this.readClass(str)
	if cls == nil {
		cls = this.loadClass(str, init)
		if init {
			cls.Raw_dvhEBA().Fwd_.init()
		}
	}
	if cls == nil {
		ex := ClassNotFoundException_LCk4ØA().New()
		ex.Init_M13Ø3g(NewString(fmt.Sprintf("Cannot find class %v", str)))
		panic(ex)
	}
	return cls
}

func (this *Class_Static) GetPrimitiveClass(var0 string) Class_dvhEBA_Ñ {
	return this.ForName_1("<primitive>" + var0)
}

func (c *Class_Static) readClass(str string) *Class_dvhEBA_Í {
	c.lock.RLock()
	defer c.lock.RUnlock()
	return c.classCache[str]
}

// This is cheap for classes not present
func (c *Class_Static) loadClass(str string, init bool) *Class_dvhEBA_Í {
	cls := Class_dvhEBA().New()
	cls.Init_611f1A()
	if str[0] == '[' && len(str) > 2 && (str[1] == 'L' || str[1] == '[') {
		// Either an array of arrays or an array of objects
		componentName := str[1:]
		if componentName[0] == 'L' && componentName[len(componentName)-1] == ';' {
			componentName = componentName[1 : len(componentName)-1]
		}
		componentCls := c.ForName_2(componentName, init, nil)
		cls.Fwd_.info = &ClassInfo{
			Name:           str,
			ComponentClass: componentCls,
			Init:           func() interface{} { return componentCls.Raw_dvhEBA().Fwd_.init() },
		}
	} else {
		prov := GetStaticRefFromClassName(str)
		if prov == nil {
			return nil
		}
		cls.Fwd_.info = prov.GetClassInfo()
	}
	c.lock.Lock()
	defer c.lock.Unlock()
	c.classCache[str] = cls
	return cls
}

//goahead:forward-instance java.lang.Class
type Class struct {
	impl *Class_dvhEBA_Í
	info *ClassInfo
}

func (this *Class) AsSubclass(cls Class_dvhEBA_Ñ) Class_dvhEBA_Ñ {
	if cls.IsAssignableFrom_IF3M2g(this.impl) {
		return this.impl
	}
	ex := ClassCastException_x20ÞLA().New()
	ex.Init_M13Ø3g(this.impl.ToString_aÞ4cSA())
	panic(ex)
}

func (this *Class) Cast(obj Object_fAFaMw_Ñ) Object_fAFaMw_Ñ {
	if obj != nil && !this.IsInstance(obj) {
		ex := ClassCastException_x20ÞLA().New()
		ex.Init_M13Ø3g(NewString(fmt.Sprintf("Cannot cast %v to %v", obj.GetClass_9pp3sQ().GetName_uasY3Q(), this.GetName())))
		panic(ex)
	}
	return obj
}

func (this *Class) DesiredAssertionStatus() bool {
	// TODO: make this configurable
	// TODO: use java's AtomicBoolean?
	return true
}

func (this *Class) GetAnnotatedInterfaces() ObjectArray__Instance {
	// TODO
	panic("Method not implemented - java/lang/Class::getAnnotatedInterfaces()[Ljava/lang/reflect/AnnotatedType;")
}

func (this *Class) GetAnnotatedSuperclass() AnnotatedType_gjnerA_Ñ {
	// TODO
	panic("Method not implemented - java/lang/Class::getAnnotatedSuperclass()Ljava/lang/reflect/AnnotatedType;")
}

func (this *Class) GetAnnotation(cls Class_dvhEBA_Ñ) Annotation_3r3c2w_Ñ {
	if ann := this.GetDeclaredAnnotation(cls); ann != nil {
		return ann
	}
	// Try parent if inherited
	if cls.IsAnnotationPresent_xiFZfw(Class_dvhEBA().ForName_xpGqyQ_Í(NewString("java.lang.annotation.Inherited"))) {
		if super := this.GetSuperclass(); super != nil {
			return super.GetAnnotation_QS851A(cls)
		}
	}
	return nil
}

func (this *Class) GetAnnotations() ObjectArray__Instance {
	// Get my parents and override
	this.assertFullInfo()
	anns := this.getAnnotationMap(false)
	arr := NewObjectArray(int32(len(anns)), "java.lang.Annotation")
	var index int32
	for _, v := range anns {
		arr.Set(index, v)
		index++
	}
	return arr
}

func (this *Class) GetAnnotationsByType(cls Class_dvhEBA_Ñ) ObjectArray__Instance {
	ret := this.GetDeclaredAnnotationsByType(cls)
	if ret.Len() == 0 {
		// Try parent if inherited
		if cls.IsAnnotationPresent_xiFZfw(Class_dvhEBA().ForName_xpGqyQ_Í(NewString("java.lang.annotation.Inherited"))) {
			if super := this.GetSuperclass(); super != nil {
				ret = super.GetAnnotationsByType_yi7WcQ(cls)
			}
		}
	}
	return ret
}

func (this *Class) GetCanonicalName() String_g9YXBQ_Ñ {
	if this.IsArray() {
		comp := this.GetComponentType().GetCanonicalName_ScNLxQ()
		if comp == nil {
			return nil
		}
		return NewString(GetString(comp.Raw_g9YXBQ()) + "[]")
	}
	if this.info.EnclosingMethod == nil {
		return nil
	}
	enclosing := this.GetEnclosingClass()
	if enclosing == nil {
		return NewString(this.GetName())
	}
	encName := enclosing.GetCanonicalName_ScNLxQ()
	if encName == nil {
		return nil
	}
	return NewString(GetString(encName) + "." + this.GetSimpleName())
}

func (this *Class) GetClassLoader() ClassLoader_4EhNNw_Ñ {
	return nil
}

func (this *Class) GetClasses() ObjectArray__Instance {
	// Get the ones from the parent and add my declared ones
	ret := []Object_fAFaMw_Ñ{}
	for _, v := range this.GetDeclaredClasses().Raw() {
		declCls := v.(Class_dvhEBA_Ñ)
		if Modifier_FvÞH3Q().IsPublic_FOQp9g_Í(declCls.GetModifiers_fmwxcw()) {
			ret = append(ret, v)
		}
	}
	if super := this.GetSuperclass(); super != nil {
		ret = append(ret, super.GetClasses_tYY7yg().Raw()...)
	}
	return NewObjectArrayFromSlice(ret, "java.lang.Class")
}

func (this *Class) GetComponentType() Class_dvhEBA_Ñ {
	return this.info.ComponentClass
}

func (this *Class) GetConstructor(var0 ObjectArray__Instance) Constructor_T7vtNA_Ñ {
	// TODO
	panic("Method not implemented - java/lang/Class::getConstructor([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;")
}

func (this *Class) GetConstructors() ObjectArray__Instance {
	// TODO
	panic("Method not implemented - java/lang/Class::getConstructors()[Ljava/lang/reflect/Constructor;")
}

func (this *Class) GetDeclaredAnnotation(cls Class_dvhEBA_Ñ) Annotation_3r3c2w_Ñ {
	this.assertFullInfo()
	Objects_dkj2tA().RequireNonNull_e1F5YQ_Í(cls)
	return this.info.Annotations[cls.Raw_dvhEBA().Fwd_.info.Name]
}

func (this *Class) GetDeclaredAnnotations() ObjectArray__Instance {
	this.assertFullInfo()
	arr := NewObjectArray(int32(len(this.info.Annotations)), "java.lang.Annotation")
	var index int32
	for _, v := range this.info.Annotations {
		arr.Set(index, v)
		index++
	}
	return arr
}

func (this *Class) GetDeclaredAnnotationsByType(cls Class_dvhEBA_Ñ) ObjectArray__Instance {
	this.assertFullInfo()
	return getAnnotationsByTypeFromDeclared(this.impl, cls)
}

func (this *Class) GetDeclaredClasses() ObjectArray__Instance {
	this.assertFullInfo()
	ret := NewObjectArray(int32(len(this.info.DeclaredClassNames)), "java.lang.Class")
	for i, v := range this.info.DeclaredClassNames {
		ret.Set(int32(i), Class_dvhEBA().ForName_xpGqyQ_Í(NewString(v)))
	}
	return ret
}

func (this *Class) GetDeclaredConstructor(var0 ObjectArray__Instance) Constructor_T7vtNA_Ñ {
	// TODO
	panic("Method not implemented - java/lang/Class::getDeclaredConstructor([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;")
}

func (this *Class) GetDeclaredConstructors() ObjectArray__Instance {
	// TODO
	panic("Method not implemented - java/lang/Class::getDeclaredConstructors()[Ljava/lang/reflect/Constructor;")
}

func (this *Class) GetDeclaredField(var0 String_g9YXBQ_Ñ) Field_qRaptA_Ñ {
	// TODO
	panic("Method not implemented - java/lang/Class::getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;")
}

func (this *Class) GetDeclaredFields() ObjectArray__Instance {
	// TODO
	panic("Method not implemented - java/lang/Class::getDeclaredFields()[Ljava/lang/reflect/Field;")
}

func (this *Class) GetDeclaredMethod(var0 String_g9YXBQ_Ñ, var1 ObjectArray__Instance) Method_hX7fZw_Ñ {
	// TODO
	panic("Method not implemented - java/lang/Class::getDeclaredMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;")
}

func (this *Class) GetDeclaredMethods() ObjectArray__Instance {
	// TODO
	panic("Method not implemented - java/lang/Class::getDeclaredMethods()[Ljava/lang/reflect/Method;")
}

func (this *Class) GetDeclaringClass() Class_dvhEBA_Ñ {
	this.assertFullInfo()
	if this.info.DeclaringClassName == "" {
		return nil
	}
	return Class_dvhEBA().ForName_xpGqyQ_Í(NewString(this.info.DeclaringClassName))
}

func (this *Class) GetEnclosingClass() Class_dvhEBA_Ñ {
	this.assertFullInfo()
	if this.info.EnclosingMethod == nil {
		return this.GetDeclaringClass()
	}
	return Class_dvhEBA().ForName_xpGqyQ_Í(NewString(this.info.EnclosingMethod.ClassName))
}

func (this *Class) GetEnclosingConstructor() Constructor_T7vtNA_Ñ {
	// TODO
	panic("Method not implemented - java/lang/Class::getEnclosingConstructor()Ljava/lang/reflect/Constructor;")
}

func (this *Class) GetEnclosingMethod() Method_hX7fZw_Ñ {
	// TODO
	panic("Method not implemented - java/lang/Class::getEnclosingMethod()Ljava/lang/reflect/Method;")
}

func (this *Class) GetEnumConstants() ObjectArray__Instance {
	this.assertFullInfo()
	if !this.IsEnum() {
		return nil
	}
	return GetStaticRefFromClassName(this.info.Name).(staticInstWithEnumConsts).Values_0sHx2g_Í()
}

func (this *Class) GetField(var0 String_g9YXBQ_Ñ) Field_qRaptA_Ñ {
	// TODO
	panic("Method not implemented - java/lang/Class::getField(Ljava/lang/String;)Ljava/lang/reflect/Field;")
}

func (this *Class) GetFields() ObjectArray__Instance {
	// TODO
	panic("Method not implemented - java/lang/Class::getFields()[Ljava/lang/reflect/Field;")
}

func (this *Class) GetGenericInterfaces() ObjectArray__Instance {
	this.assertFullInfo()
	if this.info.GenericInterfaces == nil {
		return this.GetInterfaces()
	}
	ret := NewObjectArray(int32(len(this.info.GenericInterfaces)), "java.lang.reflect.Type")
	for i, v := range this.info.GenericInterfaces {
		ret.Set(int32(i), v.ToType())
	}
	return ret
}

func (this *Class) GetGenericSuperclass() Type_T82HQA_Ñ {
	this.assertFullInfo()
	if this.info.GenericSuperClass == nil {
		return this.GetSuperclass()
	}
	return this.info.GenericSuperClass.ToType()
}

func (this *Class) GetInterfaces() ObjectArray__Instance {
	this.assertFullInfo()
	ret := NewObjectArray(int32(len(this.info.InterfaceNames)), "java.lang.Class")
	for i, v := range this.info.InterfaceNames {
		ret.Set(int32(i), Class_dvhEBA().ForName_xpGqyQ_Í(NewString(v)))
	}
	return ret
}

func (this *Class) GetMethod(var0 String_g9YXBQ_Ñ, var1 ObjectArray__Instance) Method_hX7fZw_Ñ {
	// TODO
	panic("Method not implemented - java/lang/Class::getMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;")
}

func (this *Class) GetMethods() ObjectArray__Instance {
	// TODO
	panic("Method not implemented - java/lang/Class::getMethods()[Ljava/lang/reflect/Method;")
}

func (this *Class) GetModifiers() int32 {
	this.assertFullInfo()
	return this.info.Modifiers
}

func (this *Class) GetName() string {
	return this.info.Name
}

func (this *Class) GetPackage() Package_Nvtb0g_Ñ {
	pkgName := this.GetPackageName()
	if pkgName == nil {
		return nil
	}
	return Package_Nvtb0g().GetPackage_uJv23w_Í(pkgName)
}

func (this *Class) GetPackageName() String_g9YXBQ_Ñ {
	if this.IsArray() || this.IsPrimitive() {
		return nil
	}
	lastDot := strings.LastIndex(this.info.Name, ".")
	if lastDot == -1 {
		return NewString("")
	}
	return NewString(this.info.Name[:lastDot])
}

func (this *Class) GetResourceAsStream(cls String_g9YXBQ_Ñ) InputStream_kz6r7g_Ñ {
	// TODO: think about this wrt "resource providers" that can be embedded or on disk
	panic("Method not implemented - java/lang/Class::getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;")
}

func (this *Class) GetSigners() ObjectArray__Instance {
	// TODO: figure out how to pass along signed jar info at runtime
	panic("Method not implemented - java/lang/Class::getSigners()[Ljava/lang/Object;")
}

func (this *Class) GetSimpleName() string {
	if this.IsArray() {
		return GetString(this.GetComponentType().GetSimpleName_dØwdLw()) + "[]"
	}
	if !this.info.Full {
		// Just after the last dot or last dollar sign w/ numbers trimmed is fine for no reflection
		last := strings.LastIndex(this.info.Name, "$.")
		if last == -1 || this.info.Name[last] == '.' {
			return this.info.Name[last+1:]
		}
		return strings.TrimLeft(this.info.Name[last+1:], "0123456789")
	}
	enc := this.GetEnclosingClass()
	if enc == nil {
		n := this.GetName()
		return n[strings.LastIndex(n, ".")+1:]
	}
	// Trim numbers from left
	simpleBinaryName := this.info.Name[enc.GetName_uasY3Q().Length_BadcUw():]
	return strings.TrimLeft(simpleBinaryName[1:], "0123456789")
}

func (this *Class) GetSuperclass() Class_dvhEBA_Ñ {
	if this.info.SuperClassName == "" {
		return nil
	}
	return Class_dvhEBA().ForName_xpGqyQ_Í(NewString(this.info.SuperClassName))
}

func (this *Class) GetTypeName() string {
	if this.IsArray() {
		var cl Class_dvhEBA_Ñ = this.impl
		dims := 0
		for ; cl.IsArray_daNnWw(); dims++ {
			cl = cl.GetComponentType_yNalcg()
		}
		return cl.Raw_dvhEBA().Fwd_.info.Name + strings.Repeat("[]", dims)
	}
	return this.GetName()
}

func (this *Class) GetTypeParameters() ObjectArray__Instance {
	ret := NewObjectArray(int32(len(this.info.TypeParams)), "java.lang.reflect.TypeVariable")
	for i, v := range this.info.TypeParams {
		ret.Set(int32(i), v.ToTypeVariable())
	}
	return ret
}

func (this *Class) IsAnnotation() bool {
	return this.GetModifiers()&0x00002000 != 0
}

func (this *Class) IsAnnotationPresent(cls Class_dvhEBA_Ñ) bool {
	return this.GetAnnotation(cls) != nil
}

func (this *Class) IsAnonymousClass() bool {
	return this.GetSimpleName() == ""
}

func (this *Class) IsArray() bool {
	return this.info.ComponentClass != nil
}

func (this *Class) IsAssignableFrom(cls Class_dvhEBA_Ñ) bool {
	// TODO: test all of this
	otherInfo := cls.Raw_dvhEBA().Fwd_.info
	if this.info.Name == otherInfo.Name {
		return true
	}
	if this.IsPrimitive() || cls.IsPrimitive_Þn1qDQ() {
		return false
	}
	if this.info.Name == "java.lang.Object" {
		return true
	}
	if cls.IsArray_daNnWw() {
		return this.info.Name == "java.lang.Object" ||
			this.info.Name == "java.lang.Cloneable" ||
			this.info.Name == "java.io.Serializable" ||
			(this.IsArray() && this.GetComponentType().IsAssignableFrom_IF3M2g(cls.GetComponentType_yNalcg()))
	}
	// Check all super types and their interfaces
	currCls := cls
	for currCls != nil {
		// Check the class
		if currCls.Raw_dvhEBA().Fwd_.info.Name == this.info.Name {
			return true
		}
		// Check all interfaces if I am an interface
		if this.IsInterface() && hasInterfaceName(currCls, this.info.Name) {
			return true
		}
		currCls = currCls.GetSuperclass_CqKÞgA()
	}
	return false
}

func (this *Class) IsEnum() bool {
	return this.GetModifiers()&0x00004000 != 0 &&
		this.GetSuperclass().Equals_g011Rw(Class_dvhEBA().ForName_xpGqyQ_Í(NewString("java.lang.Enum")))
}

func (this *Class) IsInstance(obj Object_fAFaMw_Ñ) bool {
	return obj != nil && this.IsAssignableFrom(obj.GetClass_9pp3sQ())
}

func (this *Class) IsInterface() bool {
	return this.info.Interface
}

func (this *Class) IsLocalClass() bool {
	return this.GetEnclosingMethod() != nil && !this.IsAnonymousClass()
}

func (this *Class) IsMemberClass() bool {
	return this.GetEnclosingClass() != nil && this.GetEnclosingMethod() == nil
}

func (this *Class) IsPrimitive() bool {
	return this.info.Primitive
}

func (this *Class) IsSynthetic() bool {
	return this.GetModifiers()&0x00001000 != 0
}

func (this *Class) NewInstance() Object_fAFaMw_Ñ {
	// TODO: get constructor and call it
	panic("Method not implemented - java/lang/Class::newInstance()Ljava/lang/Object;")
}

func (this *Class) ToGenericString() string {
	if this.IsPrimitive() {
		return this.ToString()
	}
	ret := ""
	mods := this.GetModifiers() & Modifier_FvÞH3Q().ClassModifiers_TkyFlQ_Í()
	if mods != 0 {
		ret += GetString(Modifier_FvÞH3Q().ToString_r2zA1w_Í(mods)) + " "
	}
	if this.IsAnnotation() {
		ret += "@"
	}
	if this.IsInterface() {
		ret += "interface"
	} else if this.IsEnum() {
		ret += "enum"
	} else {
		ret += "class"
	}
	ret += " " + this.GetName()
	typeParams := this.GetTypeParameters()
	if typeParams.Len() > 0 {
		pieces := make([]string, int(typeParams.Len()))
		for i, v := range typeParams.Raw() {
			pieces[i] = GetString(v.(TypeVariable_E9F93A_Ñ).GetTypeName_WhvcNw())
		}
		ret += "<" + strings.Join(pieces, ",") + ">"
	}
	return ret
}

func (this *Class) ToString() string {
	if this.IsInterface() {
		return "interface " + this.GetName()
	}
	if this.IsPrimitive() {
		return this.GetName()
	}
	return "class " + this.GetName()
}

// Helpers

func (c *Class) assertFullInfo() {
	if !c.info.Full {
		ex := ReflectiveOperationException_kIwG9w().New()
		ex.Init_M13Ø3g(NewString("Full reflection not enabled, unable to get details for " + c.info.Name))
		panic(ex)
	}
}

// This includes inherited ones
func (c *Class) getAnnotationMap(onlyInherited bool) map[string]Annotation_3r3c2w_Ñ {
	var ret map[string]Annotation_3r3c2w_Ñ
	if super := c.GetSuperclass(); super != nil {
		ret = super.Raw_dvhEBA().Fwd_.getAnnotationMap(true)
	} else {
		ret = map[string]Annotation_3r3c2w_Ñ{}
	}
	for k, v := range c.info.Annotations {
		if !onlyInherited || v.AnnotationType_yjDfig().IsAnnotationPresent_xiFZfw(Class_dvhEBA().ForName_xpGqyQ_Í(NewString("java.lang.annotation.Inherited"))) {
			ret[k] = v
		}
	}
	return ret
}

func (c *Class) init() interface{} {
	if c.info.Init == nil {
		return nil
	}
	return c.info.Init()
}

type annWithObjectArrayValue interface {
	AnnotationType_yjDfig() Class_dvhEBA_Ñ
	Value_OÞhXRA() ObjectArray__Instance
}

type staticInstWithEnumConsts interface {
	Values_0sHx2g_Í() ObjectArray__Instance
}

// Only traverses super interfaces, not super classes
func hasInterfaceName(cls Class_dvhEBA_Ñ, name string) bool {
	for _, v := range cls.GetInterfaces_iz86Kg().Raw() {
		ifaceCls := v.(Class_dvhEBA_Ñ)
		if ifaceCls.Raw_dvhEBA().Fwd_.info.Name == name || hasInterfaceName(ifaceCls, name) {
			return true
		}
	}
	return false
}
