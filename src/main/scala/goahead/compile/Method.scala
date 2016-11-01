package goahead.compile

case class Method(
  imports: Map[String, String],
  sections: Seq[LabelSection]
)