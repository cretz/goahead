package rt

import (
	"encoding/base64"
	"encoding/binary"
	"path"
	"path/filepath"
	"reflect"
	"runtime"
	"strings"
	"sync"
)

type ClassInfoProvider interface {
	GetClassInfo() *ClassInfo
}

type simpleClassInfoProvider struct {
	*ClassInfo
}

func NewClassInfoProvider(ci *ClassInfo) ClassInfoProvider { return &simpleClassInfoProvider{ci} }

func (this *simpleClassInfoProvider) GetClassInfo() *ClassInfo { return this.ClassInfo }

type lazyClassInfoProvider struct {
	fn func() *ClassInfo
}

func NewLazyClassInfoProvider(fn func() *ClassInfo) ClassInfoProvider {
	return &lazyClassInfoProvider{fn}
}

func (this *lazyClassInfoProvider) GetClassInfo() *ClassInfo { return this.fn() }

type ClassInfo struct {
	Name           string
	ComponentClass Class_dvhEBA_Ñ
	Primitive      bool
	Init           func() interface{}
	// When false, nothing below is available
	Full bool

	Interface bool
	// Keyed by class name
	Annotations        map[string]Annotation_3r3c2w_Ñ
	EnclosingMethod    *EnclosingMethodInfo
	DeclaredClassNames []string
	// Can be empty
	DeclaringClassName string
	GenericSuperClass  TypeInfo
	GenericInterfaces  []TypeInfo
	InterfaceNames     []string
	Modifiers          int32
	SuperClassName     string
	TypeParams         []TypeVariableInfo
	Fields             []*FieldInfo
}

type EnclosingMethodInfo struct {
	ClassName string
	Name      string
	Desc      string
}

type TypeInfo interface {
	ToType() Type_T82HQA_Ñ
}

type TypeVariableInfo interface {
	ToTypeVariable() TypeVariable_E9F93A_Ñ
}

type PackageInfo struct {
	Name string
	Full bool
}

type FieldInfo struct {
	Name             string
	InternalName     string
	Annotations      map[string]Annotation_3r3c2w_Ñ
	RawTypeClassName string
	Modifiers        int32
}

var classNameToStaticRef = map[string]ClassInfoProvider{}
var javaImplNameToClassName = map[string]string{}
var packages = map[string]*PackageInfo{}
var reflectionLock = sync.RWMutex{}

func AddStaticRefs(classNameMap map[string]ClassInfoProvider, javaImplNameMap map[string]string) {
	reflectionLock.Lock()
	defer reflectionLock.Unlock()
	for className, prov := range classNameMap {
		classNameToStaticRef[className] = prov
	}
	for implName, className := range javaImplNameMap {
		javaImplNameToClassName[implName] = className
	}
}

func GetStaticRefFromClassName(className string) ClassInfoProvider {
	reflectionLock.RLock()
	reflectionLock.RUnlock()
	return classNameToStaticRef[className]
}

// Result can be empty string
func GetClassNameFromJavaImplName(javaImplName string) string {
	reflectionLock.RLock()
	reflectionLock.RUnlock()
	return javaImplNameToClassName[javaImplName]
}

func AddPackageInfo(packageName string, info *PackageInfo) {
	reflectionLock.Lock()
	defer reflectionLock.Unlock()
	if packages[packageName] != nil {
		// TODO: fix this be properly supporting class loaders
		panic("Package " + packageName + " was added a second time")
	}
	packages[packageName] = info
}

func GetPackageInfo(packageName string) *PackageInfo {
	reflectionLock.RLock()
	reflectionLock.RUnlock()
	return packages[packageName]
}

func init() {
	primRefs := map[string]ClassInfoProvider{}
	primRef := func(primName, arrayName string) {
		// Create prim and prim array
		primRefs["<primitive>"+primName] = NewClassInfoProvider(&ClassInfo{Name: primName, Primitive: true})
		if arrayName != "" {
			primRefs[arrayName] = NewLazyClassInfoProvider(func() *ClassInfo {
				return &ClassInfo{
					Name:           arrayName,
					ComponentClass: Class_dvhEBA().getPrimitiveClass_iMEklA_Í(NewString(primName)),
				}
			})
		}
	}
	primRef("boolean", "[Z")
	primRef("byte", "[B")
	primRef("char", "[C")
	primRef("short", "[S")
	primRef("int", "[I")
	primRef("long", "[J")
	primRef("float", "[F")
	primRef("double", "[D")
	primRef("void", "")
	AddStaticRefs(primRefs, nil)
}

var hashReplacer = strings.NewReplacer("+", "Þ", "/", "Ø")

