package it.softre.thip.produzione.raccoltaDati.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspWriter;

import com.thera.thermfw.base.ResourceLoader;
import com.thera.thermfw.base.Trace;
import com.thera.thermfw.persist.CachedStatement;
import com.thera.thermfw.persist.KeyHelper;
import com.thera.thermfw.persist.PersistentObject;
import com.thera.thermfw.type.DateType;
import com.thera.thermfw.web.WebElement;
import com.thera.thermfw.web.WebJSTypeList;

import it.softre.thip.produzione.raccoltaDati.YPassDatiMacc;
import it.thera.thip.base.azienda.Azienda;
import it.thera.thip.base.azienda.Reparto;
import it.thera.thip.base.generale.ParametroPsn;
import it.thera.thip.base.risorse.Risorsa;
import it.thera.thip.produzione.ordese.AttivitaEsecMateriale;
import it.thera.thip.produzione.ordese.AttivitaEsecRisorsa;
import it.thera.thip.produzione.ordese.AttivitaEsecutiva;
import it.thera.thip.produzione.ordese.OrdineEsecutivo;
import it.thera.thip.produzione.ordese.PersDatiPrdCausaleRilev;
import it.thera.thip.produzione.ordese.PersDatiPrdUtenteRilev;
import it.thera.thip.produzione.raccoltaDati.AmbienteListaAttivita;
import it.thera.thip.produzione.raccoltaDati.BollaCucitaTes;
import it.thera.thip.produzione.raccoltaDati.ListaAttivita;
import it.thera.thip.produzione.raccoltaDati.RilevDatiPrdTS;
import it.thera.thip.produzione.raccoltaDati.RilevazioneDatiProdTes;
import it.thera.thip.produzione.raccoltaDati.web.RilevDatiPrdTSFormActionAdapter;
import it.thera.thip.produzione.raccoltaDati.web.RilevDatiPrdTSWebFormModifier;
import it.tonini.thip.produzione.raccoltaDati.YRilevDatiPrdTS;
import it.tonini.thip.produzione.raccoltaDati.web.YRilevDatiPrdTSFormActionAdapter;

/**
 * 
 * @author AGSOF3
 *
 *	71063	AGSOF3	02/05/2023	Se è stata fatta FINE o SOSPENSIONE allora propongo la qta buona prendendola dalla 
 *								vista di PUNSTE: SOFTRE.Y_ATV_REG_TOT_V01 *
 *	71063	AGSOF3	02/05/2023	Propongo la qta personalizzata solo se attivo il flag "ricevi qta" nella tabella 
 *								passaggio dati macchina
 */

public class YRilevDatiPrdTSWebFormModifier extends RilevDatiPrdTSWebFormModifier {

	@Override
	public void writeHeadElements(JspWriter out) throws IOException {
		super.writeHeadElements(out);
		String action = (String) getRequest().getAttribute("Action");
		if(action != null && action.equals(RilevDatiPrdTSFormActionAdapter.MONITOR)) {
			out.println(WebJSTypeList.getImportForCSS("it/tonini/thip/assets/DataTables-1.10.9/css/datatables.min.css", getRequest()));
			out.println(WebJSTypeList.getImportForJSLibrary("it/thera/thip/logisticaLight/js/extra/jquery.min.js", getRequest()));
			out.println(WebJSTypeList.getImportForJSLibrary("it/tonini/thip/produzione/raccoltaDati/js/extra/jquery.dataTables.min.js", getRequest()));
			out.println(WebJSTypeList.getImportForCSS("it/tonini/thip/produzione/raccoltaDati/css/dichiarazione_monitor.css", getRequest()));
		}
	}

	@Override
	public void writeBodyEndElements(JspWriter out) throws IOException {
		String action = getRequest().getParameter("thAction");
		super.writeBodyEndElements(out);
		if(action != null) {
			if(action.equals("FINE") || action.equals("SOSPENSIONE")) {
				RilevDatiPrdTS rilevDatiPrdTS = (RilevDatiPrdTS) getRequest().getAttribute("myObject");				
				if(rilevDatiPrdTS != null) {
					BigDecimal qtaBuona = ricevaQtaBuonaDaMacchina(rilevDatiPrdTS);
					if(qtaBuona != null) {	//TBSOF3 modifica al null pointer
						qtaBuona = qtaBuona.setScale(2);	//fix70530
						out.println("<script>");
						out.println("document.getElementById('Quantita').value = '"+qtaBuona.toString().replace('.', ',')+"'");		//fix 70530
						out.println("</script>");
					}
				}
			}
		}
	}

	@Override
	public void writeFormEndElements(JspWriter out) throws IOException {
		super.writeFormEndElements(out);
		RilevDatiPrdTS bo = (RilevDatiPrdTS) getBODataCollector().getBo();
		String action = (String) getRequest().getAttribute("Action");
		if(action != null && action.equals(YRilevDatiPrdTSFormActionAdapter.FINESTRA_DISAMBIGUAZIONE_REPARTI)) {
			displaySceltaReparti(out, bo);
			out.println("<script>");
			out.println("parent.document.getElementById('Conferma').style.display = displayNone;"); //.Qui il btn di conferma non dev'esserci
			out.println("</script>");
		}
	}

