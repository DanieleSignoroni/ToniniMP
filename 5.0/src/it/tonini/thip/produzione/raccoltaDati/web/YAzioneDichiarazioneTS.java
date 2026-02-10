package it.tonini.thip.produzione.raccoltaDati.web;

import com.thera.thermfw.web.ServletEnvironment;

import it.thera.thip.produzione.raccoltaDati.RilevDatiPrdTS;
import it.thera.thip.produzione.raccoltaDati.web.AzioneDichiarazioneTS;
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

public class YAzioneDichiarazioneTS extends AzioneDichiarazioneTS {

	private static final long serialVersionUID = 1L;

	@Override
	public String getUrlPers(ServletEnvironment se, RilevDatiPrdTS bo, String url) {
		String urlPers = super.getUrlPers(se, bo, url);
		String action = se.getRequest().getParameter(ACTION);
		if(url != null && url.indexOf("DichiarazioneMonitor.jsp") > 0 ){
			urlPers = "it/tonini/thip/produzione/raccoltaDati/YFinestraDisambiguazioneReparto.jsp";
			se.getRequest().setAttribute("Action", YRilevDatiPrdTSFormActionAdapter.FINESTRA_DISAMBIGUAZIONE_REPARTI);
			se.getRequest().setAttribute("JspName", urlPers);
		}else if(action != null && action.equals(YRilevDatiPrdTSFormActionAdapter.FINESTRA_DISAMBIGUAZIONE_SCELTA_OPERATORE)) {
			urlPers = "it/thera/thip/produzione/raccoltaDati/DichiarazioneMonitor.jsp";
			se.getRequest().setAttribute("Action", RilevDatiPrdTSFormActionAdapter.MONITOR);
			se.getRequest().setAttribute("JspName", urlPers);
		}
		url = urlPers;
		return urlPers;
	}
}
