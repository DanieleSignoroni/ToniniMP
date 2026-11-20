package it.softre.thip.vendite.uds;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.thera.thermfw.ad.ClassADCollection;
import com.thera.thermfw.ad.ClassADCollectionManager;
import com.thera.thermfw.base.Q6Calc;
import com.thera.thermfw.base.Trace;
import com.thera.thermfw.base.Utils;
import com.thera.thermfw.collector.BODataCollector;
import com.thera.thermfw.common.BaseComponentsCollection;
import com.thera.thermfw.common.ErrorMessage;
import com.thera.thermfw.persist.ConnectionManager;
import com.thera.thermfw.persist.CopyException;
import com.thera.thermfw.persist.DB2NetDatabase;
import com.thera.thermfw.persist.Factory;
import com.thera.thermfw.persist.KeyHelper;
import com.thera.thermfw.persist.PersistentObject;
import com.thera.thermfw.persist.Proxy;
import com.thera.thermfw.persist.SQLServerJTDSNoUnicodeDatabase;
import com.thera.thermfw.persist.TableManager;

import it.softre.thip.base.uds.YTipoUds;
import it.softre.thip.vendite.uds.web.YUdsVenditaGridActionAdapter;
import it.thera.thip.base.articolo.ArticoloDatiIdent;
import it.thera.thip.base.articolo.ArticoloVersione;
import it.thera.thip.base.azienda.Azienda;
import it.thera.thip.base.azienda.Magazzino;
import it.thera.thip.base.cliente.ClienteVendita;
import it.thera.thip.base.cliente.IdentificativoBanca;
import it.thera.thip.base.comuniVenAcq.GestoreCalcoloCosti;
import it.thera.thip.base.comuniVenAcq.OrdineTestata;
import it.thera.thip.base.comuniVenAcq.TipoCostoRiferimento;
import it.thera.thip.base.comuniVenAcq.TipoRiga;
import it.thera.thip.base.documenti.DocumentoBase;
import it.thera.thip.base.documenti.web.DocumentoDataCollector;
import it.thera.thip.base.generale.Numeratore;
import it.thera.thip.base.generale.ParametroPsn;
import it.thera.thip.base.generale.Serie;
import it.thera.thip.base.generale.UnitaMisura;
import it.thera.thip.base.prezziExtra.DocOrdRigaPrezziExtra;
import it.thera.thip.cs.DatiComuniEstesi;
import it.thera.thip.cs.ThipException;
import it.thera.thip.magazzino.generalemag.PersDatiMagazzino;
import it.thera.thip.vendite.documentoVE.DocumentoVenRigaLottoPrm;
import it.thera.thip.vendite.documentoVE.DocumentoVenRigaPrm;
import it.thera.thip.vendite.documentoVE.DocumentoVenRigaSec;
import it.thera.thip.vendite.documentoVE.DocumentoVendita;
import it.thera.thip.vendite.documentoVE.DocumentoVenditaRiga;
import it.thera.thip.vendite.documentoVE.web.DocumentoVenditaDataCollector;
import it.thera.thip.vendite.generaleVE.CausaleDocumentoVendita;
import it.thera.thip.vendite.generaleVE.CausaleRigaDocVen;
import it.thera.thip.vendite.generaleVE.CausaleRigaOrdVen;
import it.thera.thip.vendite.generaleVE.PersDatiVen;
import it.thera.thip.vendite.generaleVE.TipoReperimento;
import it.thera.thip.vendite.ordineVE.GestoreEvasioneVendita;
import it.thera.thip.vendite.ordineVE.OrdineVendita;
import it.thera.thip.vendite.ordineVE.OrdineVenditaRiga;
import it.thera.thip.vendite.ordineVE.OrdineVenditaRigaLottoPrm;
import it.thera.thip.vendite.ordineVE.OrdineVenditaRigaPrm;
import it.thera.thip.vendite.ordineVE.OrdineVenditaRigaSec;
import it.thera.thip.vendite.ordineVE.OrdineVenditaTestata;
import it.thera.thip.vendite.ordineVE.ParamRigaPrmDocEvaVen;
import it.thera.thip.vendite.prezziExtra.DocRigaPrezziExtraVendita;
import it.thera.thip.vendite.prezziExtra.OrdineRigaPrezziExtraVendita;

/**
 * <h1>Softre Solutions</h1>
 * 
 * @author Daniele Signoroni 04/10/2022 <br>
 *         </br>
 *         <b>70687 DSSOF3 04/10/2022</b>
 *         <p>
 *         Classe per l'evasione di un uds vendita, contiene i parametri della
 *         form.<br>
 *         E contiene inoltre i metodi per eseguire l'evasione.
 *         </p>
 *         <b>71469 DSSOF3 28/03/2024</b>
 *         <p>
 *         Introdurre recupero righe uds figlie da altre uds tramite uds
 *         padre.<br>
 *         Rimuovere checkBloccoImmissione su cliente.<br>
 *         </p>
 *         <b>71512 DSSOF3 19/04/2024</b>
 *         <p>
 *         Aggiungere check su qta uds rig > 0.<br>
 *         Aggiungere scarico dei lotti dall'ordine di vendita.<br>
 *         </p>
 *         <b>71512 DSSOF3 26/04/2024</b>
 *         <p>
 *         Ridisegnazione completa dell'evasione.<br>
 *         A parita' di riga ordine le righe uds vengono accorpate, ora in
 *         maniera corretta.<br>
 *         </p>
 */

/*
 * Revisions:
 * Number   Date        Owner    Description
 * 72463    29/04/2026  DSSOF3   Accendere il flag salva testata quando salvo una riga, mi serve per compilare i totali.
 */

public class YEvasioneUdsVendita extends DocumentoBase {

	protected String iIdAzienda;

	protected String iRSerie;

	protected Date iDataDocumento;

	protected String iRCauDocTes;

	protected String iRCliente;

	protected Date iDataRifIntestatario;

	protected String iNumeroRifIntestatario;

	protected String iRIdNumeratoreSerie;

	protected Proxy iRelRCauDocTes = new Proxy(it.thera.thip.vendite.generaleVE.CausaleDocumentoVendita.class);

	protected Proxy iRelSerie = new Proxy(it.thera.thip.base.generale.Serie.class);

	protected Proxy iRelCliente = new Proxy(it.thera.thip.base.cliente.ClienteVendita.class);

	protected Proxy iRelNumeratore = new Proxy(Numeratore.class);

	protected Proxy iMagazzinoTrasferimento = new Proxy(it.thera.thip.base.azienda.Magazzino.class);

	protected String iAzione;

	protected boolean iGeneraDocumento = false;

	@SuppressWarnings("rawtypes")
	protected List iRigheEstratte;
	protected String[] iChiaviSelezionate = null;

	public YEvasioneUdsVendita() {
		setIdAzienda(Azienda.getAziendaCorrente());
		setRIdNumeratoreSerie("DOC_VEN");
	}

	public String getIdAzienda() {
		return iIdAzienda;
	}

	public void setIdAzienda(String iIdAzienda) {
		this.iIdAzienda = iIdAzienda;
		setIdAziendaInternal(iIdAzienda);
	}

	protected void setIdAziendaInternal(String idAzienda) {
		String key1 = iRelCliente.getKey();
		iRelCliente.setKey(KeyHelper.replaceTokenObjectKey(key1, 1, idAzienda));
		String key2 = iRelRCauDocTes.getKey();
		iRelRCauDocTes.setKey(KeyHelper.replaceTokenObjectKey(key2, 1, idAzienda));
		iRelSerie.setKey(KeyHelper.replaceTokenObjectKey(iRelSerie.getKey(), 1, idAzienda));
		iRelNumeratore.setKey(KeyHelper.replaceTokenObjectKey(iRelNumeratore.getKey(), 1, idAzienda));
		iMagazzinoTrasferimento.setKey(KeyHelper.replaceTokenObjectKey(iMagazzinoTrasferimento.getKey(), 1, idAzienda));
	}

	public String getRSerie() {
		return KeyHelper.getTokenObjectKey(iRelSerie.getKey(), 3);
	}

	public Date getDataRifIntestatario() {
		return iDataRifIntestatario;
	}

	public void setDataRifIntestatario(Date iDataRifIntestatario) {
		this.iDataRifIntestatario = iDataRifIntestatario;
	}

	public String getNumeroRifIntestatario() {
		return iNumeroRifIntestatario;
	}

	public void setNumeroRifIntestatario(String iNumeroRifIntestatario) {
		this.iNumeroRifIntestatario = iNumeroRifIntestatario;
	}

	public String getRIdNumeratoreSerie() {
		return KeyHelper.getTokenObjectKey(iRelNumeratore.getKey(), 2);
	}

	public void setRIdNumeratoreSerie(String iRIdNumeratoreSerie) {
		iRelSerie.setKey(KeyHelper.replaceTokenObjectKey(iRelSerie.getKey(), 2, iRIdNumeratoreSerie));
		iRelSerie.setKey(KeyHelper.replaceTokenObjectKey(iRelSerie.getKey(), 1, Azienda.getAziendaCorrente()));
		iRelNumeratore.setKey(KeyHelper.replaceTokenObjectKey(iRelNumeratore.getKey(), 2, iRIdNumeratoreSerie));
		iRelNumeratore.setKey(KeyHelper.replaceTokenObjectKey(iRelNumeratore.getKey(), 1, Azienda.getAziendaCorrente()));
	}

	public void setNumeratoreKey(String key) {
		iRelNumeratore.setKey(key);
	}

	public String getNumeratoreKey() {
		return iRelNumeratore.getKey();
	}

	public void setNumeratore(Numeratore numRcvFsc) {
		iRelNumeratore.setObject(numRcvFsc);
	}

	public Numeratore getNumeratore() {
		return (Numeratore) iRelNumeratore.getObject();
	}

	public void setRSerie(String iIdSerie) {
		iRelSerie.setKey(KeyHelper.replaceTokenObjectKey(iRelSerie.getKey(), 3, iIdSerie));
	}

	public Date getDataDocumento() {
		return iDataDocumento;
	}

	public void setDataDocumento(Date iDataDocumento) {
		this.iDataDocumento = iDataDocumento;
	}

	public String getRCauDocTes() {
		String key = iRelRCauDocTes.getKey();
		String rCausale = KeyHelper.getTokenObjectKey(key,2);
		return rCausale;
	}

	public void setRCauDocTes(String rCausale) {
		String key = iRelRCauDocTes.getKey();
		iRelRCauDocTes.setKey(KeyHelper.replaceTokenObjectKey(key , 2, rCausale));
	}

	public String getRCliente() {
		String key = iRelCliente.getKey();
		String objRCliente = KeyHelper.getTokenObjectKey(key,2);
		return objRCliente;
	}

	public void setRCliente(String iRCliente) {
		String key = iRelCliente.getKey();
		iRelCliente.setKey(KeyHelper.replaceTokenObjectKey(key , 2, iRCliente));
	}

	public CausaleDocumentoVendita getCausale() {
		return (CausaleDocumentoVendita) iRelRCauDocTes.getObject();
	}

	public void setCausale(CausaleDocumentoVendita iCausale) {
		this.iRelRCauDocTes.setObject(iCausale);
	}

	public void setCausaleKey(String key) {
		iRelRCauDocTes.setKey(key);
		String idAzienda = KeyHelper.getTokenObjectKey(key, 1);
		setIdAzienda(idAzienda);
	}

	public String getCausaleKey() {
		return iRelRCauDocTes.getKey();
	}

	public void setSerie(Serie serie) {
		this.iRelSerie.setObject(serie);
		setOnDB(false);
	}

	public Serie getSerie() {
		return (Serie) iRelSerie.getObject();
	}

	public void setSerieKey(String key) {
		iRelSerie.setKey(key);
		setOnDB(false);
	}

	public String getSerieKey() {
		return iRelSerie.getKey();
	}

	public void setRelcliente(ClienteVendita relcliente) {
		String idAzienda = getIdAzienda();
		if (relcliente != null) {
			idAzienda = KeyHelper.getTokenObjectKey(relcliente.getKey(), 1);
		}
		setIdAzienda(idAzienda);
		this.iRelCliente.setObject(relcliente);
	}

	public ClienteVendita getRelcliente() {
		return (ClienteVendita) iRelCliente.getObject();
	}

	public void setRelclienteKey(String key) {
		iRelCliente.setKey(key);
		String idAzienda = KeyHelper.getTokenObjectKey(key, 1);
		setIdAzienda(idAzienda);
	}

	public String getRelclienteKey() {
		return iRelCliente.getKey();
	}

	public boolean isGeneraDocumento() {
		return iGeneraDocumento;
	}

