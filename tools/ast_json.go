package main

import (
	"encoding/json"
	"fmt"
	"go/ast"
	"go/importer"
	"go/parser"
	"go/token"
	"go/types"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
	"runtime"
	"strconv"
	"strings"
)

func main() {
	if len(os.Args) != 2 {
		log.Fatal("Expected single arg for package")
	}
	byts, err := run(os.Args[1])
	if err == nil {
		_, err = os.Stdout.Write(byts)
	}
	if err != nil {
		log.Fatal(err)
	}
}

func run(pkgName string) ([]byte, error) {
	pkg, err := loadPackage(pkgName)
	if err != nil {
		return nil, err
	}
	return json.MarshalIndent(pkg, "", "  ")
}

func loadPackage(pkgName string) (*Package, error) {
	dir, err := findPkgDir(pkgName)
	if err != nil {
		return nil, err
	}
	files, err := ioutil.ReadDir(dir)
	if err != nil {
		return nil, err
	}
	fset := token.NewFileSet()
	var astFiles = []*ast.File{}
	for _, file := range files {
		if strings.HasSuffix(file.Name(), ".go") {
			f, err := parser.ParseFile(fset, filepath.Join(dir, file.Name()), nil, 0)
			if err != nil {
				return nil, fmt.Errorf("Unable to parse %v: %v", file.Name(), err)
			}
			astFiles = append(astFiles, f)
		}
	}

	conf := types.Config{
		IgnoreFuncBodies: true,
		FakeImportC:      true,
		Importer:         importer.Default(),
	}
	checkedPkg, err := conf.Check(pkgName, fset, astFiles, nil)
	if err != nil {
		return nil, fmt.Errorf("Type check failed: %v", err)
	}
	return toPackage(checkedPkg), nil
}

func findPkgDir(pkg string) (string, error) {
	possiblePaths := append(filepath.SplitList(os.Getenv("GOPATH")), runtime.GOROOT())
	for _, path := range possiblePaths {
		fullPath := filepath.Join(path, "src", filepath.FromSlash(pkg))
		if info, err := os.Stat(fullPath); os.IsNotExist(err) {
			continue
		} else if err != nil {
			return "", fmt.Errorf("Failed checking path: %v", err)
		} else if !info.IsDir() {
			return "", fmt.Errorf("Path not a directory")
		}
		return fullPath, nil
	}
	return "", fmt.Errorf("Unable to find pkg '%v' after checking in %v", pkg, possiblePaths)
}

func toPackage(pkg *types.Package) *Package {
	return &Package{
		Name:  pkg.Name(),
		Decls: toDecls(pkg.Scope()),
	}
}

func toDecls(scope *types.Scope) []Decl {
	ret := []Decl{}
	for _, name := range scope.Names() {
		if ast.IsExported(name) {
			ret = append(ret, toDecl(scope.Lookup(name)))
		}
	}
	return ret
}

func toDecl(obj types.Object) Decl {
	switch obj := obj.(type) {
	case *types.Const:
		value := obj.Val().ExactString()
		if basic, ok := obj.Type().(*types.Basic); ok && basic.Info()&types.IsString != 0 {
			var err error
			if value, err = strconv.Unquote(value); err != nil {
				panic(fmt.Errorf("Value not quoted: %v", err))
			}
		}
		return &ConstDecl{
			Kind:  "const",
			Name:  obj.Name(),
			Typ:   toType(obj.Type()),
			Value: value,
		}
	case *types.Var:
		return &VarDecl{
			Kind: "var",
			Name: obj.Name(),
			Typ:  toType(obj.Type()),
		}
	case *types.Func:
		sig := obj.Type().(*types.Signature)
		return &FuncDecl{
			Kind:     "func",
			Name:     obj.Name(),
			Params:   toNamedTypes(sig.Params()),
			Results:  toNamedTypes(sig.Results()),
			Variadic: sig.Variadic(),
		}
	case *types.TypeName:
		switch typ := obj.Type().Underlying().(type) {
		case *types.Struct:
			return &StructDecl{
				Kind:    "struct",
				Name:    obj.Name(),
				Fields:  toFields(typ),
				Methods: toMethods(typ),
			}
		case *types.Interface:
			return &IfaceDecl{
				Kind:     "iface",
				Name:     obj.Name(),
				Embedded: toEmbedded(typ),
				Methods:  toInterfaceMethods(typ),
			}
		default:
			return &AliasDecl{
				Kind:    "alias",
				Name:    obj.Name(),
				Typ:     toType(typ),
				Methods: toMethods(typ),
			}
		}
	default:
		panic(fmt.Sprintf("Unknown typ: %v", obj))
	}
}

