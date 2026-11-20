package it.thera.thip.produzione.raccoltaDati.web;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import com.thera.thermfw.ad.*;
import com.thera.thermfw.batch.*;
import com.thera.thermfw.collector.*;
import com.thera.thermfw.persist.*;
import com.thera.thermfw.web.*;
import com.thera.thermfw.web.servlet.*;
import it.thera.thip.cs.*;
import it.thera.thip.produzione.ordese.*;
import it.thera.thip.produzione.raccoltaDati.*;
import it.tonini.thip.produzione.ordese.YRptStampaBollaLavorazioneBatch;
import it.tonini.thip.produzione.raccoltaDati.YRilevDatiPrdTS;
import it.thera.thip.base.azienda.Azienda;
import com.thera.thermfw.gui.ScreenData;//Fix 17698

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2010</p>
 * <p>Company: SOFTHER </p>
 * @author Amara
 * @version 1.0
 */
/**
 * Revisions:
 * Number    Date          Owner       Description
 * 13573     09/12/2010    Amara       Prima Versione
 * 14743     25/10/2011    HBT         Gestione buttone stampa bolla cucita
 * 15250     29/11/2011    HBT         correggeo reportId della stampa della bolla cucita
 * 16629     17/07/2012    TF          In caso di lancio di stampe singole non deve essere applicato l'MDV eventualmente presente
 * 17441     22/02/2013    AA          Correzioni di statoBollaLav & statoBollaCucita in modo che la bolla di lavorazione venga sempre stampata
 * 17698     16/04/2013    Linda       Spostiamo la chiamata dell bodc.setMDVApplyDisabled(true) con bodc.setMDVType(ScreenData.FROM_HDR).
 * 50850     02/10/2026    Mekki       Usare il nuovo filtro sull'attività esecutiva
 */

public class AzioneStampaBollaTS extends BaseServlet {

  // Fix 17441 Inizio
  public static final char DA_EMETTERE = '1';
  public static final char EMESSE = '2';
  public static final char ENTRAMBE = '3';
  public static final char NO = '4';
  // Fix 17441 Fine

