package rt

import "sync"

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
