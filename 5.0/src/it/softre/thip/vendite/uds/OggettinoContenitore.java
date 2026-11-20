package it.softre.thip.vendite.uds;

import java.math.BigDecimal;

/**
 * <p></p>
 *
 * <p>
 * Company: Softre Solutions<br>
 * Author: Daniele Signoroni<br>
 * Date: 20/11/2026
 * </p>
 */

/*
 * Revisions:
 * Number   Date        Owner    Description
 * 72XXX    20/11/2026  DSSOF3   Prima stesura
 */

public class OggettinoContenitore {

	protected String iIdArticolo;

	protected BigDecimal iQuantita;

	public String getIdArticolo() {
		return iIdArticolo;
	}

	public void setIdArticolo(String iIdArticolo) {
		this.iIdArticolo = iIdArticolo;
	}

	public BigDecimal getQuantita() {
		return iQuantita;
	}

	public void setQuantita(BigDecimal iQuantita) {
		this.iQuantita = iQuantita;
	}

}