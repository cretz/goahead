package main

import (
	"com.typesafe/config"
	"github.com/cretz/goahead/libs/java/rt"
	"testing"
)

func TestSimpleGet(t *testing.T) {
	conf := config.ConfigFactory_NpMidQ().Load_kbex6A_Í(rt.NewString("foo = bar"))
	str := rt.GetString(conf.GetString_oHE62Q(rt.NewString("foo")))
	if str != "wrong" {
		t.Logf("Fail: %v", str)
	}
}
