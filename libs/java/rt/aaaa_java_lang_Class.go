package rt

import (
	"fmt"
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

func (this *Class_Static) ForName(str String_g9YXBQ_Ñ) Class_dvhEBA_Ñ {
	// We would much rather doubly call GetClassInfo than put a full non-read lock on every access
	cls := this.readClass(str.Raw_g9YXBQ().Fwd_.str)
	if cls == nil {
		cls = this.loadClass(str.Raw_g9YXBQ().Fwd_.str)
	}
	if cls == nil {
		ex := NullPointerException_fnXÞLQ().New()
		ex.Init_M13Ø3g(NewString(fmt.Sprintf("Cannot find class %v", str.Raw_g9YXBQ().Fwd_.str)))
		panic(ex)
	}
	return cls
}

func (this *Class_Static) GetPrimitiveClass(var0 String_g9YXBQ_Ñ) Class_dvhEBA_Ñ {
	return this.ForName(NewString("<primitive>" + var0.Raw_g9YXBQ().Fwd_.str))
}

func (c *Class_Static) readClass(str string) *Class_dvhEBA_Í {
	c.lock.RLock()
	defer c.lock.RUnlock()
	return c.classCache[str]
}

// This is cheap for classes not present
func (c *Class_Static) loadClass(str string) *Class_dvhEBA_Í {
	cls := Class_dvhEBA().New()
	cls.Init_611f1A()
	if str[0] == '[' {
		// Since we got here, we know it's an object array (or array of arrays)
		componentName := str[1:]
		if componentName[0] == 'L' && componentName[len(componentName)-1] == ';' {
			componentName = componentName[1 : len(componentName)-1]
		}
		cls.Fwd_.info = &ClassInfo{
			Name:           str,
			ComponentClass: c.ForName(NewString(componentName)),
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

func (this *Class) Init() {}

func (this *Class) GetName() String_g9YXBQ_Ñ {
	return NewString(this.info.Name)
}