func ReflectionStringHash(str string) string {
	b := make([]byte, 4)
	binary.BigEndian.PutUint32(b, uint32(NewString(str).HashCode_Gyq6fg()))
	return hashReplacer.Replace(base64.RawStdEncoding.EncodeToString(b))
}

func IsCallerSamePackage(cls Class_dvhEBA_Ñ) bool {
	// Just be lazy and compare the static ref's package path w/ the caller's dir

	// Get the file name of the caller of my caller
	_, file, _, ok := runtime.Caller(2)
	if !ok {
		panic("Cannot retrieve caller file name")
	}

	// We get the package path by obtaining the static ref of the object
	typ := reflect.TypeOf(GetStaticRefFromClassName(GetString(cls.GetName_uasY3Q())))
	// But we have to trim off anything past "/vendor/" per https://github.com/golang/go/issues/12739
	typPath := typ.PkgPath()
	if i := strings.LastIndex(typPath, "/vendor/"); i > -1 {
		typPath = typPath[8:]
	}

	// Compare
	return strings.HasSuffix(path.Dir(filepath.ToSlash(file)), typPath)
}

func modifierPackagePrivate(mod int32) bool {
	return !Modifier_FvÞH3Q().IsPublic_FOQp9g_Í(mod) &&
		!Modifier_FvÞH3Q().IsProtected_IroxZw_Í(mod) &&
		!Modifier_FvÞH3Q().IsPrivate_nH2mvA_Í(mod)
}

func reflectValueToObject(v reflect.Value, rawTypeClassName string) Object_fAFaMw_Ñ {
	if v.IsNil() {
		return nil
	}
	switch v.Kind() {
	case reflect.Bool:
		return Boolean_avGTtA().ValueOf_v6H1dQ_Í(v.Bool())
	case reflect.Int8:
		return Byte_DBhEØA().ValueOf_Imzeqw_Í(int8(v.Int()))
	case reflect.Int16:
		return Short_d9gfCA().ValueOf_UMVsGg_Í(int16(v.Int()))
	case reflect.Int32:
		if rawTypeClassName == "<primitive>char" {
			return Character_V2YvtQ().ValueOf_NZfFfQ_Í(rune(v.Int()))
		}
		return Integer_28uoyg().ValueOf_QE3YDg_Í(int32(v.Int()))
	case reflect.Int64:
		return Long_DByqcA().ValueOf_J1lSrw_Í(v.Int())
	case reflect.Float32:
		return Float_dyK86A().ValueOf_kKAu7Q_Í(float32(v.Float()))
	case reflect.Float64:
		return Double_afgyxQ().ValueOf_x4bUgA_Í(float64(v.Float()))
	default:
		return v.Interface().(Object_fAFaMw_Ñ)
	}
}

func box(v interface{}, rawTypeClassName string) Object_fAFaMw_Ñ {
	switch v := v.(type) {
	case bool:
		return Boolean_avGTtA().ValueOf_v6H1dQ_Í(v)
	case int8:
		return Byte_DBhEØA().ValueOf_Imzeqw_Í(v)
	case int16:
		return Short_d9gfCA().ValueOf_UMVsGg_Í(v)
	case int32:
		if rawTypeClassName == "<primitive>char" {
			return Character_V2YvtQ().ValueOf_NZfFfQ_Í(rune(v))
		}
		return Integer_28uoyg().ValueOf_QE3YDg_Í(v)
	case int64:
		return Long_DByqcA().ValueOf_J1lSrw_Í(v)
	case float32:
		return Float_dyK86A().ValueOf_kKAu7Q_Í(v)
	case float64:
		return Double_afgyxQ().ValueOf_x4bUgA_Í(v)
	default:
		panic("Not a primitive")
	}
}

func unbox(obj Object_fAFaMw_Ñ) interface{} {
	switch obj := obj.(type) {
	case Boolean_avGTtA_Ñ:
		return obj.BooleanValue_ElIpQQ()
	case Byte_DBhEØA_Ñ:
		return obj.ByteValue_JTFSCQ()
	case Short_d9gfCA_Ñ:
		return obj.ShortValue_D5uiDg()
	case Character_V2YvtQ_Ñ:
		return obj.CharValue_ykJX2A()
	case Integer_28uoyg_Ñ:
		return obj.IntValue_JOg89w()
	case Long_DByqcA_Ñ:
		return obj.LongValue_fXiphQ()
	case Float_dyK86A_Ñ:
		return obj.FloatValue_PaU7YQ()
	case Double_afgyxQ_Ñ:
		return obj.DoubleValue_4Yg7VA()
	default:
		panic("Not a primitive")
	}
}

