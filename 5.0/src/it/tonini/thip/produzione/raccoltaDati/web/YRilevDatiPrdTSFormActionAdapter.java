package it.tonini.thip.produzione.raccoltaDati.web;

import java.io.IOException;

import javax.servlet.ServletException;

import com.thera.thermfw.ad.ClassADCollection;
import com.thera.thermfw.web.ServletEnvironment;

import it.thera.thip.produzione.raccoltaDati.web.RilevDatiPrdTSFormActionAdapter;

/**
 *
 * <p></p>
 *
 * <p>
 * Company: Softre Solutions<br>
 * Author: Daniele Signoroni<br>
 * Date: 10/02/2026
 * </p>
 */

/*
 * Revisions:
 * Number   Date        Owner    Description
 * 72XXX    10/02/2026  DSSOF3   Prima stesura
 */

public class YRilevDatiPrdTSFormActionAdapter extends RilevDatiPrdTSFormActionAdapter {

	private static final long serialVersionUID = 1L;

	public static final String FINESTRA_DISAMBIGUAZIONE_REPARTI = "FINESTRA_DISAMBIGUAZIONE_REPARTI";
	public static final String FINESTRA_DISAMBIGUAZIONE_SCELTA_OPERATORE = "FINESTRA_DISAMBIGUAZIONE_SCELTA_OPERATORE";

	@Override
	protected void otherActions(ClassADCollection cadc, ServletEnvironment se) throws ServletException, IOException {
		String azione = getStringParameter(se.getRequest(), ACTION).toUpperCase();
		if(azione.equals(FINESTRA_DISAMBIGUAZIONE_SCELTA_OPERATORE)) {
			azioneDichiarazione(azione, se);
		}else {
			super.otherActions(cadc, se);
		}
	}
}