	public void setGeneraDocumento(boolean iGeneraDocumento) {
		this.iGeneraDocumento = iGeneraDocumento;
	}

	public Magazzino getMagazzinoTrasferimento() {
		return (Magazzino)iMagazzinoTrasferimento.getObject();
	}

	public void setMagazzinoTrasferimento(Magazzino iMagazzino) {
		this.iMagazzinoTrasferimento.setObject(iMagazzino);
	}

	public String getMagazzinoTrasferimentoKey() {
		return iMagazzinoTrasferimento.getKey();
	}

	public void setMagazzinoTrasferimentoKey(String key) {
		iMagazzinoTrasferimento.setKey(key);
	}

	public String getIdMagazzinoTrasferimento() {
		String key = iMagazzinoTrasferimento.getKey();
		String rMagazzino = KeyHelper.getTokenObjectKey(key,2);
		return rMagazzino;
	}

	public void setIdMagazzinoTrasferimento(String rMagazzino) {
		String key = iMagazzinoTrasferimento.getKey();
		iMagazzinoTrasferimento.setKey(KeyHelper.replaceTokenObjectKey(key , 2, rMagazzino));
	}

	public String getAzione() {
		return iAzione;
	}
	public void setAzione(String iAzione) {
		this.iAzione = iAzione;
	}

