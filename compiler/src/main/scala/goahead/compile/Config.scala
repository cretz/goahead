package goahead.compile

case class Config(
  optimizeRecognizedInvokeDynamic: Boolean = true,
  localizeTempVarUsage: Boolean = false,
  reflectionSupport: Boolean = true
)
