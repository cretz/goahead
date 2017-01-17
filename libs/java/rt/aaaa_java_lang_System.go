package rt

import (
	"os"
	"log"
)

func System_StaticInit(this *System_hB0pIw_Ś) {
	ps := PrintStream_kZ4QkQ().New()
	ps.Init_iYrehg(WriterToOutputStream(os.Stdout))
	this.Out_Øj4tRQ = ps
	log.Println("System initialized")
}
