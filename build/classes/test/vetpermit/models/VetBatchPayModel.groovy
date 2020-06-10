package test.vet.models;

import com.rameses.rcp.annotations.*;
import com.rameses.rcp.common.*;
import com.rameses.seti2.models.*;
import com.rameses.osiris2.client.*
import com.rameses.osiris2.common.*;
import com.rameses.util.*;

import com.rameses.rcp.framework.ValidatorException;
import com.rameses.util.BreakException;

class VetBatchPayModel extends CrudFormModel{

    @Service('DateService')
    def dtSvc
    
    @Service('SequenceService')
    def seqSvc
    
    @Script("User")
    def userInfo
    
    @Service("VetPermitService")
    def vpSvc
    
    boolean editAllowed = false;
    
    def app
    def user
    
    public void afterCreate(){
        entity.txndate = dtSvc.getServerDate();
        //entity.createdby_name = OsirisContext.env.FULLNAME;
        //entity.createdby_id = OsirisContext.env.USERID;
        //entity.transmittalnum = dtSvc.getServerYear() +"-"+ dtSvc.getServerMonth() + seqSvc.getNextFormattedSeries('check' + dtSvc.getServerYear() + dtSvc.getServerMonth()) ;
        entity.items = [];
        
        app = userInfo.env;
        user = [objid: app.USERID, name: app.NAME, fullname: app.FULLNAME, username: app.USER ];
        //MsgBox.alert(user)
    }
    
    public void beforeSave(o){
        if(!entity.items)throw new Exception("Batch must no be empty")
        
        entity.state = "CLOSED";
        
        entity.batchnum = dtSvc.getServerYear() +"-"+ dtSvc.getServerMonth() + seqSvc.getNextFormattedSeries('batchpay' + dtSvc.getServerYear() + dtSvc.getServerMonth()) ;
        
        entity.recordlog_datecreated = dtSvc.getServerDate();
        entity.recordlog_createdbyuser = OsirisContext.env.FULLNAME;
        entity.recordlog_createdbyuserid = OsirisContext.env.USERID;
    }
    
    public void afterSave(){
        
        vpSvc.changeVetPermitState(entity)
        
    }
    
    
    def selectedItem;
    
    def itemHandler = [
        
        
        fetchList: { 
            
            if (!entity.state)
            {
               
                def p = [_schemaname: 'vetpermit'];
                p.findBy = [ 'state': 'BATCH', 'recordlog_createdbyuserid': user.objid];
                p.select = "objid,seqno,person.name,amount";
                //p.where = [ 'recordlog_createdbyuserid': user.objid];
            
                entity.items = queryService.getList( p );
                entity.items.each{
                    it.parentid = entity.objid;
                    it.vetpermitid = it.objid;
                    calculatetotal();
                }              
            
                return entity.items;
            }else
            {
                def p = [_schemaname: 'batchpayitems'];
                p.findBy = [ 'parentid': entity.objid];
                p.select = "seqno,person.name,amount";
                return queryService.getList( p );
            }
        }
       
      
     
    ] as EditorListModel;

    //    def capturePayment() {
    //        return Inv.lookupOpener("housing_ledger_capture_payment", [parent: entity ] );
    //    }

    void refreshItem() {
        itemHandler.reload();
    }
    
    void calculatetotal(){
        entity.amountdue = 0.0
        if (entity.items){
            entity.amountdue = entity.items.amount.sum();
        }
        binding.refresh('entity.amountdue')
    }
    
    def pay() {
//        def op = Inv.lookupOpener( "payorder:open", [entity: entity] );
//        op.target = 'self';
//        return op;

        def inputletter = MsgBox.prompt("Enter L for Large Animal, or S for Small Animal")
        
        def inputaccount = findAccount(inputletter)
        
        //println "=="+inputaccount+"==" 
        
        def inputname = MsgBox.prompt("Enter name for receipt:");
        def inputadd = MsgBox.prompt("Enter address for receipt:");
        
        def po = [
            
            permobjid : entity.objid,
            name : inputname,
            address: inputadd,
            //address : (entity.residential.address.city ? entity.residential.address.city : entity.residential.address.municipality),
            amount: entity.amountdue,
            
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
             _schemaname: 'batchpay', 
             objid : entity.objid, 
             state : 'APPROVED' 
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