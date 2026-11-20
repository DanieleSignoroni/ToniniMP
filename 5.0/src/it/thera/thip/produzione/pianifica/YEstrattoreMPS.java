package it.thera.thip.produzione.pianifica;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.thera.thermfw.base.TimeUtils;
import com.thera.thermfw.base.Trace;
import com.thera.thermfw.common.ErrorMessage;
import com.thera.thermfw.persist.Factory;
import com.thera.thermfw.persist.KeyHelper;

import it.softre.thip.vendite.uds.OggettinoContenitore;
import it.softre.thip.vendite.uds.YEvasioneUdsVendita;
import it.softre.thip.vendite.uds.YUdsVendita;
import it.thera.thip.base.azienda.Azienda;
import it.thera.thip.base.generale.UnitaPianificazione;
import it.tonini.thip.produzione.pianifica.YMRPLancio;

/**
 * <p></p>
 *
 * <p>
 * Company: Softre Solutions<br>
 * Author: Daniele Signoroni<br>
 * Date: 11/06/2025
 * </p>
 */

/*
 * Revisions:
 * Number   Date        Owner    Description
 * 71997    11/06/2025  DSSOF3   Prima stesura
 */

public class YEstrattoreMPS extends EstrattoreMPS {

	@SuppressWarnings("rawtypes")
	@Override
	public Vector eseguiEstrazione() {
		Vector errors = super.eseguiEstrazione();
		if(getEstrattoreFonti() != null 
				&& getEstrattoreFonti().getMRPEsecuzione() != null
				&& getEstrattoreFonti().getMRPEsecuzione().getBatchRunnableObj() != null
				&& getEstrattoreFonti().getMRPEsecuzione().getBatchRunnableObj() instanceof YMRPLancio
				&& ((YMRPLancio)getEstrattoreFonti().getMRPEsecuzione().getBatchRunnableObj()).getImpegniContenitoriUDS()) {
			List oggettini = estrazioneImpegniContenitore();
			if(!oggettini.isEmpty()) {
				errors = popolaPianificaFabFontiOfferta(oggettini);
			}
		}
		return errors;
	}

