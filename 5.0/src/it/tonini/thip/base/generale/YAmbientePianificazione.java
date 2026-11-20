package it.tonini.thip.base.generale;

import com.thera.thermfw.persist.*;
import it.thera.thip.base.generale.*;

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

public class YAmbientePianificazione extends AmbientePianificazione {

	protected boolean iImpegniContenitoriUDS = false;

	public YAmbientePianificazione() {
		super();
		setImpegniContenitoriUDS(false);
	}

	public void setImpegniContenitoriUDS(boolean ImpegniContenitoriUDS) {
		this.iImpegniContenitoriUDS = ImpegniContenitoriUDS;
		setDirty();
	}

	public boolean getImpegniContenitoriUDS() {
		return iImpegniContenitoriUDS;
	}

	public void setEqual(Copyable obj) throws CopyException {
		super.setEqual(obj);
	}

}

