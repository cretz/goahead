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
}

var classNameToStaticRef = map[string]ClassInfoProvider{}
var javaImplNameToClassName = map[string]string{}
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
