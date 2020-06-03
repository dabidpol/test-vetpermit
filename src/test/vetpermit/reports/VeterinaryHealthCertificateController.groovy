package test.check.reports;

import com.rameses.rcp.annotations.*;
import com.rameses.rcp.common.*;
import com.rameses.osiris2.common.*;
import com.rameses.osiris2.client.*;
import com.rameses.osiris2.reports.*;
import com.rameses.gov.etracs.rpt.common.*;
import com.rameses.etracs.shared.*;

public class VeterinaryHealthCertificateController extends ReportController
{
   @Service('VetPermitReportService')
   def svc;
   
   @Service("ReportParameterService")
   def paramSvc;

   
   String title = 'Certificate';
   String reportPath = 'test/vetpermit/reports/';
   String reportName = reportPath + 'vethealthcert.jasper';
   
   def entity;
   
   public def getReportData(){
       
       //return svc.getCheckReportData(entity);
       def a = svc.getVetReportData(entity)
 
       return a;
       
       
       
   }
   
   Map getParameters (){
        def params = paramSvc.getStandardParameter()
       
        params.logos = ReportUtil.getImageAsStream("buk.png")
        params.pvetlogo = ReportUtil.getImageAsStream("pvetlogo.png")
        return params 
   }
   
//    Map getParameters (){
//        def params = paramSvc.getStandardParameter()
//       
//        params.signature = EtracsReportUtil.getInputStream("sirJoelSignature.png")
//        return params 
//   }
   
   SubReport[] getSubReports(){
       return[
           new SubReport('VETHEALTHCERTITEMS', reportPath+'vethealthcertitems.jasper'), 
       ]
   }
   
    
}

