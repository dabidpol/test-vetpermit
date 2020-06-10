package test.vetpermit.models;

import com.rameses.rcp.annotations.*;
import com.rameses.rcp.common.*;
import com.rameses.seti2.models.*;
import com.rameses.osiris2.client.*
import com.rameses.osiris2.common.*;
import com.rameses.util.*;

class VetPermitOfflineModel extends CrudFormModel{

    @Service('DateService')
    def dtSvc
    
    @Service('NumberService')
    def numSvc
        
    @Service("ListService")
    def service;
    
    @Service('SequenceService')
    def seqSvc
    
    @Service("VetPermitService")
    def vpSvc
    
//    boolean isAllowPaymentOrder(){
////        def t = srSvc.findReceiptData(entity)
////        
////        //MsgBox.alert(t)
////        
////        if (t>90){
////            return true
////        }else{
////            return false
////        }
//        
//        return true
//        
//    }
    //boolean editAllowed = false;
    
   // boolean isAllowApprove() {
    //     return ( entity.state.toString().matches('DRAFT|CLOSED') ); 
   // }
   
    def genders = ['M','F','M/F']
    def agetypes = ['YEARS', 'MONTHS', 'WEEKS', 'DAYS']
   
    public void afterCreate(){
        entity.txndate = dtSvc.getBasicServerDate();
        //entity.expdate = dtSvc.getBasicServerDate() + 3;
        entity.state = "OFFLINE"
        entity.seqno = "VETOFFLINE-" + dtSvc.getServerYear()+ "-" + seqSvc.getNextFormattedSeries('vetpermitoffline');
        
    }
   
    public void beforeSave(o){
        
        
        
        entity.recordlog_datecreated = dtSvc.getServerDate();
        entity.recordlog_createdbyuser = OsirisContext.env.FULLNAME;
        entity.recordlog_createdbyuserid = OsirisContext.env.USERID;
        
//        if (entity.isnonpaying == true) {
//            entity.state = 'CLOSED'
//        }
        
       //println entity
    }

    public void afterEdit(){
        entity.recordlog_dateupdated = dtSvc.getServerDate();
        entity.recordlog_lastupdatedbyuser = OsirisContext.env.FULLNAME;
        entity.recordlog_lastupdatedbyuserid = OsirisContext.env.USERID;
    }
   
         /* ========== Lookup ========= */
    def getLookupPurpose(){
       return Inv.lookupOpener('purpose:lookup',[
               onselect :{
                     entity.purpose = it
//                   entity.purpose.objid = it.objid;
//                   entity.purpose.code = it.code;
//                   entity.purpose.name = it.name;
                   binding.refresh(); 
               },
           ])
   }
   
    def getLookupSpecie(){
       return Inv.lookupOpener('specie:lookup',[
               onselect :{
                    entity.specie = it
//                   entity.specie.objid = it.objid;
//                   entity.specie.code = it.code;
//                   entity.specie.name = it.name;
                   binding.refresh(); 
               },
           ])
   }
   
   def getLookupSpecie2(){
       return Inv.lookupOpener('specie:lookup',[
               onselect :{
                     selectedItem.specie = it
//                   selectedItem.objid = it.objid;
//                   selectedItem.code = it.code;
//                   selectedItem.name = it.name;
                   
                   binding.refresh('selectedItem.objid.*'); 
               },
           ])
   }
   
    def getLookupHealthCondition(){
       return Inv.lookupOpener('healthcondition:lookup',[
               onselect :{
                     selectedItem.healthcondition = it
//                   selectedItem.objid = it.objid;
//                   selectedItem.code = it.code;
//                   selectedItem.name = it.name;
                   
                   binding.refresh('selectedItem.objid.*'); 
               },
           ])
   }
   
    def getLookupSignatory(){
       return Inv.lookupOpener('vetsignatory:lookup',[
               onselect :{
                    entity.signatory = it
//                   entity.specie.objid = it.objid;
//                   entity.specie.code = it.code;
//                   entity.specie.name = it.name;
                   binding.refresh(); 
               },
           ])
   }

      
    def selectedItem;
    
    def itemHandler = [
        
        
        fetchList: { 
            

            
                def p = [_schemaname: 'vetpermititems'];
                p.findBy = [ 'parentid': entity.objid];
                p.select = "specie.name,tagno,gender,age,healthcondition.name";
                if(!entity.items){
                entity.items = queryService.getList( p );
            }
                //return queryService.getList( p );
                return entity.items;
            
        },
        
        createItem : {
           return[
               objid : 'VPI' + new java.rmi.server.UID(),
               parentid : entity.objid,
               isnew : false
           ]
       },
       
        onRemoveItem : {
                
                if (MsgBox.confirm('Delete item?')) {
                //service.deleteFarmerItems(it)
                entity.items.remove(it)
                persistenceSvc.removeEntity([_schemaname:'vetpermititems',objid:it.objid])
                itemHandler.reload();
                return true;
                
            }
            return false;
        },
        
        onAddItem : {
            entity.items << it; /* add to list syntax */
            //calculatetotal()
     }
     
       
      
     
    ] as EditorListModel;
    
    
    def pay() {
//        def op = Inv.lookupOpener( "payorder:open", [entity: entity] );
//        op.target = 'self';
//        return op;

        def inputletter = MsgBox.prompt("Enter L for Large Animal, or S for Small Animal")
        
        def inputaccount = findAccount(inputletter)
        
        //println "=="+inputaccount+"==" 
        
        def inputamt = MsgBox.prompt("Enter amount:");
        
        def po = [
            
            permobjid : entity.objid,
            name : entity.person.name,
            address: entity.origin,
            //address : (entity.residential.address.city ? entity.residential.address.city : entity.residential.address.municipality),
            amount: inputamt,
            
            item_objid : inputaccount.item_objid,
            item_title : inputaccount.item_title,
            item_code : inputaccount.item_code,
            type : inputaccount.type,
            valuetype : inputaccount.valuetype,
            item_fund_objid : inputaccount.item_fund_objid,
            item_fund_title : inputaccount.item_fund_title,
            item_fund_code : inputaccount.item_fund_code

           
        ]
        
        //println po
        
        def x = vpSvc.paymentorderSupport(po)
        
        MsgBox.alert "Payment Order Number : " + x.ordernum
        
        getPersistenceService().update([ 
             _schemaname: 'vetpermit', 
             objid : entity.objid, 
             state : 'FLOAT' 
        ]);
        loadData(); 
    }
    
    def findAccount(inputletter){
        
        def inputacct
        
        if (!inputletter){
            MsgBox.alert("you have not entered a value")            
        }else if (inputletter == "L" || inputletter == "l"){
            inputacct = vpSvc.locateLItemAccount()
        }else if (inputletter == "S" || inputletter == "s"){
            inputacct = vpSvc.locateSItemAccount()
        }else{
            throw new Exception("The enter value is not correct!")
        }
        
        def inputaccount = [
            item_objid : inputacct.objid,
            item_title : inputacct.title,
            item_code : inputacct.code,
            type : inputacct.type,
            valuetype : inputacct.valuetype,
            item_fund_objid : inputacct.fund.objid,
            item_fund_title : inputacct.fund.title,
            item_fund_code : inputacct.fund.code
            
        ]
        
        return inputaccount
    }
    
  
    
  
}