  public void processAction(ServletEnvironment se) throws ServletException, IOException, SQLException {
    String key = getStringParameter(se.getRequest(), "thKey");
    String className = getStringParameter(se.getRequest(), "thClassName");
    String collectorName = getStringParameter(se.getRequest(), "thCollectorName");
    String action = (String) se.getRequest().getAttribute("Action");
    String reportId = "";
    String printerId = "";
    BollaCucitaTes bollaCucTes = getBollaCucita(getStringParameter(se.getRequest(), "BollaLavorazione"));//Fix 14743
    if (collectorName == null || collectorName.equals(""))
      collectorName = BODataCollector.class.getName();
    ClassADCollection cadc = getClassADCollection(className);
    RilevDatiPrdTSDataCollector boDC = (RilevDatiPrdTSDataCollector) Factory.createObject(collectorName);
    boDC.initialize(className, true);
    RilevDatiPrdTS rdpTS = (RilevDatiPrdTS) boDC.getBo();
    if(bollaCucTes == null){//Fix 14743
      RptStampaBollaLavorazioneBatch stampaBollaLav = (RptStampaBollaLavorazioneBatch) Factory.createObject(RptStampaBollaLavorazioneBatch.class);
      if (rdpTS.getPersDatiPrdUtenteRilev() != null) {
        if (rdpTS.getPersDatiPrdUtenteRilev().getReportModelStampaBolla() != null) {
          try {
            BatchOptions opt = (BatchOptions) Factory.createObject(BatchOptions.class);
            reportId = rdpTS.getPersDatiPrdUtenteRilev().getReportModelStampaBolla();
            printerId = rdpTS.getPersDatiPrdUtenteRilev().getStampante();
            stampaBollaLav.setReportId(reportId);
            stampaBollaLav.setPrinterId(printerId);
            stampaBollaLav.setStatoBollaLav(ENTRAMBE); // Fix 17441
            stampaBollaLav.setStatoTagliando(ENTRAMBE); // Fix 17441
            fillFiltri(stampaBollaLav.getCondizioniFiltro(), se);
            //DSSOF3 72XXX Inizio
            RilevDatiPrdTS boInSessione = (RilevDatiPrdTS) se.getSession().getAttribute(getStringParameter(se.getRequest(), "thChiaveDatiSessione"));
            if(boInSessione != null) {
            	((YRptStampaBollaLavorazioneBatch)stampaBollaLav).setQtaEtichetta(((YRilevDatiPrdTS)boInSessione).getQtaEtichetta());
            	((YRptStampaBollaLavorazioneBatch)stampaBollaLav).setNrEtichette(((YRilevDatiPrdTS)boInSessione).getNrEtichette());
            }
            //DSSOF3 72XXX Fine
            opt.setExecutionMode(BatchOptions.IMMEDIATE_EXECUTION);
            stampaBollaLav.setBatchExecution(true);
            ConnectionManager.pushConnection();
            opt.initDefaultValues(YRptStampaBollaLavorazioneBatch.class, "RptBollaLav", "RUN");
            stampaBollaLav.setBatchJob(opt.getBatchJob());
            BODataCollector boDCStampaBolla = (BODataCollector) Factory.createObject(BODataCollector.class);
            boDCStampaBolla.initialize("RptStampaBollaLav", true);
            boDCStampaBolla.setBo(stampaBollaLav);
            //boDCStampaBolla.setMDVApplyDisabled(true);//Fix 16629 //Fix 17698
            boDCStampaBolla.setMDVType(ScreenData.FROM_HDR);//Fix 17698
            int resSave = boDCStampaBolla.save();
            String errors = null;
            if (resSave >= ErrorCodes.NO_ROWS_UPDATED) {
              ConnectionManager.commit();
              Socket socket = BatchService.getConnection();
              BatchService.submitJob(stampaBollaLav.getBatchJob().getBatchJobId(), socket);
              if (stampaBollaLav.getBatchJob().retrieve()) {
                if (stampaBollaLav.getBatchJob().getStatus() == BatchJob.COMPLETED_ERROR)
                  errors = stampaBollaLav.getBatchJob().getLog();
              }
              se.getRequest().setAttribute("BatchErrors", errors);
            }
            else
              ConnectionManager.rollback();

            ConnectionManager.popConnection();
          }
          catch (Exception ex1) {
            ex1.printStackTrace();
          }
        }
      }
    }else//Fix 14743 inizio
    {
      StampaBolleCuciteBatch stampaBollaCuc = (StampaBolleCuciteBatch) Factory.createObject(StampaBolleCuciteBatch.class);
     if (rdpTS.getPersDatiPrdUtenteRilev() != null) {
      // if (rdpTS.getPersDatiPrdUtenteRilev().getReportModelStampaBolla() != null) {//Fix 15250
         if (rdpTS.getPersDatiPrdUtenteRilev().getReportModelStampaBollaCuc() != null) {//Fix 15250

         try {
           BatchOptions opt = (BatchOptions) Factory.createObject(BatchOptions.class);
           printerId = rdpTS.getPersDatiPrdUtenteRilev().getStampante();
           //stampaBollaCuc.setReportId("StampaBolleCuc");//Fix 15250
           stampaBollaCuc.setReportId(rdpTS.getPersDatiPrdUtenteRilev().getReportModelStampaBollaCuc());//Fix 15250
           stampaBollaCuc.setPrinterId(printerId);
           stampaBollaCuc.setStatoBollaCucita(StampaBolleCuciteBatch.ENTRAMBE); // Fix 17441
           stampaBollaCuc.setStatoTagliando(StampaBolleCuciteBatch.ENTRAMBE); // Fix 17441
           fillFiltriBollaCucita(stampaBollaCuc.getCondizioniFiltri(), bollaCucTes);
           opt.setExecutionMode(BatchOptions.IMMEDIATE_EXECUTION);
           stampaBollaCuc.setBatchExecution(true);
           ConnectionManager.pushConnection();
           opt.initDefaultValues(StampaBolleCuciteBatch.class, "StampaBolleCuc", "RUN");
           stampaBollaCuc.setBatchJob(opt.getBatchJob());
           BODataCollector boDCStampaBolla = (BODataCollector) Factory.createObject(BODataCollector.class);
           boDCStampaBolla.initialize("StampaBolleCuciteBatch", true);
           boDCStampaBolla.setBo(stampaBollaCuc);
           //boDCStampaBolla.setMDVApplyDisabled(true);//Fix 16629 //Fix 17698
           boDCStampaBolla.setMDVType(ScreenData.FROM_HDR);//Fix 17698
           int resSave = boDCStampaBolla.save();
           String errors = null;
           if (resSave >= ErrorCodes.NO_ROWS_UPDATED) {
             ConnectionManager.commit();
             Socket socket = BatchService.getConnection();
             BatchService.submitJob(stampaBollaCuc.getBatchJob().getBatchJobId(), socket);
             if (stampaBollaCuc.getBatchJob().retrieve()) {
               if (stampaBollaCuc.getBatchJob().getStatus() == BatchJob.COMPLETED_ERROR)
                 errors = stampaBollaCuc.getBatchJob().getLog();
             }
             se.getRequest().setAttribute("BatchErrors", errors);
           }
           else
             ConnectionManager.rollback();

           ConnectionManager.popConnection();
         }
         catch (Exception ex1) {
           ex1.printStackTrace();
         }
       }
     }

    }//Fix 14743 fine
  }

