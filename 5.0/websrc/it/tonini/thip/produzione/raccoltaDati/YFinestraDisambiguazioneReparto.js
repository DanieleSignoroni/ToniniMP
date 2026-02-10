var sceltaDisplayed = false;
var gestione2 = false;
var bollaLavorazione = null;

function RilevDatiPrdTSOL() {
	parent.document.getElementById('Indietro').style.display = displayBlock;
	parent.document.getElementById('StampaBolla').style.display = displayNone;
	parent.document.getElementById('ConfermaStampa').style.display = displayNone;
	parent.document.getElementById('ProssimaAtvBut').style.display = displayNone;
	parent.document.getElementById('MonitorBut').style.display = displayNone;
}

function selectReparto(index) {
	var className = eval("document.forms[0].thClassName.value");
	eval("document.forms[0]." + idFromName['Note']).value = document.getElementById("IdReparto" + index).value;
	runActionDirect('FINESTRA_DISAMBIGUAZIONE_SCELTA_OPERATORE', 'action_submit', className, null, 'same', 'no');
}