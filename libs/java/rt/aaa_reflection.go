package rt

import "sync"

type ClassInfoProvider interface {
	GetClassInfo() *ClassInfo
}

type ClassInfo struct {
	Name string
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