  public void fillFiltri(CondizioniFiltri filtro, ServletEnvironment se){
    String BollaLavorazione = getStringParameter(se.getRequest(), "BollaLavorazione");
    String IdArticolo = getStringParameter(se.getRequest(), "IdArticolo");
    String IdAnnoOrdine = getStringParameter(se.getRequest(), "IdAnnoOrdine");
    String IdNumeroOrdine = getStringParameter(se.getRequest(), "IdNumeroOrdine");
    String where = AttivitaEsecutivaTM.NUM_RITORNO +" = '"+BollaLavorazione+"'";
    try {
      List atvList = AttivitaEsecutiva.retrieveList(where, "", false);
      Vector columns = filtro.getColonneFiltro();
      for (int i = 0; i < columns.size(); i++) {
        ColonneFiltri colonneFiltro = (ColonneFiltri) columns.elementAt(i);
        //Fix 50850 --blocco commentato
        /*if (colonneFiltro.getClassAdName().equals("IdArticolo")) {
          CondizioniFiltri.svuotaColonnaFiltro(colonneFiltro);
          CondizioniFiltri.impostaFiltroLista(colonneFiltro, IdArticolo);
        }*/
        /*if (colonneFiltro.getClassAdName().equals("IdAttivita")) {
          CondizioniFiltri.svuotaColonnaFiltro(colonneFiltro);
          if(((AttivitaEsecutiva)atvList.get(0)) != null)
          CondizioniFiltri.impostaFiltroLista(colonneFiltro, ((AttivitaEsecutiva)atvList.get(0)).getIdAttivita());
        }*/
        //Fix 50850 --inizio
        if (colonneFiltro.getClassAdName().equals("IdRigaAttivita")) {
        	CondizioniFiltri.svuotaColonnaFiltro(colonneFiltro);
        	AttivitaEsecutiva atvEsec = ((AttivitaEsecutiva)atvList.get(0));
 	         String tmp = atvEsec.getOrdineEsecutivo().getNumeroOrdFmt() + ColonneFiltri.ANNO_SEP + atvEsec.getIdRigaAttivita();
 	         CondizioniFiltri.impostaFiltroFrom(colonneFiltro, tmp);
 	         CondizioniFiltri.impostaFiltroTo(colonneFiltro, tmp);
        }
        //Fix 50850 --fine
        //Fix 50850 --blocco commentato
        /*if (colonneFiltro.getClassAdName().equals("IdNumeroOrdine")) {
          int size = 0;
          size = colonneFiltro.getAdditionalClassAd().getType().getSize();
          while (IdAnnoOrdine.length() < size)
            IdAnnoOrdine += " ";
          String annoNumero = IdAnnoOrdine + ColonneFiltri.ANNO_SEP + IdNumeroOrdine;
          CondizioniFiltri.svuotaColonnaFiltro(colonneFiltro);
          CondizioniFiltri.impostaFiltroFrom(colonneFiltro, annoNumero);
          CondizioniFiltri.impostaFiltroTo(colonneFiltro, annoNumero);
        }*/
      }
    }
    catch (Exception ex) {ex.printStackTrace();}
  }//Fix 14743 inizio
  public void fillFiltriBollaCucita(CondizioniFiltri filtro, BollaCucitaTes bollaCucTes){
    try {
      Vector columns = filtro.getColonneFiltro();
      String idAnnoBolla = bollaCucTes.getIdAnnoBolla();
      for (int i = 0; i < columns.size(); i++) {
        ColonneFiltri colonneFiltro = (ColonneFiltri) columns.elementAt(i);
        if (colonneFiltro.getClassAdName().equals("IdNumeroBolla")) {
          int size = 0;
          size = colonneFiltro.getAdditionalClassAd().getType().getSize();
          while (idAnnoBolla.length() < size)
           { idAnnoBolla += " ";
           }
          String annoNumero = idAnnoBolla + ColonneFiltri.ANNO_SEP + bollaCucTes.getIdNumeroBolla();
          CondizioniFiltri.svuotaColonnaFiltro(colonneFiltro);
          CondizioniFiltri.impostaFiltroFrom(colonneFiltro, annoNumero);
          CondizioniFiltri.impostaFiltroTo(colonneFiltro, annoNumero);
        }
      }
    }
    catch (Exception ex) {ex.printStackTrace();}
  }
  public BollaCucitaTes getBollaCucita(String numRit)
  {
  BollaCucitaTes bollCucitaTes = null;
  PreparedStatement ps;
  ResultSet resultSet = null;
  try {
    ps = RilevazioneDatiProdTes.selectBollaCucitaStatement.getStatement();
    Database db = ConnectionManager.getCurrentDatabase();
    db.setString(ps, 1, Azienda.getAziendaCorrente());
    db.setString(ps, 2, numRit);
    resultSet = ps.executeQuery();
      while (resultSet.next()) {
        String idAnnoBolla = resultSet.getString(BollaCucitaTesTM.ID_ANNO_BOLLA);
        String idNumeroBolla = resultSet.getString(BollaCucitaTesTM.ID_NUMERO_BOLLA);
        bollCucitaTes = (BollaCucitaTes) Factory.createObject(BollaCucitaTes.class);
        bollCucitaTes.setIdAzienda(Azienda.getAziendaCorrente());
        bollCucitaTes.setIdAnnoBolla(idAnnoBolla);
        bollCucitaTes.setIdNumeroBolla(idNumeroBolla);
        bollCucitaTes.retrieve();
        return bollCucitaTes;
      }
  }
  catch (SQLException ex) {
    ex.printStackTrace();
  }
  finally
  {
    if(resultSet != null )
    {
      try {
        resultSet.close();
      }
      catch (SQLException ex1) {
        ex1.printStackTrace();
      }
    }
  }
  return bollCucitaTes;
}
//Fix 14743 fine
}
