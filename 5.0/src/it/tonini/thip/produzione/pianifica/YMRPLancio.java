package it.tonini.thip.produzione.pianifica;

import java.util.List;

import it.thera.thip.produzione.pianifica.MRPLancio;
import it.tonini.thip.base.generale.YAmbientePianificazione;

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

public class YMRPLancio extends MRPLancio {

	protected boolean iImpegniContenitoriUDS = false;

	public YMRPLancio() {
		super();
		setImpegniContenitoriUDS(false);
	}

	public void setImpegniContenitoriUDS(boolean ImpegniContenitoriUDS) {
		this.iImpegniContenitoriUDS = ImpegniContenitoriUDS;
	}

	public boolean getImpegniContenitoriUDS() {
		return iImpegniContenitoriUDS;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List inizializza() {
		List sup = super.inizializza();
		if(sup.isEmpty() && getAmbientePianificazione() != null) {
			setImpegniContenitoriUDS(((YAmbientePianificazione)getAmbientePianificazione()).getImpegniContenitoriUDS());
		}
		return sup;
	}

}