func maybeString(str string) *string {
	if str == "" {
		return nil
	} else {
		return &str
	}
}

func toFields(typ *types.Struct) []*TypeWithMaybeName {
	ret := []*TypeWithMaybeName{}
	for i := 0; i < typ.NumFields(); i++ {
		f := typ.Field(i)
		if f.Exported() {
			ret = append(ret, &TypeWithMaybeName{
				Name: maybeString(f.Name()),
				Typ:  toType(f.Type()),
			})
		}
	}
	return ret
}

func toMethods(typ types.Type) []*Method {
	ret := []*Method{}
	set := types.NewMethodSet(types.NewPointer(typ))
	for i := 0; i < set.Len(); i++ {
		if m := set.At(i); m.Obj().Exported() {
			sig := m.Type().(*types.Signature)
			_, ptr := sig.Recv().Type().(*types.Pointer)
			ret = append(ret, &Method{
				Name:     m.Obj().Name(),
				Pointer:  ptr,
				Params:   toNamedTypes(sig.Params()),
				Results:  toNamedTypes(sig.Results()),
				Variadic: sig.Variadic(),
			})
		}
	}
	return ret
}

func toInterfaceMethods(typ *types.Interface) []*Method {
	ret := []*Method{}
	set := types.NewMethodSet(typ)
	for i := 0; i < set.Len(); i++ {
		if m := set.At(i); m.Obj().Exported() {
			sig := m.Type().(*types.Signature)
			ret = append(ret, &Method{
				Name:     m.Obj().Name(),
				Pointer:  false,
				Params:   toNamedTypes(sig.Params()),
				Results:  toNamedTypes(sig.Results()),
				Variadic: sig.Variadic(),
			})
		}
	}
	return ret
}

func toEmbedded(typ *types.Interface) []Type {
	ret := []Type{}
	for i := 0; i < typ.NumEmbeddeds(); i++ {
		em := typ.Embedded(i)
		if em.Obj().Exported() {
			if em.Obj().Name() != "" {
				panic(fmt.Sprintf("Expected nameless interface for embedding in %v", typ))
			}
			ret = append(ret, toType(em.Obj().Type()))
		}
	}
	return ret
}

func toNamedTypes(tuple *types.Tuple) []*TypeWithMaybeName {
	ret := []*TypeWithMaybeName{}
	for i := 0; i < tuple.Len(); i++ {
		t := tuple.At(i)
		ret = append(ret, &TypeWithMaybeName{
			Name: maybeString(t.Name()),
			Typ:  toType(t.Type()),
		})
	}
	return ret
}

