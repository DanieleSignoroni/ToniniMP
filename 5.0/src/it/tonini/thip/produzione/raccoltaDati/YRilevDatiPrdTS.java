package it.tonini.thip.produzione.raccoltaDati;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.thera.thermfw.base.Trace;
import com.thera.thermfw.persist.CachedStatement;
import com.thera.thermfw.persist.ConnectionManager;
import com.thera.thermfw.persist.Database;
import com.thera.thermfw.persist.KeyHelper;
import com.thera.thermfw.persist.PersistentObject;

import it.thera.thip.base.azienda.Azienda;
import it.thera.thip.base.azienda.Reparto;
import it.thera.thip.produzione.ordese.AttivitaEsecutiva;
import it.thera.thip.produzione.raccoltaDati.ListaAttivita;
import it.thera.thip.produzione.raccoltaDati.ListaAttivitaTM;
import it.thera.thip.produzione.raccoltaDati.RilevDatiPrdTS;

/**
 *
 * <p></p>
 *
 * <p>
 * Company: Softre Solutions<br>
 * Author: Daniele Signoroni<br>
 * Date: 12/11/2025
 * </p>
 */

/*
 * Revisions:
 * Number   Date        Owner    Description
 * 72153    12/11/2025  DSSOF3   Prima stesura
 */

public class YRilevDatiPrdTS extends RilevDatiPrdTS {

	public static final String YRES_FILE = "it.tonini.thip.produzione.raccoltaDati.resources.YRilevDatiPrdTS";

	private static final String STMT_REPARTI_AMB_ATV_DA_FARE = "SELECT "
			+ "	DISTINCT("+ListaAttivitaTM.R_REPARTO+") "
			+ "FROM "
			+ "	"+ListaAttivitaTM.TABLE_NAME+" "
			+ "WHERE "
			+ "	ID_AZIENDA = ? "
			+ "	AND ID_AMBIENTE = ? "
			+ "	AND (STATO_RIL_ATV = '"+ListaAttivita.IN_CORSO+"' "
			+ "		OR STATO_RIL_ATV = '"+ListaAttivita.SOSPESA+"')";
	public static CachedStatement cSelezionaRepartiAmbienteListaAtvDaFare = new CachedStatement(STMT_REPARTI_AMB_ATV_DA_FARE);

	protected Integer iNrEtichette;
	protected BigDecimal iQtaEtichetta;

	public Integer getNrEtichette() {
		return iNrEtichette;
	}
	public void setNrEtichette(Integer iNrEtichette) {
		this.iNrEtichette = iNrEtichette;
	}
	public BigDecimal getQtaEtichetta() {
		return iQtaEtichetta;
	}
	public void setQtaEtichetta(BigDecimal iQtaEtichetta) {
		this.iQtaEtichetta = iQtaEtichetta;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List listaRepartiAmbienteAttivita(String idAmbiente) {
		List reparti = new ArrayList();
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			Database db = ConnectionManager.getCurrentDatabase();
			ps = cSelezionaRepartiAmbienteListaAtvDaFare.getStatement();
			db.setString(ps, 1, getIdAzienda());
			db.setString(ps, 2, idAmbiente);
			rs = ps.executeQuery();
			while(rs.next()) {
				reparti.add(Reparto.elementWithKey(Reparto.class, KeyHelper.buildObjectKey(new String[] {
						getIdAzienda(),rs.getString(ListaAttivitaTM.R_REPARTO)
				}), PersistentObject.NO_LOCK));
			}
		}catch (SQLException e) {
			e.printStackTrace(Trace.excStream);
		}
		return reparti;
	}

	public static BigDecimal[] calcolaQuantitaGiacenzaMaterialePrincipale(AttivitaEsecutiva attivitaEsecutiva) {
		return new BigDecimal[] {BigDecimal.ZERO,BigDecimal.ZERO};
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Vector getListAttivitaNonIniziataInternal(String where, String orderBy) throws Exception {
		if(getNote() != null) {
			Reparto reparto = (Reparto) Reparto.elementWithKey(Reparto.class, KeyHelper.buildObjectKey(new String[] {
					Azienda.getAziendaCorrente(),getNote()
			}), PersistentObject.NO_LOCK);
			if(reparto != null) {
				where += " AND "+ListaAttivitaTM.R_REPARTO+" = '"+reparto.getIdReparto()+"' ";
			}
		}
		return super.getListAttivitaNonIniziataInternal(where, orderBy);
	}
}