	@SuppressWarnings("rawtypes")
	protected void displaySceltaReparti(JspWriter out, RilevDatiPrdTS bo) throws IOException {
		AmbienteListaAttivita ambiente = RilevDatiPrdTS.getAmbienteForCurrentUser();
		String idAmbiente = ambiente != null ? ambiente.getIdAmbiente() : "";
		String width = "\"width:117px\"";
		if(bo.getPersDatiPrdUtenteRilev().getRisoluzioneVideo()== PersDatiPrdUtenteRilev.RISOL_800_600) {
			width = "\"width:100px\"" ;
		}
		out.println("<table cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\">");
		out.println("<tr valign=\"top\">");
		out.println("<td style=\"height:0px\"></td>");
		out.println("<tr>");
		out.println("<td width=\"15px\"></td>");
		out.println("<td>");
		out.println("<td>");
		out.println("  <table id=\"extraTable\" cellpadding=\"1\" cellspacing=\"3\" class=\"monitorListTable\">");
		out.println("  <tr>");
		out.println("    <th class=\"cell\" >"+ResourceLoader.getString(YRilevDatiPrdTS.YRES_FILE, "FinDisReparto.ScegliReparto.LblIdReparto")+"</th>");
		out.println("    <th class=\"cell\" >"+ResourceLoader.getString(YRilevDatiPrdTS.YRES_FILE, "FinDisReparto.ScegliReparto.LblDescrReparto")+"</th>");
		out.println("  </tr>");
		int index = 0;
		String buttonSeleziona = ResourceLoader.getString(YRilevDatiPrdTS.YRES_FILE, "FinDisReparto.ScegliReparto.Btn");
		Iterator iter = ((YRilevDatiPrdTS)bo).listaRepartiAmbienteAttivita(idAmbiente).iterator();
		while (iter.hasNext()) {
			Reparto reparto = (Reparto) iter.next();
			out.println("    <tr>");
			out.println("    <td class=\"cell\" id=\"IdRepartoTD" + index +"\" nowrap=\"true\" >"+reparto.getIdReparto()+"</td>");
			out.println("    <td class=\"cell\" nowrap=\"true\" >"+reparto.getDescrizione().getDescrizione()+"</td>");
			out.println("    <td class=\"cell\" ><button type=\"button\" onclick=\"setCurrentEvent(event);selectReparto(" + index + ")\" style="+ width +">"+buttonSeleziona+"</button></td>");
			out.println("    <td class=\"cell\" style=\"display:none\"><input type =\"text\" id=\"IdReparto" + index + "\" value='" + WebElement.formatStringForHTML(reparto.getIdReparto()) + "' /></td>"); //Fix 14725
			out.println("   </tr>");
			index++;
		}
		out.println("  </table>");
		out.println("</td>");
		out.println("</tr>");
		out.println("<tr>"); //Fix 13264
		out.println("<td colspan=\"5\" style=\"height:100%\"></td>"); //Fix 13264
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td width=\"5px\">");//Fix 13175
		out.println("</td>");//Fix 13175
		out.println("<td colspan=\"5\">");
		out.println("<table cellpadding=\"3\" cellspacing=\"3\">"); //Fix 13574
		out.println("<tr>");
		out.println("<td><label class=\"labelError\" id=\"ErroriList\"></label></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</td>");
		out.println("</tr>");
		out.println("</table>");
	}

	@SuppressWarnings("rawtypes")
	public void displayDichiarazioneInCorso(JspWriter out, RilevDatiPrdTS bo) throws IOException {
		impostaAttributiBOPers(bo);		//Fix 42131

		List dichiarazioneInCorsoList = bo.cercaDichiarazioneInCorso();
		String width = "\"width:117px\"";
		if(bo.getPersDatiPrdUtenteRilev().getRisoluzioneVideo()== PersDatiPrdUtenteRilev.RISOL_800_600) {
			width = "\"width:100px\"" ;
		}
		out.println("<table cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\">");
		out.println("<tr valign=\"top\">");
		out.println("<td style=\"height:0px\"></td>"); //Fix 13574
		out.println("<tr>");
		out.println("<td width=\"15px\"></td>");
		out.println("<td>");
		out.println("<td>");
		out.println("  <table id=\"extraTable\" cellpadding=\"1\" cellspacing=\"3\" class=\"monitorListTable\">"); //Fix 30236
		String sospensioneBut = ResourceLoader.getString(RilevDatiPrdTS.RES_FILE, "ButSospensione");
		String fineBut = ResourceLoader.getString(RilevDatiPrdTS.RES_FILE, "ButFine");
		String ripresaBut = ResourceLoader.getString(RilevDatiPrdTS.RES_FILE, "ButRipresa");
		String butDettaglio = ResourceLoader.getString(RilevDatiPrdTS.RES_FILE, "ButDettaglio");//Fix 30298
		AmbienteListaAttivita ambiente = RilevDatiPrdTS.getAmbienteForCurrentUser();
		String idAmbiente = ambiente != null ? ambiente.getIdAmbiente() : "";
		out.println("<thead>");
		out.println("  <tr>");
		out.println("    <th class=\"cell\" ></th>");
		out.println("    <th class=\"cell\" >" + WebElement.formatStringForHTML(ResourceLoader.getString(RilevDatiPrdTS.RES_FILE, "N_ritorno")) + "</th>");
		out.println(getHTMLTitoloDichInCorsoPers01()); //40458
		out.println("    <th class=\"cell\" >" + WebElement.formatStringForHTML(ResourceLoader.getString(RilevDatiPrdTS.RES_FILE, "N_ord_bolla")) + "</th>");
		out.println(getHTMLTitoloDichInCorsoPers02()); //40458
		out.println("    <th class=\"cell\" >" + WebElement.formatStringForHTML(ResourceLoader.getString(RilevDatiPrdTS.RES_FILE, "Articolo")) + "</th>");
		out.println("    <th class=\"cell\" >Configurazione</th>");
		out.println(getHTMLTitoloDichInCorsoPers03()); //40458
		out.println("    <th class=\"cell\" >" + WebElement.formatStringForHTML(ResourceLoader.getString(RilevDatiPrdTS.RES_FILE, "Risorsa")) + "</th>");
		out.println("    <th class=\"cell\" >" + WebElement.formatStringForHTML(ResourceLoader.getString(RilevDatiPrdTS.RES_FILE, "Qta_UM")) + "</th>");
		out.println(getHTMLTitoloDichInCorsoPers04()); //40458
		out.println(getHTMLTitoloDichInCorsoPers05()); //40458
		out.println("    <th class=\"cell\" >Azioni</th>");
		out.println("  </tr>");
		out.println("</thead><tbody>");
		//Fix 30298 fine
		int index = 0;
		Reparto repartoScelto = trovaRepartoScelto(bo);
		Iterator iter = dichiarazioneInCorsoList.iterator();
		while (iter.hasNext()) {
			RilevazioneDatiProdTes testata = (RilevazioneDatiProdTes) iter.next();
			if(!testata.getRighe().isEmpty() && isRepartoCongruente(testata, repartoScelto)){
				String srcImg = "it/thera/thip/produzione/raccoltaDati/images/Green.gif";
				out.println("    <tr>");
				String bollaLav = testata.getTipoDichiarazione() != PersDatiPrdCausaleRilev.NON_PRODUTTIVA && testata.getTipoDichiarazione() != PersDatiPrdCausaleRilev.FERMO ? testata.getNumeroRitorno() : "";//Fix 16741
				String descrRilev = "";
				String numOrdFmt = "";
				String annoOrd = "";
				String numOrd = "";
				String rigaAtv = "";
				String annoBolla = "";
				String numBolla = "";
				String idArticolo = "";
				String descrArticolo = "";
				String idUmPrimaria = "";
				String idUmSecondaria = "";
				BigDecimal[] qtaDaProdurre = new BigDecimal[2];
				BigDecimal[] qtaGiacMatPrncp = YRilevDatiPrdTS.calcolaQuantitaGiacenzaMaterialePrincipale(testata.getAttivitaEsecutiva());
				String note = ""; //Fix 26907
				String commessa = ""; //Fix 30572
				String articolo_Config = ""; //Fix 33517
				String descr_Config = "";
				String tipoBolla = String.valueOf(testata.getTipoBolla());
				if (testata.getTipoBolla() == RilevazioneDatiProdTes.BOLLA_CUCITA) {
					out.println("    <td class=\"cell\"><img type=\"image\" name=\"\" src=\"" + srcImg + "\" alt=\"\" /><br>&nbsp</td>"); //Fix 35044	
					if (testata.getBollaCucita().getAttivita() != null)
						descrRilev = testata.getBollaCucita(). getAttivita().getDescrizione().getDescrizione();
					numOrdFmt = testata.getBollaCucita().getNumeroBolFmt();
					annoBolla = testata.getBollaCucita().getIdAnnoBolla();
					numBolla = testata.getBollaCucita().getIdNumeroBolla();
					note = testata.getNote();//Fix 26907
				}
				else if (testata.getTipoDichiarazione() != PersDatiPrdCausaleRilev.NON_PRODUTTIVA && testata.getTipoDichiarazione() != PersDatiPrdCausaleRilev.FERMO) {//Fix 16741
					if (testata.getAttivitaEsecutiva() == null) //Fix 35044
						continue; //Fix 35044
					out.println("    <td class=\"cell\"><img type=\"image\" name=\"\" src=\"" + srcImg + "\" alt=\"\" /><br>&nbsp</td>"); //Fix 35044
					descrRilev = testata.getAttivitaEsecutiva().getDescrizione().getDescrizione();
					numOrdFmt = testata.getOrdineEsecutivo().getNumeroOrdFmt();
					annoOrd = testata.getOrdineEsecutivo().getIdAnnoOrdine();
					numOrd = testata.getOrdineEsecutivo().getIdNumeroOrdine();
					rigaAtv = testata.getIdRigaAttivita().toString();
					idArticolo = testata.getOrdineEsecutivo().getIdArticolo();
					descrArticolo = testata.getOrdineEsecutivo().getArticolo().getDescrizioneArticoloNLS().getDescrizione();
					qtaDaProdurre = RilevDatiPrdTS.calcolaQuantitaDaPorduire(testata.getAttivitaEsecutiva());
					idUmPrimaria = testata.getIdUMPrm();
					idUmSecondaria = testata.getIdUMSec();
					note = testata.getNote();//Fix 26907
					if(testata.getOrdineEsecutivo().getIdCommessa() != null)
						commessa = testata.getOrdineEsecutivo().getIdCommessa();
					if(testata.getOrdineEsecutivo().getConfigurazione() != null) {
						articolo_Config = testata.getOrdineEsecutivo().getConfigurazione().getIdEsternoConfig(); //Fix 33688
						descr_Config = testata.getOrdineEsecutivo().getConfigurazione().getDescrizioneExt().getDescrizioneEstesa();
					}
				}
				else {
					out.println("    <td class=\"cell\"><img type=\"image\" name=\"\" src=\"" + srcImg + "\" alt=\"\" /><br>&nbsp</td>"); //Fix 35044
					descrRilev = testata.getPersDatiPrdCausaleRilev().getDescrizione();
					note = testata.getNote();//Fix 29435
				}
				if(testata.getTipoDichiarazione() != PersDatiPrdCausaleRilev.NON_PRODUTTIVA && testata.getTipoDichiarazione() != PersDatiPrdCausaleRilev.FERMO) {//Fix 16741
					out.println("    <td id=\"BollaLavTD" + index + "\" class=\"cell\" >" + WebElement.formatStringForHTML(bollaLav) + "<br>" + WebElement.formatStringForHTML(descrRilev) + "</td>");
				}
				else{
					out.println("    <td id=\"BollaLavTD" + index + "\" class=\"cell\" ></td>");
					out.println("    <td class=\"cell\" >" + WebElement.formatStringForHTML(descrRilev) + "</td>");
				}
				out.println(getHTMLCellaDichInCorsoPers01(index, testata));		//Fix 22513
				out.println(getHTMLCellaDichInCorsoPers02(index, testata));		//Fix 22513
				out.println("    <td class=\"cell\" nowrap=\"true\" >" + WebElement.formatStringForHTML(numOrdFmt) + "<br>" + WebElement.formatStringForHTML(commessa) + "</td>"); //Fix 13574 //Fix 15064 //Fix 30572
				if(testata.getTipoDichiarazione() != PersDatiPrdCausaleRilev.NON_PRODUTTIVA && testata.getTipoDichiarazione() != PersDatiPrdCausaleRilev.FERMO) {//Fix 16741
					if (testata.getTipoBolla() == RilevazioneDatiProdTes.BOLLA_CUCITA) {
						out.println("    <td class=\"cell\" ><button onclick=\"setCurrentEvent(event);dettaglioAction(" + index + ")\" style="+ width +">" + butDettaglio + "</button></td>");
					}else {
						out.println("    <td class=\"cell\" nowrap=\"true\" >" + WebElement.formatStringForHTML(idArticolo) + "<br>" + WebElement.formatStringForHTML(descrArticolo) + "</td>");
						out.println("    <td class=\"cell\" nowrap=\"true\" >" + WebElement.formatStringForHTML(articolo_Config) + "<br>" + WebElement.formatStringForHTML(descr_Config) + "</td>");
					}//Fix 30298 
					out.println(getHTMLCellaDichInCorsoPers03(index, testata));		//Fix 22513
					if(bo.getIdMacchina() == null)
						out.println("    <td class=\"cell\" nowrap=\"true\" >" + WebElement.formatStringForHTML(getMacchina(testata.getAttivitaEsecutiva())) + "</td>");
				}
				if(idUmSecondaria != null){
					out.println("    <td class=\"cell\" nowrap=\"true\" >" + getValue(qtaDaProdurre[0], 2) + " " + WebElement.formatStringForHTML(idUmPrimaria) + "<br>" + getValue(qtaDaProdurre[1], 2) + " " + WebElement.formatStringForHTML(idUmSecondaria) + getValue(qtaGiacMatPrncp[0], 2) + " " + WebElement.formatStringForHTML(idUmPrimaria) + "<br>" + getValue(qtaGiacMatPrncp[1], 2) + " " + WebElement.formatStringForHTML(idUmSecondaria) + "</td>");
				}
				else{
					out.println("    <td class=\"cell\" nowrap=\"true\" >" + qtaDaProdurre[0].intValue() + "</td>");
				}
				out.println(getHTMLCellaDichInCorsoPers04(index, testata));		//Fix 22513
				out.println(getHTMLCellaDichInCorsoPers05(index, testata));		//Fix 22513
				if (testata.getStatoRilevazione() == RilevazioneDatiProdTes.IN_CORSO) {
					out.println("    <td class=\"cell\" ><button onclick=\"setCurrentEvent(event);sospensioneAction(" + index + ")\" style="+ width +">" + sospensioneBut + "</button></td>"); //Fix 24211 //Fix 30236
					out.println("    <td class=\"cell\" ><button onclick=\"setCurrentEvent(event);fineAction(" + index + ")\" style=" + width + ">" + fineBut + "</button></td>"); //Fix 13574 //Fix 30236
					out.println(getHTMLPulsantiDichInCorsoRilevInCorsoPers(index, testata, width)); // Fix 42131
				}
				else if (testata.getStatoRilevazione() == RilevazioneDatiProdTes.SOSPESA) {
					out.println("    <td class=\"cell\" ><button onclick=\"setCurrentEvent(event);ripresaAction(" + index + ")\" style=" + width + ">" + ripresaBut + "</button></td>"); //Fix 13574 //Fix 30236
				}
				out.println("<input type =\"hidden\" id=\"IdCausaleRilevazione" + index + "\" value='" + WebElement.formatStringForHTML(testata.getIdCausaleRilevazione()) + "' />");
				out.println("<input type =\"hidden\" id=\"BollaLav" + index + "\" value='" + WebElement.formatStringForHTML(bollaLav) + "' />"); //Fix 14725
				out.println("<input type =\"hidden\" id=\"IdArticolo" + index + "\" value='" + WebElement.formatStringForHTML(testata.getIdArticolo()) + "' />");
				out.println("<input type =\"hidden\" id=\"IdAnnoOrdine" + index + "\" value='" + WebElement.formatStringForHTML(annoOrd) + "' />");
				out.println("<input type =\"hidden\" id=\"IdNumeroOrdine" + index + "\" value='" + WebElement.formatStringForHTML(numOrd) + "' />");
				out.println("<input type =\"hidden\" id=\"IdRigaAttivita" + index + "\" value='" + WebElement.formatStringForHTML(rigaAtv) + "' />");
				out.println("<input type =\"hidden\" id=\"IdAnnoBolla" + index + "\" value='" + WebElement.formatStringForHTML(annoBolla) + "' />");
				out.println("<input type =\"hidden\" id=\"IdNumeroBolla" + index + "\" value='" + WebElement.formatStringForHTML(numBolla) + "' />");
				out.println("<input type =\"hidden\" id=\"TipoBolla" + index + "\" value='" + WebElement.formatStringForHTML(tipoBolla) + "' />");
				out.println("<input type =\"hidden\" id=\"TipoRisorsa" + index + "\" value='" + testata.getTipoRisorsa() + "' />");
				out.println("<input type =\"hidden\" id=\"LivelloRisorsa" + index + "\" value='" + testata.getLivelloRisorsa() + "' />");
				out.println("<input type =\"hidden\" id=\"IdRisorsa" + index + "\" value='" + WebElement.formatStringForHTML(testata.getIdRisorsa()) + "' />");
				out.println("<input type =\"hidden\" id=\"IdAmbiente" + index + "\" value='" + WebElement.formatStringForHTML(idAmbiente) + "' />"); //Fix 12841
				out.println("<input type =\"hidden\" id=\"Quantita" + index + "\" value='" + WebElement.formatStringForHTML(getValue(qtaDaProdurre[0], 2)) + "' />");
				out.println("<input type =\"hidden\" id=\"QuantitaSec" + index + "\" value='" + WebElement.formatStringForHTML(getValue(qtaDaProdurre[1], 2)) + "' />");
				out.println("<input type =\"hidden\" id=\"Note" + index + "\" value='" + WebElement.formatStringForHTML(note) + "' />");
				//}
				out.println("   </tr>");
				index++;
			}
		}
		if(bo.getIdAmbiente() != null){
			displayProssemeDichiarazione(out, bo, index); //Fix 12841      
		}
		out.println("</tbody>");
		out.println("<tfoot>");
		out.println("<tr>");
		out.println("<th class='head-filtri'></th>");
		out.println("<th class='head-filtri'></th>");
		out.println("<th class='head-filtri'></th>");
		out.println("<th class='head-filtri'></th>");
		out.println("<th class='head-filtri'></th>");
		out.println("<th class='head-filtri'></th>");
		out.println("<th class='head-filtri'></th>");
		out.println("<th class='head-filtri'></th>");
		out.println("<th class='head-filtri'></th>");
		out.println("<th class='head-filtri'></th>");
		out.println("<th class='head-filtri'></th>");
		out.println("<th class='head-filtri'></th>");
		out.println("</tr>");
		out.println("</tfoot>");
		out.println("  </table>");
		out.println("</td>");
		out.println("</tr>");
		out.println("<tr>"); //Fix 13264
		out.println("<td colspan=\"5\" style=\"height:100%\"></td>"); //Fix 13264
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td width=\"5px\">");//Fix 13175
		out.println("</td>");//Fix 13175
		out.println("<td colspan=\"5\">");
		out.println("<table cellpadding=\"3\" cellspacing=\"3\">"); //Fix 13574
		out.println("<tr>");
		out.println("<td><label class=\"labelError\" id=\"ErroriList\"></label></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</td>");
		out.println("</tr>");
		out.println("</table>");
	}

	public Reparto trovaRepartoScelto(RilevDatiPrdTS bo) {
		Reparto reparto = null;
		if(bo.getNote() != null) {
			try {
				reparto = (Reparto) Reparto.elementWithKey(Reparto.class, KeyHelper.buildObjectKey(new String[] {
						Azienda.getAziendaCorrente(),bo.getNote()
				}), PersistentObject.NO_LOCK);
			} catch (SQLException e) {
				e.printStackTrace(Trace.excStream);
			}
		}
		return reparto;
	}

	public boolean isRepartoCongruente(RilevazioneDatiProdTes testata, Reparto reparto) {
		if(reparto != null
				&& reparto.getIdReparto().equals(testata.getIdReparto())) {
			return true;
		}else {
			return false;
		}
	}

	@SuppressWarnings("rawtypes")
	public int displayProssimeDichInternal(JspWriter out, List prossimeDich, String srcImg, RilevDatiPrdTS bo, int index) throws IOException{ //Fix 23541
		//Fix 13264 inizio
		String but = ResourceLoader.getString(RilevDatiPrdTS.RES_FILE, "ButInizio"); //Fix 13434
		//Fix 13264 fine
		Iterator iter = prossimeDich.iterator();
		while (iter.hasNext()) {
			ListaAttivita listaAttivita = (ListaAttivita) iter.next();
			if(listaAttivita.getIdAnnoBolla() == null && listaAttivita.getIdNumeroBolla() == null) { //Fix 44763
				out.println("    <tr>");
				//out.println("    <td class=\"cell\"><img type=\"image\" name=\"\" src=\"" + srcImg + "\" alt=\"\" /></td>"); //Fix 14725
				out.println("    <td class=\"cell\"><img type=\"image\" name=\"\" src=\"" + srcImg + "\" alt=\"\" /><br>&nbsp</td>"); //Fix 14725
				String bollaLav = listaAttivita.getNumRitorno();
				String descrRilev = listaAttivita.getDescrAttivita();
				String numOrdine = listaAttivita.getNumeroOrdineFmt();
				String idArticolo = listaAttivita.getIdArticolo();
				String descrArticolo = listaAttivita.getAttivitaEsecutiva().getOrdineEsecutivo().getArticolo().getDescrizioneArticoloNLS().getDescrizioneEstesa();
				String articolo_Config = "";
				String descr_Config = "";
				if(listaAttivita.getConfigurazione() != null) {
					articolo_Config = listaAttivita.getConfigurazione().getIdEsternoConfig(); //Fix 33688 
					descr_Config = listaAttivita.getConfigurazione().getDescrizioneExt().getDescrizioneEstesa();
				}
				String commessa = "";
				if(listaAttivita.getAttivitaEsecutiva().getOrdineEsecutivo().getIdCommessa() != null) {
					commessa = listaAttivita.getAttivitaEsecutiva().getOrdineEsecutivo().getIdCommessa();
				}
				BigDecimal[] qtaDaProdurre = RilevDatiPrdTS.calcolaQuantitaDaPorduire(listaAttivita.getAttivitaEsecutiva());
				BigDecimal[] qtaGiacMatPrncp = YRilevDatiPrdTS.calcolaQuantitaGiacenzaMaterialePrincipale(listaAttivita.getAttivitaEsecutiva());
				String idUmPrimaria = listaAttivita.getAttivitaEsecutiva().getOrdineEsecutivo().getArticolo().getIdUMPrmMag();
				String idUmSecondaria = listaAttivita.getAttivitaEsecutiva().getOrdineEsecutivo().getArticolo().getIdUMSecMag();
				out.println("    <td id=\"BollaLavTD" + index + "\" class=\"cell\" >" + WebElement.formatStringForHTML(bollaLav) + "<br>" + WebElement.formatStringForHTML(descrRilev) + "</td>");
				out.println(getHTMLCellaProssimeDichPers01(index, listaAttivita));		//Fix 22513
				out.println(getHTMLCellaProssimeDichPers02(index, listaAttivita));		//Fix 22513
				if(commessa.equals("")) { //Fix 37456
					out.println("    <td class=\"cell\" >" + WebElement.formatStringForHTML(numOrdine) + "</td>");
				}
				else{ //Fix 37456 --inizio
					out.println("    <td class=\"cell\" >" + WebElement.formatStringForHTML(numOrdine) + "<br>" + WebElement.formatStringForHTML(commessa) + "</td>");
				}
				out.println("    <td class=\"cell\" nowrap=\"true\" >" + WebElement.formatStringForHTML(idArticolo) + "<br>" + WebElement.formatStringForHTML(descrArticolo) + "</td>");
				out.println("    <td class=\"cell\" nowrap=\"true\" >" + WebElement.formatStringForHTML(articolo_Config) + "<br>" + WebElement.formatStringForHTML(descr_Config) + "</td>");
				out.println(getHTMLCellaProssimeDichPers03(index, listaAttivita));		//Fix 22513
				out.println("    <td class=\"cell\" nowrap=\"true\" >" + WebElement.formatStringForHTML(getMacchina(listaAttivita.getAttivitaEsecutiva()))+"</td>");
				if(idUmSecondaria != null){
					out.println("    <td class=\"cell\" nowrap=\"true\" >" + getValue(qtaDaProdurre[0].stripTrailingZeros(), 2) + " " + WebElement.formatStringForHTML(idUmPrimaria) + "<br>" + getValue(qtaDaProdurre[1], 2) + " " + WebElement.formatStringForHTML(idUmSecondaria) + getValue(qtaGiacMatPrncp[0], 2) + " " + WebElement.formatStringForHTML(idUmPrimaria) + "<br>" + getValue(qtaGiacMatPrncp[1], 2) + " " + WebElement.formatStringForHTML(idUmSecondaria) + "</td>");
				}
				else{
					out.println("    <td class=\"cell\" nowrap=\"true\" >" + qtaDaProdurre[0].intValue() +"</td>");
				}
				out.println(getHTMLCellaProssimeDichPers04(index, listaAttivita));		//Fix 22513
				out.println(getHTMLCellaProssimeDichPers05(index, listaAttivita));		//Fix 22513
				out.println("    <td class=\"cell\" ><button onclick=\"setCurrentEvent(event);inizioAction(" + index + ")\" style=\"width: 117px\">" + but + "</button></td>");
				out.println("<input type =\"hidden\" id=\"BollaLav" + index + "\" value='" + WebElement.formatStringForHTML(bollaLav) + "' />"); //Fix 14725
				out.println("<input type =\"hidden\" id=\"IdArticolo" + index + "\" value='" + WebElement.formatStringForHTML(listaAttivita.getIdArticolo()) + "' />");
				out.println("<input type =\"hidden\" id=\"IdAnnoOrdine" + index + "\" value='" + WebElement.formatStringForHTML(listaAttivita.getIdAnnoOrdine()) + "' />");
				out.println("<input type =\"hidden\" id=\"IdNumeroOrdine" + index + "\" value='" + WebElement.formatStringForHTML(listaAttivita.getIdNumeroOrdine()) + "' />");
				out.println("<input type =\"hidden\" id=\"IdRigaAttivita" + index + "\" value='" + WebElement.formatStringForHTML(listaAttivita.getIdRigaAttivita().toString()) + "' />");
				out.println("<input type =\"hidden\" id=\"TipoRisorsa" + index + "\" value='" + (bo.getIdMacchina() != null ? listaAttivita.getTipoMacchina() : listaAttivita.getTipoOperatore()) + "' />");
				out.println("<input type =\"hidden\" id=\"LivelloRisorsa" + index + "\" value='" + (bo.getIdMacchina() != null ? listaAttivita.getLivelloMacchina() : listaAttivita.getLivelloOperatore()) + "' />");
				out.println("<input type =\"hidden\" id=\"IdRisorsa" + index + "\" value='" + WebElement.formatStringForHTML((bo.getIdMacchina() != null ? listaAttivita.getIdMacchina() : listaAttivita.getIdOperatore())) + "' />");
				out.println("<input type =\"hidden\" id=\"IdAmbiente" + index + "\" value='" + WebElement.formatStringForHTML(listaAttivita.getIdAmbiente()) + "' />");
				out.println("   </tr>");
			}
			else { //Fix 44763 --inizio
				BollaCucitaTes bollaCucita = listaAttivita.getBollaCucita();
				if(bollaCucita != null) {
					iEsistListaAttivitaBC = true;
					String butDettaglio = ResourceLoader.getString(RilevDatiPrdTS.RES_FILE, "ButDettaglio");
					String width = "\"width:117px\"";
					if(bo.getPersDatiPrdUtenteRilev().getRisoluzioneVideo()== PersDatiPrdUtenteRilev.RISOL_800_600) {
						width = "\"width:100px\"" ;
					}
					out.println("    <tr>");
					out.println("    <td class=\"cell\"><img type=\"image\" name=\"\" src=\"" + srcImg + "\" alt=\"\" /><br>&nbsp</td>"); 
					String bollaLav = bollaCucita.getNumeroRitorno();
					String descrRilev = "";
					String numOrdine = bollaCucita.getNumeroBolFmt();
					out.println("");
					out.println("    <td id=\"BollaLavTD" + index + "\" class=\"cell\" >" + WebElement.formatStringForHTML(bollaLav) + "<br>" + WebElement.formatStringForHTML(descrRilev) + "</td>");
					out.println("");
					out.println("    <td class=\"cell\" >" + WebElement.formatStringForHTML(numOrdine) + "</td>");
					out.println("");
					out.println("    <td class=\"cell\" ><button onclick=\"setCurrentEvent(event);dettaglioAction(" + index + ")\" style="+ width +">" + butDettaglio + "</button></td>");
					out.println("");
					if(bo.getIdMacchina() == null)
						out.println("    <td class=\"cell\" nowrap=\"true\" >" + WebElement.formatStringForHTML("") + "<br>" + WebElement.formatStringForHTML("") + "</td>");
					out.println("    <td class=\"cell\" nowrap=\"true\" >" + "" + " " + WebElement.formatStringForHTML("") + "<br>&nbsp</td>");
					out.println("");
					out.println("    <td class=\"cell\" ><button onclick=\"setCurrentEvent(event);inizioAction(" + index + ")\" style=\"width: 117px\">" + but + "</button></td>");

					out.println("<input type =\"text\" id=\"IdCausaleRilevazione" + index + "\" value='" + WebElement.formatStringForHTML(bo.getIdCausaleRilevazione()) + "' />");
					out.println("<input type =\"text\" id=\"BollaLav" + index + "\" value='" + WebElement.formatStringForHTML(bollaLav) + "' />");
					out.println("<input type =\"text\" id=\"IdArticolo" + index + "\" value='" + WebElement.formatStringForHTML("") + "' />");
					out.println("<input type =\"text\" id=\"IdAnnoOrdine" + index + "\" value='" + WebElement.formatStringForHTML("") + "' />");
					out.println("<input type =\"text\" id=\"IdNumeroOrdine" + index + "\" value='" + WebElement.formatStringForHTML("") + "' />");
					out.println("<input type =\"text\" id=\"IdRigaAttivita" + index + "\" value='" + WebElement.formatStringForHTML("") + "' />");
					out.println("<input type =\"text\" id=\"IdAnnoBolla" + index + "\" value='" + WebElement.formatStringForHTML(bollaCucita.getIdAnnoBolla()) + "' />");
					out.println("<input type =\"text\" id=\"IdNumeroBolla" + index + "\" value='" + WebElement.formatStringForHTML(bollaCucita.getIdNumeroBolla()) + "' />");
					out.println("<input type =\"text\" id=\"TipoBolla" + index + "\" value='" + WebElement.formatStringForHTML(""+RilevazioneDatiProdTes.BOLLA_CUCITA) + "' />");
					if(bollaCucita.getLivelloRisorsa() == Risorsa.MATRICOLA) {
						out.println("<input type =\"text\" id=\"TipoRisorsa" + index + "\" value='" + (bollaCucita.getTipoRisorsa()) + "' />");
						out.println("<input type =\"text\" id=\"LivelloRisorsa" + index + "\" value='" + (bollaCucita.getLivelloRisorsa()) + "' />");
						out.println("<input type =\"text\" id=\"IdRisorsa" + index + "\" value='" + WebElement.formatStringForHTML(bollaCucita.getIdRisorsa()) + "' />");
					}
					else {
						out.println("<input type =\"text\" id=\"TipoRisorsa" + index + "\" value='" + Risorsa.MACCHINE + "' />");
						out.println("<input type =\"text\" id=\"LivelloRisorsa" + index + "\" value='" + Risorsa.MATRICOLA + "' />");
						out.println("<input type =\"text\" id=\"IdRisorsa" + index + "\" value='' />");  
					}
					out.println("<input type =\"text\" id=\"IdAmbiente" + index + "\" value='" + WebElement.formatStringForHTML(bo.getIdAmbiente()) + "' />");
					out.println("   </tr>");
				}
			} //Fix 44763 --fine
			index++;
		}
		return index; //Fix 23541
	}

	@Override
	protected String getHTMLTitoloDichInCorsoPers01() {
		return "<th class=\"cell\" >" + WebElement.formatStringForHTML(ResourceLoader.getString(YRilevDatiPrdTS.YRES_FILE, "Dt_Inizio_Rcs")) + "</th>";
	}

	@Override
	protected String getHTMLTitoloDichInCorsoPers03() {
		return "<th class=\"cell\" >" + WebElement.formatStringForHTML(ResourceLoader.getString(YRilevDatiPrdTS.YRES_FILE, "Codice_Manico")) + "</th>";
	}

	@Override
	protected String getHTMLTitoloDichInCorsoPers04() {
		return "<th class=\"cell\" >" + WebElement.formatStringForHTML(ResourceLoader.getString(YRilevDatiPrdTS.YRES_FILE, "Cliente")) + "</th>";
	}

	@Override
	protected String getHTMLTitoloDichInCorsoPers05() {
		return "<th class=\"cell\" >" + WebElement.formatStringForHTML(ResourceLoader.getString(YRilevDatiPrdTS.YRES_FILE, "Ordine_Cliente")) + "</th>";
	}

	@Override
	protected String getHTMLCellaDichInCorsoPers01(int index, RilevazioneDatiProdTes testata) {
		Date data = getDataInizioRichiesta(testata.getAttivitaEsecutiva());
		return "<td "+getDataOrderData(data)+" id=\"DataInzRcsTD" + index + "\" class=\"cell\" >" + WebElement.formatStringForHTML(getDataInizioRichiestaStr(data))+"</td>";
	}

	@Override
	protected String getHTMLCellaProssimeDichPers01(int index, ListaAttivita listaAttivita) {
		Date data = getDataInizioRichiesta(listaAttivita.getAttivitaEsecutiva());
		return "<td "+getDataOrderData(data)+" id=\"DataInzRcsTD" + index + "\" class=\"cell\" >" + WebElement.formatStringForHTML(getDataInizioRichiestaStr(data))+"</td>";
	}

	@Override
	protected String getHTMLCellaDichInCorsoPers03(int index, RilevazioneDatiProdTes testata) {
		return "<td id=\"CodiceManicoTD" + index + "\" class=\"cell\" >" + WebElement.formatStringForHTML(findCodiceManicoFromOrdineEsecutivo(testata.getOrdineEsecutivo()))+"</td>";
	}

	@Override
	protected String getHTMLCellaProssimeDichPers03(int index, ListaAttivita listaAttivita) {
		return "<td id=\"CodiceManicoTD" + index + "\" class=\"cell\" >" + WebElement.formatStringForHTML(findCodiceManicoFromOrdineEsecutivo(listaAttivita.getOrdineEsecutivo()))+"</td>";
	}

	@Override
	protected String getHTMLCellaDichInCorsoPers04(int index, RilevazioneDatiProdTes testata) {
		return "<td id=\"CodiceClienteTD" + index + "\" class=\"cell\" >" + WebElement.formatStringForHTML(getCodiceClienteRagSocStr(testata.getOrdineEsecutivo()))+"</td>";
	}

	@Override
	protected String getHTMLCellaDichInCorsoPers05(int index, RilevazioneDatiProdTes testata) {
		String str = "";
		if(testata.getOrdineEsecutivo().getOrdineVenditaRiga() != null)
			str = testata.getOrdineEsecutivo().getOrdineVenditaRiga().getTestata().getNumeroDocumentoFormattato();
		return "<td id=\"OrdineClienteTD" + index + "\" class=\"cell\" >" + WebElement.formatStringForHTML(str)+"</td>";
	}

	@Override
	protected String getHTMLCellaProssimeDichPers04(int index, ListaAttivita listaAttivita) {
		return "<td id=\"CodiceClienteTD" + index + "\" class=\"cell\" >" + WebElement.formatStringForHTML(getCodiceClienteRagSocStr(listaAttivita.getOrdineEsecutivo()))+"</td>";
	}

	@Override
	protected String getHTMLCellaProssimeDichPers05(int index, ListaAttivita listaAttivita) {
		String str = "";
		if(listaAttivita.getOrdineEsecutivo().getOrdineVenditaRiga() != null)
			str = listaAttivita.getOrdineEsecutivo().getOrdineVenditaRiga().getTestata().getNumeroDocumentoFormattato();
		return "<td id=\"OrdineClienteTD" + index + "\" class=\"cell\" >" + WebElement.formatStringForHTML(str)+"</td>";
	}

	public String getCodiceClienteRagSocStr(OrdineEsecutivo ordEsec) {
		String str = "";
		if(ordEsec.getCliente() != null) {
			str = ordEsec.getCliente().getCodSistemaInfEsterno();
		}
		return str;
	}

	@SuppressWarnings("rawtypes")
	public String findCodiceManicoFromOrdineEsecutivo(OrdineEsecutivo ordEsec) {
		String manico = "";
		Iterator iterAtv = ordEsec.getAttivitaEsecutive().iterator();
		while(iterAtv.hasNext()) {
			AttivitaEsecutiva attivitaEsecutiva = (AttivitaEsecutiva) iterAtv.next();
			Iterator iterMateriali = attivitaEsecutiva.getMateriali().iterator();
			while(iterMateriali.hasNext()) {
				AttivitaEsecMateriale atvEsecMat = (AttivitaEsecMateriale) iterMateriali.next();
				if(atvEsecMat.getArticolo() != null 
						&& atvEsecMat.getArticolo().getIdMacroFamiglia() != null
						&& idMacrofamigliaManici().equals(atvEsecMat.getArticolo().getIdMacroFamiglia())) {
					return atvEsecMat.getIdArticolo();
				}
			}
		}
		return manico;
	}

	public Date getDataInizioRichiesta(AttivitaEsecutiva atv) {
		Date dataInizioRcs = null;
		//		if(atv.getDateProgrammate().getStartDate() != null)
		//			dataInizioRcs = atv.getDateProgrammate().getStartDate();
		//		else
		//			dataInizioRcs = atv.getOrdineEsecutivo().getDateRichieste().getStartDate();
		if(atv.getOrdineEsecutivo().getOrdineVenditaRiga() != null)
			dataInizioRcs = atv.getOrdineEsecutivo().getOrdineVenditaRiga().getDataConsegnaConfermata();
		return dataInizioRcs;
	}

	public String getDataInizioRichiestaStr(Date dataInizioRcs) {
		DateType dt = new DateType();
		String dateStr = "";
		if(dataInizioRcs != null)
			dateStr = dt.objectToString(dataInizioRcs);
		return dateStr;
	}

	public String getDataOrderData(java.sql.Date data) {
		if (data == null) {
			return "";
		}
		return "data-order=\"" + data.toLocalDate().toString() + "\"";
	}

	public static String idMacrofamigliaManici() {
		String val = ParametroPsn.getValoreParametroPsn("pers.base.articolo.IdentificatoreManici", "IdMacroFamiglia");
		return val != null ? val : "";
	}

	@SuppressWarnings("rawtypes")
	public String getMacchina(AttivitaEsecutiva atv) {
		Iterator iterRsr = atv.getRisorse().iterator();
		while(iterRsr.hasNext()) {
			AttivitaEsecRisorsa rsr = (AttivitaEsecRisorsa) iterRsr.next();
			if(rsr.getTipoRisorsa() == Risorsa.MACCHINE) {
				return rsr.getIdRisorsa() + " - " + rsr.getRisorsa().getDescrizione().getDescrizione();
			}
		}
		return "";
	}

	/**
	 * 
	 * @param rilevDatiPrdTS
	 * @return qta da vista SOFTRE.Y_ATV_REG_TOT_V01
	 */
	protected BigDecimal ricevaQtaBuonaDaMacchina(RilevDatiPrdTS rilevDatiPrdTS) {
		BigDecimal qtaBuona = null;
		try {
			YPassDatiMacc ogg = YPassDatiMacc.elementWithKey(rilevDatiPrdTS.getMacchina().getKey(), 0);
			//70993 aggiunto test ogg.isRiceviQta()
			if(ogg != null && ogg.getDatiMacchina() && ogg.isRiceviQta()) {//se la macchina esiste in SOFTRE.YPASS_DATI_MACC ed ha il flag datiMacchina = true 
				String numRilFmt = rilevDatiPrdTS.getRilevazioneTesFine() != null ? rilevDatiPrdTS.getRilevazioneTesFine().getNumeroRilevFormattato() : "";
				//70499
				String select = "SELECT " + 
						"CASE " + 
						"WHEN QTA_PROD_RIC = 0 THEN QTA_PROD_CALC " + 
						"WHEN QTA_PROD_RIC != 0 THEN QTA_PROD_RIC " + 
						"END " + 
						"FROM SOFTRE.Y_ATV_REG_TOT_V01 " + 
						"WHERE ID_AZIENDA='" + rilevDatiPrdTS.getIdAzienda() + "' " + 
						"AND R_MACCHINA ='" + rilevDatiPrdTS.getIdMacchina() +"' " + 
						"AND NUMERO_RIL_FMT='" + numRilFmt + "'";
				CachedStatement cs = new CachedStatement(select);
				ResultSet rs = cs.executeQuery();
				if(rs.next()) {
					qtaBuona = rs.getBigDecimal(1);
				}else {
					qtaBuona = new BigDecimal(0);
				}
			}
		}catch(SQLException e) {
			e.printStackTrace(Trace.excStream);
		}
		return qtaBuona;
	}

}