func toType(typ types.Type) Type {
	switch typ := typ.(type) {
	case *types.Basic:
		if typ.Kind() == types.UnsafePointer {
			return &QualifiedType{
				Kind: "qualified",
				Pkg:  maybeString("unsafe"),
				Name: "Pointer",
			}
		}
		var name = typ.Name()
		if strings.HasPrefix(name, "untyped ") {
			name = name[8:]
		}
		return &BasicType{
			Kind:    "basic",
			Name:    name,
			Untyped: typ.Info()&types.IsUntyped != 0,
		}
	case *types.Array:
		return &ArrayType{
			Kind: "array",
			Size: typ.Len(),
			Typ:  toType(typ.Elem()),
		}
	case *types.Slice:
		return &SliceType{
			Kind: "slice",
			Typ:  toType(typ.Elem()),
		}
	case *types.Struct:
		return &StructType{
			Kind:    "struct",
			Fields:  toFields(typ),
			Methods: toMethods(typ),
		}
	case *types.Pointer:
		return &PointerType{
			Kind: "pointer",
			Typ:  toType(typ.Elem()),
		}
	case *types.Tuple:
		panic("Unexpected tuple type")
	case *types.Signature:
		return &FuncType{
			Kind:     "func",
			Params:   toNamedTypes(typ.Params()),
			Results:  toNamedTypes(typ.Results()),
			Variadic: typ.Variadic(),
		}
	case *types.Interface:
		return &IfaceType{
			Kind:     "iface",
			Embedded: toEmbedded(typ),
			Methods:  toInterfaceMethods(typ),
		}
	case *types.Map:
		return &MapType{
			Kind:  "map",
			Key:   toType(typ.Key()),
			Value: toType(typ.Elem()),
		}
	case *types.Chan:
		return &ChanType{
			Kind:    "chan",
			Send:    typ.Dir() == types.SendOnly || typ.Dir() == types.SendRecv,
			Receive: typ.Dir() == types.RecvOnly || typ.Dir() == types.SendRecv,
		}
	case *types.Named:
		var pkgName *string
		if pkg := typ.Obj().Pkg(); pkg != nil {
			pkgName = maybeString(pkg.Name())
		}
		return &QualifiedType{
			Kind: "qualified",
			Pkg:  pkgName,
			Name: typ.Obj().Name(),
		}
	default:
		panic(fmt.Sprintf("Unrecognized type: %v", typ))
	}
}

type Package struct {
	Name  string
	Decls []Decl
}

type Decl interface {
	decl()
}

type ConstDecl struct {
	Kind  string
	Name  string
	Typ   Type
	Value string
}

func (*ConstDecl) decl() {}

type VarDecl struct {
	Kind string
	Name string
	Typ  Type
}

func (*VarDecl) decl() {}

type FuncDecl struct {
	Kind     string
	Name     string
	Params   []*TypeWithMaybeName
	Results  []*TypeWithMaybeName
	Variadic bool
}

func (*FuncDecl) decl() {}

type StructDecl struct {
	Kind    string
	Name    string
	Fields  []*TypeWithMaybeName
	Methods []*Method
}

func (*StructDecl) decl() {}

type IfaceDecl struct {
	Kind     string
	Name     string
	Embedded []Type
	Methods  []*Method
}

func (*IfaceDecl) decl() {}

type AliasDecl struct {
	Kind    string
	Name    string
	Typ     Type
	Methods []*Method
}

func (*AliasDecl) decl() {}

type Method struct {
	Name     string
	Pointer  bool
	Params   []*TypeWithMaybeName
	Results  []*TypeWithMaybeName
	Variadic bool
}

type TypeWithMaybeName struct {
	Name *string
	Typ  Type
}

type Type interface {
	typ()
}

type QualifiedType struct {
	Kind string
	Pkg  *string
	Name string
}

func (*QualifiedType) typ() {}

type FuncType struct {
	Kind     string
	Params   []*TypeWithMaybeName
	Results  []*TypeWithMaybeName
	Variadic bool
}

func (*FuncType) typ() {}

type BasicType struct {
	Kind    string
	Name    string
	Untyped bool
}

func (*BasicType) typ() {}

type ArrayType struct {
	Kind string
	Size int64
	Typ  Type
}

func (*ArrayType) typ() {}

type SliceType struct {
	Kind string
	Typ  Type
}

func (*SliceType) typ() {}

type StructType struct {
	Kind    string
	Fields  []*TypeWithMaybeName
	Methods []*Method
}

func (*StructType) typ() {}

type PointerType struct {
	Kind string
	Typ  Type
}

func (*PointerType) typ() {}

type IfaceType struct {
	Kind     string
	Embedded []Type
	Methods  []*Method
}

func (*IfaceType) typ() {}

type MapType struct {
	Kind  string
	Key   Type
	Value Type
}

func (*MapType) typ() {}

type ChanType struct {
	Kind    string
	Typ     Type
	Send    bool
	Receive bool
}

func (*ChanType) typ() {}

type TestIFace interface {
	SomeMethod() error
}
