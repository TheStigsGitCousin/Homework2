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
    public String SellItem(Item item) throws RemoteException {
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
            message="Upload of item successful";
            
            synchronized(wishedItems){
                // Get all wished items and iterate through them
                for(Item wishedItem:wishedItems.get(item.getName())){
                    // If a wished item has the same name and a equal or higher price than the currently uploaded item => send notification
                    // to the owner of the wish-item
                    if(item.getName().equals(wishedItem.getName()) && item.getPrice()<=wishedItem.getPrice())
                        item.getOwner().wishAvaible(item);
                }
            }
        }
        return message;
    }
    
    @Override
    public List<Item> ListItems() throws RemoteException {
        synchronized(uploadedItems){
            return new ArrayList<>(uploadedItems.values());
        }
    }
    
    @Override
    public String BuyItem(Item item, Owner buyer) throws RemoteException {
        String message="Unspecified error occured when buying item. No item bought.";
        synchronized(uploadedItems){
            Account sellerAccount=item.getOwner().getBankAccount();
            Account buyerAccount=buyer.getBankAccount();
            boolean successful=false;
            try {
                buyerAccount.withdraw(item.getPrice());
                successful=true;
            } catch (RejectedException ex) {
                message="Not enough money on the bank, get a job.";
                successful=false;
            }
            if(successful){
                try {
                    sellerAccount.deposit(item.getPrice());
                } catch (RejectedException ex) {
                    message="Couldn't deposit money, not your fault.";
                }
                message="Transaction successful.";
                item.getOwner().itemSold(item);
            }
        }
        return message;
    }
    
    @Override
    public String AddWish(Item item) throws RemoteException {
        String message="Error adding wish.";
        synchronized(registeredUsers){
            Owner user=registeredUsers.get(item.getName());
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
        return message;
    }
    
    @Override
    public String Register(Owner owner) throws RemoteException {
        String message="Error registering user";
        synchronized(registeredUsers){
            if(!registeredUsers.containsKey(owner.getName())){
                registeredUsers.put(owner.getName(), owner);
                message="Registration successful";
            }else{
                message="Name already registered";
            }
        }
        return message;
    }
    
    @Override
    public String Unregister(Owner owner) throws RemoteException {
        String message="Error unregistering user";
        synchronized(registeredUsers){
            registeredUsers.remove(owner.getName());
            message="Unregistration successful";
        }
        return message;
    }
    
    @Override
    public Owner GetUser(String name) throws RemoteException {
        return registeredUsers.get(name);
    }
    
}