func zeroReflectValue(rawTypeClassName string) reflect.Value {
	switch rawTypeClassName {
	case "<primitive>bool":
		return reflect.ValueOf(false)
	case "<primitive>byte":
		return reflect.ValueOf(int8(0))
	case "<primitive>char":
		return reflect.ValueOf(rune(0))
	case "<primitive>short":
		return reflect.ValueOf(int16(0))
	case "<primitive>int":
		return reflect.ValueOf(int32(0))
	case "<primitive>long":
		return reflect.ValueOf(int64(0))
	case "<primitive>float":
		return reflect.ValueOf(float32(0))
	case "<primitive>double":
		return reflect.ValueOf(float64(0))
	default:
		return reflect.ValueOf(nil)
	}
}

func setReflectValueFromObject(obj Object_fAFaMw_Ñ, setOn reflect.Value, rawTypeClassName string) {
	switch setOn.Kind() {
	case reflect.Bool:
		if v, ok := obj.(Boolean_avGTtA_Ñ); ok {
			setOn.SetBool(v.BooleanValue_ElIpQQ())
			return
		}
	case reflect.Float64:
		if v, ok := obj.(Double_afgyxQ_Ñ); ok {
			setOn.SetFloat(v.DoubleValue_4Yg7VA())
			return
		}
		fallthrough
	case reflect.Float32:
		if v, ok := obj.(Number_e1mSPQ_Ñ); ok {
			setOn.SetFloat(v.DoubleValue_4Yg7VA())
			return
		}
		if v, ok := obj.(Character_V2YvtQ_Ñ); ok {
			setOn.SetFloat(float64(v.CharValue_ykJX2A()))
			return
		}
	case reflect.Int64:
		if v, ok := obj.(Long_DByqcA_Ñ); ok {
			setOn.SetInt(v.LongValue_fXiphQ())
			return
		}
		fallthrough
	case reflect.Int32:
		if v, ok := obj.(Integer_28uoyg_Ñ); ok && rawTypeClassName != "<primitive>char" {
			setOn.SetInt(v.LongValue_fXiphQ())
			return
		}
		if v, ok := obj.(Character_V2YvtQ_Ñ); ok {
			setOn.SetInt(int64(v.CharValue_ykJX2A()))
			return
		}
		fallthrough
	case reflect.Int16:
		if v, ok := obj.(Short_d9gfCA_Ñ); ok {
			setOn.SetInt(v.LongValue_fXiphQ())
			return
		}
		fallthrough
	case reflect.Int8:
		if v, ok := obj.(Byte_DBhEØA_Ñ); ok {
			setOn.SetInt(v.LongValue_fXiphQ())
			return
		}
	case reflect.Ptr, reflect.Interface, reflect.Slice:
		setOn.Set(reflect.ValueOf(obj))
		return
	}
	ex := IllegalArgumentException_cljØMA().New()
	ex.Init_M13Ø3g(NewString("Unexpected target type to set via reflection"))
	panic(ex)
}

// Result can be nil
func getRepeatableValueFromAnn(ann Annotation_3r3c2w_Ñ) []Object_fAFaMw_Ñ {
	if v, ok := ann.(annWithObjectArrayValue); ok {
		return v.Value_OÞhXRA().Raw()
	}
	return nil
}

func getAnnotationsByTypeFromDeclared(this AnnotatedElement_XB1CSg_Ñ, cls Class_dvhEBA_Ñ) ObjectArray__Instance {
	// The best way, based on the spec, is to get all declared annotations and pick off
	// the one given and maybe its repeated form
	retArr := []Object_fAFaMw_Ñ{}
	var repeatable Repeatable_gJFvIQ_Ñ
	if r := cls.GetAnnotation_QS851A(Class_dvhEBA().ForName_xpGqyQ_Í(NewString("java.lang.annotation.Repeatable"))); r != nil {
		repeatable = r.(Repeatable_gJFvIQ_Ñ)
	}
	for _, v := range this.GetDeclaredAnnotations_Ssh3tw().Raw() {
		ann := v.(Annotation_3r3c2w_Ñ)
		if ann.AnnotationType_yjDfig().Equals_g011Rw(cls) {
			retArr = append(retArr, ann)
		} else if repeatable != nil && ann.AnnotationType_yjDfig().Equals_g011Rw(repeatable.Value_sETAIg()) {
			retArr = append(retArr, getRepeatableValueFromAnn(ann)...)
		}
	}
	return NewObjectArrayFromSlice(retArr, cls.GetName_uasY3Q().Raw_g9YXBQ().Fwd_.str)
}
