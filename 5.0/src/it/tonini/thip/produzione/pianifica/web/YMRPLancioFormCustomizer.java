package it.tonini.thip.produzione.pianifica.web;

import com.thera.thermfw.web.WebFormCustomizerDefault;

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

public class YMRPLancioFormCustomizer extends WebFormCustomizerDefault{

	@Override
	public String getCustomizationFileName() {
		return "it/tonini/thip/produzione/pianifica/xml/YMRPLancio.xml";
	}

}
