package it.softre.thip.vendite.uds;

import com.thera.thermfw.persist.*;
import java.sql.*;
import java.util.*;

import it.softre.thip.base.uds.YTipoUds;
import it.softre.thip.vendite.documentoVE.YDocumentoVendita;
import it.thera.thip.base.cliente.ClienteVendita;
//import it.thera.thip.logisticaLight.Ubicazione;
import it.thera.thip.vendite.documentoVE.DocumentoVendita;

import java.math.*;
import it.thera.thip.cs.*;
import com.thera.thermfw.common.*;
import it.thera.thip.base.azienda.Azienda;
import it.thera.thip.base.azienda.Magazzino;

import com.thera.thermfw.security.*;

/**
 * <h1>Softre Solutions</h1>
 * @author Daniele Signoroni	04/10/2022
 * <br></br>	
 * <b>70687	DSSOF3	04/10/2022</b>
 * <p>Prima stesura.</p>
 * <b>71469	DSSOF3	28/03/2024</b>
 * <p>Aggiungere {@link #setDirty()} su {@link #setRIdUdsPadre(String)}</p>
 * <b>71512	DSSOF3	22/04/2024</b>
 * <p>
 * Correzioni.<br>
 * </p>
 */

public abstract class YUdsVenditaPO extends EntitaAzienda implements BusinessObject, Authorizable, Deletable, Conflictable {

	private static YUdsVendita cInstance;

	protected String iIdUds;

	protected java.sql.Date iDataUds;

	protected BigDecimal iPesoNetto;

	protected BigDecimal iPesoLordo;

	protected BigDecimal iPesoUds;

	protected BigDecimal iVolume;

	protected BigDecimal iAltezza;

	protected BigDecimal iLunghezza;

	protected BigDecimal iLarghezza;

	protected String iRIdUdsPadre;

	protected String iNote;

	protected char iStatoStEti = '0';

	protected char iStatoStPacking = '0';

	protected char iStatoEvasione = '0';

	protected String iRAnnoDocVen;

	protected String iRNumDocVen;

	protected Proxy iReltipouds = new Proxy(it.softre.thip.base.uds.YTipoUds.class);

	protected Proxy iRelcliente = new Proxy(it.thera.thip.base.cliente.ClienteVendita.class);

	//protected Proxy iRelubicazione = new Proxy(it.thera.thip.logisticaLight.Ubicazione.class);

	protected Proxy iRelUdsPadre = new Proxy(it.softre.thip.vendite.uds.YUdsVendita.class);

	protected Proxy iRelDocumentoVendita = new Proxy(it.thera.thip.vendite.documentoVE.DocumentoVendita.class);

	protected Proxy iRelMagazzino = new Proxy(it.thera.thip.base.azienda.Magazzino.class);

	protected OneToMany iRigheUDSVendita = new OneToMany(it.softre.thip.vendite.uds.YUdsVenRig.class, this, 3, false);

	protected Proxy iDocumentoVenditaTrasf = new Proxy(it.thera.thip.vendite.documentoVE.DocumentoVendita.class);