	@SuppressWarnings("rawtypes")
	public List getRigheEstratte() throws ThipException {
		if (iRigheEstratte == null || this.iRigheEstratte.isEmpty()) {
			iRigheEstratte = this.estraiRigheOrdine();
		}
		return iRigheEstratte;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List ordinaRigheEstratte() {
		Collections.sort(iRigheEstratte, new Comparator() {
			public int compare(Object o1, Object o2) {
				YEvasioneUdsVenRiga r1 = (YEvasioneUdsVenRiga) o1;
				YEvasioneUdsVenRiga r2 = (YEvasioneUdsVenRiga) o2;

				OrdineVenditaRigaPrm ord1 = r1.getRigaOrdine();
				OrdineVenditaRigaPrm ord2 = r2.getRigaOrdine();

				// Se uno dei due è null, lo metti in fondo
				if (ord1 == null && ord2 == null) return 0;
				if (ord1 == null) return 1;
				if (ord2 == null) return -1;

				// Compara per anno
				int cmp = ord1.getAnnoDocumento().compareTo(ord2.getAnnoDocumento());
				if (cmp != 0) return cmp;

				// Compara per numero
				cmp = ord1.getNumeroDocumento().compareTo(ord2.getNumeroDocumento());
				if (cmp != 0) return cmp;

				// Compara per sequenza
				return Integer.valueOf(ord1.getSequenzaRiga()).compareTo(Integer.valueOf(ord2.getSequenzaRiga()));
			}
		});
		return iRigheEstratte;
	}

	public String[] getChiaviSelezionate() {
		return iChiaviSelezionate;
	}

	public void setChiaviSelezionate(String[] iChiaviSelezionate) {
		this.iChiaviSelezionate = iChiaviSelezionate;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected List estraiRigheOrdine() throws ThipException {
		List righeEstratte = new ArrayList();
		List<YUdsVendita> testate = recuperaListaUdsVenditaDaSelezionate(getChiaviSelezionate());
		List<YUdsVenRig> righe = recuperaListaUdsVenditaRigheDaUdsVendita(testate);
		Collections.sort(righe, new YOrdinamentoUdsVenRigComparator().orderByIdArticolo);
		List<YOggettinoRigheAccorpate> righeAccorpate = recuperaRigheAccorpate(righe);
		Iterator<YOggettinoRigheAccorpate> iter = righeAccorpate.iterator();
		while (iter.hasNext()) {
			YEvasioneUdsVenRiga riga = creaRiga();
			boolean isKit = false;
			YOggettinoRigheAccorpate rigaUdsAccorpata = (YOggettinoRigheAccorpate) iter.next();
			YUdsVenRig rigaUds = rigaUdsAccorpata.riga;
			if(rigaUds.getDocumentoVenditaRiga() == null) {
				if (rigaUds.getQtaPrm().compareTo(BigDecimal.ZERO) == 0) {
					continue;
				}
				OrdineVenditaRigaPrm rigaOrdine = null;
				OrdineVenditaRiga rigaOvr = (OrdineVenditaRiga) rigaUds.getOrdineVenditaRigaSec();
				if(rigaOvr != null && rigaOvr instanceof OrdineVenditaRigaSec)
					rigaOrdine = ((OrdineVenditaRigaSec)rigaOvr).getRigaPrimaria();
				else
					rigaOrdine = (OrdineVenditaRigaPrm) rigaUds.getOrdineVenditaRiga();
				if(rigaOrdine != null && rigaOrdine.getArticolo().getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST)
					isKit = true;
				assegnaDatiRiga(riga, rigaUds, rigaOrdine);
				riga.setQtaDaSpedireInUMRif(rigaUdsAccorpata.getQuantita());
				riga.getRigheUdsAccorpate().addAll(rigaUdsAccorpata.righeAccorpate);
				righeEstratte.add(riga);

				if (isKit) {
					Map<String, BigDecimal> qtaPerArticolo = new HashMap<>();
					Map<String, BigDecimal> coeffPerArticolo = new HashMap<>();
					for (YUdsVenRig rigaSec : (List<YUdsVenRig>) riga.getRigheUdsAccorpate()) {
						OrdineVenditaRigaSec ovrSec = (OrdineVenditaRigaSec) rigaSec.getOrdineVenditaRigaSec();
						if (ovrSec == null 
								|| ovrSec.getCoefficienteImpiego() == null 
								|| ovrSec.getCoefficienteImpiego().compareTo(BigDecimal.ZERO) == 0) {

							throw new ThipException(new ErrorMessage("BAS0000078", "Coefficiente di impiego non valido nel KIT"));
						}
						String articolo = rigaSec.getRArticolo();
						qtaPerArticolo.merge(articolo, rigaSec.getQtaPrm(), BigDecimal::add);
						coeffPerArticolo.putIfAbsent(articolo, ovrSec.getCoefficienteImpiego());
					}

					BigDecimal kitMin = null;
					for (String articolo : qtaPerArticolo.keySet()) {
						BigDecimal qta = qtaPerArticolo.get(articolo);
						BigDecimal coeff = coeffPerArticolo.get(articolo);
						BigDecimal kit = Q6Calc.get().divide(qta, coeff, 6, RoundingMode.HALF_DOWN);

						if (kitMin == null || kit.compareTo(kitMin) < 0) {
							kitMin = kit;
						}
					}
					riga.setQtaDaSpedireInUMRif(kitMin);
				}
			}
		}
		return righeEstratte;
	}

	public static YEvasioneUdsVenRiga creaRiga() {
		return (YEvasioneUdsVenRiga) Factory.createObject(YEvasioneUdsVenRiga.class);
	}

	protected void assegnaDatiRiga(YEvasioneUdsVenRiga riga, YUdsVenRig udsVenRig, OrdineVenditaRigaPrm ordVenRig) {
		if(udsVenRig != null) {
			riga.setRigaUdsVendita(udsVenRig);
			riga.setUdsVendita(udsVenRig.getParent());
			riga.setArticolo(udsVenRig.getRelarticolo());
		}
		if(ordVenRig != null) {
			riga.setRigaOrdine(ordVenRig);
			riga.setArticolo(ordVenRig.getArticolo());
		}
	}

	public String getRagioneSocialeCli() {
		if(getRelcliente() != null) {
			return getRelcliente().getRagioneSociale();
		}
		return "";
	}

	public String getCausaleDescrizione() {
		if(getCausale() != null) {
			return getCausale().getDescrizione().getDescrizione();
		}
		return "";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Vector checkAll(BaseComponentsCollection components) {
		java.util.Vector errors = new java.util.Vector();
		components.runAllChecks(errors);
		ErrorMessage em = checkSerieCausale();
		if (em != null)
			errors.add(em);
		if(isGeneraDocumento()) {
			Iterator iterRigheEstratte;
			try {
				iterRigheEstratte = getRigheEstratte().iterator();
				while (iterRigheEstratte.hasNext()) {
					YEvasioneUdsVenRiga riga = (YEvasioneUdsVenRiga) iterRigheEstratte.next();
					if(riga.isRigaEstratta()
							&& (riga.getQtaDaSpedireInUMRif() == null
							|| riga.getQtaDaSpedireInUMRif().compareTo(BigDecimal.ZERO) == 0)) {
						errors.add(new ErrorMessage("YSOFTRE001", "Riga "+riga.getSequenza()+" qta' immessa non valida"));
					}
				}
			} catch (ThipException e) {
				e.printStackTrace(Trace.excStream);
			}
			List<YUdsVendita> testate = recuperaListaUdsVenditaDaSelezionate(getChiaviSelezionate());
			em = it.softre.thip.vendite.uds.web.YEvasioneUdsVendita.checkCongruenzaCliente(testate);
			if(em != null) {
				errors.add(em);
			}
			em = it.softre.thip.vendite.uds.web.YEvasioneUdsVendita.checkUdsEvase(testate,getAzione());
			if(em != null) {
				errors.add(em);
			}
			em = it.softre.thip.vendite.uds.web.YEvasioneUdsVendita.checkPresenzaRigheUds(testate);
			if(em != null) {
				errors.add(em);
			}
			List rows = new ArrayList();
			Iterator iterList = testate.iterator();
			while (iterList.hasNext()) {
				YUdsVendita uds = (YUdsVendita) iterList.next();
				rows.addAll(uds.getRigheUDSVendita());
				try {
					rows.addAll(uds.getRigheUDSVenditaDaFigli());
				} catch (SQLException e) {
					e.printStackTrace(Trace.excStream);
				}
			}
			em = YUdsVenRig.congruenzaIndirizzoSpedizione(rows);	
			if(em != null) {
				errors.add(em);
			}
		}

		//.Se l'azione e' trasferisci ma la causale non supporta questa opzione do' errore
		if(getAzione().equals(YUdsVenditaGridActionAdapter.TRASFERISCI_UDS)
				&& getCausale() != null && !getCausale().isTrasferimentoTraMag()) {
			errors.add(new ErrorMessage("YSOFTRE001","Si sta trasferendo l'UDS tra magazzini, scegliere una causale di trasferimento"));
		}
		return errors;
	}

	protected ErrorMessage checkSerieCausale() {
		ErrorMessage em = null;
		if (getCausale() != null && getCausale().getTipiGestione().getTPGestioneRiferimentoCli().getTipoGestione() == '1'
				&& (this.getDataRifIntestatario() == null || this.getNumeroRifIntestatario() == null)) {
			em = new ErrorMessage("YSOFTRE003", "Data e numero riferimento sono obbligatori");
		}
		return em;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int save() throws SQLException {
		Map<DocumentoVenRigaPrm, YEvasioneUdsVenRiga> righeUdsDaAggiornare = new HashMap<DocumentoVenRigaPrm, YEvasioneUdsVenRiga>();
		Map<YUdsVenRig, DocumentoVenRigaSec> righeUdsDaAggiornareDettaglioSecondaria = new HashMap<YUdsVenRig, DocumentoVenRigaSec>();
		List<YUdsVendita> testate = recuperaListaUdsVenditaDaSelezionate(getChiaviSelezionate());
		DocumentoVendita documentoVendita = creaDocumentoVenditaTestata();
		assegnaDatiTestata(documentoVendita,testate);
		DocumentoDataCollector docBODC = creaDocumentoVenditaTestataBODC(documentoVendita);
		int rc = docBODC.save();
		if (rc == BODataCollector.OK || rc == BODataCollector.WARNING) {
			Iterator iterRigheEstratte = getRigheEstratte().iterator();
			if(!iterRigheEstratte.hasNext()) {
				throw new ThipException(new ErrorMessage("YSOFTRE001", "Nessuna riga estratta"));
			}else {
				while (iterRigheEstratte.hasNext()) {
					YEvasioneUdsVenRiga riga = (YEvasioneUdsVenRiga) iterRigheEstratte.next();
					if(riga.isRigaEstratta()
							|| (riga.getUdsVendita() != null)) {
						DocumentoVenRigaPrm rigaDocumentoVE = creaDocumentoVenditaRigaPrm(documentoVendita, riga);

						if(riga.isRigaSaldata()) {
							rigaDocumentoVE.setRigaSaldata(true);
						}
						//rigaDocumentoVE.setSalvaTestata(false); 72463 rem
						//72463 accendere il salva testata < 
						rigaDocumentoVE.setSalvaTestata(true);
						//72463 >

						aggiornaAttributiDaRigaOrdine(rigaDocumentoVE, riga);

						rigaDocumentoVE.setGeneraRigheSecondarie(false);

						if (riga.getRigheUdsAccorpate().size() > 0) {
							Map<String, List<YUdsVenRig>> righePerArticolo = new HashMap<>();
							Iterator<YUdsVenRig> righeAccorpateDeep = riga.getRigheUdsAccorpate().iterator();
							while (righeAccorpateDeep.hasNext()) {
								YUdsVenRig rigaUdsAccorpata = righeAccorpateDeep.next();
								String articolo = rigaUdsAccorpata.getRArticolo();
								righePerArticolo.computeIfAbsent(articolo, k -> new ArrayList<>()).add(rigaUdsAccorpata);
							}
							for (List<YUdsVenRig> righeArticolo : righePerArticolo.values()) {
								YUdsVenRig rigaRiferimento = righeArticolo.get(0);
								OrdineVenditaRigaSec ovrSec = (OrdineVenditaRigaSec) rigaRiferimento.getOrdineVenditaRigaSec();
								if (ovrSec != null) {
									DocumentoVenRigaSec rigaSec = creaDocumentoVenditaRigaSec(rigaDocumentoVE, rigaRiferimento);
									aggiornaAttributiDaRigaOrdine(rigaSec, riga);
									if (rigaSec != null) {
										rigaDocumentoVE.getRigheSecondarie().add(rigaSec);
										for (YUdsVenRig r : righeArticolo) {
											righeUdsDaAggiornareDettaglioSecondaria.put(r, rigaSec);
										}
									}
								}
							}
						}
						rigaDocumentoVE.setServizioCalcDatiVendita(false);
						DocumentoDataCollector docBODCRigaPrm = creaDocumentoVenditaRigaPrmBODC(rigaDocumentoVE);
						int rcRig = docBODCRigaPrm.save();
						if(rcRig == DocumentoVenditaDataCollector.OK || rcRig == BODataCollector.WARNING) {
							righeUdsDaAggiornare.put(rigaDocumentoVE, riga);
						}else{
							throw new ThipException(docBODCRigaPrm.getErrorList().getErrors());
						}

					}
				}
				//72XXX <
				List<OggettinoContenitore> contenitori = recuperaContenitori(iRigheEstratte);
				if(contenitori.size() > 0) {
					String idCauRigCnr = getCausaleRigaDvContenitori();
					if(documentoVendita.isCausaleRigaSelezionata(KeyHelper.buildObjectKey(new String[] {
							Azienda.getAziendaCorrente(), idCauRigCnr
					}))) {
						for (Iterator iterator = contenitori.iterator(); iterator.hasNext();) {
							OggettinoContenitore ogg = (OggettinoContenitore) iterator.next();
							DocumentoVenRigaPrm rigaDv = creaDocumentoVenditaRigaPrmContenitore(documentoVendita, ogg, idCauRigCnr, getMagazzinoRigaDvContenitori());
							rigaDv.setSalvaTestata(true);
							rigaDv.setServizioCalcDatiVendita(false);
							DocumentoDataCollector docBODCRigaPrm = creaDocumentoVenditaRigaPrmBODC(rigaDv);
							int rcRig = docBODCRigaPrm.save();
							if(rcRig == BODataCollector.ERROR) {
								throw new ThipException(docBODCRigaPrm.getErrorList().getErrors());
							}
						}
					}
				}
				//72XXX >
				if(rc == BODataCollector.OK || rc == BODataCollector.WARNING) {
					setKey(docBODC.getBo().getKey()); //.Setto la chiave del documento salvato
					// Aggiorno i riferimento delle testate che sono state selezionate
					for (YUdsVendita testata : testate) {
						aggiornaRiferimentiDocumentoVenditaTestataUds(testata, documentoVendita);
						testata.rendiDefinitivaUdsVendita();
						testata.save();
					}
					for (Map.Entry<DocumentoVenRigaPrm, YEvasioneUdsVenRiga> entry : righeUdsDaAggiornare.entrySet()) {
						DocumentoVenRigaPrm rigaDocumentoVE = entry.getKey();
						YEvasioneUdsVenRiga riga = entry.getValue();
						YUdsVenRig rigaUds = riga.getRigaUdsVendita();
						//.Aggiorno i riferimenti del documento sull'uds se presente
						if(rigaUds != null) {
							aggiornaRiferimentiDocumentoVenditaRigaUds(rigaUds, rigaDocumentoVE);
							rigaUds.rendiDefinitivaRigaUdsVendita();
							rigaUds.save();
						}

						// ora aggiorniamo eventuali riferimenti righe accorpate deep
						Iterator<YUdsVenRig> righeAccorpateDeep = riga.getRigheUdsAccorpate().iterator();
						while (righeAccorpateDeep.hasNext()) {
							YUdsVenRig rigaUdsAccorpata = righeAccorpateDeep.next();
							aggiornaRiferimentiDocumentoVenditaRigaUds(rigaUdsAccorpata, rigaDocumentoVE);
							rigaUdsAccorpata.rendiDefinitivaRigaUdsVendita();
							rigaUdsAccorpata.save();
						}
					}
					for (Map.Entry<YUdsVenRig ,DocumentoVenRigaSec> entry : righeUdsDaAggiornareDettaglioSecondaria.entrySet()) {
						YUdsVenRig rigaUds = entry.getKey();
						DocumentoVenRigaSec rigaDvrSec = entry.getValue();

						rigaUds.setRRigaDetDocVen(rigaDvrSec.getDettaglioRigaDocumento());
						rigaUds.save();
					}
				}else {
					throw new ThipException(docBODC.getErrorList().getErrors());
				}
			}
		} else {
			throw new ThipException(docBODC.getErrorList().getErrors());
		}
		return 1;
	}

	@SuppressWarnings("rawtypes")
	protected void assegnaDatiTestata(DocumentoVendita documentoVendita, List<YUdsVendita> testate) {
		if(getAzione().equals(YUdsVenditaGridActionAdapter.TRASFERISCI_UDS)) { //.Se e' un trasferimento setto da dove viene
			documentoVendita.setMagazzinoTrasferimento(getMagazzinoTrasferimento());
		}
		String idMagazzino = null;
		for (Iterator<YUdsVendita> iterator = testate.iterator(); iterator.hasNext();) {
			YUdsVendita udsVen = (YUdsVendita) iterator.next();
			if(udsVen.getRMagazzino() != null) {
				idMagazzino = udsVen.getRMagazzino();
			}
		}
		documentoVendita.setIdMagazzino(idMagazzino);
		if(getAzione().equals(YUdsVenditaGridActionAdapter.TRASFERISCI_UDS)) { 
			documentoVendita.setAbilitaCheckBloccoImmissione(false);
		}
		BigDecimal[] totalePesi = recuperaTotalePesiDaTestate(testate);
		BigDecimal totPesoNetto = totalePesi[1];
		BigDecimal totPesoLordo = totalePesi[0];
		documentoVendita.setPesoLordo(totPesoLordo);
		documentoVendita.setPesoNetto(totPesoNetto);
		documentoVendita.setRicalcolaPesiEVolume(false);
		documentoVendita.setNumeroColli(testate.size()); // aggiungere numero colli...sono le chiavi selezionate
		documentoVendita.setSalvaRighe(false);

		OrdineVendita ordine = null;
		try {
			Iterator iterRigheEstratte = getRigheEstratte().iterator();
			while (iterRigheEstratte.hasNext()) {
				YEvasioneUdsVenRiga riga = (YEvasioneUdsVenRiga) iterRigheEstratte.next();
				if(riga.getRigaOrdine() != null) {
					ordine = (OrdineVendita) riga.getRigaOrdine().getTestata();
					break;
				}
			}
		} catch (ThipException e) {
			e.printStackTrace();
		}
		if(ordine != null)
			aggiornaAttributiDaOrdine(documentoVendita, ordine);
	}

	protected static void aggiornaAttributiDaOrdine(DocumentoVendita docVen ,OrdineVendita ordine) {
		// fix 4445 16/11/05
		docVen.setNota(ordine.getNota());
		// Fine fix 4445
		// Fix 1932
		docVen.setRAnnoOrd(ordine.getAnnoDocumento());
		docVen.setRNumeroOrd(ordine.getNumeroDocumento());
		//}//Fix 16038
		// Fine fix 1932
		docVen.setNumeroOrdineIntestatario(ordine.getNumeroOrdineIntestatario());
		docVen.setDataOrdineIntestatario(ordine.getDataOrdineIntestatario());
		// Fix 2481
		docVen.setFornitoreKey(ordine.getFornitoreKey());
		docVen.setPriorita(ordine.getPriorita());

		// Fix 3016
		docVen.setRAnnoCantiere(ordine.getRAnnoCantiere());
		docVen.setRNumeroCantiere(ordine.getRNumeroCantiere());
		// fine fix 3016
		char rep = docVen.getReperimentoCondEvasione();
		if (rep == TipoReperimento.DA_ORDINE) {
			caricaDatiClienteDaOrdine(docVen,ordine);
			if (docVen.getAssoggettamentoIVA() == null) {
				PersDatiVen pdv = PersDatiVen.getCurrentPersDatiVen();
				if (pdv == null) {
					Trace.println("Impossibile creare il documento di vendita perchè PersDatiVen non è valorizzato");
					Trace.println(
							"Eccezione nel recupero di PersDatiVen in completaBO() di " +
									docVen.getClass().getName());
					return;
				}
				docVen.setAssoggettamentoIVA(pdv.getAssIVADocOrd());
			}
		}
		else if (rep == TipoReperimento.CLIENTE_FATTURAZIONE) {
			ClienteVendita cv = docVen.getClienteFatturazione();
			if (cv != null) {
				String keyCliDiv =
						KeyHelper.buildObjectKey(
								new String[] {
										Azienda.getAziendaCorrente(), cv.getIdCliente(), null
								}
								);

				docVen.caricaDatiDaCliente(cv, keyCliDiv);
				if (docVen.getAssoggettamentoIVA() == null) {
					PersDatiVen pdv = PersDatiVen.getCurrentPersDatiVen();
					if (pdv == null) {
						Trace.println("Impossibile creare il documento di vendita perchè PersDatiVen non è valorizzato");
						Trace.println(
								"Eccezione nel recupero di PersDatiVen in completaBO() di " +
										docVen.getClass().getName());
						return;
					}
					docVen.setAssoggettamentoIVA(pdv.getAssIVADocOrd());
				}
			}
		}
		docVen.setMagazzinoKey(ordine.getMagazzinoKey());
		docVen.setMagazzinoTrasferimentoKey(ordine.getMagazzinoTrasferimentoKey());
		docVen.setCommessaKey(ordine.getCommessaKey());
		docVen.setCentroCostoKey(ordine.getCentroCostoKey());
		docVen.setCambio(ordine.getCambio());
		docVen.setDataInizioTrasporto(ordine.getDataInizioTrasporto());
		docVen.setOraInizioTrasporto(ordine.getOraInizioTrasporto());
		docVen.setNumeroColli(ordine.getNumeroColli());
		docVen.setFlagRiservatoUtente1(ordine.getFlagRiservatoUtente1());
		docVen.setFlagRiservatoUtente2(ordine.getFlagRiservatoUtente2());
		docVen.setFlagRiservatoUtente3(ordine.getFlagRiservatoUtente3());
		docVen.setFlagRiservatoUtente4(ordine.getFlagRiservatoUtente4());
		docVen.setFlagRiservatoUtente5(ordine.getFlagRiservatoUtente5());
		docVen.setAlfanumRiservatoUtente1(ordine.getAlfanumRiservatoUtente1());
		docVen.setAlfanumRiservatoUtente2(ordine.getAlfanumRiservatoUtente2());
		docVen.setNumeroRiservatoUtente1(ordine.getNumeroRiservatoUtente1());
		docVen.setNumeroRiservatoUtente2(ordine.getNumeroRiservatoUtente2());
		docVen.setSpesePercPresenti(ordine.isSpesePercPresenti());
		docVen.setIdLineaCredito(ordine.getIdLineaCredito());//Fix 19385
		docVen.completaDatiDestinatarioFatturaPA();//Fix 21534
		docVen.setRaggruppamentoBolleFat(ordine.getRaggruppamentoBolleFat());//Fix 26361
		docVen.setCliRapprFiscali(ordine.getCliRapprFiscali());//Fix 36915
	}

	public static void caricaDatiClienteDaOrdine(DocumentoVendita docVen ,OrdineVendita ordine) {
		// fix 2525
		// Se il cliente intestario è diverso dal cliente da ordine i dati
		// di destinazione vengono presi dal cliente intestatario. E quindi non
		// vengono ripresi dall'ordine.
		boolean datiDestDaOrdine = true;
		if (datiDestDaOrdine) {
			// fine fix 2525
			docVen.setClienteDestinatario(ordine.getClienteDestinatario());
			docVen.setRagioneSocaleDest(ordine.getRagioneSocaleDest());
			docVen.setIndirizzoDestinatario(ordine.getIndirizzoDestinatario());
			docVen.setLocalitaDestinatario(ordine.getLocalitaDestinatario());
			docVen.setCAPDestinatario(ordine.getCAPDestinatario());
			// fix 2402
			docVen.setIdSequenzaInd(ordine.getIdSequenzaInd());
			// fine fix 2402

			// Fix 1461
			if (ordine.getClienteDestinatario() != null ||
					ordine.getRagioneSocaleDest() != null ||
					ordine.getIndirizzoDestinatario() != null ||
					ordine.getLocalitaDestinatario() != null || ordine.getCAPDestinatario() != null) {
				docVen.setIdSequenzaInd(null);
			}

			// Fine fix 1461
			docVen.setNazioneKey(ordine.getNazioneKey());
			docVen.setProvinciaKey(ordine.getProvinciaKey());
			// fix 2525
		}
		// fine fix 2525
		//Fix 27557 inizio
		/*docVen.getIdentificativoBanca().setCodificaSIA(ordine.getIdentificativoBanca().
	                                                 isCodificaSIA());
	    docVen.getIdentificativoBanca().setBancaABICABKey(ordine.
	        getIdentificativoBanca().getBancaABICABKey());
	    docVen.getIdentificativoBanca().setBancaNonABICABKey(ordine.
	        getIdentificativoBanca().getBancaNonABICABKey());
	    docVen.getIdentificativoBanca().setBancaNonABICAB(ordine.
	        getIdentificativoBanca().getBancaNonABICAB());*/
		try {
			docVen.getIdentificativoBanca().setEqual(getIdentificativoBancaDaUsare(ordine.getIdCliente(),ordine.getIdentificativoBanca(), docVen.getIdentificativoBanca(),docVen.getIdCliente()));
		} catch (CopyException e) {
			e.printStackTrace();
		}
		//Fix 27557 fine
		docVen.setIdCatVenCli(ordine.getIdCatVenCli());
		//docVen.setContoCorrente(ordine.getContoCorrente());//Fix 27557
		docVen.setContoCorrente(getValoreContoCorrenteDaUsare(ordine.getIdCliente(),ordine.getContoCorrente(),docVen.getContoCorrente(),docVen.getIdCliente()));//Fix 27557
		docVen.setCodiceIBAN(getValoreCodiceIBANDaUsare(ordine.getIdCliente(),ordine.getCodiceIBAN(),docVen.getCodiceIBAN(),docVen.getIdCliente()));//Fix 28455
		docVen.setSpesa1Key(ordine.getSpesa1Key());
		docVen.setSpesa2Key(ordine.getSpesa2Key());
		docVen.setPrcScontoIntestatario(ordine.getPrcScontoIntestatario());
		docVen.setPrcScontoModalita(ordine.getPrcScontoModalita());
		docVen.setScontoTabellareKey(ordine.getScontoTabellareKey());
		docVen.setLinguaKey(ordine.getLinguaKey());
		docVen.setZonaKey(ordine.getZonaKey());
		docVen.setMicroAreaClientiKey(ordine.getMicroAreaClientiKey());
		docVen.setListinoPrezziKey(ordine.getListinoPrezziKey());
		docVen.setValuta(ordine.getValuta());
		docVen.setModalitaPagamentoKey(ordine.getModalitaPagamentoKey());
		docVen.setDataInizioPagamento(ordine.getDataInizioPagamento());
		docVen.setAgenteKey(ordine.getAgenteKey());
		docVen.setProvvigioneAgente(ordine.getProvvigioneAgente());
		docVen.setSubagenteKey(ordine.getSubagenteKey());
		docVen.setProvvigioneSubagente(ordine.getProvvigioneSubagente());
		// 11053 LC >
		docVen.setDifferenzaPrezzoAgente(ordine.hasDifferenzaPrezzoAgente());
		docVen.setDifferenzaPrezzoSubagente(ordine.hasDifferenzaPrezzoSubagente());
		// 11053 LC <
		docVen.setTipoLiquidazionePrv(ordine.getTipoLiquidazionePrv());
		docVen.setClienteFatturazioneKey(ordine.getClienteFatturazioneKey());
		docVen.setModalitaSpedizioneKey(ordine.getModalitaSpedizioneKey());
		docVen.setDescrModalitaSpedizione(ordine.getDescrModalitaSpedizione());
		docVen.setModalitaConsegnaKey(ordine.getModalitaConsegnaKey());
		docVen.setDescrModalitaConsegna(ordine.getDescrModalitaConsegna());
		docVen.setAspettoEsterioreKey(ordine.getAspettoEsterioreKey());
		docVen.setDescrAspettoEsteriore(ordine.getDescrAspettoEsteriore());
		docVen.setCausaleTrasportoKey(ordine.getCausaleTrasportoKey());
		docVen.setDescrCausaleTrasporto(ordine.getDescrCausaleTrasporto());
		docVen.setVettore1Key(ordine.getVettore1Key());
		docVen.setDescrVettore1(ordine.getDescrVettore1());
		docVen.setVettore2Key(ordine.getVettore2Key());
		docVen.setDescrVettore2(ordine.getDescrVettore2());
		docVen.setVettore3Key(ordine.getVettore3Key());
		docVen.setDescrVettore3(ordine.getDescrVettore3());
		docVen.setCadenzaConsegnaKey(ordine.getCadenzaConsegnaKey());
		docVen.setGiroConsegneKey(ordine.getGiroConsegneKey());
		docVen.setRifDataPerPrezzoSconti(ordine.getRifDataPerPrezzoSconti());
		//MG FIX 8344
		//	    docVen.setRicalcoloPrzScontiFattura(ordine.getRicalcoloPrzScontiFattura());
		docVen.setTpRicPrzScnFat(ordine.getTpRicPrzScnFat());
		docVen.setCondizioneFatturazione(ordine.getCondizioneFatturazione());
		docVen.setPrcScontoFineFattura(ordine.getPrcScontoFineFattura());
		docVen.setPercScontoFinaleFat(ordine.getPercScontoFinaleFat()); //33971
		docVen.setScontoFinaleFat(ordine.getScontoFinaleFat()); //33971    
		//docVen.setTotScontoFinaleFat(ordine.getTotScontoFinaleFat()); //33971   ????
		docVen.setFatturazioneSospesa(ordine.getFatturazioneSospesa());
		docVen.setFatturazionePeriodica(ordine.getFatturazionePeriodica());
		docVen.setResponsabileVenditeKey(ordine.getResponsabileVenditeKey());
		docVen.setCategoriaContabileKey(ordine.getCategoriaContabileKey());
		docVen.setGruppoContiAnaliticaKey(ordine.getGruppoContiAnaliticaKey());
		docVen.setAdbSpeseIncasso(ordine.getAdbSpeseIncasso());
		docVen.setAdbSpeseBolloFatSenzaIVA(ordine.getAdbSpeseBolloFatSenzaIVA());
		docVen.setAssoggettamentoIVA(ordine.getAssoggettamentoIVA());
		docVen.setInternalTipoDestinatario(); //Fix 21534
		docVen.completaDatiDestinatarioFatturaPA();//Fix 21534
	}

	public static IdentificativoBanca getIdentificativoBancaDaUsare(String idClienteOrdine,IdentificativoBanca identBancaDaOrdine, IdentificativoBanca identBancaAttuale,
			String idCliente) {
		if(!Utils.areEqual(idClienteOrdine, idCliente))
			return identBancaAttuale;
		return identBancaDaOrdine;
	}

	public static String getValoreContoCorrenteDaUsare(String idClienteOrdine, String contoCorrenteOrdine, String contoCorrenteAttuale,
			String idCliente) {
		if(!Utils.areEqual(idClienteOrdine, idCliente))
			return contoCorrenteAttuale;
		return contoCorrenteOrdine;

	}

	public static String getValoreCodiceIBANDaUsare(String idClienteOrdine, String codiceIBANOrdine, String codiceIBANAttuale,
			String idCliente) {
		if(!Utils.areEqual(idClienteOrdine, idCliente))
			return codiceIBANAttuale;
		return codiceIBANOrdine;

	}

	public DocumentoDataCollector creaDocumentoVenditaTestataBODC(DocumentoVendita dv) {
		try {
			String className = "DocumentoVendita";
			ClassADCollection cadc = ClassADCollectionManager.collectionWithName(className);
			String collectorName = cadc.getBODataCollector();
			if (collectorName == null) {
				collectorName = DocumentoDataCollector.class.getName();
			}
			DocumentoDataCollector bo = (DocumentoDataCollector) Factory.createObject(collectorName);
			bo.initialize(className);
			bo.setAutoCommit(false);
			bo.setBo(dv);
			bo.loadAttValue();
			bo.impostaSecondoCausale();
			return bo;
		}catch (Exception e) {
			e.printStackTrace(Trace.excStream);
		}
		return null;
	}

	public DocumentoDataCollector creaDocumentoVenditaRigaPrmBODC(DocumentoVenRigaPrm dvr) {
		try {
			String className = "DocumentoVenditaRigaPrm";
			ClassADCollection cadc = ClassADCollectionManager.collectionWithName(className);
			String collectorName = cadc.getBODataCollector();
			if (collectorName == null) {
				collectorName = DocumentoDataCollector.class.getName();
			}
			DocumentoDataCollector bo = (DocumentoDataCollector) Factory.createObject(collectorName);
			bo.initialize(className);
			bo.setAutoCommit(false);
			bo.setBo(dvr);
			bo.loadAttValue();
			//bo.impostaSecondoCausale();
			return bo;
		}catch (Exception e) {
			e.printStackTrace(Trace.excStream);
		}
		return null;
	}

	@Override
	public boolean retrieve(int lockType) throws SQLException {
		return false;
	}

	@Override
	public void setOnDB(boolean onDB) {
	}

	@Override
	public void unlock() throws SQLException {
	}

	@Override
	public void setObjQueryTimeout(int seconds) {
	}

	/**
	 * @author Daniele Signoroni 26/04/2024
	 *         <p>
	 *         Si occupa di recuperare le righe accorpate.<br>
	 *         Data una lista di righe uds vendita, queste vengono accorpate , la
	 *         rottura e' la riga ordine vendita.<br>
	 *         Viene poi ritornata una lista di oggettini di comodo che contengono
	 *         la quantita' sommata, la riga uds e le righe uds che sono state
	 *         accorpate in modo che l'utente poi abbia una lista completa di quali
	 *         righe sono accorpate, cosi che poi possa aggiornare i riferimenti del
	 *         documento vendita su suddette righe uds.<br>
	 *         </p>
	 * @param righe
	 * @return
	 */
	protected List<YOggettinoRigheAccorpate> recuperaRigheAccorpate(List<YUdsVenRig> righe) {
		Collections.sort(righe, new YComparatorRigheOrdineUds()); // le sorto per riga ordine key
		List<YOggettinoRigheAccorpate> righeAccorpate = new ArrayList<YEvasioneUdsVendita.YOggettinoRigheAccorpate>();
		YUdsVenRig rigaPrec = null;
		HashMap<String, YOggettinoRigheAccorpate> oggettini = new HashMap<String, YEvasioneUdsVendita.YOggettinoRigheAccorpate>();
		for (YUdsVenRig riga : righe) {
			String keyAccorpamento = null;
			if(riga.getOrdineVenditaRiga() != null) {
				keyAccorpamento = riga.getOrdineVenditaRigaKey();
			}else {
				keyAccorpamento = riga.getRArticolo();
			}
			if (rigaPrec != null) {
				if (isAccorpamento(rigaPrec, riga)) {
					if (oggettini.containsKey(riga.getOrdineVenditaRigaKey())) {
						YOggettinoRigheAccorpate ogg = oggettini.get(riga.getOrdineVenditaRigaKey());
						ogg.setQuantita(ogg.getQuantita().add(riga.getQtaPrm()));
						ogg.righeAccorpate.add(riga);
						oggettini.put(keyAccorpamento, ogg);
					} else {
						YEvasioneUdsVendita inst = new YEvasioneUdsVendita();
						YOggettinoRigheAccorpate ogg = inst.new YOggettinoRigheAccorpate();
						ogg.setQuantita(riga.getQtaPrm().add(rigaPrec.getQtaPrm()));
						ogg.setRiga(riga);
						ogg.setKey(riga.getKey());
						ogg.righeAccorpate.add(rigaPrec);
						oggettini.put(keyAccorpamento, ogg);
					}
				} else {
					YOggettinoRigheAccorpate ogg = new YOggettinoRigheAccorpate();
					ogg.setQuantita(riga.getQtaPrm());
					ogg.setRiga(riga);
					ogg.setKey(riga.getKey());
					// ogg.righeAccorpate.add(rigaPrec);
					if(riga.getOrdineVenditaRigaSec() != null)
						ogg.righeAccorpate.add(riga);
					oggettini.put(keyAccorpamento, ogg);
				}
			} else {
				YEvasioneUdsVendita inst = new YEvasioneUdsVendita();
				YOggettinoRigheAccorpate ogg = inst.new YOggettinoRigheAccorpate();
				ogg.setQuantita(riga.getQtaPrm());
				ogg.setRiga(riga);
				ogg.setKey(riga.getKey());
				if(riga.getOrdineVenditaRigaSec() != null)
					ogg.righeAccorpate.add(riga);
				oggettini.put(keyAccorpamento, ogg);
			}
			rigaPrec = riga;
		}
		for (Map.Entry<String, YOggettinoRigheAccorpate> entry : oggettini.entrySet()) {
			// String key = entry.getKey();
			YOggettinoRigheAccorpate val = entry.getValue();
			righeAccorpate.add(val);
		}
		return righeAccorpate;
	}

	protected static List<YOggettinoRigaLottoAccorpata> recuperaRigheLottoAccorpate(List<YUdsVenRig> righe) {
		Collections.sort(righe, new YComparatorRigheLottoUds()); // le sorto per id lotto
		List<YOggettinoRigaLottoAccorpata> righeAccorpate = new ArrayList<YEvasioneUdsVendita.YOggettinoRigaLottoAccorpata>();
		YUdsVenRig rigaPrec = null;
		HashMap<String, YOggettinoRigaLottoAccorpata> oggettini = new HashMap<String, YEvasioneUdsVendita.YOggettinoRigaLottoAccorpata>();
		for (YUdsVenRig riga : righe) {
			if (rigaPrec != null) {
				if (rigaPrec.getRLotto() != null && riga.getRLotto() != null
						&& (rigaPrec.getRLotto().equals(riga.getRLotto()))) {
					if (oggettini.containsKey(riga.getRLotto())) {
						YOggettinoRigaLottoAccorpata ogg = oggettini.get(riga.getRLotto());
						ogg.setQuantita(ogg.getQuantita().add(riga.getQtaPrm()));
						ogg.setIdLotto(riga.getRLotto());
						oggettini.put(riga.getRLotto(), ogg);
					} else {
						YEvasioneUdsVendita inst = new YEvasioneUdsVendita();
						YOggettinoRigaLottoAccorpata ogg = inst.new YOggettinoRigaLottoAccorpata();
						ogg.setQuantita(riga.getQtaPrm().add(rigaPrec.getQtaPrm()));
						ogg.setIdLotto(riga.getRLotto());
						oggettini.put(riga.getRLotto(), ogg);
					}
				} else {
					YEvasioneUdsVendita inst = new YEvasioneUdsVendita();
					YOggettinoRigaLottoAccorpata ogg = inst.new YOggettinoRigaLottoAccorpata();
					ogg.setQuantita(riga.getQtaPrm());
					ogg.setIdLotto(riga.getRLotto());
					oggettini.put(riga.getRLotto(), ogg);
				}
			} else {
				YEvasioneUdsVendita inst = new YEvasioneUdsVendita();
				YOggettinoRigaLottoAccorpata ogg = inst.new YOggettinoRigaLottoAccorpata();
				ogg.setQuantita(riga.getQtaPrm());
				ogg.setIdLotto(riga.getRLotto());
				oggettini.put(riga.getRLotto(), ogg);
			}
			rigaPrec = riga;
		}
		if (oggettini.size() == 0 && righe.size() == 1) {
			// ne ho solo 1 la inserisco
			YUdsVenRig riga0 = righe.get(0);
			YEvasioneUdsVendita inst = new YEvasioneUdsVendita();
			YOggettinoRigaLottoAccorpata ogg = inst.new YOggettinoRigaLottoAccorpata();
			ogg.setQuantita(riga0.getQtaPrm());
			ogg.setIdLotto(riga0.getRLotto());
			oggettini.put(riga0.getRLotto(), ogg);
		}
		for (Map.Entry<String, YOggettinoRigaLottoAccorpata> entry : oggettini.entrySet()) {
			// String key = entry.getKey();
			YOggettinoRigaLottoAccorpata val = entry.getValue();
			righeAccorpate.add(val);
		}
		return righeAccorpate;
	}

	/**
	 * @author Daniele Signoroni 26/04/2024
	 *         <p>
	 *         Data una lista di testate ritorna i pesi sommati.<br>
	 *         </p>
	 * @param testate
	 * @return
	 */
	protected static BigDecimal[] recuperaTotalePesiDaTestate(List<YUdsVendita> testate) {
		BigDecimal[] pesi = new BigDecimal[2];
		BigDecimal totPesoNetto = BigDecimal.ZERO;
		BigDecimal totPesoLordo = BigDecimal.ZERO;
		for (YUdsVendita testata : testate) {
			if (testata.getPesoLordo() != null) {
				totPesoLordo = totPesoLordo.add(testata.getPesoLordo());
			}
			if (testata.getPesoNetto() != null) {
				totPesoNetto = totPesoNetto.add(testata.getPesoNetto());
			}
		}
		pesi[0] = totPesoLordo;
		pesi[1] = totPesoNetto;
		return pesi;
	}

	/**
	 * @author Daniele Signoroni 26/04/2024
	 *         <p>
	 *         Data una lista di testate, recupera tutte le uds figlie.<br>
	 *         </p>
	 * @param testate
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	protected static List<YUdsVenRig> recuperaListaUdsVenditaRigheDaUdsVendita(List<YUdsVendita> testate) {
		List<YUdsVenRig> list = new ArrayList<YUdsVenRig>();
		for (YUdsVendita testata : testate) {
			try {
				list.addAll(testata.getRigheUDSVendita());
				list.addAll(testata.getRigheUDSVenditaDaFigli());
			} catch (SQLException e) {
				e.printStackTrace(Trace.excStream);
			}
		}
		return list;
	}

	/**
	 * @author Daniele Signoroni 26/04/2024
	 *         <p>
	 *         Data una lista di chiavi impaccate selezionate ritorna la lista degli
	 *         oggetti scaricati dal database.<br>
	 *         </p>
	 * @param chiaviSelezionate
	 * @return
	 */
	protected static List<YUdsVendita> recuperaListaUdsVenditaDaSelezionate(String[] chiaviSelezionate) {
		List<YUdsVendita> list = new ArrayList<YUdsVendita>();
		for (String key : chiaviSelezionate) {
			try {
				list.add((YUdsVendita) YUdsVendita.elementWithKey(YUdsVendita.class, key, PersistentObject.NO_LOCK));
			} catch (SQLException e) {
				e.printStackTrace(Trace.excStream);
			}
		}
		return list;
	}

	/**
	 * DSSOF3 Metodo per accorpare le righe uds in una riga vendita. Se ho trovato
	 * una riga UDS con gli stessi riferimenti ordine vendita della precedente,
	 * accorpo le qtà.
	 * 
	 * @param docVenRig
	 * @param udsCorrente
	 */
	public void accorpaQtaDocumentoVenditaRigaPrm(DocumentoVenRigaPrm docVenRig, YUdsVenRig udsCorrente) {
		if (docVenRig.getQtaInUMVen() != null && udsCorrente.getQtaPrm() != null)
			docVenRig.setQtaInUMVen(docVenRig.getQtaInUMVen().add(udsCorrente.getQtaPrm()));

		if (docVenRig.getQtaInUMPrm() != null && udsCorrente.getQtaPrm() != null)
			docVenRig.setQtaInUMPrm(docVenRig.getQtaInUMPrm().add(udsCorrente.getQtaPrm()));
	}

	/**
	 * @author Daniele Signoroni
	 *         <p>
	 *         DSSOF3 Metodo per verificare se gli ordini di vendita di due righe
	 *         UDS sono uguali.
	 *         </p>
	 *         <b>71XXX DSSOF3 26/04/2024</b>
	 *         <p>
	 *         Sistemare alleggerendo.<br>
	 *         </p>
	 * @return true se sono da accorpare
	 * @return false se non sono da accorpare
	 */
	public boolean isAccorpamento(YUdsVenRig uds1, YUdsVenRig uds2) {
		boolean isDaRaggruppare = false;
		if (uds2 == null) {
			return false;
		}
		//<72153 inizio
		if(uds1.getOrdineVenditaRiga() != null && uds2.getOrdineVenditaRiga()!= null) {
			if(uds1.getOrdineVenditaRigaKey().equals(uds2.getOrdineVenditaRigaKey())) {
				return true;
			} else {
				return false;
			}
		}
		if(uds1.getOrdineVenditaRiga() != null || uds2.getOrdineVenditaRiga()!= null) {
			return false;
		}
		String c1 = uds1.getRelarticolo().getIdArticolo();
		String c2 = uds2.getRelarticolo().getIdArticolo();
		//72153 fine>
		if (c1.equals(c2)) {
			isDaRaggruppare = true;
		}
		return isDaRaggruppare;
	}

	/**
	 * DSSOF3 Metodo per la creazione della testata Documento di Vendita.
	 * 
	 * @return
	 */
	public DocumentoVendita creaDocumentoVenditaTestata() {
		DocumentoVendita docVenTes = (DocumentoVendita) Factory.createObject(DocumentoVendita.class);
		docVenTes.setIdAzienda(Azienda.getAziendaCorrente());
		docVenTes.setCausale(getCausale());
		docVenTes.getNumeratoreHandler().setIdSerie(getRSerie());
		docVenTes.getNumeratoreHandler().setDataDocumento(getDataDocumento());
		docVenTes.setCliente(getRelcliente());
		docVenTes.setDataRifIntestatario(getDataRifIntestatario());
		docVenTes.setNumeroRifIntestatario(getNumeroRifIntestatario());
		docVenTes.completaBO();
		return docVenTes;

	}

	/**
	 * DSSOF3 Metodo per la creazione di una riga Documento di Vendita.
	 * 
	 * @param docVenTes
	 * @param udsVeRig
	 * @param oggino
	 * @param rigaOrdine
	 * @return
	 */
	public DocumentoVenRigaPrm creaDocumentoVenditaRigaPrm(DocumentoVendita docVenTes, YEvasioneUdsVenRiga riga) {
		DocumentoVenRigaPrm docVenRig = (DocumentoVenRigaPrm) Factory.createObject(DocumentoVenRigaPrm.class);
		docVenRig.setIdAzienda(Azienda.getAziendaCorrente());
		docVenRig.setTestata(docVenTes);
		YUdsVenRig udsVenRig = riga.getRigaUdsVendita();
		if(udsVenRig != null) {
			docVenRig.setRAnnoOrd(udsVenRig.getRAnnoOrdVen());
			docVenRig.setRNumeroOrd(udsVenRig.getRNumOrdVen());
			docVenRig.setRRigaOrd(udsVenRig.getRRigaOrdVen());
			docVenRig.setRDetRigaOrd(udsVenRig.getRRigaDetOrdVen() != null ? udsVenRig.getRRigaDetOrdVen() : 0);
		}else {
			docVenRig.setRAnnoOrd(riga.getRigaOrdine().getAnnoDocumento());
			docVenRig.setRNumeroOrd(riga.getRigaOrdine().getNumeroDocumento());
			docVenRig.setRRigaOrd(riga.getRigaOrdine().getNumeroRigaDocumento());
			docVenRig.setRDetRigaOrd(riga.getRigaOrdine().getDettaglioRigaDocumento());
		}
		if (docVenRig.getRigaOrdine() != null) {
			OrdineVenditaRigaPrm rigaOrdine = null;
			if(docVenRig.getRigaOrdine() != null && docVenRig.getRigaOrdine() instanceof OrdineVenditaRigaSec) {
				rigaOrdine = ((OrdineVenditaRigaSec)docVenRig.getRigaOrdine()).getRigaPrimaria();
			}else {
				rigaOrdine = (OrdineVenditaRigaPrm) docVenRig.getRigaOrdine();
			}
			CausaleRigaDocVen cau = trovaCausaleRigaDocVen(rigaOrdine);
			if (cau != null) {
				docVenRig.setCausaleRiga(cau);
			}
		} else {
			docVenRig.setIdCauRig(docVenTes.getCausale().getCausaleRigaDocumVenKey());
		}
		docVenRig.completaBO();
		docVenRig.setIdArticolo(udsVenRig != null ? udsVenRig.getRArticolo() : docVenRig.getRigaOrdine().getIdArticolo());
		docVenRig.setIdEsternoConfig(udsVenRig != null ? udsVenRig.getRConfig() : docVenRig.getRigaOrdine().getIdEsternoConfig());
		docVenRig.setIdVersioneRcs(udsVenRig != null ? udsVenRig.getRVersione() : docVenRig.getRigaOrdine().getIdVersioneRcs());
		//<72153 inizio
		docVenRig.setIdUMPrm(docVenRig.getArticolo().getIdUMPrmMag());
		docVenRig.setIdUMRif(docVenRig.getArticolo().getIdUMRiferimento());
		//72153 fine>
		docVenRig.cambiaArticolo(docVenRig.getArticolo(), docVenRig.getConfigurazione(), false);
		UnitaMisura um = docVenRig.getArticolo().getUMDefaultVendita();
		docVenRig.setUMRif(um);
		docVenRig.setQtaInUMVen(riga.getQtaDaSpedireInUMRif());
		BigDecimal qtaInUmPrm = docVenRig.getArticolo().convertiUM(riga.getQtaDaSpedireInUMRif(), um, docVenRig.getUMPrm(), docVenRig.getArticoloVersRichiesta());
		if(qtaInUmPrm != null)
			docVenRig.setQtaInUMPrm(qtaInUmPrm);
		return docVenRig;
	}

	/**
	 * DSSOF3 Metodo per agggiornare gli attributi tramite la riga ordine presente
	 * nei riferimenti dell'uds.
	 * 
	 * @param docVenRig
	 * @param udsVenRig
	 * @param oggino
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void aggiornaAttributiDaRigaOrdine(DocumentoVenditaRiga docVenRig, YEvasioneUdsVenRiga riga) {
		try {
			OrdineVenditaRiga rigaOrdine = (OrdineVenditaRiga) docVenRig.getRigaOrdine();
			if (rigaOrdine != null) {
				/*CausaleRigaDocVen cauRigaDoc = trovaCausaleRigaDocVen((OrdineVenditaRigaPrm) docVenRig.getRigaOrdine());
				if(cauRigaDoc != null)
					docVenRig.setCausaleRiga(cauRigaDoc);
				 */
				docVenRig.setRRigaOrd(rigaOrdine.getNumeroRigaDocumento());
				docVenRig.setSpecializzazioneRiga(rigaOrdine.getSpecializzazioneRiga());
				//docVenRig.setSequenzaRiga(rigaOrdine.getSequenzaRiga());
				docVenRig.setTipoRiga(rigaOrdine.getTipoRiga());
				if (rigaOrdine.getTipoRiga() == TipoRiga.OMAGGIO) {
					docVenRig.setServizioCalcDatiVendita(false);
				}
				YUdsVendita udsVendita = riga.getUdsVendita();
				if(udsVendita != null && udsVendita.getRMagazzino() != null)
					docVenRig.setIdMagazzino(udsVendita.getRMagazzino()); //.Prendo il magazzino dall'UDS se c'è
				else
					docVenRig.setMagazzino(rigaOrdine.getMagazzino());
				docVenRig.setArticolo(rigaOrdine.getArticolo());
				docVenRig.setArticoloVersSaldi(rigaOrdine.getArticoloVersSaldi());
				docVenRig.setArticoloVersRichiesta(rigaOrdine.getArticoloVersRichiesta());
				docVenRig.setConfigurazione(rigaOrdine.getConfigurazione());
				docVenRig.setDescrizioneArticolo(rigaOrdine.getDescrizioneArticolo());
				docVenRig.setDescrizioneExtArticolo(rigaOrdine.getDescrizioneExtArticolo());
				docVenRig.setNota(rigaOrdine.getNota());
				String nota = docVenRig.getNota();
				if (nota != null && !nota.equals("")) {
					if (rigaOrdine.getNota() != null && !rigaOrdine.getNota().equals("")) {
						nota = rigaOrdine.getNota() + " " + nota;
					}
				} else {
					nota = rigaOrdine.getNota();
				}
				if (nota != null && nota.length() > 250) {
					nota = nota.substring(0, 250);
				}
				docVenRig.setNota(nota);
				docVenRig.setDocumentoMM(rigaOrdine.getDocumentoMM());
				docVenRig.setSpesa(rigaOrdine.getSpesa());
				docVenRig.setImportoPercentualeSpesa(rigaOrdine.getImportoPercentualeSpesa());
				docVenRig.setSpesaPercentuale(rigaOrdine.isSpesaPercentuale());
				docVenRig.setUMRifKey(rigaOrdine.getUMRifKey());
				docVenRig.setUMPrmKey(rigaOrdine.getUMPrmKey());
				docVenRig.setUMSecKey(rigaOrdine.getUMSecKey());
				docVenRig.setRicalcoloQtaFattoreConv(rigaOrdine.isRicalcoloQtaFattoreConv());
				docVenRig.setCoefficienteImpiego(rigaOrdine.getCoefficienteImpiego());
				docVenRig.setBloccoRicalcoloQtaComp(rigaOrdine.isBloccoRicalcoloQtaComp());
				docVenRig.setTipoCostoRiferimento(rigaOrdine.getTipoCostoRiferimento());
				docVenRig.setDataConsegnaRichiesta(rigaOrdine.getDataConsegnaRichiesta());
				docVenRig.setDataConsegnaConfermata(rigaOrdine.getDataConsegnaConfermata());
				docVenRig.setSettConsegnaRichiesta(rigaOrdine.getSettConsegnaRichiesta());
				docVenRig.setSettConsegnaConfermata(rigaOrdine.getSettConsegnaConfermata());
				docVenRig.setStatoConfermaATP(rigaOrdine.getStatoConfermaATP());
				docVenRig.setDataPrevistaATP(rigaOrdine.getDataPrevistaATP());
				docVenRig.setDataTollerataATP(rigaOrdine.getDataTollerataATP());
				docVenRig.setIdListino(rigaOrdine.getIdListino());
				BigDecimal bd = GestoreEvasioneVendita.getBigDecimalZero();
				bd = rigaOrdine.getPrezzo() == null ? GestoreEvasioneVendita.getBigDecimalZero()
						: rigaOrdine.getPrezzo();
				docVenRig.setPrezzo(bd);
				bd = rigaOrdine.getPrezzoExtra() == null ? GestoreEvasioneVendita.getBigDecimalZero()
						: rigaOrdine.getPrezzoExtra();
				docVenRig.setPrezzoExtra(bd);
				bd = rigaOrdine.getPrezzoListino() == null ? GestoreEvasioneVendita.getBigDecimalZero()
						: rigaOrdine.getPrezzoListino();
				docVenRig.setPrezzoListino(bd);
				bd = rigaOrdine.getPrezzoExtraListino() == null ? GestoreEvasioneVendita.getBigDecimalZero()
						: rigaOrdine.getPrezzoExtraListino();
				docVenRig.setPrezzoExtraListino(bd);
				docVenRig.setRiferimentoUMPrezzo(rigaOrdine.getRiferimentoUMPrezzo());
				docVenRig.setTipoPrezzo(rigaOrdine.getTipoPrezzo());
				docVenRig.setBloccoRclPrzScnFatt(rigaOrdine.isBloccoRclPrzScnFatt());
				docVenRig.setProvenienzaPrezzo(rigaOrdine.getProvenienzaPrezzo());
				docVenRig.setTipoRigaListino(rigaOrdine.getTipoRigaListino());
				docVenRig.setAssoggettamentoIVA(rigaOrdine.getAssoggettamentoIVA());
				docVenRig.setResponsabileVendite(rigaOrdine.getResponsabileVendite());
				docVenRig.setScontoArticolo1(rigaOrdine.getScontoArticolo1());
				docVenRig.setScontoArticolo2(rigaOrdine.getScontoArticolo2());
				docVenRig.setMaggiorazione(rigaOrdine.getMaggiorazione());
				docVenRig.setSconto(rigaOrdine.getSconto());
				docVenRig.setPrcScontoIntestatario(rigaOrdine.getPrcScontoIntestatario());
				docVenRig.setPrcScontoModalita(rigaOrdine.getPrcScontoModalita());
				docVenRig.setScontoModalita(rigaOrdine.getScontoModalita());
				docVenRig.setAgente(rigaOrdine.getAgente());
				docVenRig.setProvvigione1Agente(rigaOrdine.getProvvigione1Agente());
				docVenRig.setProvvigione2Agente(rigaOrdine.getProvvigione2Agente());
				docVenRig.setSubagente(rigaOrdine.getSubagente());
				docVenRig.setProvvigione1Subagente(rigaOrdine.getProvvigione1Subagente());
				docVenRig.setProvvigione2Subagente(rigaOrdine.getProvvigione2Subagente());
				docVenRig.setDifferenzaPrezzoAgente(rigaOrdine.hasDifferenzaPrezzoAgente());
				docVenRig.setDifferenzaPrezzoSubagente(rigaOrdine.hasDifferenzaPrezzoSubagente());
				docVenRig.setCommessa(rigaOrdine.getCommessa());
				docVenRig.setCentroCosto(rigaOrdine.getCentroCosto());
				if (rigaOrdine.getDatiCA() != null) {
					docVenRig.getDatiCA().setVoceSpesaCA(rigaOrdine.getDatiCA().getVoceSpesaCA());
					docVenRig.getDatiCA().setVoceCA4(rigaOrdine.getDatiCA().getVoceCA4());
					docVenRig.getDatiCA().setVoceCA5(rigaOrdine.getDatiCA().getVoceCA5());
					docVenRig.getDatiCA().setVoceCA6(rigaOrdine.getDatiCA().getVoceCA6());
					docVenRig.getDatiCA().setVoceCA7(rigaOrdine.getDatiCA().getVoceCA7());
					docVenRig.getDatiCA().setVoceCA8(rigaOrdine.getDatiCA().getVoceCA8());
				}
				docVenRig.setGruppoContiAnalitica(rigaOrdine.getGruppoContiAnalitica());
				docVenRig.setFornitore(rigaOrdine.getFornitore());
				docVenRig.setStatoAccantonamentoPrenot(rigaOrdine.getStatoAccantonamentoPrenot());
				docVenRig.setPriorita(rigaOrdine.getPriorita());
				docVenRig.setStatoInvioEDI(rigaOrdine.getStatoInvioEDI());
				docVenRig.setStatoInvioDatawarehouse(rigaOrdine.getStatoInvioDatawarehouse());
				docVenRig.setStatoInvioLogistica(rigaOrdine.getStatoInvioLogistica());
				docVenRig.setStatoInvioContAnalitica(rigaOrdine.getStatoInvioContAnalitica());
				docVenRig.setNonFatturare(rigaOrdine.isNonFatturare());
				docVenRig.setFlagRiservatoUtente1(rigaOrdine.getFlagRiservatoUtente1());
				docVenRig.setFlagRiservatoUtente2(rigaOrdine.getFlagRiservatoUtente2());
				docVenRig.setFlagRiservatoUtente3(rigaOrdine.getFlagRiservatoUtente3());
				docVenRig.setFlagRiservatoUtente4(rigaOrdine.getFlagRiservatoUtente4());
				docVenRig.setFlagRiservatoUtente5(rigaOrdine.getFlagRiservatoUtente5());
				docVenRig.setAlfanumRiservatoUtente1(rigaOrdine.getAlfanumRiservatoUtente1());
				docVenRig.setAlfanumRiservatoUtente2(rigaOrdine.getAlfanumRiservatoUtente2());
				docVenRig.setNumeroRiservatoUtente1(rigaOrdine.getNumeroRiservatoUtente1());
				docVenRig.setNumeroRiservatoUtente2(rigaOrdine.getNumeroRiservatoUtente2());
				docVenRig.setCostoUnitario(rigaOrdine.getCostoUnitario());
				PersDatiVen pdv = PersDatiVen.getCurrentPersDatiVen();
				if (pdv.getTipoCostoRiferimento() == TipoCostoRiferimento.COSTO_ULTIMO) {
					GestoreCalcoloCosti gesCalcoloCosti = (GestoreCalcoloCosti) Factory
							.createObject(GestoreCalcoloCosti.class);
					gesCalcoloCosti.initialize(rigaOrdine.getIdAzienda(), rigaOrdine.getIdArticolo(),
							rigaOrdine.getIdVersioneSal(), rigaOrdine.getIdConfigurazione(),
							rigaOrdine.getIdMagazzino());
					gesCalcoloCosti.impostaCostoUnitario();
					docVenRig.setCostoUnitario(gesCalcoloCosti.getCostoUnitario());
				}
				if (rigaOrdine.getRigaPrezziExtra() != null) {
					docVenRig.istanziaRigaPrezziExtra();
					if (docVenRig.getRigaPrezziExtra() != null) {
						DocRigaPrezziExtraVendita rigaPrezzi = (DocRigaPrezziExtraVendita) docVenRig
								.getRigaPrezziExtra();
						DocOrdRigaPrezziExtra rigaPrezziOrd = rigaOrdine.getRigaPrezziExtra();
						rigaPrezzi.setAnnoContrattoMateriaPrima(rigaPrezziOrd.getAnnoContrattoMateriaPrima());
						rigaPrezzi.setIdAzienda(rigaPrezziOrd.getIdAzienda());
						rigaPrezzi.setIdRigaCondizione(rigaPrezziOrd.getIdRigaCondizione());
						rigaPrezzi.setIdSchemaPrezzo(rigaPrezziOrd.getIdSchemaPrezzo());
						rigaPrezzi.setNumContrattoMateriaPrima(rigaPrezziOrd.getNumContrattoMateriaPrima());
						rigaPrezzi.setRAnnoCantiere(((OrdineRigaPrezziExtraVendita) rigaPrezziOrd).getRAnnoCantiere());
						rigaPrezzi.setRNumeroCantiere(
								((OrdineRigaPrezziExtraVendita) rigaPrezziOrd).getRNumeroCantiere());
						rigaPrezzi.setRRigaCantiere(((OrdineRigaPrezziExtraVendita) rigaPrezziOrd).getRRigaCantiere());
						rigaPrezzi.setPrezzoRiferimento(
								((OrdineRigaPrezziExtraVendita) rigaPrezziOrd).getPrezzoRiferimento());
						try {
							rigaPrezzi.getPrezziExtra().setEqual(rigaPrezziOrd.getPrezziExtra());
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						rigaPrezzi.setRigaOrdine(rigaOrdine);
						rigaPrezzi.setIdDetRigaOrdine(rigaOrdine.getDettaglioRigaDocumento());
					}
				}
				if (docVenRig.getRigaOrdine() != null) {
					OrdineVenditaTestata ordVen = (OrdineVenditaTestata) docVenRig.getRigaOrdine().getTestata();
					if (ordVen.getTipoEvasioneOrdine() == OrdineTestata.SALDO_AUTOMATICO) {
						docVenRig.setRigaSaldata(true);
					}
				}
				docVenRig.setRifCommessaCli(rigaOrdine.getRifCommessaCli());
				docVenRig.setDifferenzaPrezzoAgente(rigaOrdine.hasDifferenzaPrezzoAgente());
				docVenRig.setDifferenzaPrezzoSubagente(rigaOrdine.hasDifferenzaPrezzoSubagente());
				docVenRig.setPrezzoNetto(rigaOrdine.getPrezzoNetto());
			}
			if (PersDatiMagazzino.getCurrentPersDatiMagazzino().getGesLotti()) {
				// if(rigaOrdine != null) {
				// qui dovrei accorpare i lotti e creare un nuovo oggettino con la somma delle
				// quantita' accorpate per lotto
				List<YOggettinoRigaLottoAccorpata> righeLottoAccorpate = recuperaRigheLottoAccorpate(riga.getRigheUdsAccorpate());
				Iterator iterRigheTot = righeLottoAccorpate.iterator();
				while (iterRigheTot.hasNext()) {
					YOggettinoRigaLottoAccorpata lotto = (YOggettinoRigaLottoAccorpata) iterRigheTot.next();
					DocumentoVenRigaLottoPrm rigaLotto = (DocumentoVenRigaLottoPrm) Factory
							.createObject(DocumentoVenRigaLottoPrm.class);
					rigaLotto.setFather(docVenRig);
					rigaLotto.setIdArticolo(docVenRig.getIdArticolo());
					rigaLotto.setIdLotto(lotto.getIdLotto()); // 24-04-24 cambiare a lotto della riga uds
					rigaLotto.getQtaAttesaEvasione().setQuantitaInUMRif(lotto.getQuantita());
					rigaLotto.getQtaAttesaEvasione().setQuantitaInUMPrm(lotto.getQuantita());

					docVenRig.getRigheLotto().add(rigaLotto);
				}

				//.Vuol dire che sto evadendo una riga ordine e non UDS quindi genero i lotti a partire da quelli dell'ordine
				if(riga.getRigheUdsAccorpate().isEmpty()) { 
					Iterator iterLotti = rigaOrdine.getRigheLotto().iterator();
					while(iterLotti.hasNext()) {
						OrdineVenditaRigaLottoPrm rigaLottoOrdVenRig = (OrdineVenditaRigaLottoPrm) iterLotti.next();

						DocumentoVenRigaLottoPrm rigaLottoDocVenRig = (DocumentoVenRigaLottoPrm) Factory
								.createObject(DocumentoVenRigaLottoPrm.class);
						rigaLottoDocVenRig.setFather(docVenRig);
						rigaLottoDocVenRig.setIdArticolo(docVenRig.getIdArticolo());
						rigaLottoDocVenRig.setIdLotto(rigaLottoOrdVenRig.getIdLotto());
						rigaLottoDocVenRig.getQtaAttesaEvasione().setQuantitaInUMRif(rigaLottoOrdVenRig.getQtaInUMRif());
						rigaLottoDocVenRig.getQtaAttesaEvasione().setQuantitaInUMPrm(rigaLottoOrdVenRig.getQtaInUMPrmMag());

						docVenRig.getRigheLotto().add(rigaLottoDocVenRig);
					}
				}
			}
			// }
		} catch (Exception e) {
			e.printStackTrace(Trace.excStream);
		}
	}

	public DocumentoVenRigaSec creaDocumentoVenditaRigaSec(DocumentoVenRigaPrm rigaPrm, YUdsVenRig udsVenRig) {
		DocumentoVenRigaSec docVenRig = (DocumentoVenRigaSec) Factory.createObject(DocumentoVenRigaSec.class);
		docVenRig.setIdAzienda(Azienda.getAziendaCorrente());
		docVenRig.setRigaPrimaria(rigaPrm);
		if(udsVenRig != null) {
			docVenRig.setRAnnoOrd(udsVenRig.getRAnnoOrdVen());
			docVenRig.setRNumeroOrd(udsVenRig.getRNumOrdVen());
			docVenRig.setRRigaOrd(udsVenRig.getRRigaOrdVen());
			docVenRig.setRDetRigaOrd(udsVenRig.getRRigaDetOrdVen() != null ? udsVenRig.getRRigaDetOrdVen() : 0);
		}
		if (docVenRig.getRigaOrdine() != null) {
			OrdineVenditaRigaPrm rigaOrdine = null;
			if(docVenRig.getRigaOrdine() != null && docVenRig.getRigaOrdine() instanceof OrdineVenditaRigaSec) {
				rigaOrdine = ((OrdineVenditaRigaSec)docVenRig.getRigaOrdine()).getRigaPrimaria();
			}else {
				rigaOrdine = (OrdineVenditaRigaPrm) docVenRig.getRigaOrdine();
			}
			CausaleRigaDocVen cau = trovaCausaleRigaDocVen(rigaOrdine);
			if (cau != null) {
				docVenRig.setCausaleRiga(cau);
			}
		} else {
			docVenRig.setIdCauRig(((DocumentoVendita)rigaPrm.getTestata()).getCausale().getCausaleRigaDocumVenKey());
		}
		docVenRig.completaBO();
		docVenRig.setIdArticolo(udsVenRig != null ? udsVenRig.getRArticolo() : docVenRig.getRigaOrdine().getIdArticolo());
		docVenRig.setIdEsternoConfig(udsVenRig != null ? udsVenRig.getRConfig() : docVenRig.getRigaOrdine().getIdEsternoConfig());
		docVenRig.setIdVersioneRcs(udsVenRig != null ? udsVenRig.getRVersione() : docVenRig.getRigaOrdine().getIdVersioneRcs());
		docVenRig.setIdUMPrm(docVenRig.getArticolo().getIdUMPrmMag());
		docVenRig.setIdUMRif(docVenRig.getArticolo().getIdUMRiferimento());
		docVenRig.cambiaArticolo(docVenRig.getArticolo(), docVenRig.getConfigurazione(), false);
		UnitaMisura um = docVenRig.getArticolo().getUMDefaultVendita();
		docVenRig.setUMRif(um);
		docVenRig.setQtaInUMVen(udsVenRig.getQtaPrm());
		BigDecimal qtaInUmPrm = docVenRig.getArticolo().convertiUM(udsVenRig.getQtaPrm(), um, docVenRig.getUMPrm(), docVenRig.getArticoloVersRichiesta());
		if(qtaInUmPrm != null)
			docVenRig.setQtaInUMPrm(qtaInUmPrm);
		return docVenRig;
	}

	/**
	 * DSSOF3 Metodo copiato dallo standard per prendere la versione dell'articolo.
	 * 
	 * @param idAzienda
	 * @param idArticolo
	 * @param idVersione
	 * @return
	 */
	public ArticoloVersione getArticoloVersione(String idAzienda, String idArticolo, String idVersione) {
		ArticoloVersione av = null;
		String azienda = Azienda.getAziendaCorrente();
		if (idAzienda != null) {
			azienda = idAzienda;
		}
		String key = KeyHelper.buildObjectKey(new String[] { azienda, idArticolo, idVersione });
		try {
			av = ArticoloVersione.elementWithKey(key, PersistentObject.NO_LOCK);
		} catch (Exception ex) {
			ex.printStackTrace(Trace.excStream);
		}
		return av;
	}

	/**
	 * DSSOF3 Metodo copiato dallo standard per prendere l'unità di misura.
	 * 
	 * @param idUM
	 * @return
	 */
	public UnitaMisura getUnitaMisura(String idUM) {
		UnitaMisura um = null;
		if (idUM == null || idUM.equals(""))
			return um;
		try {
			um = UnitaMisura.getUM(idUM);
		} catch (Exception ex) {
			ex.printStackTrace(Trace.excStream);
		}
		return um;
	}

	/**
	 * DSSOF3	Metodo per aggiornare i riferimenti al Documento di Vendita sulla riga uds.
	 * @param udsVeRig
	 * @param docVeRig
	 */
	public void aggiornaRiferimentiDocumentoVenditaRigaUds(YUdsVenRig udsVeRig, DocumentoVenRigaPrm docVeRig) {
		if(getAzione().equals(YUdsVenditaGridActionAdapter.EVADI_UDS)) {
			udsVeRig.setRAnnoDocVen(docVeRig.getTestata().getAnnoDocumento());
			udsVeRig.setRNumDocVen(docVeRig.getTestata().getNumeroDocumento());
			udsVeRig.setRRigaDocVen(docVeRig.getNumeroRigaDocumento());
			if(docVeRig.getDettaglioRigaDocumento() == null) {
				udsVeRig.setRRigaDetDocVen(docVeRig.getDettaglioRigaDocumento());
			}
		}else if(getAzione().equals(YUdsVenditaGridActionAdapter.TRASFERISCI_UDS)) {
			udsVeRig.setIdAnnoDocumentoTrasf(docVeRig.getTestata().getAnnoDocumento());
			udsVeRig.setIdNumeroDocumentoTrasf(docVeRig.getTestata().getNumeroDocumento());
			udsVeRig.setIdRigaDocumentoTrasf(docVeRig.getNumeroRigaDocumento());
		}
	}

	/**
	 * DSSOF3	Metodo per aggiornare i riferimenti al Documento di Vendita sulla testata uds.
	 * @param udsVeTes
	 * @param docVenTes
	 */
	public void aggiornaRiferimentiDocumentoVenditaTestataUds(YUdsVendita udsVeTes, DocumentoVendita docVenTes) {
		if(getAzione().equals(YUdsVenditaGridActionAdapter.EVADI_UDS)) {
			udsVeTes.setRAnnoDocVen(docVenTes.getAnnoDocumento());
			udsVeTes.setRNumDocVen(docVenTes.getNumeroDocumento());
		}else if(getAzione().equals(YUdsVenditaGridActionAdapter.TRASFERISCI_UDS)) {
			udsVeTes.setIdAnnoDocumentoTrasf(docVenTes.getAnnoDocumento());
			udsVeTes.setIdNumeroDocumentoTrasf(docVenTes.getNumeroDocumento());
		}
	}

	/**
	 * DSSOF3 Metodo per formattare la data da stringa java.sql.Date.
	 * 
	 * @param data
	 * @return
	 */
	public static Date getDataFormattata(String data) {
		java.sql.Date date = null;
		try {
			SimpleDateFormat formatoWeb = new SimpleDateFormat("dd/MM/yyyy");
			// SimpleDateFormat formatoSql = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat formatoDB2 = new SimpleDateFormat("yyyy-MM-dd");
			String dataOk = "";
			if (ConnectionManager.getCurrentDatabase() instanceof SQLServerJTDSNoUnicodeDatabase)
				dataOk = formatoDB2.format(formatoWeb.parse(data));
			else if (ConnectionManager.getCurrentDatabase() instanceof DB2NetDatabase)
				dataOk = formatoDB2.format(formatoWeb.parse(data));
			date = java.sql.Date.valueOf(dataOk);
		} catch (ParseException e) {
			e.printStackTrace(Trace.excStream);
		}
		return date;
	}

	/**
	 * <h1>Softre Solutions</h1> <br>
	 * 
	 * @author Daniele Signoroni 26/04/2024 <br>
	 *         <br>
	 *         <b>71XXX DSSOF3 26/04/2024</b>
	 *         <p>
	 *         Prima stesura.<br>
	 *         E' una classe di comodo per tenere le righe uds accorpate.<br>
	 *         {@link #riga} e' la riga uds principale.<br>
	 *         {@link #righeAccorpate} e' la lista che contiene le righe che sono
	 *         state accorpate a quelle principali.<br>
	 *         {@link #quantita} la quantita' totale sommata.<br>
	 *         {@link #key} la chiave della riga uds principale.<br>
	 *         </p>
	 */
	public class YOggettinoRigheAccorpate {

		protected BigDecimal quantita = BigDecimal.ZERO;
		protected String key = null;
		protected YUdsVenRig riga = null;

		List<YUdsVenRig> righeAccorpate = new ArrayList<YUdsVenRig>();

		public BigDecimal getQuantita() {
			return quantita;
		}

		public void setQuantita(BigDecimal quantita) {
			this.quantita = quantita;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public YUdsVenRig getRiga() {
			return riga;
		}

		public void setRiga(YUdsVenRig riga) {
			this.riga = riga;
		}
	}

	public class YOggettinoRigaLottoAccorpata {

		protected BigDecimal quantita = BigDecimal.ZERO;
		protected String idLotto = null;

		public BigDecimal getQuantita() {
			return quantita;
		}

		public void setQuantita(BigDecimal quantita) {
			this.quantita = quantita;
		}

		public String getIdLotto() {
			return idLotto;
		}

		public void setIdLotto(String idLotto) {
			this.idLotto = idLotto;
		}

	}

	@SuppressWarnings("rawtypes")
	public void aggiornaRigheDocumento(List righeParamRiga) throws ThipException {
		List righeDoc = this.getRigheEstratte();
		Iterator iter = righeParamRiga.iterator();
		while (iter.hasNext()) {
			try {
				ParamRigaPrmDocEvaVen pr = (ParamRigaPrmDocEvaVen) iter.next();
				YEvasioneUdsVenRiga rigaPrm = (YEvasioneUdsVenRiga) righeDoc.get(pr.iPos);
				if(rigaPrm == null)
					righeDoc.remove(pr.iPos);
				rigaPrm.aggiornaRiga(pr);
			}
			catch (IndexOutOfBoundsException ex) {
				ex.printStackTrace(Trace.excStream);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public CausaleRigaDocVen trovaCausaleRigaDocVen(OrdineVenditaRigaPrm
			rigaOrdine) {
		List causaliRigaDoc = new ArrayList();
		// pongo come prina causale quella eventualmente selezionata come default
		List causaliRigaDocTemp = getCausale().getCausaliRiga();
		CausaleRigaDocVen cauRigaDocDefault = getCausale().getCausaleRigaDocumVen();
		int idx = 0;
		String cauRigaDocDefaultKey = "";

		if (cauRigaDocDefault != null &&
				cauRigaDocDefault.getDatiComuniEstesi().getStato() ==
				DatiComuniEstesi.VALIDO) {
			causaliRigaDoc.add(idx, cauRigaDocDefault);
			cauRigaDocDefaultKey = cauRigaDocDefault.getKey();
			idx++;
		}
		Iterator iterDocTemp = causaliRigaDocTemp.iterator();
		while (iterDocTemp.hasNext()) {
			CausaleRigaDocVen cauObj = (CausaleRigaDocVen) iterDocTemp.next();
			if (!cauObj.getKey().equals(cauRigaDocDefaultKey) &&
					cauObj.getDatiComuniEstesi().getStato() == DatiComuniEstesi.VALIDO) {
				causaliRigaDoc.add(idx, cauObj);
				idx++;
			}
		}
		// cerca la causale appropriata
		CausaleRigaDocVen cauRigaDoc = null;
		boolean isTrovata = false;
		cauRigaDoc = trovaCausaleRigaDocVen(rigaOrdine, causaliRigaDoc, true);
		if (cauRigaDoc == null)
			cauRigaDoc = trovaCausaleRigaDocVen(rigaOrdine, causaliRigaDoc, false);
		isTrovata = (cauRigaDoc != null) ? true : false;
		//Fix 16542 fine
		if (!isTrovata) {
			cauRigaDoc = null;
		}
		return cauRigaDoc;
	}

	@SuppressWarnings("rawtypes")
	public CausaleRigaDocVen trovaCausaleRigaDocVen(OrdineVenditaRigaPrm rigaOrdine, List causaliRigaDoc, boolean conTestSuGestioneCatalogo) {
		CausaleRigaDocVen cauRigaDoc = null;
		boolean isTrovata = false;
		boolean isForzaControllo = false;
		CausaleRigaOrdVen cauRigaOrd = (CausaleRigaOrdVen) rigaOrdine.getCausaleRiga();
		Iterator iterDoc = causaliRigaDoc.iterator();
		if (cauRigaOrd != null) {
			if (rigaOrdine.getTipoRiga() == TipoRiga.SPESE_MOV_VALORE) {
				CausaleRigaDocVen cauDaSpesa = null;
				if (rigaOrdine.getSpesa() != null) {
					cauDaSpesa = (CausaleRigaDocVen) rigaOrdine.getSpesa().
							getCausaleRigaDocVen();
				}
				if (cauDaSpesa != null) {
					while (iterDoc.hasNext() && !isTrovata) {
						CausaleRigaDocVen cau = (CausaleRigaDocVen) iterDoc.next();
						if (cauDaSpesa.getKey().equals(cau.getKey())) {
							cauRigaDoc = cau;
							isTrovata = true;
						}
					}
					if (!isTrovata) {
						isForzaControllo = true;
					}
				}
				else {
					isForzaControllo = true;
				}
			}
			iterDoc = causaliRigaDoc.iterator();
			if (rigaOrdine.getTipoRiga() == TipoRiga.OMAGGIO) {
				boolean isROOmaggioSA = rigaOrdine.getCausaleRiga().isOmaggioScontoArticolo();
				while (iterDoc.hasNext() && !isTrovata) {
					CausaleRigaDocVen cau = (CausaleRigaDocVen) iterDoc.next();
					if (cau.getTipoRiga() == TipoRiga.OMAGGIO) {
						if (isROOmaggioSA == cau.isOmaggioScontoArticolo()) {
							Iterator i = cau.getCausaliRigaOrdine().iterator();
							while (i.hasNext() && !isTrovata) {
								CausaleRigaOrdVen cro = (CausaleRigaOrdVen) i.next();
								if (cro.getKey().equals(cauRigaOrd.getKey())) {
									if (!conTestSuGestioneCatalogo || (cro.isCatalogoEstPerOrdineAbl() == cau.isCatalogoEstPerOrdineAbl()) )
									{
										isTrovata = true;
										cauRigaDoc = cau;
									}
								}
							}
						}
					}
				}
			}
			else if (rigaOrdine.getTipoRiga() == TipoRiga.MERCE ||
					rigaOrdine.getTipoRiga() == TipoRiga.SERVIZIO ||
					isForzaControllo) {
				while (iterDoc.hasNext() && !isTrovata) {
					cauRigaDoc = (CausaleRigaDocVen) iterDoc.next();
					List causaliRigaOrd = cauRigaDoc.getCausaliRigaOrdine();
					Iterator iterOrd = causaliRigaOrd.iterator();
					while (iterOrd.hasNext() && !isTrovata) {
						CausaleRigaOrdVen cau = (CausaleRigaOrdVen) iterOrd.next();
						if (cau.getKey().equals(cauRigaOrd.getKey())) {
							if (!conTestSuGestioneCatalogo || (cau.isCatalogoEstPerOrdineAbl() == cauRigaDoc.isCatalogoEstPerOrdineAbl()))
							{
								isTrovata = true;
							}
						}
					}
				}
			}
		}
		if (!isTrovata && isForzaControllo) {
			Iterator iterDocUltima = causaliRigaDoc.iterator();
			while (iterDocUltima.hasNext()) {
				cauRigaDoc = (CausaleRigaDocVen) iterDocUltima.next();
				if (cauRigaDoc.getTipoRiga() == rigaOrdine.getTipoRiga()) {
					isTrovata = true;
					break;
				}
			}
		}
		return (isTrovata) ? cauRigaDoc : null;
	}

	@Override
	protected TableManager getTableManager() throws SQLException {
		return null;
	}

	//72XXX <
	public DocumentoVenRigaPrm creaDocumentoVenditaRigaPrmContenitore(DocumentoVendita testata, OggettinoContenitore ogg, String idCau, String idMagazzino) {
		DocumentoVenRigaPrm docVenRig = (DocumentoVenRigaPrm) Factory.createObject(DocumentoVenRigaPrm.class);
		docVenRig.setIdAzienda(Azienda.getAziendaCorrente());
		docVenRig.setTestata(testata);
		docVenRig.setIdCauRig(idCau);
		docVenRig.setIdMagazzino(idMagazzino);
		docVenRig.completaBO();
		docVenRig.setIdArticolo(ogg.getIdArticolo());
		docVenRig.setIdUMPrm(docVenRig.getArticolo().getIdUMPrmMag());
		docVenRig.setIdUMRif(docVenRig.getArticolo().getIdUMRiferimento());
		docVenRig.cambiaArticolo(docVenRig.getArticolo(), docVenRig.getConfigurazione(), false);
		UnitaMisura um = docVenRig.getArticolo().getUMDefaultVendita();
		docVenRig.setUMRif(um);
		docVenRig.setQtaInUMVen(ogg.getQuantita());
		BigDecimal qtaInUmPrm = docVenRig.getArticolo().convertiUM(ogg.getQuantita(), um, docVenRig.getUMPrm(), docVenRig.getArticoloVersRichiesta());
		if(qtaInUmPrm != null)
			docVenRig.setQtaInUMPrm(qtaInUmPrm);
		return docVenRig;
	}

	public static String getCausaleRigaDvContenitori() {
		String value = ParametroPsn.getValoreParametroPsn("YEvasioneUdsVendita", "IdCauRigCnr");
		return value != null ? value : "";
	}

	public static String getMagazzinoRigaDvContenitori() {
		String value = ParametroPsn.getValoreParametroPsn("YEvasioneUdsVendita", "IdMagRigCnr");
		return value != null ? value : "";
	}

	public List<OggettinoContenitore> recuperaContenitori(List<YEvasioneUdsVenRiga> righeEstratte) {
	    Map<String, OggettinoContenitore> contenitoriMap = new LinkedHashMap<>();
	    Set<String> udsVisitate = new HashSet<>();

	    for (YEvasioneUdsVenRiga riga : righeEstratte) {
	        if (!riga.isRigaEstratta())
	            continue;
	        
	        aggiungiContenitore(riga.getUdsVendita(), riga.getUdsVenditaKey(), contenitoriMap, udsVisitate);
	    }
	    return new ArrayList<>(contenitoriMap.values());
	}
	
	public static void aggiungiContenitore(
			YUdsVendita uds,
			String udsKey,
			Map<String, OggettinoContenitore> contenitoriMap,
			Set<String> udsVisitate) {
		if (uds == null || udsKey == null)
			return;

		YTipoUds tipoUds = uds.getReltipouds();
		if (tipoUds == null || tipoUds.getRArticolo() == null)
			return;

		// Evita di conteggiare più volte la stessa UDS
		if (!udsVisitate.add(udsKey))
			return;

		String idArticolo = tipoUds.getRArticolo();
		OggettinoContenitore contenitore = contenitoriMap.get(idArticolo);

		if (contenitore == null) {
			contenitore = (OggettinoContenitore) Factory.createObject(OggettinoContenitore.class);
			contenitore.setIdArticolo(idArticolo);
			contenitore.setQuantita(BigDecimal.ZERO);

			contenitoriMap.put(idArticolo, contenitore);
		}
		contenitore.setQuantita(contenitore.getQuantita().add(BigDecimal.ONE));
	}
	//72XXX >
}
