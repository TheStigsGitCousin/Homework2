/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package homework2;

import homework2.bank.Account;
import homework2.bank.RejectedException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author David
 */
public class MarketRequestImpl extends UnicastRemoteObject implements MarketRequest{
    
    // The key is the item-ID
    private final Map<Long, Item> uploadedItems=new HashMap<>();
    private final Map<String, List<Item>> wishedItems=new HashMap<>();
    private final Map<String, Owner> registeredUsers=new HashMap<>();
    
    public MarketRequestImpl() throws RemoteException
    {
    }
    
    @Override
    public Message SellItem(Item item) throws RemoteException {
        System.out.println("sell item. "+item.toString());
        String message="error uploading item";
        synchronized(uploadedItems){
            long id;
            // Find a unique ID (i.e. an ID thats not present in the uploadedItems collection (Hashmap)
            do {
                id=Handler.getRandomLong();
            }
            while(uploadedItems.containsKey(id));
            // Set item ID
            item.setId(id);
            uploadedItems.put(item.getId(), item);
            System.out.println("Upload items length = "+uploadedItems.size());
            message="Upload of ("+item.toString()+") successful";
            
            synchronized(wishedItems){
                System.out.println(item.getName());
                // Get all wished items and iterate through them
                List<Item> li=wishedItems.get(item.getName());
                if(li!=null){
                    for(Item wishedItem:li){
                        // If a wished item has the same name and a equal or higher price than the currently uploaded item => send notification
                        // to the owner of the wish-item
                        if(item.getName().equals(wishedItem.getName()) && item.getPrice()<=wishedItem.getPrice())
                            wishedItem.getOwner().wishAvaible(item);
                    }
                }
            }
        }
        Message msg=new Message();
        msg.message=message;
        return msg;
    }
    
    @Override
    public Message ListItems() throws RemoteException {
        synchronized(uploadedItems){
            System.out.println("Upload items length = "+uploadedItems.size());
            Message msg=new Message();
            msg.obj=new ArrayList<>(uploadedItems.values());
            msg.message="Get items successful";
            return msg;
        }
    }
    
    @Override
    public Message BuyItem(long itemId, Owner buyer) throws RemoteException {
        String message="Unspecified error occured when buying item. No item bought.";
        synchronized(uploadedItems){
            Item item=uploadedItems.get(itemId);
            if(item!=null){
                System.out.println("buy item. item = "+item.toString()+", buyer = "+buyer.toString());
                
                Account sellerAccount=item.getOwner().getBankAccount();
                Account buyerAccount=buyer.getBankAccount();
                boolean successful=false;
                try {
                    buyerAccount.withdraw(item.getPrice());
                    successful=true;
                } catch (RejectedException ex) {
                    message="Could not withdraw money.";
                    successful=false;
                }
                if(successful){
                    try {
                        sellerAccount.deposit(item.getPrice());
                    } catch (RejectedException ex) {
                        message="Couldn't deposit money.";
                    }
                    message="Transaction successful.";
                    uploadedItems.remove(itemId);
                    item.getOwner().itemSold(item);
                }
            }else{
                message="Item not found.";
            }
        }
        Message msg=new Message();
        msg.message=message;
        return msg;
    }
    
    @Override
    public Message AddWish(Item item) throws RemoteException {
        String message="Error adding wish.";
        synchronized(registeredUsers){
            Owner user=registeredUsers.get(item.getOwner().getName());
            if(user!=null){
                synchronized(wishedItems){
                    List<Item> list=wishedItems.get(item.getName());
                    if(list==null){
                        list=new ArrayList<Item>();
                        wishedItems.put(item.getName(), list);
                    }
                    list.add(item);
                    message="Wish added successfully";
                }
            }else{
                message="User not recognized";
            }
        }
        Message msg=new Message();
        msg.message=message;
        return msg;
    }
    
    @Override
    public Message Register(Owner owner) throws RemoteException {
        String message="Error registering user";
        synchronized(registeredUsers){
            if(!registeredUsers.containsKey(owner.getName())){
                registeredUsers.put(owner.getName(), owner);
                message="Registration successful";
            }else{
                message="Name already registered";
            }
        }
        Message msg=new Message();
        msg.message=message;
        return msg;
    }
    
    @Override
    public Message Unregister(Owner owner) throws RemoteException {
        String message="Error unregistering user";
        synchronized(registeredUsers){
            registeredUsers.remove(owner.getName());
            message="Unregistration successful";
        }
        Message msg=new Message();
        msg.message=message;
        return msg;
    }
    
    @Override
    public Message GetUser(String name) throws RemoteException {
        Message msg=new Message();
        msg.obj=registeredUsers.get(name);
        return msg;
    }
    
}