	@SuppressWarnings("rawtypes")
	protected Vector popolaPianificaFabFontiOfferta(List oggettini) {
		Vector errors = new Vector();
		for (Iterator iterator = oggettini.iterator(); iterator.hasNext();) {
			OggettinoContenitore oggettino = (OggettinoContenitore) iterator.next();
			salvaOrdiniFabbisognoOffertaClienteRiga(getEstrattoreFonti().getMRPEsecuzione().getAmbientePianificazione().getIdAmbientePianificazione()
					, "S16", oggettino, errors);
		}
		return errors;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ErrorMessage salvaOrdiniFabbisognoOffertaClienteRiga(String idAmbientePianificazione, String idFonte, OggettinoContenitore oggettino, Vector errors)
	{
		PianificaFab pianificaFab = (PianificaFab)Factory.createObject(PianificaFab.class);
		//@MPS Data :
		pianificaFab.setIdAzienda(Azienda.getAziendaCorrente());
		pianificaFab.setIdAmbientePianificazione(idAmbientePianificazione);
		pianificaFab.setIdArticolo(oggettino.getIdArticolo());
		//fix 3106 inizio
		pianificaFab.setIdConfigurazione(0);
		//fix 3106 fine
		pianificaFab.setIdMagazzino(YEvasioneUdsVendita.getMagazzinoRigaDvContenitori());
		pianificaFab.setIdPianificatore(pianificaFab.getArticolo().getIdPianificatore());
		pianificaFab.setIdLineaProdotto(pianificaFab.getArticolo().getIdLineaProdotto());
		pianificaFab.setIdGrpProdotto(pianificaFab.getArticolo().getIdGruppoProdotto());
		pianificaFab.setIdMacrofamiglia(pianificaFab.getArticolo().getIdMacroFamiglia());
		pianificaFab.setIdSubfamiglia(pianificaFab.getArticolo().getIdSubFamiglia());
		pianificaFab.setIdMicrofamiglia(pianificaFab.getArticolo().getIdMicroFamiglia());
		pianificaFab.setIdClasseMerc(pianificaFab.getArticolo().getIdClasseMerclg());
		pianificaFab.setIdClasseMateriale(pianificaFab.getArticolo().getIdClasseMateriale());
		//@Ordini data
		pianificaFab.setQuantita(oggettino.getQuantita());
		pianificaFab.setDataEvento(TimeUtils.getCurrentDate()); //data odierna
		pianificaFab.setDtConsConfCl(null);
		pianificaFab.setIdArtVersione(1);
		pianificaFab.setAnnoOrdine(null);
		pianificaFab.setNumeroOrdine(null);
		pianificaFab.setRigaOrdine(0);
		pianificaFab.setDettRigaOrdine(0);
		pianificaFab.setNumeroOrdForm(null);
		pianificaFab.setIdCliente(null);

		pianificaFab.setIdCommessa(null);

		//@FonteFab
		pianificaFab.setIdFonteFab(idFonte);
		FonteFab fonteFab = getFonteFab(pianificaFab.getIdFonteFab());
		pianificaFab.setTipoFonte(fonteFab.getTipoFonte());
		pianificaFab.setTipoFontePianif(fonteFab.getTipoFontePianif());
		pianificaFab.setSegnoFonte(fonteFab.getSegnoFonte());
		pianificaFab.setIdRilFonteFab(pianificaFab.getIdFonteFab());

		//@Ricavata : Calcolo Risorse
		pianificaFab.setLivelloMinimo(pianificaFab.getArticolo().getLivelloMinimo());
		pianificaFab.setIdLivelloRisorsa(pianificaFab.getArticolo().getLivelloRisorsaMPS());
		pianificaFab.setIdTipoRisorsa(pianificaFab.getArticolo().getTipoRisorsaMPS());
		pianificaFab.setIdRisorsa(pianificaFab.getArticolo().getIdRisorsaMPS());
		BigDecimal artCosto = new BigDecimal("0.0");//fix 2587
		//	    if(mpsData.IdConfigurazione != null)//fix 2587 tolto con fix 3106
		artCosto = getArtCosto(pianificaFab.getIdAzienda(),
				pianificaFab.getIdArticolo(),
				pianificaFab.getIdVersione(),
				pianificaFab.getIdConfigurazione(),
				getEstrattoreFonti().getAmbientePianificazione().getUpDataPianificazione()); //...FIX 2318 //Fix 37196
		pianificaFab.setCostoUnitario(artCosto);

		BigDecimal carico = pianificaFab.getQuantita().multiply(BigDecimal.ZERO);
		String idUMKey    = KeyHelper.buildObjectKey(new String[] {pianificaFab.getIdAzienda(), pianificaFab.getArticolo().getIdUMCaricoMPS()});
		BigDecimal  oreCarico = getOreCarico(carico, idUMKey);

		//  int len0 = getAttributeRefSize("PianificaFabb", "OreCarico");
		//pianificaFab.setOreCarico(oreCarico.setScale(lgOreCarico, BigDecimal.ROUND_HALF_EVEN).intValue());//28417
		pianificaFab.setOreCarico(oreCarico.setScale(lgOreCarico, BigDecimal.ROUND_HALF_EVEN));//28417

		// int len1 = getAttributeRefSize("PianificaFabb", "Valore");
		pianificaFab.setValoreEvento(artCosto.multiply(pianificaFab.getQuantita()).setScale(lgValore, BigDecimal.ROUND_HALF_EVEN));

		//@ Assegnazioni ricavabili dal magazzino
		UnitaPianificazione up = getEstrattoreFonti().getMagazzinoUnita(pianificaFab.getIdMagazzino()); //Fix 37196
		pianificaFab.setIdUnitaPianif(up.getIdUnitaPianificazione());
		pianificaFab.setIdStabilimento(up.getIdStabilimento());
		//@ Reperimento modello produttivo
		pianificaFab.setIdResponRlsOrd(pianificaFab.getArticolo().getIdResponRlsOrdini());
		int reg = getEstrattoreFonti().getIdRegistrazioneCalcolato(); //Fix 37196
		pianificaFab.setIdRiferSorgente(null);
		//fix 3006 inizio
		ErrorMessage em = salvaFabbisogno(pianificaFab , new Integer(reg),MPS_ESTRAZIONE_ORD_FASI);
		if(em != null)
		{
			errors.add(em);
		}
		else
		{
			countOrd++;
			if(countOrd % 1000 == 0 && getLogger().isEnable())
				getLogger().printForcedMessage("    Offerta cliente: " + countOrd);
		}

		return em;
	}

	protected List<OggettinoContenitore> estrazioneImpegniContenitore() {
		String where = "ID_AZIENDA = '" + Azienda.getAziendaCorrente() + "' "
				+ " AND STATO_EVASIONE < '" + YUdsVendita.VERSATO_A_MAGAZZINO + "'"
				+ " AND R_ANNO_DOC_VEN IS NULL"
				+ " AND R_NUM_DOC_VEN IS NULL";

		Map<String, OggettinoContenitore> contenitoriMap = new LinkedHashMap<>();
		Set<String> udsVisitate = new HashSet<>();
		try {

			Vector<?> udsEstratte = YUdsVendita.retrieveList(where, "", false);
			for (Object obj : udsEstratte) {
				YUdsVendita uds = (YUdsVendita) obj;

				YEvasioneUdsVendita.aggiungiContenitore(uds, uds.getKey(), contenitoriMap, udsVisitate);
			}

		} catch (ClassNotFoundException | InstantiationException |
				IllegalAccessException | SQLException e) {
			e.printStackTrace(Trace.excStream);
			throw new IllegalStateException("Errore estrazione impegni contenitori", e);
		}
		return new ArrayList<>(contenitoriMap.values());
	}
}