	@SuppressWarnings("rawtypes")
	public static Vector retrieveList(String where, String orderBy, boolean optimistic) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (cInstance == null)
			cInstance = (YUdsVendita)Factory.createObject(YUdsVendita.class);
		return PersistentObject.retrieveList(cInstance, where, orderBy, optimistic);
	}

	public static YUdsVendita elementWithKey(String key, int lockType) throws SQLException {
		return (YUdsVendita)PersistentObject.elementWithKey(YUdsVendita.class, key, lockType);
	}

	public YUdsVenditaPO() {
		setStatoStEti('0');
		setStatoStPacking('0');
		setStatoEvasione('0');
		setIdAzienda(Azienda.getAziendaCorrente());
	}

	public void setIdUds(String idUds) {
		this.iIdUds = idUds;
		setDirty();
		setOnDB(false);
		iRigheUDSVendita.setFatherKeyChanged();
	}

	public String getIdUds() {
		return iIdUds;
	}

	public void setDataUds(java.sql.Date dataUds) {
		this.iDataUds = dataUds;
		setDirty();
	}

	public java.sql.Date getDataUds() {
		return iDataUds;
	}

	public void setPesoNetto(BigDecimal pesoNetto) {
		this.iPesoNetto = pesoNetto;
		setDirty();
	}

	public BigDecimal getPesoNetto() {
		return iPesoNetto;
	}

	public void setPesoLordo(BigDecimal pesoLordo) {
		this.iPesoLordo = pesoLordo;
		setDirty();
	}

	public BigDecimal getPesoLordo() {
		return iPesoLordo;
	}

	public void setPesoUds(BigDecimal pesoUds) {
		this.iPesoUds = pesoUds;
		setDirty();
	}

	public BigDecimal getPesoUds() {
		return iPesoUds;
	}

	public void setVolume(BigDecimal volume) {
		this.iVolume = volume;
		setDirty();
	}

	public BigDecimal getVolume() {
		return iVolume;
	}

	public void setAltezza(BigDecimal altezza) {
		this.iAltezza = altezza;
		setDirty();
	}

	public BigDecimal getAltezza() {
		return iAltezza;
	}

	public void setLunghezza(BigDecimal lunghezza) {
		this.iLunghezza = lunghezza;
		setDirty();
	}

	public BigDecimal getLunghezza() {
		return iLunghezza;
	}

	public void setLarghezza(BigDecimal larghezza) {
		this.iLarghezza = larghezza;
		setDirty();
	}

	public BigDecimal getLarghezza() {
		return iLarghezza;
	}

	public void setNote(String note) {
		this.iNote = note;
		setDirty();
	}

	public String getNote() {
		return iNote;
	}

	public void setStatoStEti(char statoStEti) {
		this.iStatoStEti = statoStEti;
		setDirty();
	}

	public char getStatoStEti() {
		return iStatoStEti;
	}

	public void setStatoStPacking(char statoStPacking) {
		this.iStatoStPacking = statoStPacking;
		setDirty();
	}

	public char getStatoStPacking() {
		return iStatoStPacking;
	}

	public void setStatoEvasione(char statoEvasione) {
		this.iStatoEvasione = statoEvasione;
		setDirty();
	}

	public char getStatoEvasione() {
		return iStatoEvasione;
	}

	public void setReltipouds(YTipoUds reltipouds) {
		String oldObjectKey = getKey();
		String idAzienda = getIdAzienda();
		if (reltipouds != null) {
			idAzienda = KeyHelper.getTokenObjectKey(reltipouds.getKey(), 1);
		}
		setIdAziendaInternal(idAzienda);
		this.iReltipouds.setObject(reltipouds);
		setDirty();
		if (!KeyHelper.areEqual(oldObjectKey, getKey())) {
			setOnDB(false);
			iRigheUDSVendita.setFatherKeyChanged();
		}
	}

	public YTipoUds getReltipouds() {
		return (YTipoUds)iReltipouds.getObject();
	}

	public void setReltipoudsKey(String key) {
		String oldObjectKey = getKey();
		iReltipouds.setKey(key);
		String idAzienda = KeyHelper.getTokenObjectKey(key, 1);
		setIdAziendaInternal(idAzienda);
		setDirty();
		if (!KeyHelper.areEqual(oldObjectKey, getKey())) {
			setOnDB(false);
			iRigheUDSVendita.setFatherKeyChanged();
		}
	}

	public String getReltipoudsKey() {
		return iReltipouds.getKey();
	}

	public void setRTipoUds(String rTipoUds) {
		String key = iReltipouds.getKey();
		iReltipouds.setKey(KeyHelper.replaceTokenObjectKey(key , 2, rTipoUds));
		setDirty();
	}

	public String getRTipoUds() {
		String key = iReltipouds.getKey();
		String objRTipoUds = KeyHelper.getTokenObjectKey(key,2);
		return objRTipoUds;
	}

	public void setRelcliente(ClienteVendita relcliente) {
		String oldObjectKey = getKey();
		String idAzienda = getIdAzienda();
		if (relcliente != null) {
			idAzienda = KeyHelper.getTokenObjectKey(relcliente.getKey(), 1);
		}
		setIdAziendaInternal(idAzienda);
		this.iRelcliente.setObject(relcliente);
		setDirty();
		if (!KeyHelper.areEqual(oldObjectKey, getKey())) {
			setOnDB(false);
			iRigheUDSVendita.setFatherKeyChanged();
		}
	}

	public ClienteVendita getRelcliente() {
		return (ClienteVendita)iRelcliente.getObject();
	}

	public void setRelclienteKey(String key) {
		String oldObjectKey = getKey();
		iRelcliente.setKey(key);
		String idAzienda = KeyHelper.getTokenObjectKey(key, 1);
		setIdAziendaInternal(idAzienda);
		setDirty();
		if (!KeyHelper.areEqual(oldObjectKey, getKey())) {
			setOnDB(false);
			iRigheUDSVendita.setFatherKeyChanged();
		}
	}

	public String getRelclienteKey() {
		return iRelcliente.getKey();
	}

	public void setRCliente(String rCliente) {
		String key = iRelcliente.getKey();
		iRelcliente.setKey(KeyHelper.replaceTokenObjectKey(key , 2, rCliente));
		setDirty();
	}

	public String getRCliente() {
		String key = iRelcliente.getKey();
		String objRCliente = KeyHelper.getTokenObjectKey(key,2);
		return objRCliente;
	}

	//	public void setRelubicazione(Ubicazione relubicazione) {
	//		String oldObjectKey = getKey();
	//		String idAzienda = getIdAzienda();
	//		if (relubicazione != null) {
	//			idAzienda = KeyHelper.getTokenObjectKey(relubicazione.getKey(), 1);
	//		}
	//		setIdAziendaInternal(idAzienda);
	//		this.iRelubicazione.setObject(relubicazione);
	//		setDirty();
	//		if (!KeyHelper.areEqual(oldObjectKey, getKey())) {
	//			setOnDB(false);
	//			iRigheUDSVendita.setFatherKeyChanged();
	//		}
	//	}

	public Magazzino getMagazzino() {
		return (Magazzino)iRelMagazzino.getObject();
	}

	public void setMagazzino(Magazzino iMagazzino) {
		this.iRelMagazzino.setObject(iMagazzino);
		setDirty();
	}

	public String getMagazzinoKey() {
		return iRelMagazzino.getKey();
	}

	public void setMagazzinoKey(String key) {
		iRelMagazzino.setKey(key);
		setDirty();
	}

	public String getRMagazzino() {
		String key = iRelMagazzino.getKey();
		String rMagazzino = KeyHelper.getTokenObjectKey(key,2);
		return rMagazzino;
	}

	public void setRMagazzino(String rMagazzino) {
		String key = iRelMagazzino.getKey();
		iRelMagazzino.setKey(KeyHelper.replaceTokenObjectKey(key , 2, rMagazzino));
		setDirty();
	}

	/*
	public Ubicazione getRelubicazione() {
		return (Ubicazione)iRelubicazione.getObject();
	}

	public void setRelubicazioneKey(String key) {
		String oldObjectKey = getKey();
		iRelubicazione.setKey(key);
		String idAzienda = KeyHelper.getTokenObjectKey(key, 1);
		setIdAziendaInternal(idAzienda);
		setDirty();
		if (!KeyHelper.areEqual(oldObjectKey, getKey())) {
			setOnDB(false);
			iRigheUDSVendita.setFatherKeyChanged();
		}
	}

	public String getRelubicazioneKey() {
		return iRelubicazione.getKey();
	}
	 */

	public void setIdAzienda(String idAzienda) {
		setIdAziendaInternal(idAzienda);
		setDirty();
		setOnDB(false);
		iRigheUDSVendita.setFatherKeyChanged();
	}

	public String getIdAzienda() {
		String key = iAzienda.getKey();
		return key;
	}

	/*
	public void setRUbicazione(String rUbicazione) {
		String key = iRelubicazione.getKey();
		iRelubicazione.setKey(KeyHelper.replaceTokenObjectKey(key , 3, rUbicazione));
		setDirty();
	}

	public String getRUbicazione() {
		String key = iRelubicazione.getKey();
		String objRUbicazione = KeyHelper.getTokenObjectKey(key,3);
		return objRUbicazione;
	}
	 */

	@SuppressWarnings("rawtypes")
	public List getRigheUDSVendita() {
		return getRigheUDSVenditaInternal();
	}

	public void setEqual(Copyable obj) throws CopyException {
		super.setEqual(obj);
		YUdsVenditaPO yUdsVenditaPO = (YUdsVenditaPO)obj;
		if (yUdsVenditaPO.iDataUds != null)
			iDataUds = (java.sql.Date)yUdsVenditaPO.iDataUds.clone();
		iReltipouds.setEqual(yUdsVenditaPO.iReltipouds);
		iRelcliente.setEqual(yUdsVenditaPO.iRelcliente);
		//iRelubicazione.setEqual(yUdsVenditaPO.iRelubicazione);
		iRigheUDSVendita.setEqual(yUdsVenditaPO.iRigheUDSVendita);
	}

	@SuppressWarnings("rawtypes")
	public Vector checkAll(BaseComponentsCollection components) {
		Vector errors = new Vector();
		components.runAllChecks(errors);
		return errors;
	}

	public void setKey(String key) {
		setIdAzienda(KeyHelper.getTokenObjectKey(key, 1));
		setIdUds(KeyHelper.getTokenObjectKey(key, 2));
	}

	public String getKey() {
		String idAzienda = getIdAzienda();
		String idUds = getIdUds();
		Object[] keyParts = {idAzienda, idUds};
		return KeyHelper.buildObjectKey(keyParts);
	}

	public boolean isDeletable() {
		return checkDelete() == null;
	}

	public int saveOwnedObjects(int rc) throws SQLException {
		rc = iRigheUDSVendita.save(rc);
		return rc;
	}

	public int deleteOwnedObjects() throws SQLException {
		return getRigheUDSVenditaInternal().delete();
	}

	public boolean initializeOwnedObjects(boolean result) {
		result = iRigheUDSVendita.initialize(result);
		return result;
	}

	public String toString() {
		return getClass().getName() + " [" + KeyHelper.formatKeyString(getKey()) + "]";
	}

	protected TableManager getTableManager() throws SQLException {
		return YUdsVenditaTM.getInstance();
	}

	protected OneToMany getRigheUDSVenditaInternal() {
		if (iRigheUDSVendita.isNew())
			iRigheUDSVendita.retrieve();
		return iRigheUDSVendita;
	}

	protected void setIdAziendaInternal(String idAzienda) {
		iAzienda.setKey(idAzienda);
		String key2 = iRelUdsPadre.getKey();
		iRelUdsPadre.setKey(KeyHelper.replaceTokenObjectKey(key2, 1, idAzienda));
		String key3 = iRelcliente.getKey();
		iRelcliente.setKey(KeyHelper.replaceTokenObjectKey(key3, 1, idAzienda));
		String keyMagaz = iRelMagazzino.getKey();
		iRelMagazzino.setKey(KeyHelper.replaceTokenObjectKey(keyMagaz, 1, idAzienda));
		//String key4 = iRelubicazione.getKey();
		//iRelubicazione.setKey(KeyHelper.replaceTokenObjectKey(key4, 1, idAzienda));
		iDocumentoVenditaTrasf.setKey(KeyHelper.replaceTokenObjectKey(iDocumentoVenditaTrasf.getKey(), 1, idAzienda));
		//72XXX <
		String key4 = iReltipouds.getKey();
		iReltipouds.setKey(KeyHelper.replaceTokenObjectKey(key4, 1, idAzienda));
		//72XXX >
	}

	public void setRelUdsPadre(YUdsVendita parent) {
		String idAzienda = getIdAzienda();
		if (parent != null) {
			idAzienda = KeyHelper.getTokenObjectKey(parent.getKey(), 1);
		}
		setIdAziendaInternal(idAzienda);
		this.iRelUdsPadre.setObject(parent);
		setDirty();
		setOnDB(false);
	}

	public YUdsVendita getRelUdsPadre() {
		return (YUdsVendita)iRelUdsPadre.getObject();
	}

	public void setRelUdsPadreKey(String key) {
		iRelUdsPadre.setKey(key);
		String idAzienda = KeyHelper.getTokenObjectKey(key, 1);
		setIdAziendaInternal(idAzienda);
		setDirty();
		setOnDB(false);
	}

	public String getRelUdsPadreKey() {
		return iRelUdsPadre.getKey();
	}

	public String getRIdUdsPadre() {
		String key = iRelUdsPadre.getKey();
		String objRIdUdsPadre = KeyHelper.getTokenObjectKey(key,2);
		return objRIdUdsPadre;
	}

	public void setRIdUdsPadre(String iRIdUdsPadre) {
		String key = iRelUdsPadre.getKey();
		iRelUdsPadre.setKey(KeyHelper.replaceTokenObjectKey(key , 2, iRIdUdsPadre));
		setDirty();
	}

	public void setRAnnoDocVen(String rAnnoDocVen) {
		this.iRAnnoDocVen = rAnnoDocVen;
		setDirty();
	}

	public String getRAnnoDocVen() {
		return iRAnnoDocVen;
	}

	public void setRNumDocVen(String rNumDocVen) {
		this.iRNumDocVen = rNumDocVen;
		setDirty();
	}

	public String getRNumDocVen() {
		return iRNumDocVen;
	}

	public DocumentoVendita getDocumentoVendita(){
		return (DocumentoVendita)iRelDocumentoVendita.getObject();
	}

	public void setDocumentoVendita(DocumentoVendita documentoVendita){
		iRelDocumentoVendita.setObject(documentoVendita);
	}

	public String getDocumentoVenditaKey(){
		return iRelDocumentoVendita.getKey();
	}

	public void setDocumentoVenditaKey(String key){
		iRelDocumentoVendita.setKey(key);
		setDirty();
	}

	public void setIdAnnoDocumentoTrasf(String annoDocumentoIC){
		String key = iDocumentoVenditaTrasf.getKey();
		iDocumentoVenditaTrasf.setKey(KeyHelper.replaceTokenObjectKey(key , 2, annoDocumentoIC));
		setDirty();
	}
	
	public String getIdAnnoDocumentoTrasf(){
		String key = iDocumentoVenditaTrasf.getKey();
		String objRAnnoDoc = KeyHelper.getTokenObjectKey(key,2);
		return objRAnnoDoc;
	}
	
	public void setIdNumeroDocumentoTrasf(String numeroDocumentoIC){
		String key = iDocumentoVenditaTrasf.getKey();
		iDocumentoVenditaTrasf.setKey(KeyHelper.replaceTokenObjectKey(key , 3, numeroDocumentoIC));
		setDirty();
	}
	
	public String getIdNumeroDocumentoTrasf(){
		String key = iDocumentoVenditaTrasf.getKey();
		String objRNumeroDoc = KeyHelper.getTokenObjectKey(key,3);
		return objRNumeroDoc;
	}

	public void setDocumentoVenditaTrasf(DocumentoVendita documento){
		this.iDocumentoVenditaTrasf.setObject(documento);
		setDirty();
	}

	public DocumentoVendita getDocumentoVenditaTrasf(){
		return (DocumentoVendita)iDocumentoVenditaTrasf.getObject();
	}

	public void setDocumentoVenditaTrasfKey(String key){
		iDocumentoVenditaTrasf.setKey(key);
		setDirty();
	}

	public String getDocumentoVenditaTrasfKey(){
		return iDocumentoVenditaTrasf.getKey();
	}

	public YDocumentoVendita getDocumentoVenditaObj() throws SQLException {
		YDocumentoVendita doc = null;
		String key = Azienda.getAziendaCorrente() + KEY_SEPARATOR + this.getRAnnoDocVen() + KEY_SEPARATOR + this.getRNumDocVen();
		doc = (YDocumentoVendita) YDocumentoVendita.elementWithKey(YDocumentoVendita.class, key, NO_LOCK);
		return doc;
	}
}

