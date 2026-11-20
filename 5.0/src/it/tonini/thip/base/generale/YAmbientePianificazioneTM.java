package it.tonini.thip.base.generale;

import java.sql.*;
import it.thera.thip.base.generale.*;
import com.thera.thermfw.base.*;

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

public class YAmbientePianificazioneTM extends AmbientePianificazioneTM {

	public static final String IMP_CNR_UDS = "IMP_CNR_UDS";

	public static final String TABLE_NAME_EXT = SystemParam.getSchema("THIPPERS") + "YAMBIENTI_PIAN";

	private static final String CLASS_NAME = it.tonini.thip.base.generale.YAmbientePianificazione.class.getName();

	public YAmbientePianificazioneTM() throws SQLException {
		super();
	}

	protected void initialize() throws SQLException {
		super.initialize();
		setObjClassName(CLASS_NAME);
	}

	protected void initializeRelation() throws SQLException {
		super.initializeRelation();
		linkTable(TABLE_NAME_EXT);
		addAttributeOnTable("ImpegniContenitoriUDS", IMP_CNR_UDS, TABLE_NAME_EXT);

	}

}

